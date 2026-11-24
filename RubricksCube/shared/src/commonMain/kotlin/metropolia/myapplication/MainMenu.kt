package metropolia.myapplication

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import metropolia.myapplication.profile.LoginDialog
import metropolia.myapplication.renderingCube.CubeRendering


@Composable
fun MainNavButtons(
    onNavigateToCamera: () -> Unit,
    onNavigateToTimer: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateToAlgorithms: () -> Unit,
    onNavigateToRacing: () -> Unit
) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
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
    onNavigateToProfile: () -> Unit,
    isLoggedIn: Boolean = false,
    username: String = "Guest",
) {

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Qube Solver",
                    style = MaterialTheme.typography.headlineMedium
                )
                if (isLoggedIn) {
                    Text(
                        text = "Hi $username",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

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