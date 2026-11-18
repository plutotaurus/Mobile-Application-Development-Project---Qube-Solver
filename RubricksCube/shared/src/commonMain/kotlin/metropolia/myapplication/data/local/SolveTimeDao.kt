package metropolia.myapplication.data.local

import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

private const val LEGACY_SOLVE_HISTORY = "solve_history"

@Entity(tableName = "history_imports")
internal data class HistoryImportEntity(@PrimaryKey val source: String)

@Dao
internal abstract class SolveTimeDao {
    @Query("SELECT * FROM solve_times WHERE userId IS NULL ORDER BY localOrder")
    abstract fun observeGuestHistory(): Flow<List<SolveTimeEntity>>

    @Query("SELECT * FROM solve_times WHERE userId = :userId ORDER BY localOrder")
    abstract fun observeHistory(userId: String): Flow<List<SolveTimeEntity>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    abstract suspend fun insert(solve: SolveTimeEntity)

    @Query("SELECT EXISTS(SELECT 1 FROM history_imports WHERE source = '$LEGACY_SOLVE_HISTORY')")
    abstract suspend fun wasLegacyHistoryImported(): Boolean

    @Insert
    protected abstract suspend fun insertImported(solves: List<SolveTimeEntity>)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    protected abstract suspend fun markImported(import: HistoryImportEntity): Long

    @Transaction
    open suspend fun importLegacyHistory(solves: List<SolveTimeEntity>) {
        // Marking and inserting in one transaction makes the import safe to retry.
        if (markImported(HistoryImportEntity(LEGACY_SOLVE_HISTORY)) != -1L) {
            insertImported(solves)
        }
    }
}
