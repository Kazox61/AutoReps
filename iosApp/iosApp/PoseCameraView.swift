import AVFoundation
import CoreVideo
import MediaPipeTasksVision
import Shared
import UIKit

/// Registered with the shared module at launch so Compose can create camera views.
final class IOSPoseCameraFactory: NSObject, PoseCameraFactory {
    static let shared = IOSPoseCameraFactory()

    func createView(
        showLandmarks: Bool,
        onFrame: @escaping (Pose?, KotlinLong, KotlinInt, KotlinInt) -> Void
    ) -> UIView {
        PoseCameraUIView(onFrame: onFrame)
    }
}

/// Camera capture plus MediaPipe pose detection.
///
/// Landmarks are handed back in the same normalized display space Android produces: origin
/// top-left, y growing downward, mirrored to match the front-camera preview. The Kotlin side
/// depends on both platforms agreeing on that, so any change here needs the Android side too.
final class PoseCameraUIView: UIView {
    private let session = AVCaptureSession()
    private let videoOutput = AVCaptureVideoDataOutput()
    private let sessionQueue = DispatchQueue(label: "autoreps.camera.session")
    private let analysisQueue = DispatchQueue(label: "autoreps.camera.analysis")
    private var previewLayer: AVCaptureVideoPreviewLayer?
    private var poseLandmarker: PoseLandmarker?

    private let onFrame: (Pose?, KotlinLong, KotlinInt, KotlinInt) -> Void

    /// Frame geometry written in `captureOutput` and read when the matching detection comes
    /// back. Both values are constant for the life of the session — the rotation is pinned in
    /// `setupSession` and the preset never changes — so a detection that lands a frame or two
    /// late cannot pick up geometry belonging to a different frame.
    private var pendingWidth: Int32 = 0
    private var pendingHeight: Int32 = 0
    private var pendingIsLandscape = false

    init(onFrame: @escaping (Pose?, KotlinLong, KotlinInt, KotlinInt) -> Void) {
        self.onFrame = onFrame
        super.init(frame: .zero)
        setupLandmarker()
        setupSession()
    }

    @available(*, unavailable)
    required init?(coder: NSCoder) { fatalError("init(coder:) is not used") }

    private func setupLandmarker() {
        guard let modelPath = Bundle.main.path(forResource: "pose_landmarker_full", ofType: "task") else {
            assertionFailure("pose_landmarker_full.task is missing from the app bundle")
            return
        }

        let options = PoseLandmarkerOptions()
        options.baseOptions.modelAssetPath = modelPath
        options.runningMode = .liveStream
        options.numPoses = 1
        options.minPoseDetectionConfidence = 0.5
        options.minPosePresenceConfidence = 0.5
        options.minTrackingConfidence = 0.5
        options.poseLandmarkerLiveStreamDelegate = self

        do {
            poseLandmarker = try PoseLandmarker(options: options)
        } catch {
            assertionFailure("Failed to create PoseLandmarker: \(error)")
        }
    }

    private func setupSession() {
        sessionQueue.async { [weak self] in
            guard let self else { return }
            guard
                let device = AVCaptureDevice.default(.builtInWideAngleCamera, for: .video, position: .front),
                let input = try? AVCaptureDeviceInput(device: device),
                self.session.canAddInput(input)
            else { return }

            // The capture session takes over the app's shared AVAudioSession when it starts,
            // which silently replaced the rep-tone player's category a moment after that player
            // configured it. There is no audio input here — pose detection is video only — so
            // the session has no business touching audio at all.
            self.session.automaticallyConfiguresApplicationAudioSession = false

            self.session.beginConfiguration()
            self.session.sessionPreset = .high
            self.session.addInput(input)

            self.videoOutput.videoSettings =
                [kCVPixelBufferPixelFormatTypeKey as String: kCVPixelFormatType_32BGRA]
            self.videoOutput.alwaysDiscardsLateVideoFrames = true
            self.videoOutput.setSampleBufferDelegate(self, queue: self.analysisQueue)
            if self.session.canAddOutput(self.videoOutput) {
                self.session.addOutput(self.videoOutput)
            }

            // Portrait, pinned. Left alone, the data-output connection's rotation follows the
            // device orientation, and what `videoRotationAngle` reports versus what it actually
            // does to data-output buffers does not line up the way Android's
            // `imageInfo.rotationDegrees` does — the previous version of this file trusted the
            // reported angle for the landmark transform and drew the skeleton sideways. Pinning
            // the angle here, and deriving the transform from the buffer's own dimensions in
            // `captureOutput`, makes coordinates and reported frame size agree by construction.
            if let connection = self.videoOutput.connection(with: .video),
               connection.isVideoRotationAngleSupported(90) {
                connection.videoRotationAngle = 90
            }
            self.session.commitConfiguration()
            self.session.startRunning()
        }

        let preview = AVCaptureVideoPreviewLayer(session: session)
        preview.videoGravity = .resizeAspectFill
        layer.addSublayer(preview)
        previewLayer = preview
    }

