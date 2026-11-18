package metropolia.myapplication.timer

import androidx.compose.runtime.Composable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import metropolia.myapplication.data.local.QubeDatabase
import metropolia.myapplication.data.local.SolveTimeEntity
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

internal class SolveStorage(
    database: QubeDatabase,
    private val readLegacyHistory: () -> String,
) {
    private val dao = database.solves()
    private val importMutex = Mutex()
    private var imported = false

    val history: Flow<List<Solve>> = flow {
        ensureImported()
        emitAll(dao.observeGuestHistory().map { rows ->
            rows.map { Solve(it.timeMs, it.scramble) }
        })
    }

    @OptIn(ExperimentalTime::class)
    fun completedSolve(solve: Solve): SolveTimeEntity =
        solve.toEntity(dateRecorded = Clock.System.now().toEpochMilliseconds())

    suspend fun save(solve: SolveTimeEntity) {
        ensureImported()
        dao.insert(solve)
    }

    private suspend fun ensureImported() = importMutex.withLock {
        if (!imported) {
            withContext(Dispatchers.Default) {
                if (!dao.wasLegacyHistoryImported()) {
                    val legacySolves = decodeSolves(readLegacyHistory())
                    dao.importLegacyHistory(legacySolves.map { it.toEntity(dateRecorded = null) })
                }
            }
            imported = true
        }
    }

    @OptIn(ExperimentalUuidApi::class)
    private fun Solve.toEntity(dateRecorded: Long?) = SolveTimeEntity(
        solveId = Uuid.random().toString(),
        userId = null,
        timeMs = durationMillis,
        scramble = scramble,
        dateRecorded = dateRecorded,
        cubeType = "3x3",
    )
}

@Composable
internal expect fun rememberSolveStorage(): SolveStorage
