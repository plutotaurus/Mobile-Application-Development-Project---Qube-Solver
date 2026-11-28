package metropolia.myapplication.data.local

import metropolia.myapplication.AlgorithmStatus

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "users", indices = [Index(value = ["username"], unique = true)])
internal data class UserEntity(
    @PrimaryKey val userId: String,
    val username: String,
    val solvingMethod: String? = null,
    val passwordHash: String? = null,
    val cubeBrand: String? = null,
    val email: String? = null,
    val picture: String? = null,
)

@Entity(
    tableName = "solve_times",
    foreignKeys = [ForeignKey(
        entity = UserEntity::class,
        parentColumns = ["userId"],
        childColumns = ["userId"],
        onDelete = ForeignKey.RESTRICT,
    )],
    indices = [Index("userId"), Index(value = ["solveId"], unique = true)],
)
internal data class SolveTimeEntity(
    val solveId: String,
    val userId: String?,
    val timeMs: Long,
    val scramble: String,
    val dateRecorded: Long?,
    val cubeType: String,
    // Keep insertion order even when older solves have no date.
    @PrimaryKey(autoGenerate = true) val localOrder: Long = 0,
)

@Entity(
    tableName = "algorithms",
    foreignKeys = [ForeignKey(
        entity = UserEntity::class,
        parentColumns = ["userId"],
        childColumns = ["userId"],
        onDelete = ForeignKey.CASCADE,
    )],
    indices = [Index("userId")],
)
internal data class AlgorithmEntity(
    @PrimaryKey val algorithmId: String,
    val userId: String,
    val type: String,
    val algorithmText: String,
)

@Entity(
    tableName = "algorithm_variants",
    primaryKeys = ["algorithmId", "algorithmText"],
    foreignKeys = [ForeignKey(
        entity = AlgorithmEntity::class,
        parentColumns = ["algorithmId"],
        childColumns = ["algorithmId"],
        onDelete = ForeignKey.CASCADE,
    )],
)
internal data class AlgorithmVariantEntity(
    val algorithmId: String,
    val algorithmText: String,
)

@Entity(
    tableName = "algorithm_favourites",
    foreignKeys = [ForeignKey(
        entity = AlgorithmVariantEntity::class,
        parentColumns = ["algorithmId", "algorithmText"],
        childColumns = ["algorithmId", "algorithmText"],
        onDelete = ForeignKey.CASCADE,
    )],
    indices = [Index(value = ["algorithmId", "algorithmText"])],
)
internal data class AlgorithmFavouriteEntity(
    @PrimaryKey val algorithmId: String,
    val algorithmText: String,
)

@Entity(
    tableName = "algorithm_progress",
    primaryKeys = ["profile", "algorithmId"],
    foreignKeys = [ForeignKey(
        entity = AlgorithmEntity::class,
        parentColumns = ["algorithmId"], childColumns = ["algorithmId"],
        onDelete = ForeignKey.CASCADE,
    )],
    indices = [Index("algorithmId")],
)
internal data class AlgorithmProgressEntity(
    val profile: String,
    val algorithmId: String,
    val status: AlgorithmStatus,
)

internal data class AlgorithmWithFavourite(
    @Embedded val algorithm: AlgorithmEntity,
    val favouriteText: String?,
    val status: AlgorithmStatus = AlgorithmStatus.Unlearned,
) {
    val selectedMoves: String get() = favouriteText ?: algorithm.algorithmText
}

@Entity(
    tableName = "races",
    foreignKeys = [ForeignKey(
        entity = UserEntity::class,
        parentColumns = ["userId"],
        childColumns = ["creatorUserId"],
        onDelete = ForeignKey.CASCADE,
    )],
    indices = [Index("creatorUserId")],
)
internal data class RaceEntity(
    @PrimaryKey val raceId: String,
    val creatorUserId: String,
    val createdAt: Long,
    val scramble: String,
    val status: String,
)

@Entity(
    tableName = "race_participants",
    primaryKeys = ["raceId", "userId"],
    foreignKeys = [
        ForeignKey(RaceEntity::class, ["raceId"], ["raceId"], onDelete = ForeignKey.CASCADE),
        ForeignKey(UserEntity::class, ["userId"], ["userId"], onDelete = ForeignKey.CASCADE),
        ForeignKey(SolveTimeEntity::class, ["solveId"], ["solveId"], onDelete = ForeignKey.SET_NULL),
    ],
    indices = [Index("userId"), Index("solveId")],
)
internal data class RaceParticipantEntity(
    val raceId: String,
    val userId: String,
    val solveId: String? = null,
    val position: Int? = null,
)

@Entity(
    tableName = "shared_solves",
    foreignKeys = [
        ForeignKey(SolveTimeEntity::class, ["solveId"], ["solveId"], onDelete = ForeignKey.CASCADE),
        ForeignKey(UserEntity::class, ["userId"], ["sharedWithUserId"], onDelete = ForeignKey.CASCADE),
    ],
    indices = [Index(value = ["solveId", "sharedWithUserId"], unique = true), Index("sharedWithUserId")],
)
internal data class SharedSolveEntity(
    @PrimaryKey val shareId: String,
    val solveId: String,
    val sharedWithUserId: String,
)

@Entity(
    tableName = "friendships",
    foreignKeys = [
        ForeignKey(UserEntity::class, ["userId"], ["requesterUserId"], onDelete = ForeignKey.CASCADE),
        ForeignKey(UserEntity::class, ["userId"], ["receiverUserId"], onDelete = ForeignKey.CASCADE),
    ],
    indices = [Index(value = ["requesterUserId", "receiverUserId"], unique = true), Index("receiverUserId")],
)
internal data class FriendshipEntity(
    @PrimaryKey val friendId: String,
    val requesterUserId: String,
    val receiverUserId: String,
    val status: String,
)

@Entity(tableName = "badges")
internal data class BadgeEntity(
    @PrimaryKey val badgeId: String,
    val title: String,
    val description: String,
    val icon: String?,
)

@Entity(
    tableName = "user_badges",
    primaryKeys = ["userId", "badgeId"],
    foreignKeys = [
        ForeignKey(UserEntity::class, ["userId"], ["userId"], onDelete = ForeignKey.CASCADE),
        ForeignKey(BadgeEntity::class, ["badgeId"], ["badgeId"], onDelete = ForeignKey.CASCADE),
    ],
    indices = [Index("badgeId")],
)
internal data class UserBadgeEntity(
    val userId: String,
    val badgeId: String,
)
