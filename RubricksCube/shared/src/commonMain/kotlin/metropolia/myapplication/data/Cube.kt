package metropolia.myapplication.data

import androidx.compose.ui.graphics.Color
import metropolia.myapplication.renderingCube.Vector3i

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
    companion object {
        fun solved(): Cube {
            val pieces = mutableListOf<Cubelet>()

            for (x in -1..1) {
                for (y in -1..1) {
                    for (z in -1..1) {

                        if (x == 0 && y == 0 && z == 0) continue

                        val stickers = mutableMapOf<Face, CubeColor>()

                        if (x == 1) stickers[Face.RIGHT] = CubeColor.BLUE
                        if (x == -1) stickers[Face.LEFT] = CubeColor.GREEN

                    if (y == 1) stickers[Face.TOP] = CubeColor.WHITE
                    if (y == -1) stickers[Face.BOTTOM] = CubeColor.YELLOW

                    if (z == 1) stickers[Face.FRONT] = CubeColor.RED
                    if (z == -1) stickers[Face.BACK] = CubeColor.ORANGE

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

    fun rotate(cube: Cube, direction: RotateCube): Cube {
        val rotatedCubelets = cube.cubelets.map { cubelet ->

            val newPosition = rotatePosition(
                cubelet.position,
                direction
            )

            val newStickers = cubelet.stickers.mapKeys { (face, _) ->
                rotateFace(face, direction)
            }

            cubelet.copy(
                position = newPosition,
                stickers = newStickers
            )
        }

        return cube.copy(cubelets = rotatedCubelets)
    }

    private fun rotatePosition(
        position: Vector3i,
        direction: RotateCube
    ): Vector3i {

        val (x, y, z) = position

        return when (direction) {
            RotateCube.X -> Vector3i(x, -z, y)
            RotateCube.X_PRIME -> Vector3i(x, z, -y)

            RotateCube.Y -> Vector3i(z, y, -x)
            RotateCube.Y_PRIME -> Vector3i(-z, y, x)

            RotateCube.Z -> Vector3i(-y, x, z)
            RotateCube.Z_PRIME -> Vector3i(y, -x, z)
        }
    }

    private fun rotateFace(
        face: Face,
        direction: RotateCube
    ): Face {
        val normal = when (face) {
            Face.RIGHT -> Vector3i(1, 0, 0)
            Face.LEFT -> Vector3i(-1, 0, 0)
            Face.TOP -> Vector3i(0, 1, 0)
            Face.BOTTOM -> Vector3i(0, -1, 0)
            Face.FRONT -> Vector3i(0, 0, 1)
            Face.BACK -> Vector3i(0, 0, -1)
        }

        val rotated = rotatePosition(normal, direction)

        return when {
            rotated.x == 1 -> Face.RIGHT
            rotated.x == -1 -> Face.LEFT
            rotated.y == 1 -> Face.TOP
            rotated.y == -1 -> Face.BOTTOM
            rotated.z == 1 -> Face.FRONT
            else -> Face.BACK
        }
    }
}