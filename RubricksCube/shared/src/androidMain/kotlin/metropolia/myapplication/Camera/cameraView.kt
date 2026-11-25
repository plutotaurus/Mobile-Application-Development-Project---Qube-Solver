package metropolia.myapplication.camera

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.view.LifecycleCameraController
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.LocalLifecycleOwner
import java.util.concurrent.Executor

@Composable
actual fun CameraView(
    captureRequested: Boolean,
    onCaptureHandled: () -> Unit,
    onPhotoCaptured: (ImageBitmap) -> Unit
) {
    val context = LocalContext.current
    val lifeCycleOwner = LocalLifecycleOwner.current
    val cameraController = remember(context) {
        LifecycleCameraController(context).apply {
            cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
        }
    }

    LaunchedEffect(lifeCycleOwner, cameraController) {
        try {
            cameraController.bindToLifecycle(lifeCycleOwner)
        } catch (e: Exception) {
            Log.e("CameraView", "Failed to bind camera to lifecycle", e)
        }
    }

    LaunchedEffect(captureRequested) {
        if (captureRequested) {
            CapturePhoto(
                context = context,
                cameraController = cameraController,
                onPhotoCaptured = { bitmap ->
                    try {
                        onPhotoCaptured(bitmap.asImageBitmap())
                    } catch (e: Exception) {
                        Log.e("CameraView", "Error converting bitmap", e)
                    } finally {
                        onCaptureHandled()
                    }
                },
                onCaptureFailed = onCaptureHandled
            )
        }
    }

    val previewView = remember { PreviewView(context) }

    AndroidView(
        factory = {
            previewView.apply {
                controller = cameraController
            }
        },
        modifier = Modifier.fillMaxSize(),
    )
}

@Composable
actual fun CameraPermissionGate(content: @Composable () -> Unit) {
    val context = LocalContext.current
    var granted by remember {
        mutableStateOf(
            context.checkSelfPermission(Manifest.permission.CAMERA) ==
                    PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        granted = isGranted
    }

    LaunchedEffect(Unit) {
        if (!granted) {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    if (granted) {
        content()
    } else {
        Text("Camera permission is required")
    }
}

fun CapturePhoto(
    context: Context,
    cameraController: LifecycleCameraController,
    onPhotoCaptured: (Bitmap) -> Unit,
    onCaptureFailed: () -> Unit
) {
    val mainExecutor: Executor = context.mainExecutor

    try {
        cameraController.takePicture(
            mainExecutor,
            object : ImageCapture.OnImageCapturedCallback() {
                override fun onCaptureSuccess(image: ImageProxy) {
                    try {
                        val bitmap = image.toBitmap()
                        val softwareBitmap = if (bitmap.config == Bitmap.Config.HARDWARE) {
                            bitmap.copy(Bitmap.Config.ARGB_8888, false) ?: bitmap
                        } else {
                            bitmap
                        }
                        onPhotoCaptured(softwareBitmap)
                    } catch (e: Exception) {
                        Log.e("CameraView", "Photo processing failed: ${e.message}", e)
                        onCaptureFailed()
                    } finally {
                        image.close()
                    }
                }

                override fun onError(exception: ImageCaptureException) {
                    Log.e("CameraView", "Photo capture failed: ${exception.message}", exception)
                    onCaptureFailed()
                }
            }
        )
    } catch (e: Exception) {
        Log.e("CameraView", "CapturePhoto exception: ${e.message}", e)
        onCaptureFailed()
    }
}
