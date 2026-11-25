package metropolia.myapplication.solver

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import metropolia.myapplication.data.Cube
import metropolia.myapplication.data.CubeColor
import metropolia.myapplication.data.Face
import metropolia.myapplication.data.local.QubeDatabase
import metropolia.myapplication.data.local.seedStarterAlgorithms
import metropolia.myapplication.data.parseMoveSequence
import kotlin.coroutines.CoroutineContext

/**
 * Returns a space-separated CFOP solution using the local algorithm library.
 * The white center is turned to the bottom before solving the cross.
 * An already solved cube returns an empty string.
 */
internal suspend fun QubeDatabase.solveCfop(
    cube: Cube,
    onF2lPair: (F2lPairStep) -> Unit = {},
): String {
    val dao = algorithms()
    dao.seedStarterAlgorithms()
    val catalog = dao.observeAll().first().map {
        LastLayerAlgorithm(
            id = it.algorithm.algorithmId,
            stage = it.algorithm.type,
            original = it.algorithm.algorithmText,
            favourite = it.favouriteText
        )
    }
    return withContext(Dispatchers.Default) { solveCfop(cube, catalog, coroutineContext, onF2lPair) }
}

internal data class LastLayerAlgorithm(
    val id: String,
    val stage: String,
    val original: String,
    val favourite: String? = null,
)

private val whiteDown = mapOf(
    Face.BOTTOM to "",
    Face.TOP to "x x",
    Face.FRONT to "x'",
    Face.BACK to "x",
    Face.RIGHT to "z",
    Face.LEFT to "z'"
)
private val lastLayerSetups = listOf("", "y", "y y", "y'")

private fun Cube.whiteDownSetup(): String =
    whiteDown.getValue(centers().entries.single { it.value == CubeColor.WHITE }.key)

internal suspend fun whiteCrossPaths(cube: Cube): List<String> = withContext(Dispatchers.Default) {
    validateCube(cube)
    val setup = cube.whiteDownSetup()
    CfopFirstLayers(coroutineContext).crossPaths(cube.applyMoves(setup)).map { path ->
        joinMoves(setup, path.joinToString(" "))
    }
}

internal fun solveCfop(
    cube: Cube,
    catalog: List<LastLayerAlgorithm>,
    context: CoroutineContext,
    onF2lPair: (F2lPairStep) -> Unit = {},
): String {
    validateCube(cube)
    if (cube.isSolved()) return ""

    val firstLayers = CfopFirstLayers(context)
    val moves = mutableListOf<String>()
    var state = cube
    fun apply(sequence: String) {
        if (sequence.isBlank()) return
        state = state.applyMoves(sequence)
        moves += sequence
    }

    apply(state.whiteDownSetup())
    check(state.centers()[Face.BOTTOM] == CubeColor.WHITE)
    firstLayers.cross(state).forEach(::apply)
    firstLayers.f2l(state, onF2lPair).forEach(::apply)
    check(state.hasSolvedFirstTwoLayers()) { "F2L did not preserve the white cross and all four slots." }

    if (!state.isLastLayerOriented()) {
        apply(findLastLayerMoves(state, catalog, "OLL", context))
    }
    if (!state.isSolved()) {
        apply(findLastLayerMoves(state, catalog, "PLL", context))
    }
    check(state.isSolved()) { "CFOP moves did not solve the cube." }
    return moves.joinToString(" ")
}

private fun findLastLayerMoves(
    cube: Cube,
    catalog: List<LastLayerAlgorithm>,
    stage: String,
    context: CoroutineContext
): String {
    val isOll = stage == "OLL"
    if (!isOll) {
        upperTurns.firstOrNull { cube.applyMoves(it).isSolved() }?.let { return it }
    }

    // Recognize the case from its original sequence, then align the chosen variant independently.
    fun match(sequence: String): String? {
        if (parseMoveSequence(sequence) == null) return null
        for (rotation in lastLayerSetups) {
            for (turn in upperTurns) {
                context.ensureActive()
                val prefix = joinMoves(turn, rotation)
                val result = cube.applyMoves(prefix).applyMoves(sequence)
                val finish = if (isOll) {
                    if (result.isLastLayerOriented()) "" else null
                } else {
                    upperTurns.firstOrNull { result.applyMoves(it).isSolved() }
                }
                if (finish != null) return joinMoves(prefix, sequence, finish)
            }
        }
        return null
    }

    for (algorithm in catalog.filter { it.stage == stage }) {
        val original = match(algorithm.original) ?: continue
        return algorithm.favourite?.let {
            checkNotNull(match(it)) { "Favourite for ${algorithm.id} does not solve its $stage case." }
        } ?: original
    }
    throw IllegalArgumentException("No $stage case matches this cube state.")
}

private fun joinMoves(vararg parts: String): String = parts.filter { it.isNotBlank() }.joinToString(" ")

private fun Cube.isSolved(): Boolean {
    val centers = centers()
    return cubelets.all { isSolvedPiece(it, centers) }
}

private fun Cube.hasSolvedFirstTwoLayers(): Boolean {
    val centers = centers()
    return centers[Face.BOTTOM] == CubeColor.WHITE &&
        cubelets.filter { it.position.y <= 0 }.all { isSolvedPiece(it, centers) }
}

private fun Cube.isLastLayerOriented(): Boolean {
    val topColor = centers().getValue(Face.TOP)
    return hasSolvedFirstTwoLayers() && cubelets.count { it.stickers[Face.TOP] == topColor } == 9
}

private fun validateCube(cube: Cube) {
    val expected = Cube.solved().cubelets.associateBy { it.position }
    require(cube.cubelets.size == expected.size && cube.cubelets.map { it.position }.toSet() == expected.keys &&
        cube.cubelets.all { it.stickers.keys == expected.getValue(it.position).stickers.keys }
    ) { "Expected a complete 3×3 cube with 54 stickers." }
    val actualPieces = cube.cubelets.map { it.stickers.values.toSet() }
    val expectedPieces = expected.values.map { it.stickers.values.toSet() }.toSet()
    require(actualPieces.toSet() == expectedPieces) { "Cube pieces are missing, duplicated, or have invalid colors." }
}
