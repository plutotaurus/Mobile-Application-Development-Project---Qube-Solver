package metropolia.myapplication.Camera

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Color

fun getAverageColor(bitmap: ImageBitmap): androidx.compose.ui.graphics.Color {
    val pixels = IntArray(bitmap.width * bitmap.height)
    bitmap.readPixels(
        buffer = pixels,
        startX = 0,
        startY = 0,
        width = bitmap.width,
        height = bitmap.height
    )

    val width = bitmap.width
    val height = bitmap.height
    var redSum = 0L
    var greenSum = 0L
    var blueSum = 0L
    var pixelCount = 0L

    for (x in 0 until width step 4) {
        for (y in 0 until height step 4) {
            val pixelColor = pixels[y * width + x]
            redSum += ((pixelColor ushr 16) and 0xFF).toLong()
            greenSum += ((pixelColor ushr 8) and 0xFF).toLong()
            blueSum += (pixelColor and 0xFF).toLong()
            pixelCount++
        }
    }

    val avgRed = (redSum / pixelCount).toInt()
    val avgGreen = (greenSum / pixelCount).toInt()
    val avgBlue = (blueSum / pixelCount).toInt()

    return androidx.compose.ui.graphics.Color(avgRed, avgGreen, avgBlue)
}