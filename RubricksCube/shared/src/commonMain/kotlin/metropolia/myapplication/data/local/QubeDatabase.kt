package metropolia.myapplication.data.local

import androidx.compose.runtime.Composable
import androidx.room.AutoMigration
import androidx.room.ConstructedBy
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.RoomDatabaseConstructor
import androidx.room.migration.Migration
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import androidx.sqlite.execSQL

@Database(
    entities = [
        UserEntity::class,
        SolveTimeEntity::class,
        AlgorithmEntity::class,
        AlgorithmVariantEntity::class,
        AlgorithmFavouriteEntity::class,
        AlgorithmProgressEntity::class,
        RaceEntity::class,
        RaceParticipantEntity::class,
        SharedSolveEntity::class,
        FriendshipEntity::class,
        BadgeEntity::class,
        UserBadgeEntity::class,
        HistoryImportEntity::class,
    ],
    version = 5,
    autoMigrations = [AutoMigration(from = 1, to = 2), AutoMigration(from = 2, to = 3), AutoMigration(from = 4, to = 5)],
)
@ConstructedBy(QubeDatabaseConstructor::class)
internal abstract class QubeDatabase : RoomDatabase() {
    abstract fun solves(): SolveTimeDao
    abstract fun users(): UserDao
    abstract fun algorithms(): AlgorithmDao
    abstract fun races(): RaceDao
    abstract fun social(): SocialDao
    abstract fun badges(): BadgeDao
}

@Suppress("KotlinNoActualForExpect")
internal expect object QubeDatabaseConstructor : RoomDatabaseConstructor<QubeDatabase> {
    override fun initialize(): QubeDatabase
}

// Version 3 shipped both before and after the nullable passwordHash column was added.
internal val migration3To4 = object : Migration(3, 4) {
    override fun migrate(connection: SQLiteConnection) {
        val hasPasswordHash = connection.prepare(
            "SELECT 1 FROM pragma_table_info('users') WHERE name = 'passwordHash'"
        ).use { it.step() }
        if (!hasPasswordHash) {
            connection.execSQL("ALTER TABLE users ADD COLUMN passwordHash TEXT")
        }
    }
}

internal fun RoomDatabase.Builder<QubeDatabase>.buildQubeDatabase(): QubeDatabase =
    addMigrations(migration3To4).setDriver(BundledSQLiteDriver()).build()

@Composable
internal expect fun rememberQubeDatabase(): QubeDatabase
