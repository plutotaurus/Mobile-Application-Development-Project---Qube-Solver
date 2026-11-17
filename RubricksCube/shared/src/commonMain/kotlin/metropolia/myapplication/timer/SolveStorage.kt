package metropolia.myapplication.timer

import androidx.compose.runtime.Composable

interface SolveStorage {
    fun load(): List<Solve>
    fun save(solves: List<Solve>)
}

@Composable
expect fun rememberSolveStorage(): SolveStorage

