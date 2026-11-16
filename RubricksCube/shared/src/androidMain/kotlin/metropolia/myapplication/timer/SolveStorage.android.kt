package metropolia.myapplication.timer

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext

@Composable
actual fun rememberSolveStorage(): SolveStorage {
    val context = LocalContext.current.applicationContext
    return remember(context) {
        val preferences = context.getSharedPreferences("solve_history", Context.MODE_PRIVATE)
        object : SolveStorage {
            override fun load() = decodeSolves(preferences.getString("solves", "") ?: "")
            override fun save(solves: List<Solve>) {
                preferences.edit().putString("solves", encodeSolves(solves)).apply()
            }
        }
    }
}
