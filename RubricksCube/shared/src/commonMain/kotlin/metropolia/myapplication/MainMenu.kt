package metropolia.myapplication

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import metropolia.myapplication.renderingCube.CubeRendering
import metropolia.myapplication.ui.QubeButton
import metropolia.myapplication.ui.QubeButtonStyle
import metropolia.myapplication.ui.QubeScreenTitle


@Composable
fun MainNavButtons(
    onNavigateToCamera: () -> Unit,
    onNavigateToTimer: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateToAlgorithms: () -> Unit,
    onNavigateToRacing: () -> Unit,
) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item { QubeButton("Camera", onNavigateToCamera, style = QubeButtonStyle.Tonal) }
        item { QubeButton("Timer", onNavigateToTimer, style = QubeButtonStyle.Tonal) }
        item { QubeButton("Profile", onNavigateToProfile, style = QubeButtonStyle.Tonal) }
        item { QubeButton("Algorithms", onNavigateToAlgorithms, style = QubeButtonStyle.Tonal) }
        item { QubeButton("Racing", onNavigateToRacing, style = QubeButtonStyle.Tonal) }
    }
}

@Composable
fun MainMenuScreen(
    onNavigateToCamera: () -> Unit,
    onNavigateToAlgorithms: () -> Unit,
    onNavigateToTimer: () -> Unit,
    onNavigateToRacing: () -> Unit,
    onNavigateToProfile: () -> Unit,
    username: String?,
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
                QubeScreenTitle("Qube Solver")
                if (username != null) {
                    Text(
                        text = "Hi $username",
                        style = MaterialTheme.typography.bodySmall,
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