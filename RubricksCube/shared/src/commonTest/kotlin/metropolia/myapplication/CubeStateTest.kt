package metropolia.myapplication

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotEquals
import metropolia.myapplication.data.Cube
import metropolia.myapplication.data.CubeOrientation
import metropolia.myapplication.data.Move
import metropolia.myapplication.data.RotateCube

class CubeStateTest {
    @Test
    fun animatorStartsAndResetsToDeclaredCube() {
        val initialCube = Cube.solved().applyMoves("R U F2")
        val animator = metropolia.myapplication.renderingCube.animaton.CubeAnimator(initialCube)
        assertEquals(initialCube, animator.cube)
        animator.rotateCube(RotateCube.X)
        assertNotEquals(initialCube, animator.cube)
        animator.reset()
        assertEquals(initialCube, animator.cube)
    }

    @Test
    fun scrambleAndInverseRestoreSolvedState() {
        val solved = Cube.solved()
        val scrambled = solved.applyMoves("R U F2 L' D B2")
        assertNotEquals(solved, scrambled)
        assertEquals(solved, scrambled.applyMoves("B2 D' L F2 U' R'"))
        assertEquals(Cube.solved(), solved)
    }

    @Test
    fun notationMatchesOrderedMoveList() {
        assertEquals(
            Cube.solved().applyMoves(listOf(Move.R, Move.U, Move.R_PRIME, Move.U_PRIME)),
            Cube.solved().applyMoves("R U R' U'")
        )
    }

    @Test
    fun supportsWholeCubeRotations() {
        assertEquals(
            CubeOrientation.rotate(Cube.solved(), RotateCube.X),
            Cube.solved().applyMoves("x")
        )
        assertEquals(Cube.solved(), Cube.solved().applyMoves("x y z z' y' x'"))
    }

    @Test
    fun faceTurnsUseTheNewOrientationAfterCubeRotations() {
        val cube = Cube.solved().applyMoves("R U F2 L' D B2")

        // Rotate a different face into the top, front, or right position.
        assertEquals(cube.applyMoves("F"), cube.applyMoves("x U x'"))
        assertEquals(cube.applyMoves("R"), cube.applyMoves("y F y'"))
        assertEquals(cube.applyMoves("U"), cube.applyMoves("z R z'"))
    }

    @Test
    fun emptySequenceLeavesCubeUnchangedAndInvalidNotationIsRejected() {
        val cube = Cube.solved()
        assertEquals(cube, cube.applyMoves("  "))
        assertEquals(cube, cube.applyMoves(emptyList()))
        assertFailsWith<IllegalArgumentException> { cube.applyMoves("R invalid U") }
    }
}
