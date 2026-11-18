package metropolia.myapplication

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.unit.dp
import metropolia.myapplication.Camera.DrawCubeViewFinder
import metropolia.myapplication.Camera.getAverageColor

@Composable
fun CameraScreen(
    onNavigateBack: () -> Unit
) {

    var captureRequested by remember { mutableStateOf(false) }

    var avgColor: Color? by remember { mutableStateOf<Color?>(null) }


    // Camera screen content
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp)
    ) {
        Text("Camera Screen")
        Box(
            modifier = Modifier
                .weight(1f)
        ) {
            CameraPermissionGate {
                CameraView(
                    captureRequested = captureRequested,
                    onCaptureHandled = { captureRequested = false },
                    onPhotoCaptured = { image ->
                        avgColor = getAverageColor(image)
                    }
                )
            }
            DrawCubeViewFinder()
            if (avgColor != null) {
                Canvas(modifier = Modifier.size(100.dp).padding(16.dp)) {
                    drawCircle(color = avgColor!!)
                }
            }
        }
        Button(
            onClick = { captureRequested = true }
        ) {
            Text("Take Photo")
        }
        Button(
            onClick = onNavigateBack
        ) {
            Text("Back")
        }
    }
}

@Composable
expect fun CameraView(
    captureRequested: Boolean,
    onCaptureHandled: () -> Unit,
    onPhotoCaptured: (ImageBitmap) -> Unit
)

@Composable
expect fun CameraPermissionGate(content: @Composable () -> Unit)