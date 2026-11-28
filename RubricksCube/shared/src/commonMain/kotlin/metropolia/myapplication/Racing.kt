package metropolia.myapplication

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import metropolia.myapplication.ui.QubeButton
import metropolia.myapplication.ui.QubeButtonStyle
import metropolia.myapplication.ui.QubeScreenTitle

@Composable
fun RacingScreen(
    onNavigateBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        QubeScreenTitle("Racing")
        QubeButton("Back", onNavigateBack, style = QubeButtonStyle.Text)
    }
}