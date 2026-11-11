package metropolia.myapplication

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import metropolia.myapplication.renderingCube.CubeRendering


@Composable
fun MainNavButtons(
    onNavigateToCamera: () -> Unit,
    onNavigateToTimer: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateToAlgorithms: () -> Unit,
    onNavigateToRacing: () -> Unit
) {
    LazyRow {
        item{
            Button(
                onClick = {
                    onNavigateToCamera()
                }
            ) {
                Text("Camera")
            }
        }
        item{
            Button(
                onClick = {
                    onNavigateToTimer()
                }
            ){
                Text("Timer")
            }
        }
        item{
            Button(
                onClick = {
                    onNavigateToProfile()
                }
            ){
                Text("Profile")
            }
        }
        item{
            Button(
                onClick = {
                    onNavigateToAlgorithms()
                }
            ){
                Text("Algorithms")
            }
        }
        item{
            Button(
                onClick = {
                    onNavigateToRacing()
                }
            ){
                Text("Racing")
            }
        }
    }
}

@Composable
fun MainMenuScreen(
    onNavigateToCamera: () -> Unit,
    onNavigateToAlgorithms: () -> Unit,
    onNavigateToTimer: () -> Unit,
    onNavigateToRacing: () -> Unit,
    onNavigateToProfile: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        Text("Qube Solver")
        MainNavButtons(
            onNavigateToCamera = onNavigateToCamera,
            onNavigateToTimer = onNavigateToTimer,
            onNavigateToProfile = onNavigateToProfile,
            onNavigateToAlgorithms = onNavigateToAlgorithms,
            onNavigateToRacing = onNavigateToRacing
        )

        CubeRendering()
    }
}