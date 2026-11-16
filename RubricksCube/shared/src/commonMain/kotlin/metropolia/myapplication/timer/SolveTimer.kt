package metropolia.myapplication.timer

import kotlin.math.roundToLong

enum class TimerPhase { IDLE, HOLDING, READY, RUNNING }

/** Pure timer transitions; callers supply monotonic milliseconds. */
data class SolveTimer(
    val phase: TimerPhase = TimerPhase.IDLE,
    val elapsedMillis: Long = 0,
    private val pressedAt: Long = 0,
    private val startedAt: Long = 0
) {
    fun press(now: Long): SolveTimer = when (phase) {
        TimerPhase.IDLE -> copy(phase = TimerPhase.HOLDING, pressedAt = now)
        TimerPhase.RUNNING -> copy(phase = TimerPhase.IDLE, elapsedMillis = now - startedAt)
        else -> this
    }

    fun tick(now: Long): SolveTimer = when (phase) {
        TimerPhase.HOLDING -> if (now - pressedAt >= HOLD_MILLIS) copy(phase = TimerPhase.READY) else this
        TimerPhase.RUNNING -> copy(elapsedMillis = now - startedAt)
        else -> this
    }

    fun release(now: Long): SolveTimer = when {
        phase == TimerPhase.READY || (phase == TimerPhase.HOLDING && now - pressedAt >= HOLD_MILLIS) ->
            copy(phase = TimerPhase.RUNNING, startedAt = now, elapsedMillis = 0)
        phase == TimerPhase.HOLDING -> copy(phase = TimerPhase.IDLE)
        else -> this
    }

    fun cancel(): SolveTimer = copy(phase = TimerPhase.IDLE)

    companion object {
        const val HOLD_MILLIS = 500L
    }
}

data class Solve(val durationMillis: Long, val scramble: String)

/** Rolling trimmed average: remove ceil(5%) from each end of the latest window. */
fun solveAverage(solves: List<Solve>, count: Int): Long? {
    require(count >= 3)
    if (solves.size < count) return null
    val trim = (count + 19) / 20
    val kept = solves.takeLast(count).map { it.durationMillis }.sorted().drop(trim).dropLast(trim)
    return (kept.average() / 10).roundToLong() * 10
}

fun formatSolveTime(millis: Long): String {
    val centiseconds = millis.coerceAtLeast(0) / 10
    val seconds = centiseconds / 100
    val fraction = (centiseconds % 100).toString().padStart(2, '0')
    return if (seconds < 60) "$seconds.$fraction"
    else "${seconds / 60}:${(seconds % 60).toString().padStart(2, '0')}.$fraction"
}
