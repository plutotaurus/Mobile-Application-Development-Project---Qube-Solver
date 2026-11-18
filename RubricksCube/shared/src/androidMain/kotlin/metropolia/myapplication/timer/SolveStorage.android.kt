package metropolia.myapplication.timer

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import metropolia.myapplication.data.local.getQubeDatabase

private object AndroidSolveStorage {
    private var storage: SolveStorage? = null

    @Synchronized
    fun get(context: Context): SolveStorage = storage ?: run {
        val appContext = context.applicationContext
        SolveStorage(getQubeDatabase(appContext)) {
            appContext.getSharedPreferences("solve_history", Context.MODE_PRIVATE)
                .getString("solves", "").orEmpty()
        }.also { storage = it }
    }
}

@Composable
internal actual fun rememberSolveStorage(): SolveStorage {
    val context = LocalContext.current.applicationContext
    return remember(context) { AndroidSolveStorage.get(context) }
}
