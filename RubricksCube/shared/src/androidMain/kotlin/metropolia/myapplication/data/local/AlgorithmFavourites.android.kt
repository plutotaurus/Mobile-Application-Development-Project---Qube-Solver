package metropolia.myapplication.data.local

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext

@Composable
internal actual fun rememberLegacyAlgorithmFavourites(): Map<String, String> {
    val context = LocalContext.current.applicationContext
    return remember(context) {
        val preferences = context.getSharedPreferences("algorithm_favourites", Context.MODE_PRIVATE)
        preferences.all.mapNotNull { (id, value) ->
            (value as? String)?.let { id to it }
        }.toMap()
    }
}
