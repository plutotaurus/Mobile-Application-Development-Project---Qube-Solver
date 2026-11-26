package metropolia.myapplication.camera

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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.decodeToImageBitmap
import androidx.compose.ui.interop.UIKitView
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.readValue
import kotlinx.cinterop.readBytes
import kotlinx.cinterop.useContents
import platform.AVFoundation.AVCaptureDevice
import platform.AVFoundation.AVCaptureDeviceInput
import platform.AVFoundation.AVCapturePhoto
import platform.AVFoundation.AVCapturePhotoOutput
import platform.AVFoundation.AVCapturePhotoCaptureDelegateProtocol
import platform.AVFoundation.AVCapturePhotoSettings
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
import platform.AVFoundation.authorizationStatusForMediaType
import platform.AVFoundation.fileDataRepresentation
import platform.AVFoundation.play
import platform.AVFoundation.requestAccessForMediaType
import platform.AVFoundation.position
import platform.AVFoundation.seekToTime
import platform.CoreGraphics.CGRectZero
import platform.CoreMedia.kCMTimeZero
import platform.Foundation.NSBundle
import platform.Foundation.NSError
import platform.Foundation.NSNotificationCenter
import platform.Foundation.NSProcessInfo
import platform.UIKit.UIView
import platform.darwin.NSObject

@OptIn(ExperimentalForeignApi::class)
@Composable
actual fun CameraView(
    captureRequested: Boolean,
    onCaptureHandled: () -> Unit,
    onPhotoCaptured: (ImageBitmap) -> Unit
) {
    // iOS-specific camera view implementation


    // check environment to display mockup video
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

    LaunchedEffect(captureRequested) {
        if (captureRequested) {
            resources.capturePhoto(
                onPhotoCaptured = onPhotoCaptured,
                onCaptureHandled = onCaptureHandled
            )
        }
    }
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
    private val photoOutput = AVCapturePhotoOutput()

    init {
        session.sessionPreset = AVCaptureSessionPresetPhoto
        session.addInput(input)
        check(session.canAddOutput(photoOutput)) {
            "Could not add photo output to camera session"
        }
        session.addOutput(photoOutput)
    }

    @OptIn(ExperimentalForeignApi::class)
    fun capturePhoto(
        onPhotoCaptured: (ImageBitmap) -> Unit,
        onCaptureHandled: () -> Unit
    ) {
        val previewAspectRatio = previewLayer.bounds.useContents {
            (size.width / size.height).toFloat()
        }
        check(previewAspectRatio.isFinite() && previewAspectRatio > 0f) {
            "Camera preview has no valid size"
        }

        val settings = AVCapturePhotoSettings.photoSettings()
        val delegate = PhotoCaptureDelegate(
            previewAspectRatio = previewAspectRatio,
            onPhotoCaptured = onPhotoCaptured,
            onCaptureHandled = onCaptureHandled
        )
        photoOutput.capturePhotoWithSettings(settings, delegate)
    }
}

private class PhotoCaptureDelegate(
    private val previewAspectRatio: Float,
    private val onPhotoCaptured: (ImageBitmap) -> Unit,
    private val onCaptureHandled: () -> Unit
) : NSObject(), AVCapturePhotoCaptureDelegateProtocol {

    @OptIn(ExperimentalForeignApi::class)
    override fun captureOutput(
        output: AVCapturePhotoOutput,
        didFinishProcessingPhoto: AVCapturePhoto,
        error: NSError?
    ) {
        if (error != null) {
            onCaptureHandled()
            return
        }

        val photoData = didFinishProcessingPhoto.fileDataRepresentation()
        if (photoData == null) {
            onCaptureHandled()
            return
        }

        val encodedImage = photoData.bytes?.readBytes(photoData.length.toInt())
        if (encodedImage == null) {
            onCaptureHandled()
            return
        }

        val imageBitmap = encodedImage
            .decodeToImageBitmap()
            .centerCropToAspectRatio(previewAspectRatio)

        onPhotoCaptured(imageBitmap)
        onCaptureHandled()
    }
}

private fun ImageBitmap.centerCropToAspectRatio(targetAspectRatio: Float): ImageBitmap {
    val sourceAspectRatio = width.toFloat() / height
    val cropWidth: Int
    val cropHeight: Int
    val cropLeft: Int
    val cropTop: Int

    if (sourceAspectRatio > targetAspectRatio) {
        cropHeight = height
        cropWidth = (height * targetAspectRatio).toInt().coerceIn(1, width)
        cropLeft = (width - cropWidth) / 2
        cropTop = 0
    } else {
        cropWidth = width
        cropHeight = (width / targetAspectRatio).toInt().coerceIn(1, height)
        cropLeft = 0
        cropTop = (height - cropHeight) / 2
    }

    if (cropWidth == width && cropHeight == height) return this

    val croppedImage = ImageBitmap(cropWidth, cropHeight)
    Canvas(croppedImage).drawImage(
        image = this,
        topLeftOffset = Offset(-cropLeft.toFloat(), -cropTop.toFloat()),
        paint = Paint()
    )
    return croppedImage
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