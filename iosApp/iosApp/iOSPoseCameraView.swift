// iOSMain/iosApp/PoseCameraView.swift

import UIKit
import AVFoundation
import ComposeApp


class iOSNativeViewFactory: NativeViewFactory {
    static var shared = iOSNativeViewFactory()
    
    func createPoseCameraView(showLandmarks: Bool, onPoseDetected: @escaping (Pose?) -> Void) -> UIView {
        let view = iOSPoseCameraView()
        
        return view
    }
}

@objc class iOSPoseCameraView: UIView {

    private let session = AVCaptureSession()
    private var previewLayer: AVCaptureVideoPreviewLayer?

    override init(frame: CGRect) {
        super.init(frame: frame)
        commonInit()
    }

    required init?(coder: NSCoder) {
        super.init(coder: coder)
        commonInit()
    }

    private func commonInit() {
        guard let device = AVCaptureDevice.default(for: .video),
              let input = try? AVCaptureDeviceInput(device: device),
              session.canAddInput(input) else {
            return
        }

        session.addInput(input)

        let previewLayer = AVCaptureVideoPreviewLayer(session: session)
        previewLayer.videoGravity = .resizeAspectFill
        self.layer.addSublayer(previewLayer)
        self.previewLayer = previewLayer

        // ✅ Start camera on background thread
        DispatchQueue.global(qos: .userInitiated).async { [weak self] in
            self?.session.startRunning()
        }
    }

    override func layoutSubviews() {
        super.layoutSubviews()
        previewLayer?.frame = self.bounds
    }

    deinit {
        if session.isRunning {
            session.stopRunning()
        }
    }
}
