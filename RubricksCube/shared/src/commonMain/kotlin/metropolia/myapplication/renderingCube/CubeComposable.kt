package metropolia.myapplication.renderingCube

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import metropolia.myapplication.data.Cube

@Composable
fun CubeRendering() {

    val cubeState = remember {
        mutableStateOf(Cube.solved())
    }

    val yawState = remember {
        mutableFloatStateOf(0.65f)
    }

    val pitchState = remember {
        mutableFloatStateOf(0.5708f)
    }

    val camera = Camera(
        yaw = yawState.floatValue,
        pitch = pitchState.floatValue
    )

    MaterialTheme {

        Surface(
            modifier = Modifier.fillMaxSize(),
        ) {

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .pointerInput(Unit) {
                            detectDragGestures(
                                onDrag = { change, dragAmount ->
                                    change.consume()

                                    yawState.floatValue += dragAmount.x * 0.01f

                                    pitchState.floatValue = (
                                            pitchState.floatValue + dragAmount.y * 0.01f
                                            ).coerceIn(-1.5f, 1.5f)
                                }
                            )
                        }
                ) {

                    val faces = renderCube(
                        cubeState.value,
                        camera,
                        size.width,
                        size.height
                    )

                    for ((points, _, _, color) in faces) {

                        val path = Path().apply {

                            moveTo(
                                points[0].x,
                                points[0].y
                            )

                            for (i in 1 until points.size) {
                                lineTo(
                                    points[i].x,
                                    points[i].y
                                )
                            }

                            close()
                        }

                        drawPath(
                            path = path,
                            color = color.color
                        )

                        drawPath(
                            path = path,
                            color = Color.Black,
                            style = Stroke(width = 2f)
                        )
                    }
                }


                Button(
                    onClick = {
                        cubeState.value = Cube.solved()
                        yawState.floatValue = 0.65f
                        pitchState.floatValue = 0.5708f
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Reset cube")
                }
            }
        }
    }
}
