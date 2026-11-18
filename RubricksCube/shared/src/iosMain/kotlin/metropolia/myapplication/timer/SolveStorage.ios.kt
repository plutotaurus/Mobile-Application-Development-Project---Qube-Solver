package metropolia.myapplication.timer

import androidx.compose.runtime.Composable
import metropolia.myapplication.data.local.iosQubeDatabase
import platform.Foundation.NSUserDefaults

private val iosSolveStorage by lazy {
    SolveStorage(iosQubeDatabase) {
        NSUserDefaults.standardUserDefaults.stringForKey("solve_history").orEmpty()
    }
}

@Composable
internal actual fun rememberSolveStorage(): SolveStorage = iosSolveStorage
