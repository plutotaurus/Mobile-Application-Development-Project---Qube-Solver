package metropolia.myapplication.data.local

import androidx.compose.runtime.Composable
import androidx.room.Room
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSSearchPathForDirectoriesInDomains
import platform.Foundation.NSUserDomainMask

internal val iosQubeDatabase by lazy {
    val directory = NSSearchPathForDirectoriesInDomains(
        NSDocumentDirectory, NSUserDomainMask, true,
    ).first() as String
    Room.databaseBuilder<QubeDatabase>(name = "$directory/qube.db").buildQubeDatabase()
}

@Composable
internal actual fun rememberQubeDatabase(): QubeDatabase = iosQubeDatabase
