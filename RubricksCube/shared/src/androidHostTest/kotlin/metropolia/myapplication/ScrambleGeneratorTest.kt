package metropolia.myapplication

import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue
import metropolia.myapplication.data.parseMoveSequence
import metropolia.myapplication.timer.generate3x3Scramble
import org.worldcubeassociation.tnoodle.puzzle.ThreeByThreeCubePuzzle

class ScrambleGeneratorTest {
    @Test
    fun generatedScramblesParseAndPassTheWcaDistanceFilter() = runBlocking {
        val puzzle = ThreeByThreeCubePuzzle()
        repeat(3) {
            val scramble = generate3x3Scramble()
            assertTrue(scramble.isNotBlank())
            assertNotNull(parseMoveSequence(scramble))
            val state = puzzle.solvedState.applyAlgorithm(scramble)
            assertNull(state.solveIn(1))
        }
    }
}
