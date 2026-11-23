package metropolia.myapplication.data.local

import androidx.compose.runtime.Composable
import androidx.room.AutoMigration
import androidx.room.ConstructedBy
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.RoomDatabaseConstructor
import androidx.sqlite.driver.bundled.BundledSQLiteDriver

@Database(
    entities = [
        UserEntity::class,
        SolveTimeEntity::class,
        AlgorithmEntity::class,
        AlgorithmVariantEntity::class,
        AlgorithmFavouriteEntity::class,
        RaceEntity::class,
        RaceParticipantEntity::class,
        SharedSolveEntity::class,
        FriendshipEntity::class,
        BadgeEntity::class,
        UserBadgeEntity::class,
        HistoryImportEntity::class,
    ],
    version = 3,
    autoMigrations = [AutoMigration(from = 1, to = 2), AutoMigration(from = 2, to = 3)],
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

internal fun RoomDatabase.Builder<QubeDatabase>.buildQubeDatabase(): QubeDatabase =
    setDriver(BundledSQLiteDriver()).build()

@Composable
internal expect fun rememberQubeDatabase(): QubeDatabase