    override func layoutSubviews() {
        super.layoutSubviews()
        previewLayer?.frame = bounds
    }

    deinit {
        let session = self.session
        // stopRunning blocks; never on the main thread.
        DispatchQueue.global(qos: .userInitiated).async {
            if session.isRunning { session.stopRunning() }
        }
    }
}

extension PoseCameraUIView: AVCaptureVideoDataOutputSampleBufferDelegate {
    func captureOutput(
        _ output: AVCaptureOutput,
        didOutput sampleBuffer: CMSampleBuffer,
        from connection: AVCaptureConnection
    ) {
        guard let landmarker = poseLandmarker else { return }

        // Whether the buffer stands upright is read from the buffer itself, never from
        // `connection.videoRotationAngle`. With the rotation pinned to portrait in setupSession,
        // a device that physically rotates data-output buffers delivers portrait frames and one
        // that does not delivers the sensor's landscape frames — the dimensions say which, and
        // the landmark transform below keys off the same bit, so coordinates and reported frame
        // size cannot disagree the way the angle-derived version did.
        guard let pixelBuffer = CMSampleBufferGetImageBuffer(sampleBuffer) else { return }
        let isLandscape = CVPixelBufferGetWidth(pixelBuffer) > CVPixelBufferGetHeight(pixelBuffer)

        // The analysis buffer is never pre-mirrored for the front camera, so the mirrored
        // orientation variants are the honest description for inference.
        let orientation: UIImage.Orientation = isLandscape ? .rightMirrored : .upMirrored

        do {
            let image = try MPImage(sampleBuffer: sampleBuffer, orientation: orientation)
            pendingIsLandscape = isLandscape
            pendingWidth = Int32(isLandscape ? image.height : image.width)
            pendingHeight = Int32(isLandscape ? image.width : image.height)

            // Presentation time is monotonic; wall-clock time is not, and MediaPipe rejects
            // timestamps that go backwards.
            let seconds = CMTimeGetSeconds(CMSampleBufferGetPresentationTimeStamp(sampleBuffer))
            try landmarker.detectAsync(image: image, timestampInMilliseconds: Int(seconds * 1000))
        } catch {
            // Dropping a frame is fine; the next one is 33ms away.
        }
    }

    /// Stands a landscape sensor frame up: 90° clockwise, what a portrait-held phone's front
    /// camera needs. Paired with the width/height swap in `captureOutput` — wherever the swap
    /// applies, this applies — so landmark space and reported frame size always agree. The
    /// landmark rotation reproduces the transform the working version of this app shipped:
    /// rotate, then mirror in the detection callback.
    fileprivate static func rotateUpright(x: Float, y: Float) -> (x: Float, y: Float) {
        (x: 1.0 - y, y: x)
    }
}

extension PoseCameraUIView: PoseLandmarkerLiveStreamDelegate {
    func poseLandmarker(
        _ poseLandmarker: PoseLandmarker,
        didFinishDetection result: PoseLandmarkerResult?,
        timestampInMilliseconds: Int,
        error: Error?
    ) {
        let width = KotlinInt(int: pendingWidth)
        let height = KotlinInt(int: pendingHeight)
        let timestamp = KotlinLong(longLong: Int64(timestampInMilliseconds))

        func emit(_ pose: Pose?) {
            DispatchQueue.main.async { [weak self] in
                self?.onFrame(pose, timestamp, width, height)
            }
        }

        guard
            error == nil,
            let result,
            let normalized = result.landmarks.first,
            let world = result.worldLandmarks.first
        else {
            // Report the empty frame rather than staying silent: the rep detector has to know
            // tracking was lost, or it will hold a half-finished repetition open indefinitely.
            emit(nil)
            return
        }

        let landmarks: [Pose.Landmark] = normalized.enumerated().compactMap { index, point in
            guard
                index < world.count,
                let type = LandmarkType.companion.fromIndex(index: Int32(index))
            else { return nil }
            let worldPoint = world[index]

            let rotated = pendingIsLandscape
                ? Self.rotateUpright(x: point.x, y: point.y)
                : (x: point.x, y: point.y)

            return Pose.Landmark(
                type: type,
                // Mirrored to match the front-camera preview, which AVFoundation flips while the
                // analysis buffer stays unflipped.
                x: 1.0 - rotated.x,
                y: rotated.y,
                wx: worldPoint.x,
                wy: worldPoint.y,
                wz: worldPoint.z,
                visibility: point.visibility?.floatValue ?? 1.0
            )
        }

        emit(Pose(landmarks: landmarks))
    }
}
