package metropolia.myapplication.renderingCube

import androidx.compose.ui.geometry.Offset
import metropolia.myapplication.data.Cube
import metropolia.myapplication.data.CubeColor
import metropolia.myapplication.data.Face
import metropolia.myapplication.data.Move
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin


data class Camera(
    val yaw: Float = 0.65f,
    val pitch: Float = -0.45f
)

data class RenderFace(
    val points: List<Offset>,
    val depth: Float,
    val face: Face,
    val color: CubeColor,
    val position: Vector3i,
    val arrow: List<Offset> = emptyList(),
)

fun rotate3D(
    point: Vector3,
    camera: Camera
): Vector3 {

    // Rotate around Y axis
    val cosY = cos(camera.yaw)
    val sinY = sin(camera.yaw)

    val x1 = point.x * cosY + point.z * sinY
    val z1 = -point.x * sinY + point.z * cosY

    // Rotate around X axis
    val cosX = cos(camera.pitch)
    val sinX = sin(camera.pitch)

    val y2 = point.y * cosX - z1 * sinX
    val z2 = point.y * sinX + z1 * cosX

    return Vector3(x1, y2, z2)
}

fun project3D(
    point: Vector3,
    width: Float,
    height: Float
): Offset {

    val cameraDistance = 6f
    val focalLength = 5f

    val depth = cameraDistance - point.z

    val scale = min(width, height) * 0.25f
    val perspective = focalLength / depth

    return Offset(
        x = width / 2f + point.x * perspective * scale,
        y = height / 2f - point.y * perspective * scale
    )
}

fun renderCube(
    cube: Cube,
    camera:Camera,
    width: Float,
    height: Float,
    move: Move? = null,
    angleDegrees: Float = 0f,
    wholeCube: Boolean = false,
): List<RenderFace> {

    val result = mutableListOf<RenderFace>()

    val radians = angleDegrees * (PI / 180).toFloat()
    val c = cos(radians)
    val s = sin(radians)
    for ((position, stickers) in cube.cubelets) {

        val layer = when (move?.axis) {
            0 -> position.x
            1 -> position.y
            else -> position.z
        }
        val turning = move != null && (wholeCube || layer == move.layer || (move.wide && layer == 0))
        val layerMove = if (turning) move else null
        fun view(point: Vector3): Vector3 {
            val rotated = if (layerMove == null) point else when (layerMove.axis) {
                0 -> Vector3(point.x, c * point.y - s * point.z, s * point.y + c * point.z)
                1 -> Vector3(c * point.x + s * point.z, point.y, -s * point.x + c * point.z)
                else -> Vector3(c * point.x - s * point.y, s * point.x + c * point.y, point.z)
            }
            return rotate3D(rotated, camera)
        }

        for ((face, color) in stickers) {

            val normal = view(getFaceNormal(face))

            // Ignore faces pointing away from the camera
            if (normal.z <= 0f) continue

            val vertices = getFaceVertices(
                position,
                face
            )

            val rotated = vertices.map {
                view(it)
            }

            val projected = rotated.map {
                project3D(it, width, height)
            }

            val depth = rotated.map {
                it.z
            }.average().toFloat()

            result.add(
                RenderFace(
                    points = projected,
                    depth = depth,
                    face = face,
                    color = color,
                    arrow = turnArrowVertices(vertices, face, layerMove).map {
                        project3D(view(it), width, height)
                    },
                    position = position
                )
            )
        }
    }
    // Draw distant faces first
    return result.sortedBy { it.depth }
}
