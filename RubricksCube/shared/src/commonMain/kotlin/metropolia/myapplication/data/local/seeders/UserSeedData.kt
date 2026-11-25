package metropolia.myapplication.data.local.seeders

import metropolia.myapplication.data.local.PasswordHasher
import metropolia.myapplication.data.local.UserDao
import metropolia.myapplication.data.local.UserEntity
import kotlin.uuid.Uuid

internal val defaultUser = UserEntity(
    userId = Uuid.random().toString(),
    username = "SpeedySam",
    passwordHash = PasswordHasher.hashPassword("password123"),
    solvingMethod = "CFOP",
    cubeBrand = "GAN 12 MagLev",
    email = "sam@speedcuber.org"
)

internal suspend fun UserDao.seedSampleUser() {
    save(defaultUser)
}