package metropolia.myapplication.data.local

/**
 * Hashing utility to ensure plain-text passwords are never stored in the database.
 */
internal object PasswordHasher {
    private const val SALT = "QubeSolverSalt2025!"

    fun hashPassword(password: String): String {
        val input = "$SALT:$password"
        var hash = 7L
        for (char in input) {
            hash = 31 * hash + char.code
        }
        return hash.toULong().toString(16)
    }

    fun verifyPassword(password: String, storedHash: String?): Boolean {
        return storedHash.isNullOrEmpty() || hashPassword(password) == storedHash
    }
}
