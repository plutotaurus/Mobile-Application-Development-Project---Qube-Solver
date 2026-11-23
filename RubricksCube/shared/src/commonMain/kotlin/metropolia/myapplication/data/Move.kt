package metropolia.myapplication.data
enum class Move(
    val axis: Int,
    val layer: Int,
    val angle: Float,
    val wide: Boolean = false,
) {
    // Upper
    U(1, 1, -90f),
    U_PRIME(1, 1, 90f),
    U2(1, 1, -180f),

    // Down
    D(1, -1, 90f),
    D_PRIME(1, -1, -90f),
    D2(1, -1, 180f),

    // Left
    L(0, -1, 90f),
    L_PRIME(0, -1, -90f),
    L2(0, -1, 180f),

    // Right
    R(0, 1, -90f),
    R_PRIME(0, 1, 90f),
    R2(0, 1, -180f),

    // Front
    F(2, 1, -90f),
    F_PRIME(2, 1, 90f),
    F2(2, 1, -180f),

    // Back
    B(2, -1, 90f),
    B_PRIME(2, -1, -90f),
    B2(2, -1, 180f),

    // Middle
    M(0, 0, 90f),
    M_PRIME(0, 0, -90f),
    M2(0, 0, 180f),

    //Moves both right layers often denoted with r
    WIDE_R(0, 1, -90f, wide = true),
    WIDE_R_PRIME(0, 1, 90f, wide = true),
    WIDE_R2(0, 1, -180f, wide = true),

    //Moves both left layers often denoted with l
    WIDE_L(0, -1, 90f, wide = true),
    WIDE_L_PRIME(0, -1, -90f, wide = true),
    WIDE_L2(0, -1, 180f, wide = true)
}

fun parseMoveSequence(input: String): List<SequenceMove>? {

    val moveMap = mapOf(
        "U" to Move.U,
        "U'" to Move.U_PRIME,
        "U2" to Move.U2,

        "D" to Move.D,
        "D'" to Move.D_PRIME,
        "D2" to Move.D2,

        "L" to Move.L,
        "L'" to Move.L_PRIME,
        "L2" to Move.L2,

        "R" to Move.R,
        "R'" to Move.R_PRIME,
        "R2" to Move.R2,

        "F" to Move.F,
        "F'" to Move.F_PRIME,
        "F2" to Move.F2,

        "B" to Move.B,
        "B'" to Move.B_PRIME,
        "B2" to Move.B2,

        "M" to Move.M,
        "M'" to Move.M_PRIME,
        "M2" to Move.M2,

        "r" to Move.WIDE_R,
        "r'" to Move.WIDE_R_PRIME,
        "r2" to Move.WIDE_R2,

        "l" to Move.WIDE_L,
        "l'" to Move.WIDE_L_PRIME,
        "l2" to Move.WIDE_L2,
    )

    val rotationMap = mapOf(
        "x" to RotateCube.X,
        "x'" to RotateCube.X_PRIME,
        "y" to RotateCube.Y,
        "y'" to RotateCube.Y_PRIME,
        "z" to RotateCube.Z,
        "z'" to RotateCube.Z_PRIME
    )

    if (input.isBlank()) return null

    val result = mutableListOf<SequenceMove>()

    for (token in input.trim().split(Regex("\\s+"))) {
        when {
            token in moveMap -> {
                result.add(SequenceMove.FaceTurn(moveMap[token]!!))
            }

            token in rotationMap -> {
                result.add(SequenceMove.CubeTurn(rotationMap[token]!!))
            }

            else -> return null
        }
    }

    return result
}


enum class RotateCube(internal val faceTurn: Move) {
    X(Move.R), X_PRIME(Move.R_PRIME),
    Y(Move.U), Y_PRIME(Move.U_PRIME),
    Z(Move.F), Z_PRIME(Move.F_PRIME)
}

sealed class SequenceMove {
    data class FaceTurn(val move: Move) : SequenceMove()
    data class CubeTurn(val direction: RotateCube) : SequenceMove()
}