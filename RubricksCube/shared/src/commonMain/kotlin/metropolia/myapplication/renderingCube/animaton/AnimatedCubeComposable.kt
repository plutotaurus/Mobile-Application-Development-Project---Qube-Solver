
package metropolia.myapplication.renderingCube.animaton

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import metropolia.myapplication.data.SequenceMove
import metropolia.myapplication.data.parseMoveSequence
import metropolia.myapplication.renderingCube.Camera
import metropolia.myapplication.renderingCube.renderCube
import kotlin.time.Duration.Companion.milliseconds

@Composable
fun AnimatedCubeRendering() {

    val animator = remember {
        CubeAnimator()
    }

    var currentMoveIndex by remember {
        mutableIntStateOf(-1)
    }

    val scope = rememberCoroutineScope()

    val moveSequence = "x y z"

    var playbackJob by remember { mutableStateOf<Job?>(null) }

    var isPlayingSequence by remember {
        mutableStateOf(false)
    }

    var sequenceError by remember {
        mutableStateOf<String?>(null)
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
            color = Color.White
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
                ) {
                    val faces = renderCube(
                        animator.cube,
                        camera,
                        size.width,
                        size.height
                    )

                    for ((points, _, _, color) in faces) {
                        val path = Path().apply {
                            moveTo(points[0].x, points[0].y)

                            for (i in 1 until points.size) {
                                lineTo(points[i].x, points[i].y)
                            }

                            close()
                        }

                        drawPath(path, color.color)

                        drawPath(
                            path,
                            Color.Black,
                            style = Stroke(width = 2f)
                        )
                    }
                }

                Spacer(Modifier.height(8.dp))

                Spacer(Modifier.height(8.dp))

                val moveTokens = moveSequence
                    .trim()
                    .split(Regex("\\s+"))
                    .filter { it.isNotBlank() }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {

                    moveTokens.forEachIndexed { index, move ->

                        Box(
                            modifier = Modifier
                                .background(
                                    color = if (index == currentMoveIndex) {
                                        MaterialTheme.colorScheme.primaryContainer
                                    } else {
                                        Color.Transparent
                                    },
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .clickable(enabled = !isPlayingSequence) {
                                    playbackJob = scope.launch {
                                        val moves = parseMoveSequence(moveSequence)

                                        if (moves != null) {
                                            isPlayingSequence = true

                                            try {
                                                animator.reset()

                                                yawState.floatValue = 0.65f
                                                pitchState.floatValue = 0.5708f
                                                currentMoveIndex = -1

                                                for (i in 0..index) {
                                                    currentMoveIndex = i

                                                    when (val selectedMove = moves[i]) {
                                                        is SequenceMove.FaceTurn -> {
                                                            animator.performMove(selectedMove.move)
                                                        }

                                                        is SequenceMove.CubeTurn -> {
                                                            animator.rotateCube(selectedMove.direction)
                                                        }
                                                    }
                                                }
                                            } finally {
                                                isPlayingSequence = false
                                                playbackJob = null
                                            }
                                        }
                                    }
                                }
                                .padding(10.dp)
                        ) {
                            Text(
                                text = move,
                                fontWeight = if (index == currentMoveIndex) {
                                    FontWeight.Bold
                                } else {
                                    FontWeight.Normal
                                }
                            )
                        }
                    }
                }

                sequenceError?.let {
                    Text(
                        text = it,
                        color = MaterialTheme.colorScheme.error
                    )
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            if (playbackJob?.isActive == true) return@Button

                            val moves = parseMoveSequence(moveSequence) ?: return@Button

                            playbackJob = scope.launch {
                                isPlayingSequence = true

                                try {
                                    for ((index, sequenceMove) in moves.withIndex()) {
                                        currentMoveIndex = index

                                        when (sequenceMove) {
                                            is SequenceMove.FaceTurn -> {
                                                animator.performMove(sequenceMove.move)
                                            }

                                            is SequenceMove.CubeTurn -> {
                                                animator.rotateCube(sequenceMove.direction)
                                                delay(500.milliseconds)
                                            }
                                        }
                                    }
                                } finally {
                                    isPlayingSequence = false
                                    playbackJob = null
                                }
                            }
                        },
                        enabled = !isPlayingSequence
                    ) {
                        Text("Play")
                    }
                    Button(
                        onClick = {
                            scope.launch {
                                playbackJob?.cancelAndJoin()
                                playbackJob = null
                                isPlayingSequence = false
                            }
                        },
                        enabled = isPlayingSequence
                    ) {
                        Text("Stop")
                    }

                    Button(
                        onClick = {
                            scope.launch {
                                animator.reset()
                                currentMoveIndex = -1
                            }
                        },
                        enabled = !isPlayingSequence && !animator.isAnimating
                    ) {
                        Text("Reset cube")
                    }
                }
            }
        }
    }
}
