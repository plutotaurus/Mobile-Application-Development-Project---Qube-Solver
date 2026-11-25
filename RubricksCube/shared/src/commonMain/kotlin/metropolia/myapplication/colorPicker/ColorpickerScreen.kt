package metropolia.myapplication.colorPicker

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp

@Composable
fun ColorpickerPrototype(
    sides: List<List<Color>>,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val faceSize = minOf(size.width, size.height) / 4f
        val cellSize = faceSize / 3f
        val netSize = faceSize * 4f
        val startX = (size.width - netSize) / 2f
        val startY = (size.height - netSize) / 2f
        val borderWidth = 2.dp.toPx()

        // Captured sides are ordered white, green, red, yellow, blue, orange.
        val facePositions = listOf(
            Triple(0, 1, 0), // White above red
            Triple(1, 0, 1), // Green
            Triple(2, 1, 1), // Red
            Triple(4, 2, 1), // Blue
            Triple(3, 1, 2), // Yellow below red
            Triple(5, 3, 1)  // Orange
        )

        facePositions.forEach { (sideIndex, faceColumn, faceRow) ->
            val face = sides.getOrNull(sideIndex) ?: return@forEach

            for (column in 0..2) {
                for (row in 0..2) {
                    val color = face.getOrNull(row + column * 3) ?: continue
                    val topLeft = Offset(
                        x = startX + (faceColumn * 3 + column) * cellSize,
                        y = startY + (faceRow * 3 + row) * cellSize
                    )
                    val cellSizeInCanvas = Size(cellSize, cellSize)

                    drawRect(color, topLeft, cellSizeInCanvas)
                    drawRect(
                        color = Color.Black,
                        topLeft = topLeft,
                        size = cellSizeInCanvas,
                        style = Stroke(width = borderWidth)
                    )
                }
            }
        }
    }
}