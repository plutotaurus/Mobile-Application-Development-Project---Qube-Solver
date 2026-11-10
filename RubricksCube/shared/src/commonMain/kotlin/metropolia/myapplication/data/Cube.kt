package metropolia.myapplication.data

import androidx.compose.ui.graphics.Color
import metropolia.myapplication.renderingCube.Vector3i

enum class CubeColor(color: Color) {
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
    val cubelets: List<Cubelet>
)