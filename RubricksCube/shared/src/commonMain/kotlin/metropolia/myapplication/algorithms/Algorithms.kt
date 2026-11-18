package metropolia.myapplication

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import metropolia.myapplication.data.Cube
import metropolia.myapplication.data.local.AlgorithmEntity
import metropolia.myapplication.data.local.rememberQubeDatabase
import metropolia.myapplication.data.local.seedStarterAlgorithms
import metropolia.myapplication.data.parseMoveSequence
import metropolia.myapplication.renderingCube.animaton.AnimatedCubeRendering

@Composable
fun AlgorithmsScreen(
    onNavigateBack: () -> Unit
) {
    val dao = rememberQubeDatabase().algorithms()
    var loadAttempt by remember { mutableIntStateOf(0) }
    val algorithms by produceState<Result<List<AlgorithmEntity>>?>(null, dao, loadAttempt) {
        value = null
        dao.observeAll()
            .map { Result.success(it) }
            .onStart { dao.seedStarterAlgorithms() }
            .catch { emit(Result.failure(it)) }
            .collect { value = it }
    }
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

            val result = algorithms
            val rows = result?.getOrNull().orEmpty()
            when {
                result == null -> CircularProgressIndicator()
                result.isFailure -> {
                    Text("Couldn't load algorithms.")
                    TextButton(onClick = { loadAttempt++ }) { Text("Retry") }
                }
                rows.isEmpty() -> Text("No algorithms saved yet.")
                else -> LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    items(rows, key = { it.algorithmId }) { row ->
                        val startingCube = remember(row.algorithmText) {
                            row.algorithmText
                                .takeIf { parseMoveSequence(it) != null }
                                ?.let { Cube.solved().applyMoves(invertAlgorithm(it)) }
                        }
                        AlgorithmComponent(
                            algorithmName = row.algorithmId,
                            algorithm = row.algorithmText,
                            cube = startingCube,
                            onImageClick = {
                                if (startingCube != null) {
                                    selectedAnimationAlgorithm = row.algorithmText to startingCube
                                }
                            },
                        )
                    }
                }
            }

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