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

fun getSideColors(bitmap: ImageBitmap): List<Color> {

    val pixels = IntArray(bitmap.width * bitmap.height)

    //left to right top to bottom

    val colorsList = mutableListOf<Color>()

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


    for (x_cubelet in 1..3) {
        for (y_cubelet in 1..3) {
            for (x in x_cubelet * (width / 3) until (x_cubelet + 1) * (width / 3) step 4) {
                for (y in y_cubelet * (height / 3) until (y_cubelet + 1) * (height / 3) step 4) {
                    val pixelColor = pixels[y * width + x]
                    redSum += ((pixelColor ushr 16) and 0xFF).toLong()
                    greenSum += ((pixelColor ushr 8) and 0xFF).toLong()
                    blueSum += (pixelColor and 0xFF).toLong()
                    pixelCount++
                }
            }
        }
        colorsList.add(androidx.compose.ui.graphics.Color((redSum / pixelCount).toInt(), (greenSum / pixelCount).toInt(), (blueSum / pixelCount).toInt()))
    }

    return colorsList

}