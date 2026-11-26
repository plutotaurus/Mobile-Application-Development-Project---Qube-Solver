package metropolia.myapplication.solver

import kotlinx.coroutines.runBlocking
import metropolia.myapplication.data.Cube
import metropolia.myapplication.data.CubeColor
import metropolia.myapplication.data.Face
import metropolia.myapplication.data.local.QubeDatabase
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class CfopSolverTest {
    @Test
    fun offersThreeDistinctCrossPathsInTheUsersOrientation() = runBlocking {
        for (scramble in listOf("D", "B' x", "R F2 D B' L U2 z")) {
            println("White cross scramble: $scramble")
            val cube = Cube.solved().applyMoves(scramble)
            val paths = whiteCrossPaths(cube)
            val turns = paths.map { path -> path.split(" ").dropWhile { it.first() in "xyz" } }
            assertEquals(3, paths.size)
            assertEquals(3, turns.map { it.first().first() }.distinct().size)
            assertEquals(turns.map { it.size }.sorted(), turns.map { it.size })
            for ((index, path) in paths.withIndex()) {
                println("  Path ${index + 1} (face turns: ${turns[index].size}): $path")
                val result = cube.applyMoves(path)
                val centers = result.centers()
                assertEquals(CubeColor.WHITE, centers[Face.BOTTOM])
                val edges = result.cubelets.filter { it.stickers.size == 2 && Face.BOTTOM in it.stickers }
                assertEquals(4, edges.size)
                assertTrue(edges.all { result.isSolvedPiece(it, centers) })
            }
        }
    }

    @Test
    fun alreadySolvedCrossOnlyNeedsOrientation() = runBlocking {
        assertEquals(listOf(""), whiteCrossPaths(Cube.solved().applyMoves("U")))
        assertEquals(listOf("x'"), whiteCrossPaths(Cube.solved().applyMoves("U x")))
    }

    @Test
    fun solvesCrossAndF2lAsSeparateStages() {
        val scramble = "U2 R B2 D2 R B2 D2 L' D2 R B2 U2 F' D R B2 L2 B' L' B2 U'"
        println("Cross and F2L input: $scramble")
        val cube = Cube.solved().applyMoves(scramble)
        val firstLayers = CfopFirstLayers(EmptyCoroutineContext)

        val crossMoves = firstLayers.cross(cube).joinToString(" ")
        println("White cross: $crossMoves")
        val cross = cube.applyMoves(crossMoves)
        val centers = cross.centers()
        assertTrue(cross.cubelets.filter { it.stickers.size == 2 && Face.BOTTOM in it.stickers }
            .all { cross.isSolvedPiece(it, centers) })

        val steps = mutableListOf<F2lPairStep>()
        val f2lMoves = firstLayers.f2l(cross, steps::add).joinToString(" ")
        assertEquals(f2lMoves, steps.joinToString(" ") { it.moves })
        var replay = cross
        for ((index, step) in steps.withIndex()) {
            val colors = step.slot.values.joinToString("–") { it.name.lowercase() }
            val slot = step.slot.keys.joinToString("-") { it.name.lowercase() }
            println("F2L pair ${index + 1}: $colors → $slot | ${step.moves}")
            val edgeColors = step.slot.values
            val cornerColors = edgeColors + CubeColor.WHITE
            assertEquals(2, step.slot.size)
            assertEquals(centers.filterKeys { it in step.slot }, step.slot)
            val edge = replay.pieceWithColors(edgeColors)
            val corner = replay.pieceWithColors(cornerColors)
            assertTrue(!replay.isSolvedPiece(edge, centers) || !replay.isSolvedPiece(corner, centers))
            replay = replay.applyMoves(step.moves)
            assertEquals(step.slot, replay.pieceWithColors(edgeColors).stickers)
            assertEquals(step.slot + (Face.BOTTOM to CubeColor.WHITE), replay.pieceWithColors(cornerColors).stickers)
        }
        println("F2L: $f2lMoves")
        val f2l = cross.applyMoves(f2lMoves)
        assertTrue(f2l.cubelets.filter { it.position.y <= 0 }.all { f2l.isSolvedPiece(it, centers) })
    }

    @Test
    fun fullSolveReportsTheFirstPairUsingTheCurrentCenters() {
        val cube = Cube.solved().applyMoves("y R U R'")
        val steps = mutableListOf<F2lPairStep>()
        val solution = solveCfop(cube, emptyList(), EmptyCoroutineContext, steps::add)
        val first = steps.single()
        val centers = cube.centers()
        assertEquals(centers.filterKeys { it == Face.FRONT || it == Face.RIGHT }, first.slot)
        assertTrue(solution.startsWith(first.moves))
        val solved = cube.applyMoves(solution)
        assertTrue(solved.cubelets.all { solved.isSolvedPiece(it, centers) })
    }

    @Test
    fun solvesF2lWithWhiteCrossAlreadySolved() {
        val scramble = "R U2 F' D L B2 U R' F2 U' R F B2 R' L'"
        val cube = Cube.solved().applyMoves(scramble)
        val centers = cube.centers()
        val crossEdges = cube.cubelets.filter { it.stickers.size == 2 && Face.BOTTOM in it.stickers }
        val firstTwoLayers = cube.cubelets.filter { it.position.y <= 0 }
        assertEquals(CubeColor.WHITE, centers[Face.BOTTOM])
        assertTrue(crossEdges.all { cube.isSolvedPiece(it, centers) })
        assertTrue(firstTwoLayers.any { !cube.isSolvedPiece(it, centers) })

        val f2lMoves = CfopFirstLayers(EmptyCoroutineContext).f2l(cube).joinToString(" ")
        println("F2L-only scramble: $scramble")
        println("F2L-only solution: $f2lMoves")

        val result = cube.applyMoves(f2lMoves)
        assertTrue(result.cubelets.filter { it.position.y <= 0 }.all { result.isSolvedPiece(it, centers) })
    }

    @Test
    fun avoidsRepeatedBackTurnsAndLimitsRegrips() {
        val solver = CfopFirstLayers(EmptyCoroutineContext)
        val solved = Cube.solved()
        val centers = solved.centers()
        // The direct inverse needs four back turns. One regrip can put the pair on the right.
        val rear = solved.applyMoves("B U B' U' B U2 B'")
        val steps = mutableListOf<F2lPairStep>()
        val insertion = solver.f2l(rear, steps::add).joinToString(" ")
        val turns = insertion.split(" ")
        assertTrue(turns.none { it.startsWith("B") }, insertion)
        assertTrue(turns.count { it.first() in "yz" } <= 2 && turns.size <= 9, insertion)
        val step = steps.single()
        assertEquals(step.moves, insertion)
        val result = rear.applyMoves(insertion)
        assertEquals(centers, result.centers())
        assertEquals(step.slot, result.pieceWithColors(step.slot.values).stickers)
        assertTrue(result.cubelets.filter { it.position.y <= 0 }.all { result.isSolvedPiece(it, centers) })

        val easy = solved.applyMoves("R U R'")
        assertEquals("R U' R'", solver.f2l(easy).joinToString(" "))
    }

    @Test
    fun usesSavedOllAndPllVariantsAfterRecognizingTheirCases() {
        val oll = LastLayerAlgorithm("27", "OLL", "R U R' U R U2 R'", "R U R' U R U2 R' U'")
        val pll = LastLayerAlgorithm("Ua-perm", "PLL", "M2 U M U2 M' U M2", "R U' R U R U R U' R' U' R2")
        val input = Cube.solved().applyMoves("R2 U R U R' U' R' U' R' U R' U R U2 R' U' R U' R' x")

        val solution = solveCfop(input, listOf(oll, pll), EmptyCoroutineContext)
        println("Expected OLL favourite (${oll.id}): ${oll.favourite}")
        println("Expected PLL favourite (${pll.id}): ${pll.favourite}")
        println("CFOP solution: $solution")
        val solved = input.applyMoves(solution)
        assertTrue(solution.contains(requireNotNull(oll.favourite)))
        assertTrue(solution.contains(requireNotNull(pll.favourite)))
        assertEquals(CubeColor.WHITE, solved.centers()[Face.BOTTOM])
        assertTrue(solved.cubelets.all { solved.isSolvedPiece(it, solved.centers()) })
    }

    @Test
    fun solvesFullCubeFromScramble() {
        val oll = LastLayerAlgorithm("27", "OLL", "R U R' U R U2 R'")
        val pll = LastLayerAlgorithm("Ua-perm", "PLL", "M2 U M U2 M' U M2")
        val scramble = "R2 U R U R' U' R' U' R' U R' R U2 R' U' R U' R' R U R' B"
        val cube = Cube.solved().applyMoves(scramble)
        println("Full solve scramble: $scramble")

        val solution = solveCfop(cube, listOf(oll, pll), EmptyCoroutineContext)
        println("Full solve solution: $solution")
        assertTrue(oll.original in solution)
        assertTrue(pll.original in solution)

        val solved = cube.applyMoves(solution)
        val centers = solved.centers()
        assertTrue(solved.cubelets.all { solved.isSolvedPiece(it, centers) })
    }

    @Test
    fun handlesSolvedAndIncompleteCubes() {
        val solution = solveCfop(Cube.solved(), emptyList(), EmptyCoroutineContext)
        println("Solved cube returned ${solution.length} characters")
        assertEquals("", solution)
        val error = assertFailsWith<IllegalArgumentException> {
            solveCfop(Cube(emptyList()), emptyList(), EmptyCoroutineContext)
        }
        println("Incomplete cube rejected: ${error.message}")
    }
}

internal suspend fun verifyFullSolveWithDatabase(database: QubeDatabase) {
    val scramble = "L2 U2 B2 D F2 U L2 B2 U2 B2 F2 U2 R' B L2 U B2 U2 L2 R' F2"
    val cube = Cube.solved().applyMoves(scramble)
    println("Full solve scramble: $scramble")

    val solution = database.solveCfop(cube)
    println("Full solve solution: $solution")

    val solved = cube.applyMoves(solution)
    val centers = solved.centers()
    assertTrue(solved.cubelets.all { solved.isSolvedPiece(it, centers) })
}
