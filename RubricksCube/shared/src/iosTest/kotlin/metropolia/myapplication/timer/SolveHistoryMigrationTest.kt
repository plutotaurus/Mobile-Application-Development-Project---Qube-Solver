package metropolia.myapplication.timer

import androidx.room.Room
import kotlinx.coroutines.runBlocking
import metropolia.myapplication.data.local.QubeDatabase
import metropolia.myapplication.data.local.buildQubeDatabase
import kotlin.test.Test

class SolveHistoryMigrationTest {
    @Test
    fun legacyHistoryKeepsItsOrderAndUnknownDatesWithoutImportingAgain() = runBlocking {
        val database = Room.inMemoryDatabaseBuilder<QubeDatabase>().buildQubeDatabase()
        try {
            verifySolveHistoryMigration(database)
        } finally {
            database.close()
        }
    }
}
