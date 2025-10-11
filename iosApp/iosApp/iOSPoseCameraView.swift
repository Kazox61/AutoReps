import UIKit
import AVFoundation
import MediaPipeTasksVision
import ComposeApp

class iOSNativeViewFactory: NativeViewFactory {
    static var shared = iOSNativeViewFactory()
    
    func createPoseCameraView(showLandmarks: Bool, onPoseDetected: @escaping (ComposeApp.Pose?, KotlinInt, KotlinInt) -> Void) -> UIView {
        let view = iOSPoseCameraView()
        view.onPoseDetected = onPoseDetected
        return view
    }
}

@objc class iOSPoseCameraView: UIView {
    
    private let session = AVCaptureSession()
    private let videoOutput = AVCaptureVideoDataOutput()
    private let cameraQueue = DispatchQueue(label: "cameraQueue")
    private var previewLayer: AVCaptureVideoPreviewLayer?

    private var poseLandmarker: PoseLandmarker?
    var onPoseDetected: ((ComposeApp.Pose?, KotlinInt, KotlinInt) -> Void)?
    var imageWidth: KotlinInt = 0
    var imageHeight: KotlinInt = 0
    
    override init(frame: CGRect) {
        super.init(frame: frame)
        initialize()
    }

    required init?(coder: NSCoder) {
        super.init(coder: coder)
        initialize()
    }

    private func initialize() {
        setupCamera()
        setupPoseLandmarker()
    }

    private func setupCamera() {
        guard let device = AVCaptureDevice.default(.builtInWideAngleCamera, for: .video, position: .front),
              let input = try? AVCaptureDeviceInput(device: device),
              session.canAddInput(input) else {
            print("Could not set up camera input.")
            return
        }

        session.beginConfiguration()
        session.sessionPreset = .high
        session.addInput(input)
        
        videoOutput.videoSettings = [kCVPixelBufferPixelFormatTypeKey as String:
                                     kCVPixelFormatType_32BGRA]


        videoOutput.alwaysDiscardsLateVideoFrames = true
        videoOutput.setSampleBufferDelegate(self, queue: cameraQueue)

        if session.canAddOutput(videoOutput) {
            session.addOutput(videoOutput)
        }

        session.commitConfiguration()

        let preview = AVCaptureVideoPreviewLayer(session: session)
        preview.videoGravity = .resizeAspectFill
        layer.addSublayer(preview)
        self.previewLayer = preview

        cameraQueue.async { [weak self] in
            self?.session.startRunning()
        }
    }

    private func setupPoseLandmarker() {
        guard let modelPath = Bundle.main.path(forResource: "pose_landmarker_full", ofType: "task") else {
            print("PoseLandmarker model not found.")
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
            print("Failed to initialize PoseLandmarker: \(error)")
        }
    }

    override func layoutSubviews() {
        super.layoutSubviews()
        previewLayer?.frame = bounds
    }

    deinit {
        if session.isRunning {
            session.stopRunning()
        }
    }
}

extension iOSPoseCameraView: AVCaptureVideoDataOutputSampleBufferDelegate {
    func captureOutput(_ output: AVCaptureOutput, didOutput sampleBuffer: CMSampleBuffer, from connection: AVCaptureConnection) {
        guard let landmarker = poseLandmarker else { return }

        let orientation: UIImage.Orientation

        switch connection.videoRotationAngle {
        case 0:
            orientation = .rightMirrored
        case 90:
            orientation = .downMirrored
        case 180:
            orientation = .leftMirrored
        case 270:
            orientation = .upMirrored
        default:
            orientation = .rightMirrored
        }

        do {
            let mpImage = try MPImage(sampleBuffer: sampleBuffer, orientation: orientation)
            
            if (connection.videoRotationAngle == 0 || connection.videoRotationAngle == 180) {
                self.imageWidth = KotlinInt(int: Int32(mpImage.height))
                self.imageHeight = KotlinInt(int: Int32(mpImage.width))
            }
            else {
                self.imageWidth = KotlinInt(int: Int32(mpImage.width))
                self.imageHeight = KotlinInt(int: Int32(mpImage.height))
            }

            let timestamp = CMTimeGetSeconds(CMSampleBufferGetPresentationTimeStamp(sampleBuffer)) * 1000
            try landmarker.detectAsync(image: mpImage, timestampInMilliseconds: Int(timestamp))
        } catch {
            print("Pose detection failed: \(error)")
        }
    }
}

extension iOSPoseCameraView: PoseLandmarkerLiveStreamDelegate {
    func poseLandmarker(_ landmarker: PoseLandmarker, didFinishDetection result: PoseLandmarkerResult?, timestampInMilliseconds: Int, error: Error?) {
        guard error == nil, let result = result else {
            print("Detection error: \(String(describing: error))")
            return
        }

        guard let mediaPipeLandmarks = result.landmarks.first else {
            self.onPoseDetected?(nil, self.imageWidth, self.imageHeight)
            return
        }
        
        guard let worldLandmarks = result.worldLandmarks.first else {
            print("World landmarks missing")
            self.onPoseDetected?(nil, self.imageWidth, self.imageHeight)
            return
        }

        let imageOrientation: UIImage.Orientation = .leftMirrored
        let landmarks = mediaPipeLandmarks.enumerated().compactMap { (index, landmark) -> ComposeApp.Pose.Landmark? in
            guard index < worldLandmarks.count else { return nil }
            let worldLandmark = worldLandmarks[index]
            let rotated = rotateLandmark(landmark, orientation: imageOrientation)
            
            return ComposeApp.Pose.Landmark(
                type: ComposeApp.LandmarkType.entries[index],
                x: rotated.x,
                y: rotated.y,
                wx: Float(worldLandmark.x),
                wy: Float(worldLandmark.y),
                wz: Float(worldLandmark.z)            )
        }

        let pose = ComposeApp.Pose(landmarks: landmarks)
        DispatchQueue.main.async {
            self.onPoseDetected?(pose, self.imageWidth, self.imageHeight)
        }
    }
    
    func rotateLandmark(_ landmark: NormalizedLandmark, orientation: UIImage.Orientation) -> (x: Float, y: Float) {
        switch orientation {
        case .leftMirrored:
            return (x: 1-Float(landmark.y), y: Float(landmark.x))
        case .left:
            return (x: Float(landmark.y), y: Float(landmark.x))
        case .rightMirrored:
            return (x: 1-Float(landmark.y), y: Float(landmark.x))
        case .right:
            return (x: Float(landmark.y), y: Float(landmark.x))
        default:
            return (x: Float(landmark.x), y: Float(landmark.y))
        }
    }
}
