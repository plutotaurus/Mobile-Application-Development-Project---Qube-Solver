package metropolia.myapplication.ui

import androidx.compose.material3.Button
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics

internal enum class QubeButtonStyle { Filled, Tonal, Outlined, Text }

@Composable
internal fun QubeButton(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    style: QubeButtonStyle = QubeButtonStyle.Filled,
) {
    when (style) {
        QubeButtonStyle.Filled -> Button(onClick, modifier, enabled) { Text(label) }
        QubeButtonStyle.Tonal -> FilledTonalButton(onClick, modifier, enabled) { Text(label) }
        QubeButtonStyle.Outlined -> OutlinedButton(onClick, modifier, enabled) { Text(label) }
        QubeButtonStyle.Text -> TextButton(onClick, modifier, enabled) { Text(label) }
    }
}

@Composable
internal fun QubeScreenTitle(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        modifier = modifier.semantics { heading() },
        style = MaterialTheme.typography.headlineSmall,
    )
}
