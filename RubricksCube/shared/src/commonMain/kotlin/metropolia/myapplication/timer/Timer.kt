package metropolia.myapplication

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.semantics.customActions
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import metropolia.myapplication.data.Cube
import metropolia.myapplication.data.local.SolveTimeEntity
import metropolia.myapplication.timer.*
import kotlin.time.TimeSource

@Composable
fun TimerScreen(onNavigateBack: () -> Unit) {
    val scope = rememberCoroutineScope()
    val storage = rememberSolveStorage()
    var solves by remember(storage) { mutableStateOf(emptyList<Solve>()) }
    var historyReady by remember(storage) { mutableStateOf(false) }
    var historyError by remember { mutableStateOf(false) }
    var historyRequest by remember { mutableIntStateOf(0) }
    var pendingSolve by remember { mutableStateOf<SolveTimeEntity?>(null) }
    var saving by remember { mutableStateOf(false) }
    var timer by remember { mutableStateOf(SolveTimer()) }
    val clock = remember { TimeSource.Monotonic.markNow() }
    fun now() = clock.elapsedNow().inWholeMilliseconds

    var scramble by remember { mutableStateOf<String?>(null) }
    var scrambleRequest by remember { mutableIntStateOf(0) }
    var loading by remember { mutableStateOf(true) }
    var scrambleError by remember { mutableStateOf<String?>(null) }

    val scrambledCube = remember(scramble, loading) {
        if (loading) null else scramble?.let { Cube.solved().applyMoves(it) }
    }

    LaunchedEffect(storage, historyRequest) {
        historyError = false
        try {
            storage.history.collect {
                solves = it
                historyReady = true
            }
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (_: Exception) {
            historyReady = false
            historyError = true
        }
    }

    fun savePendingSolve(solve: SolveTimeEntity) {
        saving = true
        scope.launch(start = CoroutineStart.UNDISPATCHED) {
            try {
                // Finish writing a completed solve even if the screen closes.
                withContext(NonCancellable) { storage.save(solve) }
                pendingSolve = null
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (_: Exception) {
                // Keep the same solve and timestamp available for retry.
            } finally {
                saving = false
            }
        }
    }

    LaunchedEffect(scrambleRequest) {
        loading = true
        scrambleError = null
        try {
            scramble = generate3x3Scramble()
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (error: Exception) {
            scramble = null
            scrambleError = error.message ?: "Could not generate a scramble. Please try again."
        } finally {
            loading = false
        }
    }

    LaunchedEffect(timer.phase) {
        while (timer.phase == TimerPhase.HOLDING || timer.phase == TimerPhase.RUNNING) {
            timer = timer.tick(now())
            delay(16)
        }
    }

    // Leaving the foreground abandons an unfinished attempt without adding a solve.
    LifecycleEventEffect(Lifecycle.Event.ON_STOP) { timer = timer.cancel() }

    val canStart = !loading && scramble != null && historyReady && pendingSolve == null
    val onPress by rememberUpdatedState<() -> Unit>({
        if (timer.phase == TimerPhase.RUNNING) {
            timer = timer.press(now())
            val completed = Solve(timer.elapsedMillis, requireNotNull(scramble))
            val record = storage.completedSolve(completed)
            pendingSolve = record
            savePendingSolve(record)
            loading = true
            scrambleRequest++
        } else if (canStart) {
            timer = timer.press(now())
        }
    })
    val onRelease by rememberUpdatedState<() -> Unit>({ timer = timer.release(now()) })
    val onCancel by rememberUpdatedState<() -> Unit>({ timer = timer.cancel() })

    val active = timer.phase != TimerPhase.IDLE
    KeepTimerScreenAwake(enabled = active)
    val readyColor = Color(0xFF237A38)
    val timerColor = when (timer.phase) {
        TimerPhase.READY -> readyColor
        TimerPhase.HOLDING -> Color(0xFF946000)
        else -> MaterialTheme.colorScheme.onSurface
    }
    val instruction = when (timer.phase) {
        TimerPhase.HOLDING -> "Keep holding…"
        TimerPhase.READY -> "Ready — release to start"
        TimerPhase.RUNNING -> "Touch to stop"
        TimerPhase.IDLE -> when {
            historyError -> "Solve history unavailable"
            !historyReady -> "Loading solve history…"
            saving -> "Saving your solve…"
            pendingSolve != null -> "Solve needs to be saved"
            loading -> "Preparing your scramble…"
            scrambleError != null -> "Scramble unavailable — retry above"
            else -> "Hold for 0.5 seconds, then release"
        }
    }

    BoxWithConstraints(Modifier.fillMaxSize()) {
        val timerHeight = (maxHeight - 410.dp).coerceIn(180.dp, 420.dp)
        Column(
            Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text("3×3×3 Timer", style = MaterialTheme.typography.titleLarge, modifier = Modifier.weight(1f))
                TextButton(onClick = onNavigateBack, enabled = !active && !saving) { Text("Back") }
            }

            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Text("Scramble", style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                        TextButton(
                            onClick = { loading = true; scrambleRequest++ },
                            enabled = !active && !loading
                        ) { Text(if (scrambleError != null) "Retry" else "New scramble") }
                    }
                    if (loading) {
                        LinearProgressIndicator(Modifier.fillMaxWidth())
                    } else if (scrambleError != null) {
                        Text(scrambleError!!, color = MaterialTheme.colorScheme.error)
                    } else {
                        Text(
                            scramble.orEmpty(),
                            fontFamily = FontFamily.Monospace,
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            }

            Box(
                Modifier.fillMaxWidth().height(timerHeight)
                    .background(
                        if (timer.phase == TimerPhase.READY) readyColor.copy(alpha = 0.12f)
                        else MaterialTheme.colorScheme.surfaceVariant,
                        RoundedCornerShape(24.dp)
                    )
                    .semantics {
                        contentDescription = "${formatSolveTime(timer.elapsedMillis)}. $instruction"
                        // A default click action can bypass physical press/release handling
                        // on native hosts. Offer an explicit assistive action instead.
                        customActions = listOf(CustomAccessibilityAction(
                            label = if (timer.phase == TimerPhase.RUNNING) "Stop timer" else "Start timer",
                            action = {
                                if (timer.phase == TimerPhase.RUNNING) {
                                    onPress()
                                    true
                                } else if (canStart && timer.phase == TimerPhase.IDLE) {
                                    onPress()
                                    scope.launch {
                                        delay(SolveTimer.HOLD_MILLIS)
                                        onRelease()
                                    }
                                    true
                                } else false
                            }
                        ))
                    }
                    .timerTouchInput(
                        onPress = onPress,
                        onRelease = onRelease,
                        onCancel = onCancel
                    ),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        formatSolveTime(timer.elapsedMillis),
                        fontSize = if (timer.elapsedMillis < 60000) 64.sp else 48.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        color = timerColor,
                        maxLines = 1
                    )
                    Text(instruction, color = timerColor, textAlign = TextAlign.Center, modifier = Modifier.padding(12.dp))
                }
            }

            if (historyError) {
                TextButton(onClick = { historyRequest++ }) { Text("Retry loading solve history") }
            }
            pendingSolve?.let { solve ->
                if (!saving) {
                    TextButton(onClick = { savePendingSolve(solve) }) { Text("Retry saving solve") }
                }
            }

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Card(Modifier.weight(1f)) {
                    Column(Modifier.padding(8.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("Last 5", style = MaterialTheme.typography.titleSmall)
                        if (historyReady && solves.isEmpty()) {
                            Text("No solves yet", style = MaterialTheme.typography.bodySmall)
                        }
                        solves.takeLast(5).asReversed().forEachIndexed { index, solve ->
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("${solves.size - index}.", style = MaterialTheme.typography.bodySmall)
                                Text(formatSolveTime(solve.durationMillis), fontFamily = FontFamily.Monospace, style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                }
                Card(Modifier.weight(1f)) {
                    Column(Modifier.padding(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Averages", style = MaterialTheme.typography.titleSmall)
                        listOf(5, 12, 100).forEach { count ->
                            val average = solveAverage(solves, count)
                            Column {
                                Text("ao$count", style = MaterialTheme.typography.labelSmall)
                                Text(average?.let(::formatSolveTime) ?: "—", fontFamily = FontFamily.Monospace)
                            }
                        }
                        Text("${solves.size} solves", style = MaterialTheme.typography.bodySmall)
                    }
                }
                Card(Modifier.weight(1.2f)) {
                    Column(
                        Modifier.padding(8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text("Cube", style = MaterialTheme.typography.titleSmall)
                        Box(Modifier.fillMaxWidth().aspectRatio(1f), contentAlignment = Alignment.Center) {
                            if (scrambledCube != null) {
                                CubeThumbnail(
                                    cube = scrambledCube,
                                    modifier = Modifier.fillMaxSize().semantics {
                                        contentDescription = "Cube after applying the displayed scramble"
                                    }
                                )
                            } else {
                                Text(
                                    if (loading) "Loading…" else "Unavailable",
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                        Text(
                            "White top\nRed front",
                            style = MaterialTheme.typography.bodySmall,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
            Text(
                "Averages use the latest solves, excluding the fastest and slowest 5% (rounded up).",
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}
