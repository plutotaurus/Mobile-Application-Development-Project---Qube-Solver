package metropolia.myapplication.solver

import kotlinx.coroutines.ensureActive
import metropolia.myapplication.data.Cube
import metropolia.myapplication.data.CubeColor
import metropolia.myapplication.data.Cubelet
import metropolia.myapplication.data.Face
import kotlin.coroutines.CoroutineContext

private const val PIECE_STATES = 24
private const val CROSS_STATES = 24 * 24 * 24 * 24
private const val PAIR_STATES = 24 * 24

internal val upperTurns = listOf("", "U", "U2", "U'")
private val sideFaces = listOf("R", "F", "L", "B")
private val faceTurns = (sideFaces + listOf("U", "D")).flatMap { listOf(it, "${it}2", "$it'") }
private val crossMoves by lazy { PieceMoves(faceTurns) }
private val f2lMoves by lazy {
    // U setups and slot triggers. Each trigger restores every white cross edge.
    val triggers = sideFaces.flatMap { face ->
        upperTurns.drop(1).flatMap { turn -> listOf("$face $turn $face'", "$face' $turn $face") }
    }
    PieceMoves(upperTurns.drop(1) + triggers)
}

// Relative effort per executed turn. Charge both the setup rotation and the return.
private fun turnCost(turn: String): Int = when (turn.first()) {
    'B' -> 18
    'y', 'z' -> 15
    'D' -> 6
    'U' -> 2
    else -> 3
}

private data class F2lGrip(val setup: String, val undo: String, val notation: List<String>) {
    val rotationCost = if (setup.isEmpty()) 0 else turnCost(setup) + turnCost(undo)
    val costs = notation.map { it.split(" ").sumOf(::turnCost) }
}

private val f2lGrips by lazy {
    val faces = mapOf(
        Face.TOP to 'U', Face.BOTTOM to 'D', Face.FRONT to 'F',
        Face.BACK to 'B', Face.RIGHT to 'R', Face.LEFT to 'L'
    )
    val solved = Cube.solved()
    val centers = solved.centers()
    listOf("" to "", "y" to "y'", "y'" to "y", "z" to "z'", "z'" to "z").map { (setup, undo) ->
        // Follow the centers so the rewritten turns affect exactly the same pieces.
        val rotated = solved.applyMoves(setup).centers()
        val faceByColor = rotated.entries.associate { (face, color) -> color to faces.getValue(face) }
        val translation = centers.entries.associate { (face, color) ->
            faces.getValue(face) to faceByColor.getValue(color)
        }
        val notation = f2lMoves.notation.map { sequence ->
            sequence.split(" ").joinToString(" ") { translation.getValue(it.first()) + it.drop(1) }
        }
        F2lGrip(setup, undo, notation)
    }
}

private data class PairInsertion(val target: Int, val path: List<Int>, val grip: F2lGrip, val cost: Int) {
    fun notation(): List<String> = buildList {
        if (grip.setup.isNotEmpty()) add(grip.setup)
        path.mapTo(this, grip.notation::get)
        if (grip.undo.isNotEmpty()) add(grip.undo)
    }
}

/** Side centers of the target slot in the current orientation, and the moves to insert its pair. */
internal data class F2lPairStep(val slot: Map<Face, CubeColor>, val moves: String)

internal class CfopFirstLayers(private val context: CoroutineContext) {
    private val solved = Cube.solved()
    private val crossTargets = solved.cubelets.filter {
        it.stickers.size == 2 && Face.BOTTOM in it.stickers
    }
    private val slots = solved.cubelets.filter {
        it.stickers.size == 3 && Face.BOTTOM in it.stickers
    }.map { corner ->
        val sides = corner.stickers.filterKeys { it != Face.BOTTOM }.keys
        val edge = solved.cubelets.single { it.stickers.keys == sides }
        Slot(corner, edge)
    }

