package metropolia.myapplication

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import metropolia.myapplication.data.Cube
import metropolia.myapplication.renderingCube.animaton.AnimatedCubeRendering

@Composable
fun AlgorithmsScreen(
    onNavigateBack: () -> Unit
) {
    var selectedAnimationAlgorithm by remember { mutableStateOf<Pair<String, Cube>?>(null) }

    if (selectedAnimationAlgorithm != null) {
        AnimatedCubeRendering(
            moveSequence = selectedAnimationAlgorithm!!.first,
            initialCube = selectedAnimationAlgorithm!!.second,
            onClose = { selectedAnimationAlgorithm = null }
        )
    } else {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("Algorithms Screen")

            val tPermAlgorithm = "R U R' U' R' F R2 U' R' U' R U R' F'"

            // Change this sequence to choose the starting state for the thumbnail and player.
            val startingMoves = invertAlgorithm(tPermAlgorithm)
            val startingCube = remember(startingMoves) {
                Cube.solved().applyMoves(startingMoves)
            }

            AlgorithmComponent(
                algorithmName = "T-Perm",
                algorithm = tPermAlgorithm,
                cube = startingCube,
                onImageClick = {
                    selectedAnimationAlgorithm = tPermAlgorithm to startingCube
                }
            )

            Button(
                onClick = onNavigateBack
            ) {
                Text("Back")
            }
        }
    }
}
fun invertAlgorithm(algorithm: String): String {
    return algorithm.trim()
        .split(Regex("\\s+"))
        .filter { it.isNotEmpty() }
        .reversed()
        .map { move ->
            when {
                move.endsWith("2") -> move
                move.endsWith("'") -> move.dropLast(1)
                else -> "$move'"
            }
        }
        .joinToString(" ")
}