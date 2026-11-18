package metropolia.myapplication.Camera

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp

@Composable
fun DrawCubeViewFinder() {
    Box(
        modifier = androidx.compose.ui.Modifier.fillMaxSize()
    ) {
        Canvas(
            modifier = androidx.compose.ui.Modifier.fillMaxSize()
                .aspectRatio(1/1f)
                .padding(40.dp)
        ) {
            val canvasWidth = size.width
            val canvasHeight = size.height

            val linewidth = 3.dp.toPx()
            drawRect(
                color = androidx.compose.ui.graphics.Color.Black,
                style = Stroke(width = linewidth)
            )

            // draw cube face lines

            repeat(3) {
                i ->
                drawLine(
                    color = androidx.compose.ui.graphics.Color.Black,
                    start = androidx.compose.ui.geometry.Offset(
                        x = 0f,
                        y = canvasHeight / 3 * i
                    ),
                    end = androidx.compose.ui.geometry.Offset(
                        x = canvasWidth,
                        y = canvasHeight / 3 * i
                    ),
                    strokeWidth = linewidth
                )
                drawLine(
                    color = androidx.compose.ui.graphics.Color.Black,
                    start = androidx.compose.ui.geometry.Offset(
                        x = canvasWidth / 3 * i,
                        y = 0f
                    ),
                    end = androidx.compose.ui.geometry.Offset(
                        x = canvasWidth / 3 * i,
                        y = canvasHeight
                    ),
                    strokeWidth = linewidth
                )
            }
        }
    }
}