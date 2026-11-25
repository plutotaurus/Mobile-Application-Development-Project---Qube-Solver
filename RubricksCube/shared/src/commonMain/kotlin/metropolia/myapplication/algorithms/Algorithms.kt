package metropolia.myapplication

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.dialog
import androidx.compose.ui.semantics.hideFromAccessibility
import androidx.compose.ui.semantics.paneTitle
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.navigationevent.NavigationEventDispatcher
import androidx.navigationevent.NavigationEventHandler
import androidx.navigationevent.NavigationEventInfo
import androidx.navigationevent.compose.LocalNavigationEventDispatcherOwner
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import metropolia.myapplication.data.Cube
import metropolia.myapplication.data.local.AlgorithmEntity
import metropolia.myapplication.data.local.AlgorithmFavouriteEntity
import metropolia.myapplication.data.local.AlgorithmWithFavourite
import metropolia.myapplication.data.local.rememberLegacyAlgorithmFavourites
import metropolia.myapplication.data.local.rememberQubeDatabase
import metropolia.myapplication.data.local.seeders.seedStarterAlgorithms
import metropolia.myapplication.data.parseMoveSequence
import metropolia.myapplication.renderingCube.animaton.AnimatedCubeRendering

@Composable
fun AlgorithmsScreen(
    onNavigateBack: () -> Unit
) {
    val dao = rememberQubeDatabase().algorithms()
    val legacyFavourites = rememberLegacyAlgorithmFavourites()
    val scope = rememberCoroutineScope()
    var isSavingFavourite by remember { mutableStateOf(false) }
    var loadAttempt by remember { mutableIntStateOf(0) }
    val algorithms by produceState<Result<List<AlgorithmWithFavourite>>?>(null, dao, loadAttempt, legacyFavourites) {
        value = null
        dao.observeAll()
            .map { Result.success(it) }
            .onStart {
                dao.seedStarterAlgorithms()
                dao.importLegacyFavourites(legacyFavourites)
            }
            .catch { emit(Result.failure(it)) }
            .collect { value = it }
    }
    var selectedAnimationAlgorithm by remember { mutableStateOf<Pair<String, Cube>?>(null) }

    var selectedAlgorithmId by remember { mutableStateOf<String?>(null) }
    var favouriteSaveFailed by remember(selectedAlgorithmId) { mutableStateOf(false) }
    val selectedRow = algorithms?.getOrNull()?.firstOrNull {
        it.algorithm.algorithmId == selectedAlgorithmId
    }
    val selectedAlgorithm = selectedRow?.algorithm

    if (selectedAnimationAlgorithm != null) {
        AnimatedCubeRendering(
            moveSequence = selectedAnimationAlgorithm!!.first,
            initialCube = selectedAnimationAlgorithm!!.second,
            onClose = { selectedAnimationAlgorithm = null }
        )
        return
    }

    Box(Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .semantics { if (selectedAlgorithm != null) hideFromAccessibility() }
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
                    items(rows, key = { it.algorithm.algorithmId }) { row ->
                        val moves = row.selectedMoves
                        val startingCube = remember(moves) {
                            moves
                                .takeIf { parseMoveSequence(it) != null }
                                ?.let { Cube.solved().applyMoves(invertAlgorithm(it)) }
                        }
                        AlgorithmComponent(
                            algorithmName = row.algorithm.algorithmId,
                            algorithm = moves,
                            cube = startingCube,
                            onAlgorithmClick = { selectedAlgorithmId = row.algorithm.algorithmId },
                            onImageClick = {
                                if (startingCube != null) {
                                    selectedAnimationAlgorithm = moves to startingCube
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

        if (selectedRow != null && selectedAlgorithm != null) {
            val variants by produceState<Result<List<String>>?>(null, dao, selectedAlgorithm.algorithmId) {
                dao.observeVariants(selectedAlgorithm.algorithmId)
                    .map { Result.success(it) }
                    .catch { emit(Result.failure(it)) }
                    .collect { value = it }
            }
            AlgorithmVariantsOverlay(
                algorithm = selectedAlgorithm,
                variants = variants,
                favouriteMoves = selectedRow.selectedMoves,
                isSavingFavourite = isSavingFavourite,
                favouriteSaveFailed = favouriteSaveFailed,
                onFavourite = { moves ->
                    if (!isSavingFavourite) {
                        isSavingFavourite = true
                        favouriteSaveFailed = false
                        scope.launch {
                            try {
                                if (moves == selectedAlgorithm.algorithmText) {
                                    dao.clearFavourite(selectedAlgorithm.algorithmId)
                                } else {
                                    dao.saveFavourite(AlgorithmFavouriteEntity(selectedAlgorithm.algorithmId, moves))
                                }
                            } catch (e: CancellationException) {
                                throw e
                            } catch (_: Exception) {
                                favouriteSaveFailed = true
                            } finally {
                                isSavingFavourite = false
                            }
                        }
                    }
                },
                onDismiss = { selectedAlgorithmId = null }
            )
        }
    }
}

@Composable
private fun AlgorithmVariantsOverlay(
    algorithm: AlgorithmEntity,
    variants: Result<List<String>>?,
    favouriteMoves: String,
    isSavingFavourite: Boolean,
    favouriteSaveFailed: Boolean,
    onFavourite: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val dispatcher = LocalNavigationEventDispatcherOwner.current?.navigationEventDispatcher
    val dismissOnBack by rememberUpdatedState(onDismiss)
    DisposableEffect(dispatcher) {
        val handler = object : NavigationEventHandler<NavigationEventInfo>(
            initialInfo = NavigationEventInfo.None,
            isBackEnabled = true,
            isForwardEnabled = false
        ) {
            override fun onBackCompleted() = dismissOnBack()
        }
        // Close the overlay before the NavHost can pop the Algorithms destination.
        dispatcher?.addHandler(handler, NavigationEventDispatcher.PRIORITY_OVERLAY)
        onDispose { handler.remove() }
    }

    // Keep the overlay in this scene to avoid a separate UIKit appearance transition.
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Box(
            Modifier.matchParentSize()
                .background(Color.Black.copy(alpha = 0.6f))
                .pointerInput(onDismiss) { detectTapGestures { onDismiss() } }
        )

        Surface(
            modifier = Modifier
                .padding(24.dp)
                .widthIn(max = 560.dp)
                .fillMaxWidth()
                .semantics {
                    dialog()
                    paneTitle = algorithm.algorithmId
                },
            shape = MaterialTheme.shapes.extraLarge,
            color = MaterialTheme.colorScheme.surfaceContainerHigh
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(algorithm.algorithmId, style = MaterialTheme.typography.headlineSmall)

                LazyColumn(
                    modifier = Modifier.fillMaxWidth().heightIn(max = 320.dp).weight(1f, fill = false),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        AlgorithmVariantOption(
                            moves = algorithm.algorithmText,
                            isFavourite = favouriteMoves == algorithm.algorithmText,
                            canSelect = !isSavingFavourite,
                            onFavourite = { onFavourite(algorithm.algorithmText) }
                        )
                    }
                    when {
                        variants == null -> item { CircularProgressIndicator() }
                        variants.isFailure -> item { Text("Couldn't load alternative algorithms.") }
                        else -> items(variants.getOrThrow().filter { it != algorithm.algorithmText }) { moves ->
                            AlgorithmVariantOption(
                                moves = moves,
                                isFavourite = favouriteMoves == moves,
                                canSelect = !isSavingFavourite,
                                onFavourite = { onFavourite(moves) }
                            )
                        }
                    }
                }

                if (favouriteSaveFailed) {
                    Text("Couldn't save favourite.", color = MaterialTheme.colorScheme.error)
                }
                TextButton(onClick = onDismiss, modifier = Modifier.align(Alignment.End)) {
                    Text("Close")
                }
            }
        }
    }
}

@Composable
private fun AlgorithmVariantOption(
    moves: String,
    isFavourite: Boolean,
    canSelect: Boolean,
    onFavourite: () -> Unit
) {
    Column(Modifier.fillMaxWidth()) {
        Text(moves)
        TextButton(
            onClick = onFavourite,
            enabled = canSelect && !isFavourite,
            modifier = Modifier.align(Alignment.End)
        ) {
            Text(if (isFavourite) "Favourite" else "Set as favourite")
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