    fun cross(cube: Cube): List<String> = crossPaths(cube, 1).first()

    fun crossPaths(cube: Cube): List<List<String>> = crossPaths(cube, 3)

    private fun crossPaths(cube: Cube, count: Int): List<List<String>> {
        val centers = cube.centers()
        val targets = crossTargets.map { crossMoves.index(it, CubeColor.WHITE) }.toIntArray()
        val start = crossTargets.map {
            val colors = it.stickers.keys.map(centers::getValue)
            crossMoves.index(cube.pieceWithColors(colors), CubeColor.WHITE)
        }.toIntArray()
        val goal = encodeCross(targets)
        val state = encodeCross(start)
        if (state == goal) return listOf(emptyList())

        val distance = crossDistances(goal)
        require(distance[state] >= 0) { "The white cross cannot be solved from this cube state." }

        // Search shortest lengths first. Each alternative starts on a different face
        // and changes the cross on every turn, without revisiting a cross position.
        val paths = mutableListOf<List<String>>()
        val openings = mutableSetOf<Char>()
        val visited = mutableSetOf(state)
        val path = mutableListOf<String>()
        fun search(current: Int, remaining: Int) {
            context.ensureActive()
            if (current == goal) {
                if (remaining == 0 && openings.add(path.first().first())) paths.add(path.toList())
                return
            }
            if (remaining == 0 || distance[current] > remaining) return
            for ((index, move) in faceTurns.withIndex()) {
                if (paths.size == count || path.firstOrNull()?.first() in openings) return
                val face = move.first()
                if (face == path.lastOrNull()?.first() || (path.isEmpty() && face in openings)) continue
                val next = moveCross(current, index)
                if (distance[next] >= remaining || !visited.add(next)) continue
                path.add(move)
                search(next, remaining - 1)
                path.removeAt(path.lastIndex)
                visited.remove(next)
            }
        }

        var length = distance[state].toInt()
        while (paths.size < count) {
            context.ensureActive()
            search(state, length++)
        }
        return paths
    }

    private fun crossDistances(goal: Int): ByteArray {
        val distance = ByteArray(CROSS_STATES) { -1 }
        val queue = IntArray(CROSS_STATES)
        var read = 0
        var write = 1
        queue[0] = goal
        distance[goal] = 0
        while (read < write) {
            if (read % 1024 == 0) context.ensureActive()
            val state = queue[read++]
            for (move in crossMoves.notation.indices) {
                val next = moveCross(state, move)
                if (distance[next] < 0) {
                    distance[next] = (distance[state] + 1).toByte()
                    queue[write++] = next
                }
            }
        }
        return distance
    }

    private fun moveCross(state: Int, move: Int): Int {
        val map = crossMoves.edges[move]
        var remaining = state
        var next = 0
        var place = 1
        repeat(4) {
            next += map[remaining % PIECE_STATES] * place
            remaining /= PIECE_STATES
            place *= PIECE_STATES
        }
        return next
    }

    private fun encodeCross(pieces: IntArray): Int =
        pieces.fold(0) { code, piece -> code * PIECE_STATES + piece }

