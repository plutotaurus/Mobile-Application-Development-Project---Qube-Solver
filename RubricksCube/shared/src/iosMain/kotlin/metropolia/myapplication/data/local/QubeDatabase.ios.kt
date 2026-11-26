package metropolia.myapplication.data.local

import androidx.compose.runtime.Composable
import androidx.room.Room
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import metropolia.myapplication.data.local.seeders.seedSampleUser
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSSearchPathForDirectoriesInDomains
import platform.Foundation.NSUserDomainMask

internal val iosQubeDatabase by lazy {
    val directory = NSSearchPathForDirectoriesInDomains(
        NSDocumentDirectory, NSUserDomainMask, true,
    ).first() as String
    Room.databaseBuilder<QubeDatabase>(name = "$directory/qube.db")
        .fallbackToDestructiveMigration(true)
        .buildQubeDatabase()
        .also { db ->
            CoroutineScope(Dispatchers.Default).launch {
                db.users().seedSampleUser()
            }
        }
}

@Composable
internal actual fun rememberQubeDatabase(): QubeDatabase = iosQubeDatabase
