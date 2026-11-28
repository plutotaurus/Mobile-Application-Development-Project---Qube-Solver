package metropolia.myapplication.camera

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.unit.dp
import metropolia.myapplication.colorPicker.ColorpickerPrototype
import metropolia.myapplication.ui.QubeButton
import metropolia.myapplication.ui.QubeButtonStyle
import metropolia.myapplication.ui.QubeScreenTitle

@Composable
fun CameraScreen(
    onNavigateBack: () -> Unit,
    onSidesCaptured: () -> Unit
) {

    var capturedSides by remember { mutableStateOf<List<List<Color>>>(emptyList()) }

    // Camera screen content
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp)
    ) {
        if (capturedSides.size == 6) {
            // All sides captured, navigate to the color picker screen
            ColorpickerPrototype(
                sides = capturedSides,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            )
        } else {
            QubeScreenTitle("Camera")
            CubeSideCollecting(
                Modifier.weight(1f),
                onAllSidesCaptured = { sides ->
                    capturedSides = sides
                }
            )
        }
        QubeButton("Back", onNavigateBack, style = QubeButtonStyle.Text)
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