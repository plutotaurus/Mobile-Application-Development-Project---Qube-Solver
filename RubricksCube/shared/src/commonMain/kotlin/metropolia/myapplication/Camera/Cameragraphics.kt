package metropolia.myapplication.Camera

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp

@Composable
fun DrawCubeViewFinder(
    colors: List<Color>
) {
    val gridArea = ViewFinderArea()

    Box(
        modifier = androidx.compose.ui.Modifier
            .fillMaxSize()
    ) {
        Canvas(
            modifier = androidx.compose.ui.Modifier
                .fillMaxSize()
                .aspectRatio(1 / 1f)
        ) {

            val canvasWidth = size.width
            val canvasHeight = size.height

            val gridLeft = size.width * gridArea.left
            val gridTop = size.height * gridArea.top
            val gridWidth = size.width * gridArea.width
            val gridHeight = size.height * gridArea.height

            val linewidth = 3.dp.toPx()

            drawRect(
                color = androidx.compose.ui.graphics.Color.Black,
                topLeft = Offset(gridLeft, gridTop),
                size = Size(gridWidth, gridHeight),
                style = Stroke(width = linewidth)
            )

            // draw cube face lines

            repeat(3) { i ->
                drawLine(
                    color = androidx.compose.ui.graphics.Color.Black,
                    start = androidx.compose.ui.geometry.Offset(
                        x = gridLeft,
                        y =  gridTop + i * gridHeight / 3
                    ),
                    end = androidx.compose.ui.geometry.Offset(
                        x = gridLeft + gridWidth,
                        y =  gridTop + i * gridHeight / 3
                    ),
                    strokeWidth = linewidth
                )
                drawLine(
                    color = androidx.compose.ui.graphics.Color.Black,
                    start = androidx.compose.ui.geometry.Offset(
                        x = gridLeft + i * gridWidth / 3,
                        y = gridTop
                    ),
                    end = androidx.compose.ui.geometry.Offset(
                        x = gridLeft + i * gridWidth / 3,
                        y = gridTop + gridHeight
                    ),
                    strokeWidth = linewidth
                )
            }

            // draw cubelet side colors

            for (x in 0..2) {
                for (y in 0..2) {
                    val colorIndex = y + (x * 3)
                    if (colorIndex < colors.size) {
                        val cubeletColor = colors[colorIndex]
                        drawRect(
                            color = cubeletColor,
                            topLeft = androidx.compose.ui.geometry.Offset(
                                x = gridWidth / 3 * x + linewidth + gridLeft,
                                y = gridHeight / 3 * y + linewidth + gridTop
                            ),
                            size = androidx.compose.ui.geometry.Size(
                                width = gridWidth / 3 - linewidth * 2,
                                height = gridHeight / 3 - linewidth * 2
                            )
                        )
                    }
                }
            }

        }
    }
}

data class ViewFinderArea(
    val left: Float = 0.10f,
    val top: Float = 0.20f,
    val width: Float = 0.80f,
    val height: Float = 0.80f
)