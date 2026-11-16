package metropolia.myapplication

import kotlin.test.*
import metropolia.myapplication.timer.*

class SolveTimerTest {
    @Test
    fun releasingBeforeHalfASecondDoesNotStart() {
        val timer = SolveTimer().press(1000).release(1499)
        assertEquals(TimerPhase.IDLE, timer.phase)
        assertEquals(0L, timer.elapsedMillis)
    }

    @Test
    fun holdingArmsButOnlyReleaseStartsAndNextPressStops() {
        val ready = SolveTimer().press(1000).tick(1500)
        assertEquals(TimerPhase.READY, ready.phase)
        assertEquals(0L, ready.elapsedMillis)
        val running = ready.release(2000)
        assertEquals(TimerPhase.RUNNING, running.phase)
        assertEquals(1234L, running.tick(3234).elapsedMillis)
        val stopped = running.press(14345)
        assertEquals(TimerPhase.IDLE, stopped.phase)
        assertEquals(12345L, stopped.elapsedMillis)
        assertEquals(stopped, stopped.release(15000))
    }

    @Test
    fun releaseAtThresholdStartsEvenWithoutAnAnimationTick() {
        assertEquals(TimerPhase.RUNNING, SolveTimer().press(0).release(500).phase)
    }

    @Test
    fun cancelledHoldNeverStarts() {
        val cancelled = SolveTimer().press(1000).tick(1600).cancel().release(2000)
        assertEquals(TimerPhase.IDLE, cancelled.phase)
    }

    @Test
    fun aShortHoldPreservesPreviousResult() {
        val stopped = SolveTimer().press(0).release(500).press(2500)
        assertEquals(2000L, stopped.press(3000).release(3100).elapsedMillis)
    }

    @Test
    fun rollingAveragesTrimOutliersAndRequireFullWindow() {
        fun solves(vararg times: Long) = times.map { Solve(it, "R U") }
        assertNull(solveAverage(solves(1000, 2000, 3000, 4000), 5))
        assertEquals(3000L, solveAverage(solves(1, 2000, 3000, 4000, 99000), 5))
        assertEquals(4000L, solveAverage(solves(500000, 1, 3000, 4000, 5000, 99000), 5))
        assertEquals(6500L, solveAverage((1L..12L).map { Solve(it * 1000, "") }, 12))
        assertEquals(50500L, solveAverage((1L..100L).map { Solve(it * 1000, "") }, 100))
    }

    @Test
    fun averagesRoundToHundredthsAndTimerFormatsMinutes() {
        assertEquals(1010L, solveAverage(List(5) { Solve(1006, "") }, 5))
        assertEquals(1010L, solveAverage(List(5) { Solve(1005, "") }, 5))
        assertEquals("0.00", formatSolveTime(0))
        assertEquals("12.34", formatSolveTime(12349))
        assertEquals("1:02.34", formatSolveTime(62349))
    }

    @Test
    fun historyRoundTripsAndSkipsDamagedRecords() {
        val solves = listOf(Solve(12000, "R U2 F'"), Solve(63010, "D B2"))
        assertEquals(solves, decodeSolves(encodeSolves(solves)))
        assertEquals(emptyList(), decodeSolves(""))
        assertEquals(solves, decodeSolves("broken\n-1|R\n" + encodeSolves(solves)))
    }
}
