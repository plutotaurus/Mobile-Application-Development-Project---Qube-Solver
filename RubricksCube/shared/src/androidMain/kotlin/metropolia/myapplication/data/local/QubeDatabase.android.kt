package metropolia.myapplication.data.local

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.room.Room
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import metropolia.myapplication.data.local.seeders.seedSampleUser

private object AndroidQubeDatabase {
    private var database: QubeDatabase? = null

    @Synchronized
    fun get(context: Context): QubeDatabase = database ?: Room
        .databaseBuilder<QubeDatabase>(context.applicationContext, "qube.db")
        .fallbackToDestructiveMigration(true)
        .buildQubeDatabase()
        .also { db ->
            database = db
            CoroutineScope(Dispatchers.Default).launch {
                db.users().seedSampleUser()
            }
        }
}

internal fun getQubeDatabase(context: Context): QubeDatabase = AndroidQubeDatabase.get(context)

@Composable
internal actual fun rememberQubeDatabase(): QubeDatabase {
    val context = LocalContext.current.applicationContext
    return remember(context) { getQubeDatabase(context) }
}
