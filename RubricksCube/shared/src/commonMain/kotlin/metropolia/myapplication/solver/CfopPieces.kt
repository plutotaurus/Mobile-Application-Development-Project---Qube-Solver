package metropolia.myapplication.solver

import metropolia.myapplication.data.Cube
import metropolia.myapplication.data.CubeColor
import metropolia.myapplication.data.Cubelet
import metropolia.myapplication.data.Face
import metropolia.myapplication.renderingCube.Vector3i

internal data class PiecePosition(val position: Vector3i, val direction: Face)

internal class PieceMoves(val notation: List<String>) {
    private val positions = Cube.solved().cubelets
        .filter { it.stickers.size >= 2 }
        .groupBy { it.stickers.size }
        .mapValues { (_, pieces) ->
            pieces.flatMap { piece ->
                piece.stickers.keys.map { PiecePosition(piece.position, it) }
            }
        }

    val edges = transitions(2)
    val corners = transitions(3)

    private fun transitions(size: Int): Array<IntArray> {
        val states = positions.getValue(size)
        return Array(notation.size) { move ->
            IntArray(states.size) { index ->
                val (position, direction) = states[index]
                val marker = Cube(listOf(Cubelet(position, mapOf(direction to CubeColor.WHITE))))
                val piece = marker.applyMoves(notation[move]).cubelets.single()
                states.indexOf(PiecePosition(piece.position, piece.stickers.keys.single()))
            }
        }
    }

    fun index(piece: Cubelet, referenceColor: CubeColor): Int {
        val face = piece.stickers.entries.single { it.value == referenceColor }.key
        return positions.getValue(piece.stickers.size).indexOf(PiecePosition(piece.position, face))
    }
}

internal fun Cube.centers(): Map<Face, CubeColor> =
    cubelets.filter { it.stickers.size == 1 }.associate { it.stickers.entries.single().toPair() }

internal fun Cube.pieceWithColors(colors: Collection<CubeColor>): Cubelet =
    cubelets.singleOrNull { it.stickers.size == colors.size && it.stickers.values.containsAll(colors) }
        ?: throw IllegalArgumentException("A cube piece is missing or duplicated.")

internal fun Cube.isSolvedPiece(piece: Cubelet, centers: Map<Face, CubeColor>): Boolean =
    piece.stickers.all { (face, color) -> centers[face] == color }
