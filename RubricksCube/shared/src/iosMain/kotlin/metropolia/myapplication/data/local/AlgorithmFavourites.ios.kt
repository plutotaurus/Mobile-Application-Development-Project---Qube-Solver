package metropolia.myapplication.data.local

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import platform.Foundation.NSUserDefaults

private const val FAVOURITE_PREFIX = "algorithm_favourite."

@Composable
internal actual fun rememberLegacyAlgorithmFavourites(): Map<String, String> = remember {
    val defaults = NSUserDefaults.standardUserDefaults
    defaults.dictionaryRepresentation().mapNotNull { (key, value) ->
        val name = key as? String
        if (name == null || !name.startsWith(FAVOURITE_PREFIX) || value !is String) {
            null
        } else {
            name.removePrefix(FAVOURITE_PREFIX) to value
        }
    }.toMap()
}
