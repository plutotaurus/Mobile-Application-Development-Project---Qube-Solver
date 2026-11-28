package metropolia.myapplication

import androidx.compose.foundation.background
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.IconButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.graphics.StrokeCap
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
import metropolia.myapplication.ui.QubeButton
import metropolia.myapplication.ui.QubeButtonStyle
import metropolia.myapplication.ui.QubeScreenTitle

@Composable
fun AlgorithmsScreen(
    profile: String = "guest",
    onNavigateBack: () -> Unit
) {
    val dao = rememberQubeDatabase().algorithms()
    val legacyProgress = rememberLegacyProgress(profile)
    var statusFilter by remember { mutableStateOf<AlgorithmStatus?>(null) }
    val legacyFavourites = rememberLegacyAlgorithmFavourites()
    val scope = rememberCoroutineScope()
    var isSavingFavourite by remember { mutableStateOf(false) }
    var statusSaveFailed by remember(profile) { mutableStateOf(false) }
    fun cycleStatus(id: String) {
        scope.launch {
            try {
                dao.cycleStatus(profile, id)
                statusSaveFailed = false
            } catch (e: CancellationException) {
                throw e
            } catch (_: Exception) {
                statusSaveFailed = true
            }
        }
    }
    var loadAttempt by remember { mutableIntStateOf(0) }
    val algorithms by produceState<Result<List<AlgorithmWithFavourite>>?>(null, dao, loadAttempt, legacyFavourites, profile) {
        value = null
        dao.observeAll(profile)
            .map { Result.success(it) }
            .onStart {
                dao.seedStarterAlgorithms()
                dao.importLegacyFavourites(legacyFavourites)
                dao.importLegacyProgress(profile, legacyProgress)
            }
            .catch { emit(Result.failure(it)) }
            .collect { value = it }
    }
    var selectedAnimationAlgorithm by remember { mutableStateOf<Pair<String, Cube>?>(null) }

    var selectedAlgorithmId by remember { mutableStateOf<String?>(null) }
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf<String?>(null) }
    var favouritesOnly by remember { mutableStateOf(false) }
    var showFilterMenu by remember { mutableStateOf(false) }
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
            QubeScreenTitle("Algorithms")

            AlgorithmSearchField(
                query = searchQuery,
                onQueryChange = { searchQuery = it },
            )

            Row(
                modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                FilterChip(
                    selected = selectedCategory == null,
                    onClick = { selectedCategory = null },
                    label = { Text("3×3") },
                )
                FilterChip(
                    selected = selectedCategory == "OLL",
                    onClick = { selectedCategory = if (selectedCategory == "OLL") null else "OLL" },
                    label = { Text("OLL") },
                )
                FilterChip(
                    selected = selectedCategory == "PLL",
                    onClick = { selectedCategory = if (selectedCategory == "PLL") null else "PLL" },
                    label = { Text("PLL") },
                )
                Box {
                    IconButton(
                        onClick = { showFilterMenu = true },
                        modifier = Modifier.semantics { contentDescription = "More filters" },
                    ) {
                        AlgorithmFilterGlyph()
                    }
                    DropdownMenu(
                        expanded = showFilterMenu,
                        onDismissRequest = { showFilterMenu = false },
                    ) {
                        for (status in listOf(null) + AlgorithmStatus.entries) {
                            DropdownMenuItem(
                                text = { Text(status?.name ?: "All statuses") },
                                trailingIcon = { if (statusFilter == status) Text("✓") },
                                onClick = {
                                    statusFilter = status
                                    showFilterMenu = false
                                },
                            )
                        }
                        DropdownMenuItem(
                            text = { Text("Favourites only") },
                            onClick = {
                                favouritesOnly = !favouritesOnly
                                showFilterMenu = false
                            },
                            trailingIcon = { if (favouritesOnly) Text("✓") },
                        )
                    }
                }
            }

            if (statusSaveFailed && selectedAlgorithm == null) Text("Couldn't save status. Tap the chip to retry.")
            val result = algorithms
            val rows = result?.getOrNull().orEmpty().filter { row ->
                val matchesQuery = searchQuery.isBlank() ||
                    row.algorithm.algorithmId.contains(searchQuery, ignoreCase = true) ||
                    row.selectedMoves.contains(searchQuery, ignoreCase = true)
                val matchesCategory = selectedCategory == null || row.algorithm.type == selectedCategory
                matchesQuery && matchesCategory &&
                    (statusFilter == null || row.status == statusFilter) &&
                    (!favouritesOnly || row.favouriteText != null)
            }
            when {
                result == null -> CircularProgressIndicator()
                result.isFailure -> {
                    Text("Couldn't load algorithms.")
                    TextButton(onClick = { loadAttempt++ }) { Text("Retry") }
                }
                result.getOrThrow().isEmpty() -> Text("No algorithms saved yet.")
                rows.isEmpty() -> Text("No matching algorithms.")
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
                            statusChip = {
                                AlgorithmStatusChip(row.status, row.algorithm.algorithmId) {
                                    cycleStatus(row.algorithm.algorithmId)
                                }
                            },
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

            QubeButton("Back", onNavigateBack, style = QubeButtonStyle.Text)
        }

        if (selectedRow != null && selectedAlgorithm != null) {
            val variants by produceState<Result<List<String>>?>(null, dao, selectedAlgorithm.algorithmId) {
                dao.observeVariants(selectedAlgorithm.algorithmId)
                    .map { Result.success(it) }
                    .catch { emit(Result.failure(it)) }
                    .collect { value = it }
            }
            AlgorithmVariantsOverlay(
                status = selectedRow.status,
                onCycle = { cycleStatus(selectedAlgorithm.algorithmId) },
                statusSaveFailed = statusSaveFailed,
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
private fun AlgorithmSearchField(
    query: String,
    onQueryChange: (String) -> Unit,
) {
    val focusManager = LocalFocusManager.current
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(32.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        contentColor = MaterialTheme.colorScheme.onSurface,
    ) {
        Row(
            modifier = Modifier.height(56.dp).padding(horizontal = 18.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            AlgorithmSearchGlyph()
            BasicTextField(
                value = query,
                onValueChange = onQueryChange,
                modifier = Modifier.weight(1f),
                singleLine = true,
                textStyle = MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.onSurface),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = { focusManager.clearFocus() }),
                decorationBox = { innerTextField ->
                    Box {
                        if (query.isEmpty()) {
                            Text("Search algorithms", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        innerTextField()
                    }
                },
            )
        }
    }
}

@Composable
private fun AlgorithmSearchGlyph() {
    val color = MaterialTheme.colorScheme.onSurfaceVariant
    Canvas(Modifier.size(24.dp)) {
        val stroke = 2.dp.toPx()
        drawCircle(
            color = color,
            radius = size.minDimension * 0.29f,
            center = Offset(size.width * 0.42f, size.height * 0.42f),
            style = Stroke(width = stroke),
        )
        drawLine(
            color = color,
            start = Offset(size.width * 0.63f, size.height * 0.63f),
            end = Offset(size.width * 0.88f, size.height * 0.88f),
            strokeWidth = stroke,
            cap = StrokeCap.Round,
        )
    }
}

@Composable
private fun AlgorithmFilterGlyph() {
    val color = MaterialTheme.colorScheme.onSurfaceVariant
    Canvas(Modifier.size(24.dp)) {
        val stroke = 2.dp.toPx()
        listOf(0.28f, 0.5f, 0.72f).forEachIndexed { index, y ->
            drawLine(color, Offset(size.width * 0.16f, size.height * y), Offset(size.width * 0.84f, size.height * y), stroke, cap = StrokeCap.Round)
            val knobX = if (index == 1) 0.38f else 0.62f
            drawCircle(color, radius = stroke * 1.35f, center = Offset(size.width * knobX, size.height * y))
        }
    }
}

@Composable
private fun AlgorithmVariantsOverlay(
    status: AlgorithmStatus,
    onCycle: () -> Unit,
    statusSaveFailed: Boolean,
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
                .background(MaterialTheme.colorScheme.scrim.copy(alpha = 0.6f))
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
                AlgorithmTitle(algorithm.algorithmId) {
                    AlgorithmStatusChip(status, algorithm.algorithmId, onCycle)
                }

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

                if (statusSaveFailed) Text("Couldn't save status. Tap the chip to retry.")
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
