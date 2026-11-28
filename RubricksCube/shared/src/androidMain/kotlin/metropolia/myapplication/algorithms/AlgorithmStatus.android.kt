package metropolia.myapplication

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import android.content.Context
import androidx.compose.ui.platform.LocalContext

@Composable
internal actual fun rememberLegacyProgress(profile: String): Map<String, AlgorithmStatus> {
    val context = LocalContext.current.applicationContext
    return remember(context, profile) {
        val prefs = context.getSharedPreferences("algorithm_progress", Context.MODE_PRIVATE)
        val prefix = "${profile.length}:$profile:"
        prefs.all.mapNotNull { (key, value) ->
            if (!key.startsWith(prefix)) return@mapNotNull null
            AlgorithmStatus.entries.find { it.name == value }?.let { key.removePrefix(prefix) to it }
        }.toMap()
    }
}
