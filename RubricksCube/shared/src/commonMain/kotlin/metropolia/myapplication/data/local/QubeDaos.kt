package metropolia.myapplication.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
internal interface UserDao {
    @Upsert
    suspend fun save(user: UserEntity)

    @Query("SELECT * FROM users WHERE userId = :userId")
    fun observe(userId: String): Flow<UserEntity?>
}

@Dao
internal abstract class AlgorithmDao {
    @Upsert
    abstract suspend fun save(algorithm: AlgorithmEntity)

    @Query("SELECT * FROM algorithms WHERE userId = :userId ORDER BY type, algorithmId")
    abstract fun observeForUser(userId: String): Flow<List<AlgorithmEntity>>

    @Query("SELECT * FROM algorithms ORDER BY type, algorithmId")
    abstract fun observeAll(): Flow<List<AlgorithmEntity>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    protected abstract suspend fun insertCatalogOwner(user: UserEntity)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    protected abstract suspend fun insertStarterAlgorithms(algorithms: List<AlgorithmEntity>)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    protected abstract suspend fun markCatalogImported(import: HistoryImportEntity): Long

    @Transaction
    open suspend fun importStarterCatalog(
        versionKey: String,
        owner: UserEntity,
        algorithms: List<AlgorithmEntity>,
    ) {
        if (markCatalogImported(HistoryImportEntity(versionKey)) != -1L) {
            insertCatalogOwner(owner)
            insertStarterAlgorithms(algorithms)
        }
    }
}

@Dao
internal interface RaceDao {
    @Upsert
    suspend fun save(race: RaceEntity)

    @Upsert
    suspend fun saveParticipant(participant: RaceParticipantEntity)

    @Query("SELECT * FROM races WHERE raceId = :raceId")
    fun observe(raceId: String): Flow<RaceEntity?>

    @Query("SELECT * FROM race_participants WHERE raceId = :raceId ORDER BY position IS NULL, position, userId")
    fun observeParticipants(raceId: String): Flow<List<RaceParticipantEntity>>
}

@Dao
internal abstract class SocialDao {
    @Insert
    abstract suspend fun shareSolve(share: SharedSolveEntity)

    @Query("""
        SELECT solve_times.* FROM solve_times
        JOIN shared_solves ON shared_solves.solveId = solve_times.solveId
        WHERE sharedWithUserId = :userId
        ORDER BY solve_times.localOrder DESC
    """)
    abstract fun observeSharedSolves(userId: String): Flow<List<SolveTimeEntity>>

    @Query("SELECT * FROM friendships WHERE requesterUserId = :userId OR receiverUserId = :userId")
    abstract fun observeFriendships(userId: String): Flow<List<FriendshipEntity>>

    @Query("""
        SELECT EXISTS(SELECT 1 FROM friendships
        WHERE (requesterUserId = :first AND receiverUserId = :second)
        OR (requesterUserId = :second AND receiverUserId = :first))
    """)
    abstract suspend fun hasFriendship(first: String, second: String): Boolean

    @Insert
    protected abstract suspend fun insertFriendship(friendship: FriendshipEntity)

    @Transaction
    open suspend fun requestFriendship(friendship: FriendshipEntity) {
        require(friendship.requesterUserId != friendship.receiverUserId)
        if (!hasFriendship(friendship.requesterUserId, friendship.receiverUserId)) {
            insertFriendship(friendship)
        }
    }

    @Query("UPDATE friendships SET status = :status WHERE friendId = :friendId")
    abstract suspend fun updateFriendshipStatus(friendId: String, status: String)
}

@Dao
internal interface BadgeDao {
    @Upsert
    suspend fun save(badge: BadgeEntity)

    @Insert
    suspend fun award(badge: UserBadgeEntity)

    @Query("""
        SELECT badges.* FROM badges
        JOIN user_badges ON badges.badgeId = user_badges.badgeId
        WHERE user_badges.userId = :userId
        ORDER BY badges.title
    """)
    fun observeEarned(userId: String): Flow<List<BadgeEntity>>
}
