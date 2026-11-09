package metropolia.myapplication

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun AlgorithmsScreen(
    onNavigateBack: () -> Unit
) {
    // Camera screen content

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        Text("Algorithms Screen")
        Button(
            onClick = onNavigateBack
        ) {
            Text("Back")
        }
    }
}