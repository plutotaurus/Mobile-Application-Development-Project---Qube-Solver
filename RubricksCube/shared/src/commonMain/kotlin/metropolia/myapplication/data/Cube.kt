package metropolia.myapplication.data

import androidx.compose.ui.graphics.Color
import metropolia.myapplication.renderingCube.Vector3i
import metropolia.myapplication.renderingCube.animaton.CubeRotation

enum class CubeColor(val color: Color) {
    WHITE (Color.White),
    YELLOW (Color(0xFFFFD500)),
    RED (Color(0xFFE53935)),
    ORANGE (Color(0xFFFF8F00)),
    BLUE (Color(0xFF303F9F)),
    GREEN(Color(0xFF2E9D50))
}


enum class Face(val axis: Int, val sign: Int) {
    RIGHT(0, 1),
    LEFT(0, -1),
    TOP(1, 1),
    BOTTOM(1, -1),
    FRONT(2, 1),
    BACK(2, -1)
}


data class Cubelet(
    val position: Vector3i,
    val stickers: Map<Face, CubeColor>
)

data class Cube(
    val cubelets: List<Cubelet>,
){
    /** Returns a new cube after applying the moves in order, without animation. */
    fun applyMoves(moves: List<Move>): Cube =
        moves.fold(this) { cube, move -> CubeRotation.applyMove(cube, move) }

    /** Supports standard notation, for example "R U R' U'", including x/y/z rotations. */
    fun applyMoves(sequence: String): Cube {
        if (sequence.isBlank()) return this
        val moves = requireNotNull(parseMoveSequence(sequence)) {
            "Invalid move sequence: $sequence"
        }
        return moves.fold(this) { cube, move ->
            when (move) {
                is SequenceMove.FaceTurn -> CubeRotation.applyMove(cube, move.move)
                is SequenceMove.CubeTurn -> CubeOrientation.rotate(cube, move.direction)
            }
        }
    }

    companion object {
        fun solved(): Cube {
            val pieces = mutableListOf<Cubelet>()

            for (x in -1..1) {
                for (y in -1..1) {
                    for (z in -1..1) {

                        if (x == 0 && y == 0 && z == 0) continue

                        val stickers = mutableMapOf<Face, CubeColor>()

                        if (x == 1) stickers[Face.RIGHT] = CubeColor.ORANGE
                        if (x == -1) stickers[Face.LEFT] = CubeColor.RED

                    if (y == 1) stickers[Face.TOP] = CubeColor.YELLOW
                    if (y == -1) stickers[Face.BOTTOM] = CubeColor.WHITE

                    if (z == 1) stickers[Face.FRONT] = CubeColor.GREEN
                    if (z == -1) stickers[Face.BACK] = CubeColor.BLUE

                    pieces.add(
                        Cubelet(
                            position = Vector3i(x, y, z),
                            stickers = stickers
                        )
                    )
                }
            }
        }

        return Cube(pieces)
    }
    }
}

object CubeOrientation {
    fun rotate(cube: Cube, direction: RotateCube): Cube =
        CubeRotation.applyWholeCubeTurn(cube, direction.faceTurn)
}
