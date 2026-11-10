package metropolia.myapplication.data

import metropolia.myapplication.renderingCube.Vector3i

enum class CubeColor {
    WHITE,
    YELLOW,
    RED,
    ORANGE,
    BLUE,
    GREEN
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
    val cubelets: List<Cubelet>
)