package metropolia.myapplication

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import platform.Foundation.NSUserDefaults

@Composable
internal actual fun rememberLegacyProgress(profile: String): Map<String, AlgorithmStatus> = remember(profile) {
    val defaults = NSUserDefaults.standardUserDefaults
    val prefix = "algorithm_progress.${profile.length}:$profile:"
    defaults.dictionaryRepresentation().mapNotNull { (key, value) ->
        if (key !is String || !key.startsWith(prefix)) return@mapNotNull null
        AlgorithmStatus.entries.find { it.name == value }?.let { key.removePrefix(prefix) to it }
    }.toMap()
}
