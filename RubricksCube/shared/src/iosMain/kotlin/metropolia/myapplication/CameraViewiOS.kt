package metropolia.myapplication

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.interop.UIKitView
import androidx.compose.ui.unit.dp
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.readValue
import platform.AVFoundation.AVCaptureDevice
import platform.AVFoundation.AVCaptureDeviceInput
import platform.AVFoundation.AVCaptureDevicePositionBack
import platform.AVFoundation.AVCaptureSession
import platform.AVFoundation.AVCaptureSessionPresetPhoto
import platform.AVFoundation.AVCaptureVideoPreviewLayer
import platform.AVFoundation.AVLayerVideoGravityResizeAspectFill
import platform.AVFoundation.AVMediaTypeVideo
import platform.AVFoundation.AVAuthorizationStatusAuthorized
import platform.AVFoundation.AVAuthorizationStatusNotDetermined
import platform.AVFoundation.AVPlayer
import platform.AVFoundation.AVPlayerItem
import platform.AVFoundation.AVPlayerItemDidPlayToEndTimeNotification
import platform.AVFoundation.AVPlayerLayer
import platform.AVFoundation.AVPlayerLooper
import platform.AVFoundation.AVQueuePlayer
import platform.AVFoundation.authorizationStatusForMediaType
import platform.AVFoundation.duration
import platform.AVFoundation.play
import platform.AVFoundation.requestAccessForMediaType
import platform.AVFoundation.position
import platform.AVFoundation.seekToTime
import platform.CoreGraphics.CGRectZero
import platform.CoreMedia.CMTimeRange
import platform.CoreMedia.CMTimeRangeMake
import platform.CoreMedia.kCMTimeRangeInvalid
import platform.CoreMedia.kCMTimeZero
import platform.Foundation.NSBundle
import platform.Foundation.NSNotificationCenter
import platform.Foundation.NSProcessInfo
import platform.UIKit.UIView

@OptIn(ExperimentalForeignApi::class)
@Composable
actual fun CameraView() {
    // iOS-specific camera view implementation

    val isSimulator = NSProcessInfo.processInfo.environment["SIMULATOR_DEVICE_NAME"] != null

    if (isSimulator) {
        MockCameraPreview()
        return
    }

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
            object : UIView(CGRectZero.readValue()) {
                init {
                    layer.addSublayer(previewLayer)
                    resources.previewLayer.videoGravity = AVLayerVideoGravityResizeAspectFill
                }

                override fun layoutSubviews() {
                    super.layoutSubviews()
                    previewLayer.setFrame(bounds)
                }
            }
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

@OptIn(ExperimentalForeignApi::class)
@Composable
fun MockCameraPreview() {
    val item = AVPlayerItem(
        uRL = NSBundle.mainBundle.URLForResource("mock-camera", "mp4")!!
    )

    UIKitView(
        modifier = Modifier.fillMaxSize(),
        factory = {
            object : UIView(frame = CGRectZero.readValue()) {
                val player = AVPlayer(item)

                val observer = NSNotificationCenter.defaultCenter.addObserverForName(
                    name = AVPlayerItemDidPlayToEndTimeNotification,
                    `object` = item,
                    queue = null
                ) {
                    player.seekToTime(kCMTimeZero.readValue())
                    player.play()
                }
                private val playerLayer =
                    AVPlayerLayer.playerLayerWithPlayer(player).apply {
                        videoGravity = AVLayerVideoGravityResizeAspectFill
                    }

                init {
                    layer.addSublayer(playerLayer)
                    player.play()
                }

                override fun layoutSubviews() {
                    super.layoutSubviews()
                    playerLayer.setFrame(bounds)
                }
            }
        }
    )
    return
}