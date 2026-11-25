package metropolia.myapplication.camera

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Color

fun getAverageColor(bitmap: ImageBitmap): Color {
    val width = bitmap.width
    val height = bitmap.height
    if (width <= 0 || height <= 0) return Color.Black

    val pixels = IntArray(width * height)
    try {
        bitmap.readPixels(
            buffer = pixels,
            startX = 0,
            startY = 0,
            width = width,
            height = height
        )
    } catch (_: Exception) {
        return Color.Black
    }

    var redSum = 0L
    var greenSum = 0L
    var blueSum = 0L
    var pixelCount = 0L

    for (x in 0 until width step 4) {
        for (y in 0 until height step 4) {
            val index = y * width + x
            if (index in pixels.indices) {
                val pixelColor = pixels[index]
                redSum += ((pixelColor ushr 16) and 0xFF).toLong()
                greenSum += ((pixelColor ushr 8) and 0xFF).toLong()
                blueSum += (pixelColor and 0xFF).toLong()
                pixelCount++
            }
        }
    }

    if (pixelCount == 0L) return Color.Black

    val avgRed = (redSum / pixelCount).toInt().coerceIn(0, 255)
    val avgGreen = (greenSum / pixelCount).toInt().coerceIn(0, 255)
    val avgBlue = (blueSum / pixelCount).toInt().coerceIn(0, 255)

    return Color(avgRed, avgGreen, avgBlue)
}
