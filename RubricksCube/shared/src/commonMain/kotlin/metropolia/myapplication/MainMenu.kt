package metropolia.myapplication

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import metropolia.myapplication.renderingCube.CubeRendering


@Composable
fun MainNavButtons(
    onNavigateToCamera: () -> Unit
) {
    Button(
        onClick = onNavigateToCamera
    ) {
        Text("Camera")
    }
}

@Composable
fun MainMenuScreen(
    onNavigateToCamera: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        Text("Qube Solver")
        MainNavButtons(
            onNavigateToCamera = onNavigateToCamera
        )

        CubeRendering()
    }
}