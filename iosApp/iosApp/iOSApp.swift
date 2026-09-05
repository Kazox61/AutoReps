import SwiftUI
import Shared

@main
struct iOSApp: App {
    init() {
        InitKoinKt.doInitKoin()
        // MediaPipe has no Kotlin/Native bindings, so capture and detection live in Swift and
        // are handed to Compose through this bridge.
        PoseCameraBridge.shared.factory = IOSPoseCameraFactory.shared
    }

    var body: some Scene {
        WindowGroup {
            ContentView()
        }
    }
}
