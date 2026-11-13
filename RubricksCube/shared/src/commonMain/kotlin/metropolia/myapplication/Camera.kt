package metropolia.myapplication

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun CameraScreen(
    onNavigateBack: () -> Unit
) {
    // Camera screen content

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        Text("Camera Screen")
        Box(
            modifier = Modifier
                .weight(1f)
        ) {
        CameraPermissionGate {
            CameraView()
        }
        }
        Button(
            onClick = onNavigateBack
        ) {
            Text("Back")
        }
    }
}

@Composable
expect fun CameraView()

@Composable
expect fun CameraPermissionGate(content: @Composable () -> Unit)