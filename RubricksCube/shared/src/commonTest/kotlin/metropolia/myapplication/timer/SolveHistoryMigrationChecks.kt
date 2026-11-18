package metropolia.myapplication.timer

import kotlinx.coroutines.flow.first
import metropolia.myapplication.data.local.QubeDatabase
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

internal suspend fun verifySolveHistoryMigration(database: QubeDatabase) {
    val legacy = listOf(Solve(12_000, "R U"), Solve(8_000, "F"), Solve(8_000, "F"))
    val storage = SolveStorage(database) { encodeSolves(legacy) }
    assertEquals(legacy, storage.history.first())

    val restartedStorage = SolveStorage(database) { error("History was already imported") }
    val newSolve = Solve(11_000, "L R")
    val completed = restartedStorage.completedSolve(newSolve)
    restartedStorage.save(completed)
    restartedStorage.save(completed)

    assertEquals(legacy + newSolve, restartedStorage.history.first())
    val records = database.solves().observeGuestHistory().first()
    assertEquals(listOf(null, null, null), records.take(legacy.size).map { it.dateRecorded })
    assertNotNull(records.last().dateRecorded)
}
