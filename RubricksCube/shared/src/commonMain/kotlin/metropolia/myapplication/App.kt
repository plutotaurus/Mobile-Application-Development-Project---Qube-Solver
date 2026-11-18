package metropolia.myapplication

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
@Preview
fun App() {
    // Keep the current light design consistent with the native iOS host.
    MaterialTheme(colorScheme = lightColorScheme()) {
        // MaterialTheme supplies colors but doesn't paint a background. Paint the
        // entire window so transparent screens cannot reveal a dark native view.
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background,
            contentColor = MaterialTheme.colorScheme.onBackground
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .safeContentPadding()
                    .padding(2.dp)
            ) {
                QubeApp()
            }
        }
    }
}
