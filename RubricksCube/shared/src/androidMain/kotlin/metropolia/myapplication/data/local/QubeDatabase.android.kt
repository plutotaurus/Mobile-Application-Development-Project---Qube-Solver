package metropolia.myapplication.data.local

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.room.Room

private object AndroidQubeDatabase {
    private var database: QubeDatabase? = null

    @Synchronized
    fun get(context: Context): QubeDatabase = database ?: Room
        .databaseBuilder<QubeDatabase>(context.applicationContext, "qube.db")
        .fallbackToDestructiveMigration(true)
        .buildQubeDatabase()
        .also { database = it }
}

internal fun getQubeDatabase(context: Context): QubeDatabase = AndroidQubeDatabase.get(context)

@Composable
internal actual fun rememberQubeDatabase(): QubeDatabase {
    val context = LocalContext.current.applicationContext
    return remember(context) { getQubeDatabase(context) }
}
