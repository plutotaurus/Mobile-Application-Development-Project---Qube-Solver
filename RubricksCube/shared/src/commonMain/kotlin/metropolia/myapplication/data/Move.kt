package metropolia.myapplication.data
enum class Move(
    val axis: Int,
    val layer: Int,
    val angle: Float
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
    B2(2, -1, 180f)
}

fun parseMoveSequence(input: String): List<Move>? {

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
        "B2" to Move.B2
    )

    val tokens = input.trim().split(Regex("\\s+"))

    if (input.isBlank()) return null

    val moves = mutableListOf<Move>()

    for (token in tokens) {
        val move = moveMap[token] ?: return null
        moves.add(move)
    }

    return moves
}


enum class RotateCube {
    UP,
    DOWN,
    LEFT,
    RIGHT
}
