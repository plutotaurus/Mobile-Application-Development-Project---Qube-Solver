package metropolia.myapplication.solver

import androidx.room.Room
import androidx.test.platform.app.InstrumentationRegistry
import kotlinx.coroutines.runBlocking
import metropolia.myapplication.data.local.QubeDatabase
import metropolia.myapplication.data.local.buildQubeDatabase
import kotlin.test.Test

class CfopDatabaseTest {
    @Test
    fun solvesFullCubeWithUnknownOllAndPllCases() = runBlocking {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val database = Room.inMemoryDatabaseBuilder<QubeDatabase>(context).buildQubeDatabase()
        try {
            verifyFullSolveWithDatabase(database)
        } finally {
            database.close()
        }
    }
}
