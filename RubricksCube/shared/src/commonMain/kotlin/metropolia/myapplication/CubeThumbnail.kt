package metropolia.myapplication

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import metropolia.myapplication.data.Cube
import metropolia.myapplication.renderingCube.Camera
import metropolia.myapplication.renderingCube.renderCube

@Composable
fun CubeThumbnail(
    cube: Cube,
    modifier: Modifier = Modifier,
    camera: Camera = Camera(yaw = 0.65f, pitch = 0.5708f) // Isometric view angle
) {
    Canvas(modifier = modifier) {
        val faces = renderCube(
            cube = cube,
            camera = camera,
            width = size.width,
            height = size.height
        )

        for ((points, _, _, color) in faces) {
            val path = Path().apply {
                moveTo(points[0].x, points[0].y)
                for (i in 1 until points.size) {
                    lineTo(points[i].x, points[i].y)
                }
                close()
            }

            // Draw sticker fill
            drawPath(
                path = path,
                color = color.color
            )

            // Draw sticker border lines
            drawPath(
                path = path,
                color = Color.Black,
                style = Stroke(width = 1.5f)
            )
        }
    }
}