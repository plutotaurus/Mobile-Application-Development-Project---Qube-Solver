package metropolia.myapplication.Camera

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.min
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import metropolia.myapplication.data.CubeColor
import metropolia.myapplication.data.Cubelet
import kotlin.math.abs
import kotlin.time.Duration.Companion.milliseconds


@Composable
fun CubeSideCollecting(
    modifier: Modifier,
    onAllSidesCaptured: (List<List<Color>>) -> Unit
) {

    //sides of the cube
    // in order: white, green, red, yellow, blue, orange

    // each side contains a list of the colors on that side
    // in the configuration:
    // 147
    // 258
    // 369
    val sides by remember { mutableStateOf(mutableListOf<List<Color>>()) }

    var captureRequested by remember { mutableStateOf(false) }

    var sideColors by remember {
        mutableStateOf(getNextViewFinderColor(1))
    }

    var currentSide: Int by remember { mutableStateOf(1) }

    val scope = rememberCoroutineScope()

    var isWaiting by remember { mutableStateOf(false) }

        Column(modifier = modifier) {

            Box(
                modifier = Modifier
                    .weight(1f)
            ) {
                CameraPermissionGate {
                    CameraView(
                        captureRequested = captureRequested,
                        onCaptureHandled = { captureRequested = false },
                        onPhotoCaptured = { image ->
                            sideColors = matchToCubeColors(getSideColors(image))

                            sides.add(sideColors)

                            isWaiting = true

                            scope.launch {
                                delay(1000.milliseconds) // Wait for 1 second before proceeding to the next side

                                if (sides.size == 6) {
                                    onAllSidesCaptured(sides.toList())
                                } else {
                                    currentSide++
                                    isWaiting = false
                                    sideColors = getNextViewFinderColor(currentSide)
                                }
                            }
                        }
                    )
                }
                CubePhotoInstructions(side = currentSide)
                DrawCubeViewFinder(colors = sideColors)
            }
            Button(
                onClick = { captureRequested = true },
                enabled = !captureRequested && !isWaiting
            ) {
                Text("Take Photo")
            }
        }
}

@Composable
fun BoxScope.CubePhotoInstructions(side: Int) {
    Text(
        text =
        buildAnnotatedString {
            append("Take a photo of the ")
            when (side) {
            1 -> withStyle(SpanStyle(color = CubeColor.WHITE.color)) {
                append("white")
                }
            2 -> withStyle(SpanStyle(color = CubeColor.GREEN.color)) {
                append("green")
                }
            3 -> withStyle(SpanStyle(color = CubeColor.RED.color)) {
                append("red")
                }
            4 -> withStyle(SpanStyle(color = CubeColor.YELLOW.color)) {
                append("yellow")
                }
            5 -> withStyle(SpanStyle(color = CubeColor.BLUE.color)) {
                append("blue")
                }
            6 -> withStyle(SpanStyle(color = CubeColor.ORANGE.color)) {
                append("orange")
                }
        }

        append(" face \n with the ")

        when (side) {
            1 -> withStyle(SpanStyle(color = CubeColor.BLUE.color)) {
                append("blue")
            }
            2 -> withStyle(SpanStyle(color = CubeColor.WHITE.color)) {
                append("white")
            }
            3 -> withStyle(SpanStyle(color = CubeColor.WHITE.color)) {
                append("white")
            }
            4 -> withStyle(SpanStyle(color = CubeColor.RED.color)) {
                append("red")
            }
            5 -> withStyle(SpanStyle(color = CubeColor.RED.color)) {
                append("red")
            }
            6 -> withStyle(SpanStyle(color = CubeColor.BLUE.color)) {
                append("blue")
            }
        }

        append(" face on top")
    },
        modifier = Modifier
            .align(Alignment.TopCenter)
            .padding(top = 80.dp),
        fontSize = 20.sp
    )
}

fun getNextViewFinderColor(face: Int): List<Color> {
    return when (face) {
        1 -> listOf(Color.Transparent, Color.Transparent, Color.Transparent, Color.Transparent, Color.White, Color.Transparent)
        2 -> listOf(Color.Transparent, Color.Transparent, Color.Transparent, Color.Transparent, Color.Green, Color.Transparent)
        3 -> listOf(Color.Transparent, Color.Transparent, Color.Transparent, Color.Transparent, Color.Red, Color.Transparent)
        4 -> listOf(Color.Transparent, Color.Transparent, Color.Transparent, Color.Transparent, Color.Yellow, Color.Transparent)
        5 -> listOf(Color.Transparent, Color.Transparent, Color.Transparent, Color.Transparent, Color.Blue, Color.Transparent)
        6 -> listOf(Color.Transparent, Color.Transparent, Color.Transparent, Color.Transparent, Color(0xFFFF8000), Color.Transparent)
        else -> emptyList()
    }
}

fun matchToCubeColors(colors: List<Color>): List<Color> {

    return colors.map { color ->
        return colors.map { sampledColor ->
            val red = sampledColor.red
            val green = sampledColor.green
            val blue = sampledColor.blue

            val brightestChannel = maxOf(red, green, blue)
            val darkestChannel = minOf(red, green, blue)
            val averageBrightness = (red + green + blue) / 3f

            val channelRange = brightestChannel - darkestChannel
            val colorSaturation = if (brightestChannel > 0f) {
                channelRange / brightestChannel
            } else {
                0f
            }

            val looksWhite = averageBrightness >= 0.55f && colorSaturation <= 0.30f

            if (looksWhite) {
                CubeColor.WHITE.color
            } else {
                val nonWhiteCubeColors = CubeColor.entries.filterNot { cubeColor ->
                    cubeColor == CubeColor.WHITE
                }

                val closestCubeColor = nonWhiteCubeColors.minBy { cubeColor ->
                    val redDifference = abs(cubeColor.color.red - red)
                    val greenDifference = abs(cubeColor.color.green - green)
                    val blueDifference = abs(cubeColor.color.blue - blue)

                    redDifference + greenDifference + blueDifference
                }

                closestCubeColor.color
            }
        }
    }
    }