package metropolia.myapplication.data.local

import androidx.room.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import androidx.sqlite.execSQL
import androidx.test.platform.app.InstrumentationRegistry
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import java.util.UUID
import kotlin.test.Test
import kotlin.test.assertEquals

class QubeDatabaseMigrationTest {
    @Test
    fun upgradesBothVersion3SchemasWithoutLosingUsersOrSolves() = runBlocking {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val user = UserEntity(userId = "1", username = "test", passwordHash = "saved-hash")
        val solve = SolveTimeEntity("solve", user.userId, 12345, "R U", null, "3x3")

        for (hasPasswordHash in listOf(false, true)) {
            val name = "qube-migration-${UUID.randomUUID()}.db"
            fun open() = Room.databaseBuilder<QubeDatabase>(context, name).buildQubeDatabase()
            var database = open()
            try {
                database.users().save(user)
                database.solves().insert(solve)
                database.close()

                BundledSQLiteDriver().open(context.getDatabasePath(name).absolutePath).use { connection ->
                    connection.execSQL("DROP TABLE algorithm_progress")
                    if (!hasPasswordHash) connection.execSQL("ALTER TABLE users DROP COLUMN passwordHash")
                    val hash = if (hasPasswordHash) "126aa9017c1750016a1fc660f488c8ee"
                               else "b6e6a4cf4d6b7aac2a39509b9c43a878"
                    connection.execSQL("UPDATE room_master_table SET identity_hash = '$hash' WHERE id = 42")
                    connection.execSQL("PRAGMA user_version = 3")
                }

                database = open()
                val expected = if (hasPasswordHash) user else user.copy(passwordHash = null)
                assertEquals(expected, database.users().findByUsername(user.username))
                assertEquals(solve.solveId, database.solves().observeHistory(user.userId).first().single().solveId)
            } finally {
                database.close()
                context.deleteDatabase(name)
            }
        }
    }
}
