package metropolia.myapplication.solver

import androidx.room.Room
import kotlinx.coroutines.runBlocking
import metropolia.myapplication.data.local.QubeDatabase
import metropolia.myapplication.data.local.buildQubeDatabase
import kotlin.test.Test

class CfopDatabaseTest {
    @Test
    fun solvesFullCubeWithUnknownOllAndPllCases() = runBlocking {
        val database = Room.inMemoryDatabaseBuilder<QubeDatabase>().buildQubeDatabase()
        try {
            verifyFullSolveWithDatabase(database)
        } finally {
            database.close()
        }
    }
}
