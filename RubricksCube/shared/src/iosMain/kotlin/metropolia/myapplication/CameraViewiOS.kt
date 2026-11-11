package metropolia.myapplication

import androidx.compose.runtime.Composable

@Composable
actual fun CameraView() {
    // iOS-specific camera view implementation
}

@Composable
actual fun CameraPermissionGate(content: @Composable () -> Unit) {
    // iOS-specific camera permission handling
    content()
}