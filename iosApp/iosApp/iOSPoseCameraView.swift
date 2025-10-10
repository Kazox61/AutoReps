// iOSMain/iosApp/PoseCameraView.swift

import UIKit
import AVFoundation
import ComposeApp
import MLKitVision
import MLKitPoseDetection


class iOSNativeViewFactory: NativeViewFactory {
    static var shared = iOSNativeViewFactory()
    
    func createPoseCameraView(showLandmarks: Bool, onPoseDetected: @escaping (ComposeApp.Pose?) -> Void) -> UIView {
        let view = iOSPoseCameraView()
        view.onPoseDetected = onPoseDetected
        return view
    }
}

@objc class iOSPoseCameraView: UIView, AVCaptureVideoDataOutputSampleBufferDelegate {
    
    private let session = AVCaptureSession()
    private var previewLayer: AVCaptureVideoPreviewLayer?
    private var videoOutput = AVCaptureVideoDataOutput()
    private let cameraQueue = DispatchQueue(label: "cameraQueue")
    var onPoseDetected: ((ComposeApp.Pose?) -> Void)?
    
    // The pose detector instance
    private let poseDetector: PoseDetector
    
    override init(frame: CGRect) {
        // create pose detector before super.init
        let options = PoseDetectorOptions()
        options.detectorMode = .stream
        self.poseDetector = PoseDetector.poseDetector(options: options)
        super.init(frame: frame)
        commonInit()
    }
    
    required init?(coder: NSCoder) {
        // You might not support init(from coder) for pose detection scenario
        let options = PoseDetectorOptions()
        options.detectorMode = .stream
        self.poseDetector = PoseDetector.poseDetector(options: options)
        super.init(coder: coder)
        commonInit()
    }
    
    private func commonInit() {
        // camera input
        guard let device = AVCaptureDevice.default(.builtInWideAngleCamera, for: .video, position: .front),
              let input = try? AVCaptureDeviceInput(device: device),
              session.canAddInput(input) else {
            print("⚠️ Could not get camera input")
            return
        }
        session.beginConfiguration()
        session.sessionPreset = .high
        session.addInput(input)
        
        // output setup
        videoOutput.alwaysDiscardsLateVideoFrames = true
        videoOutput.setSampleBufferDelegate(self, queue: cameraQueue)
        if session.canAddOutput(videoOutput) {
            session.addOutput(videoOutput)
        }
        session.commitConfiguration()
        
        // preview layer
        let pl = AVCaptureVideoPreviewLayer(session: session)
        pl.videoGravity = .resizeAspectFill
        layer.addSublayer(pl)
        previewLayer = pl
        
        // start session on background
        cameraQueue.async { [weak self] in
            self?.session.startRunning()
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
    
    // MARK: - AVCaptureVideoDataOutputSampleBufferDelegate
    
    func captureOutput(_ output: AVCaptureOutput,
                       didOutput sampleBuffer: CMSampleBuffer,
                       from connection: AVCaptureConnection) {
        // Convert sampleBuffer to VisionImage
        let image = VisionImage(buffer: sampleBuffer)
        image.orientation = imageOrientation(
            deviceOrientation: UIDevice.current.orientation,
            cameraPosition: .front
        )
        
        poseDetector.process(image) { [weak self] poses, error in
            guard error == nil else {
                print("Pose detection error: \(error!.localizedDescription)")
                return
            }
            guard let poses = poses, !poses.isEmpty else {
                
                return
            }
            
            
            let mllandmarks = poses[0].landmarks
           
            var landmarks = [ComposeApp.Pose.Landmark]()
            
            for (index, lm) in mllandmarks.enumerated() {
               let landmark = ComposeApp.Pose.Landmark(
                    type: Int32(index),
                    x: Float(lm.position.x),
                    y: Float(lm.position.z),
                    wx: Float(lm.position.x),
                    wy: Float(lm.position.y),
                    wz: Float(lm.position.z),
                   confidence: Float(lm.inFrameLikelihood)
               )
               
               landmarks.append(landmark)
           }
            
            let pose = ComposeApp.Pose(landmarks: landmarks)
            
            self?.onPoseDetected?(pose)
            
        }
    }
    
    private func imageOrientation(deviceOrientation: UIDeviceOrientation,
                                  cameraPosition: AVCaptureDevice.Position) -> UIImage.Orientation {
        switch deviceOrientation {
        case .portrait:
            return cameraPosition == .front ? .leftMirrored : .right
        case .landscapeLeft:
            return cameraPosition == .front ? .downMirrored : .up
        case .portraitUpsideDown:
            return cameraPosition == .front ? .rightMirrored : .left
        case .landscapeRight:
            return cameraPosition == .front ? .upMirrored : .down
        default:
            return .up
        }
    }
}
