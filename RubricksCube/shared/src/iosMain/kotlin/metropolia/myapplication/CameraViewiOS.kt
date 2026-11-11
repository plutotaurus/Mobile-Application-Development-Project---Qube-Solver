package metropolia.myapplication

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.interop.UIKitView
import kotlinx.cinterop.CValue
import kotlinx.cinterop.ExperimentalForeignApi
import platform.AVFoundation.AVCaptureDevice
import platform.AVFoundation.AVCaptureDeviceInput
import platform.AVFoundation.AVCaptureDevicePositionBack
import platform.AVFoundation.AVCaptureSession
import platform.AVFoundation.AVCaptureSessionPresetPhoto
import platform.AVFoundation.AVCaptureStillImageOutput
import platform.AVFoundation.AVCaptureVideoPreviewLayer
import platform.AVFoundation.AVLayerVideoGravityResizeAspectFill
import platform.AVFoundation.AVMediaTypeVideo
import platform.AVFoundation.AVAuthorizationStatusAuthorized
import platform.AVFoundation.AVAuthorizationStatusNotDetermined
import platform.AVFoundation.AVVideoCodecJPEG
import platform.AVFoundation.AVVideoCodecKey
import platform.AVFoundation.authorizationStatusForMediaType
import platform.AVFoundation.requestAccessForMediaType
import platform.AVFoundation.position
import platform.CoreGraphics.CGRect
import platform.QuartzCore.CATransaction
import platform.QuartzCore.kCATransactionDisableActions
import platform.UIKit.UIView

@OptIn(ExperimentalForeignApi::class)
@Composable
actual fun CameraView() {
    // iOS-specific camera view implementation
    val resources = remember { CameraResources() }
    val session = resources.session
    val previewLayer = resources.previewLayer

    DisposableEffect(session) {
        session.startRunning()

        onDispose {
            session.stopRunning()
        }
    }

    UIKitView(
        modifier = Modifier.fillMaxSize(),
        factory = {
            UIView().also { container ->
                container.layer.addSublayer(previewLayer)
                previewLayer.videoGravity = AVLayerVideoGravityResizeAspectFill
            }
        },
        onResize = { container, rect ->
            container.layer.setFrame(rect)
            previewLayer.setFrame(rect)
        }
    )
}

@Composable
actual fun CameraPermissionGate(content: @Composable () -> Unit) {
    var granted by remember {
        mutableStateOf(
            AVCaptureDevice.authorizationStatusForMediaType(AVMediaTypeVideo) ==
                AVAuthorizationStatusAuthorized
        )
    }

    LaunchedEffect(Unit) {
        if (AVCaptureDevice.authorizationStatusForMediaType(AVMediaTypeVideo) ==
            AVAuthorizationStatusNotDetermined
        ) {
            AVCaptureDevice.requestAccessForMediaType(AVMediaTypeVideo) { isGranted ->
                granted = isGranted
            }
        }
    }

    if (granted) {
        content()
    } else {
        Text("Camera permission is required")
    }
}

private class CameraResources {
    val session: AVCaptureSession  = AVCaptureSession()

    private val device =
        AVCaptureDevice.devicesWithMediaType(AVMediaTypeVideo)
            .firstOrNull { (it as AVCaptureDevice).position == AVCaptureDevicePositionBack }
                as? AVCaptureDevice
            ?: error("No back camera available")

    @OptIn(ExperimentalForeignApi::class)
    private val input =
        AVCaptureDeviceInput.deviceInputWithDevice(device, null)
                as? AVCaptureDeviceInput
            ?: error("Could not create camera input")

    val previewLayer = AVCaptureVideoPreviewLayer(session = session)

    init {
        session.sessionPreset = AVCaptureSessionPresetPhoto
        session.addInput(input)
    }
}