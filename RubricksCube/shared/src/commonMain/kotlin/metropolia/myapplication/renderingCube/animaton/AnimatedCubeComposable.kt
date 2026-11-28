package metropolia.myapplication.renderingCube.animaton

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.StrokeCap
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.launch
import metropolia.myapplication.data.Cube
import metropolia.myapplication.data.SequenceMove
import metropolia.myapplication.data.parseMoveSequence
import metropolia.myapplication.renderingCube.Camera
import metropolia.myapplication.renderingCube.renderCube
import metropolia.myapplication.ui.QubeButton
import metropolia.myapplication.ui.QubeButtonStyle

@Composable
fun AnimatedCubeRendering(
    moveSequence: String = "x y z",
    onClose: () -> Unit = {},
    initialCube: Cube = Cube.solved()
) {

    val animator = remember(initialCube) {
        CubeAnimator(initialCube)
    }

    var currentMoveIndex by remember {
        mutableIntStateOf(-1)
    }

    val scope = rememberCoroutineScope()

    var playbackJob by remember { mutableStateOf<Job?>(null) }

    var isPlayingSequence by remember {
        mutableStateOf(false)
    }

    var sequenceError by remember {
        mutableStateOf<String?>(null)
    }

    val camera = Camera(yaw = -0.65f, pitch = 0.5708f)

    MaterialTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Start
                ) {
                    QubeButton("Back", onClose, style = QubeButtonStyle.Text)
                }

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                ) {
                    Canvas(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(240.dp)
                    ) {
                        val faces = renderCube(
                            animator.cube,
                            camera,
                            size.width,
                            size.height,
                            animator.activeMove,
                            animator.rotation.value,
                            animator.wholeCubeTurn,
                        )

                        for (face in faces) {
                            val points = face.points
                            val color = face.color
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
                            val ink = if (color.color.luminance() > 0.18f) Color.Black else Color.White
                            face.arrow.zipWithNext { a, b ->
                                drawLine(ink, a, b, 2.dp.toPx(), cap = StrokeCap.Round)
                            }
                        }
                    }

                    Spacer(Modifier.height(8.dp))

                    val moveTokens = moveSequence
                        .trim()
                        .split(Regex("\\s+"))
                        .filter { it.isNotBlank() }

                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
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

                                                    currentMoveIndex = -1

                                                    for (i in 0..index) {
                                                        currentMoveIndex = i

                                                        when (val selectedMove = moves[i]) {
                                                            is SequenceMove.FaceTurn -> {
                                                                animator.performMove(selectedMove.move)
                                                            }

                                                            is SequenceMove.CubeTurn -> {
                                                                animator.animateCubeTurn(selectedMove.direction)
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
                                    .padding(horizontal = 10.dp, vertical = 8.dp)
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
                }

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    QubeButton(
                        label = "Play",
                        onClick = {
                            if (playbackJob?.isActive == true) return@QubeButton

                            val moves = parseMoveSequence(moveSequence) ?: return@QubeButton

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
                                                animator.animateCubeTurn(sequenceMove.direction)
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
                    )
                    QubeButton(
                        label = "Stop",
                        style = QubeButtonStyle.Tonal,
                        onClick = {
                            scope.launch {
                                playbackJob?.cancelAndJoin()
                                playbackJob = null
                                isPlayingSequence = false
                            }
                        },
                        enabled = isPlayingSequence
                    )

                    QubeButton(
                        label = "Reset cube",
                        style = QubeButtonStyle.Outlined,
                        onClick = {
                            scope.launch {
                                animator.reset()
                                currentMoveIndex = -1
                            }
                        },
                        enabled = !isPlayingSequence && !animator.isAnimating
                    )
                }
            }
        }
    }
}
