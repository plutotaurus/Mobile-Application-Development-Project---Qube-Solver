package metropolia.myapplication.renderingCube

import metropolia.myapplication.data.Face


data class Vector3i(
    val x: Int,
    val y: Int,
    val z: Int
)

data class Vector3(
    val x: Float,
    val y: Float,
    val z: Float
)

private fun rotatePosition(
    position: Vector3i,
    axis: Int,
    direction: Int
): Vector3i {

    val x = position.x
    val y = position.y
    val z = position.z

    return when (axis) {

        // X-axis
        0 -> if (direction == 1) {
            Vector3i(x, -z, y)
        } else {
            Vector3i(x, z, -y)
        }

        // Y-axis
        1 -> if (direction == 1) {
            Vector3i(z, y, -x)
        } else {
            Vector3i(-z, y, x)
        }

        // Z-axis
        else -> if (direction == 1) {
            Vector3i(-y, x, z)
        } else {
            Vector3i(y, -x, z)
        }
    }
}

private fun rotateFace(
    face: Face,
    axis: Int,
    direction: Int
): Face {

    val normal = when (face) {
        Face.RIGHT -> Vector3i(1, 0, 0)
        Face.LEFT -> Vector3i(-1, 0, 0)
        Face.TOP -> Vector3i(0, 1, 0)
        Face.BOTTOM -> Vector3i(0, -1, 0)
        Face.FRONT -> Vector3i(0, 0, 1)
        Face.BACK -> Vector3i(0, 0, -1)
    }

    val rotated = rotatePosition(normal, axis, direction)

    return when {
        rotated.x == 1 -> Face.RIGHT
        rotated.x == -1 -> Face.LEFT
        rotated.y == 1 -> Face.TOP
        rotated.y == -1 -> Face.BOTTOM
        rotated.z == 1 -> Face.FRONT
        else -> Face.BACK
    }
}

fun getFaceNormal(face: Face):Vector3 {
    return when (face) {
        Face.RIGHT -> Vector3(1f, 0f, 0f)
        Face.LEFT -> Vector3(-1f, 0f, 0f)
        Face.TOP -> Vector3(0f, 1f, 0f)
        Face.BOTTOM -> Vector3(0f, -1f, 0f)
        Face.FRONT -> Vector3(0f, 0f, 1f)
        Face.BACK -> Vector3(0f, 0f, -1f)
    }
}

fun getFaceVertices(
    position: Vector3i,
    face: Face
): List<Vector3> {

    val x = position.x.toFloat()
    val y = position.y.toFloat()
    val z = position.z.toFloat()

    val half = 0.49f
    val inset = 0.46f

    val plane = when (face.axis) {
        0 -> x + face.sign * half
        1 -> y + face.sign * half
        else -> z + face.sign * half
    }

    return when (face.axis) {

        // X faces
        0 -> listOf(
            Vector3(plane, y - inset, z - inset),
            Vector3(plane, y + inset, z - inset),
            Vector3(plane, y + inset, z + inset),
            Vector3(plane, y - inset, z + inset)
        )

        // Y faces
        1 -> listOf(
            Vector3(x - inset, plane, z - inset),
            Vector3(x + inset, plane, z - inset),
            Vector3(x + inset, plane, z + inset),
            Vector3(x - inset, plane, z + inset)
        )

        // Z faces
        else -> listOf(
            Vector3(x - inset, y - inset, plane),
            Vector3(x + inset, y - inset, plane),
            Vector3(x + inset, y + inset, plane),
            Vector3(x - inset, y + inset, plane)
        )
    }
}