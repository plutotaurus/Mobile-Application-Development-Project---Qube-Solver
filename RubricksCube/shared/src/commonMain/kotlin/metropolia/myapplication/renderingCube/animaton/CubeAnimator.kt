package metropolia.myapplication.renderingCube.animaton

import metropolia.myapplication.data.Cube
import metropolia.myapplication.data.Move
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import metropolia.myapplication.data.CubeOrientation
import metropolia.myapplication.data.RotateCube

class CubeAnimator(
    initialCube: Cube = Cube.solved()
) {
    var cube by mutableStateOf(initialCube)
        private set

    var activeMove by mutableStateOf<Move?>(null)
        private set

    var isAnimating by mutableStateOf(false)
        private set

    val rotation = Animatable(0f)

    fun rotateCube(direction: RotateCube) {
        if (isAnimating) return

        cube = CubeOrientation.rotate(cube, direction)
    }

    suspend fun performMove(move: Move) {
        performMoves(listOf(move))
    }

    suspend fun performMoves(moves: List<Move>) {

        if (isAnimating) return

        isAnimating = true

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
                cube = CubeRotation.applyMove(
                    cube = cube,
                    move = move
                )

                // Reset the temporary rotation
                rotation.snapTo(0f)

                activeMove = null
            }
        } finally {
            activeMove = null
            isAnimating = false
            rotation.snapTo(0f)
        }
    }

    fun reset() {
        if (!isAnimating) {
            cube = Cube.solved()
        }
    }
}

private fun Float.absoluteValue(): Float =
    if (this < 0f) -this else this
