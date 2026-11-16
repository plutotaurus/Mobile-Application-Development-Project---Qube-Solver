package metropolia.myapplication.renderingCube.animaton

import metropolia.myapplication.data.Cube
import metropolia.myapplication.data.CubeColor
import metropolia.myapplication.data.Cubelet
import metropolia.myapplication.data.Face
import metropolia.myapplication.data.Move
import metropolia.myapplication.renderingCube.Vector3i

object CubeRotation {

    // Rotate a 3D integer vector by a quarter turn
    private fun rotateVector(
        vector: Vector3i,
        axis: Int,
        quarterTurns: Int
    ): Vector3i {

        var x = vector.x
        var y = vector.y
        var z = vector.z

        repeat(((quarterTurns % 4) + 4) % 4) {

            val oldX = x
            val oldY = y
            val oldZ = z

            when (axis) {
                0 -> {
                    x = oldX
                    y = -oldZ
                    z = oldY
                }

                1 -> {
                    x = oldZ
                    y = oldY
                    z = -oldX
                }

                2 -> {
                    x = -oldY
                    y = oldX
                    z = oldZ
                }
            }
        }

        return Vector3i(x, y, z)
    }

    // Convert a Face into its direction vector
    private fun faceToVector(face: Face): Vector3i {
        return when (face) {
            Face.RIGHT -> Vector3i(1, 0, 0)
            Face.LEFT -> Vector3i(-1, 0, 0)
            Face.TOP -> Vector3i(0, 1, 0)
            Face.BOTTOM -> Vector3i(0, -1, 0)
            Face.FRONT -> Vector3i(0, 0, 1)
            Face.BACK -> Vector3i(0, 0, -1)
        }
    }

    // Convert a direction vector back into a Face
    private fun vectorToFace(vector: Vector3i): Face {
        return when {
            vector.x == 1 -> Face.RIGHT
            vector.x == -1 -> Face.LEFT
            vector.y == 1 -> Face.TOP
            vector.y == -1 -> Face.BOTTOM
            vector.z == 1 -> Face.FRONT
            vector.z == -1 -> Face.BACK
            else -> error("Invalid face direction")
        }
    }

    // Rotate the stickers of a cubelet
    private fun rotateStickers(
        stickers: Map<Face, CubeColor>,
        axis: Int,
        quarterTurns: Int
    ): Map<Face, CubeColor> {

        val rotatedStickers = mutableMapOf<Face, CubeColor>()

        stickers.forEach { (face, color) ->

            val direction = faceToVector(face)

            val rotatedDirection = rotateVector(
                direction,
                axis,
                quarterTurns
            )

            val newFace = vectorToFace(rotatedDirection)

            rotatedStickers[newFace] = color
        }

        return rotatedStickers
    }

    // Apply a completed move to the cube
    fun applyMove(
        cube: Cube,
        move: Move
    ): Cube {

        val quarterTurns = (move.angle / 90f).toInt()

        val updatedCubelets = cube.cubelets.map { cubelet ->

            val position = cubelet.position

            val belongsToLayer = when (move.axis) {
                0 -> position.x == move.layer
                1 -> position.y == move.layer
                2 -> position.z == move.layer
                else -> false
            }

            if (!belongsToLayer) {
                cubelet
            } else {

                val newPosition = rotateVector(
                    position,
                    move.axis,
                    quarterTurns
                )

                val newStickers = rotateStickers(
                    cubelet.stickers,
                    move.axis,
                    quarterTurns
                )

                Cubelet(
                    position = newPosition,
                    stickers = newStickers
                )
            }
        }

        return Cube(updatedCubelets)
    }
}
