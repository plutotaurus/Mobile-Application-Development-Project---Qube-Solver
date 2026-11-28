package metropolia.myapplication.renderingCube.animaton

import metropolia.myapplication.data.Cube
import metropolia.myapplication.data.Move
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.withContext
import metropolia.myapplication.data.CubeOrientation
import metropolia.myapplication.data.RotateCube

class CubeAnimator(
    private val initialCube: Cube = Cube.solved()
) {
    var cube by mutableStateOf(initialCube)
        private set

    var activeMove by mutableStateOf<Move?>(null)
        private set

    var isAnimating by mutableStateOf(false)
        private set

    var wholeCubeTurn by mutableStateOf(false)
        private set

    val rotation = Animatable(0f)

    fun rotateCube(direction: RotateCube) {
        if (isAnimating) return

        cube = CubeOrientation.rotate(cube, direction)
    }

    suspend fun animateCubeTurn(direction: RotateCube) = animateTurns(listOf(direction.faceTurn), true)

    suspend fun performMove(move: Move) {
        performMoves(listOf(move))
    }

    suspend fun performMoves(moves: List<Move>) = animateTurns(moves, false)

    private suspend fun animateTurns(moves: List<Move>, wholeCube: Boolean) {

        if (isAnimating) return

        isAnimating = true
        wholeCubeTurn = wholeCube

        try {
            for (move in moves) {

                activeMove = move
                rotation.snapTo(0f)

                // Animate the selected face
                rotation.animateTo(
                    targetValue = move.angle,
                    animationSpec = tween(
                        durationMillis = if (move.angle.absoluteValue() == 180f) {
                            1600
                        } else {
                            1400
                        },
                        easing = FastOutSlowInEasing
                    )
                )

                // Commit the completed rotation
                cube = if (wholeCube) CubeRotation.applyWholeCubeTurn(cube, move) else CubeRotation.applyMove(cube, move)

                rotation.snapTo(0f)
                activeMove = null
            }
        }finally {
                withContext(NonCancellable) {
                    rotation.stop()
                    rotation.snapTo(0f)
                    activeMove = null
                    wholeCubeTurn = false
                    isAnimating = false
                }

        }
    }
    fun reset() {
        if (!isAnimating) {
            cube = initialCube
        }
    }
}

private fun Float.absoluteValue(): Float =
    if (this < 0f) -this else this
