package metropolia.myapplication.timer

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import org.worldcubeassociation.tnoodle.puzzle.ThreeByThreeCubePuzzle
import java.security.SecureRandom

private val scrambleMutex = Mutex()
private val puzzle by lazy { ThreeByThreeCubePuzzle() }
private val random by lazy { SecureRandom() }

actual suspend fun generate3x3Scramble(): String = withContext(Dispatchers.Default) {
    // TNoodle samples a random legal state, solves it in reverse, and filters trivial states.
    scrambleMutex.withLock { puzzle.generateWcaScramble(random).trim() }
}