    fun f2l(cube: Cube, onPair: (F2lPairStep) -> Unit = {}): List<String> {
        val centers = cube.centers()
        val goals = slots.map { slot ->
            pairState(
                slot.corner, CubeColor.WHITE,
                slot.edge, slot.edge.stickers.getValue(slot.referenceFace)
            )
        }.toIntArray()
        val starts = slots.map { slot ->
            val corner = cube.pieceWithColors(slot.corner.stickers.keys.map(centers::getValue))
            val edge = cube.pieceWithColors(slot.edge.stickers.keys.map(centers::getValue))
            pairState(corner, CubeColor.WHITE, edge, centers.getValue(slot.referenceFace))
        }.toIntArray()
        // Trying the other pair orders can avoid leaving a difficult rear slot until last.
        val plan = mutableListOf<PairInsertion>()
        var bestPlan: List<PairInsertion>? = null
        var bestCost = Int.MAX_VALUE

        fun planPairs(states: IntArray, cost: Int) {
            if (cost >= bestCost) return
            if (states.contentEquals(goals)) {
                bestPlan = plan.toList()
                bestCost = cost
                return
            }
            val completed = goals.indices.filter { states[it] == goals[it] }
            val allowedMoves = f2lMoves.notation.indices.filter { move ->
                completed.all { movePair(goals[it], move) == goals[it] }
            }
            val candidates = goals.indices.filter { states[it] != goals[it] }.mapNotNull { slot ->
                var best: PairInsertion? = null
                for (grip in f2lGrips) {
                    val limit = best?.cost ?: (bestCost - cost)
                    if (grip.rotationCost >= limit) continue
                    findPair(slot, states[slot], goals[slot], allowedMoves, grip, limit)?.let { best = it }
                }
                best
            }
            for (insertion in candidates.sortedBy { it.cost }) {
                val nextCost = cost + insertion.cost
                if (nextCost >= bestCost) break
                val next = states.copyOf()
                for (move in insertion.path) {
                    for (slot in next.indices) next[slot] = movePair(next[slot], move)
                }
                plan.add(insertion)
                planPairs(next, nextCost)
                plan.removeAt(plan.lastIndex)
            }
        }

        planPairs(starts, 0)
        val solution = requireNotNull(bestPlan) { "The next F2L pair could not be solved from this cube state." }
        return solution.flatMap { insertion ->
            val slot = slots[insertion.target].edge.stickers.keys.associateWith(centers::getValue)
            val notation = insertion.notation()
            onPair(F2lPairStep(slot, notation.joinToString(" ")))
            notation
        }
    }

    private fun pairState(
        corner: Cubelet,
        cornerReference: CubeColor,
        edge: Cubelet,
        edgeReference: CubeColor
    ): Int = f2lMoves.index(corner, cornerReference) * PIECE_STATES +
        f2lMoves.index(edge, edgeReference)

    private fun movePair(state: Int, move: Int): Int =
        f2lMoves.corners[move][state / PIECE_STATES] * PIECE_STATES +
            f2lMoves.edges[move][state % PIECE_STATES]

    private fun findPair(
        target: Int,
        start: Int,
        goal: Int,
        allowedMoves: List<Int>,
        grip: F2lGrip,
        limit: Int,
    ): PairInsertion? {
        val previous = IntArray(PAIR_STATES) { -1 }
        val usedMove = IntArray(PAIR_STATES)
        val costs = IntArray(PAIR_STATES) { Int.MAX_VALUE }
        // Dijkstra with integer cost buckets. A state can be reached more cheaply later.
        val buckets = mutableListOf(mutableListOf(start))
        costs[start] = 0
        var cost = 0
        while (cost < buckets.size) {
            context.ensureActive()
            for (state in buckets[cost]) {
                if (costs[state] != cost) continue
                if (state == goal) {
                    val path = mutableListOf<Int>()
                    var current = goal
                    while (current != start) {
                        path += usedMove[current]
                        current = previous[current]
                    }
                    return PairInsertion(target, path.asReversed(), grip, cost + grip.rotationCost)
                }
                for (move in allowedMoves) {
                    val next = movePair(state, move)
                    val nextCost = cost + grip.costs[move]
                    if (nextCost >= costs[next] || nextCost + grip.rotationCost >= limit) continue
                    previous[next] = state
                    usedMove[next] = move
                    costs[next] = nextCost
                    while (buckets.size <= nextCost) buckets.add(mutableListOf())
                    buckets[nextCost].add(next)
                }
            }
            cost++
        }
        return null
    }

    private data class Slot(val corner: Cubelet, val edge: Cubelet) {
        val referenceFace = edge.stickers.keys.first()
    }
}
