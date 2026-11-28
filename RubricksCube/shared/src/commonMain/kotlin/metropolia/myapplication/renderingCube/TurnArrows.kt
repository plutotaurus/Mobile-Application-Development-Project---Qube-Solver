package metropolia.myapplication.renderingCube

import metropolia.myapplication.data.Face
import metropolia.myapplication.data.Move

// Build the arrow in sticker coordinates so it stays on the face throughout the turn.
internal fun turnArrowVertices(vertices: List<Vector3>, face: Face, move: Move?): List<Vector3> {
    // Only the perimeter stickers of a moving layer carry arrows.
    if (move == null || face.axis == move.axis) return emptyList()

    val a = vertices[0]
    val b = vertices[2]
    val center = Vector3((a.x + b.x) / 2, (a.y + b.y) / 2, (a.z + b.z) / 2)
    val tangent = when (move.axis) {
        0 -> Vector3(0f, -center.z, center.y)
        1 -> Vector3(center.z, 0f, -center.x)
        else -> Vector3(-center.y, center.x, 0f)
    }
    // These local axes are ordered so their cross product points along the positive turn axis.
    fun point(u: Float, v: Float) = when (face.axis) {
        0 -> Vector3(center.x, center.y + u, center.z + v)
        1 -> Vector3(center.x + v, center.y, center.z + u)
        else -> Vector3(center.x + u, center.y + v, center.z)
    }
    val (u, v) = when (face.axis) {
        0 -> tangent.y to tangent.z
        1 -> tangent.z to tangent.x
        else -> tangent.x to tangent.y
    }
    val direction = if (move.angle > 0) 1f else -1f
    val length = kotlin.math.sqrt(u * u + v * v)
    val dx = direction * u / length
    val dy = direction * v / length
    val tipU = 0.29f * dx
    val tipV = 0.29f * dy
    val tip = point(tipU, tipV)
    return listOf(
        point(-tipU, -tipV),
        tip,
        point(tipU - 0.14f * dx - 0.11f * dy, tipV - 0.14f * dy + 0.11f * dx),
        tip,
        point(tipU - 0.14f * dx + 0.11f * dy, tipV - 0.14f * dy - 0.11f * dx),
    )
}
