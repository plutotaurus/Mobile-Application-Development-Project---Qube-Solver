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
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import metropolia.myapplication.data.Cubelet
import kotlin.time.Duration.Companion.milliseconds


@Composable
fun cubeSideCollecting(modifier: Modifier): List<Cubelet> {

    val cubelets by remember { mutableStateOf(mutableListOf<Cubelet>()) }

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
                            sideColors = getSideColors(image)

                            isWaiting = true

                            scope.launch {
                                delay(1000.milliseconds) // Wait for 1 second before proceeding to the next side
                                currentSide++
                                isWaiting = false
                                sideColors = getNextViewFinderColor(currentSide)
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

    return cubelets
}

@Composable
fun BoxScope.CubePhotoInstructions(side: Int) {
    Text(
        text =
        buildAnnotatedString {
            append("Take a photo of the ")
            when (side) {
            1 -> withStyle(SpanStyle(color = Color.White)) {
                append("white")
                }
            2 -> withStyle(SpanStyle(color = Color.Green)) {
                append("green")
                }
            3 -> withStyle(SpanStyle(color = Color.Red)) {
                append("red")
                }
            4 -> withStyle(SpanStyle(color = Color.Yellow)) {
                append("yellow")
                }
            5 -> withStyle(SpanStyle(color = Color.Blue)) {
                append("blue")
                }
            6 -> withStyle(SpanStyle(color = Color(0xFFFF8000))) {
                append("orange")
                }
        }

        append(" face \n with the ")

        when (side) {
            1 -> withStyle(SpanStyle(color = Color.Blue)) {
                append("blue")
            }
            2 -> withStyle(SpanStyle(color = Color.White)) {
                append("white")
            }
            3 -> withStyle(SpanStyle(color = Color.White)) {
                append("white")
            }
            4 -> withStyle(SpanStyle(color = Color.Red)) {
                append("red")
            }
            5 -> withStyle(SpanStyle(color = Color.Red)) {
                append("red")
            }
            6 -> withStyle(SpanStyle(color = Color.Blue)) {
                append("blue")
            }
        }

        append(" face on top")
    },
        modifier = Modifier
            .align(Alignment.TopCenter)
            .padding(top = 30.dp),
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