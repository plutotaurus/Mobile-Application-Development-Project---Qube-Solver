package metropolia.myapplication.timer

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import platform.Foundation.NSUserDefaults

@Composable
actual fun rememberSolveStorage(): SolveStorage = remember {
    object : SolveStorage {
        private val defaults = NSUserDefaults.standardUserDefaults
        override fun load() = decodeSolves(defaults.stringForKey("solve_history") ?: "")
        override fun save(solves: List<Solve>) {
            defaults.setObject(encodeSolves(solves), forKey = "solve_history")
        }
    }
}
