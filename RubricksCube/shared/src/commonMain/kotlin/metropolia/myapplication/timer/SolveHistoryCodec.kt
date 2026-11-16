package metropolia.myapplication.timer

internal fun encodeSolves(solves: List<Solve>): String =
    solves.joinToString("\n") { "${it.durationMillis}|${it.scramble}" }

internal fun decodeSolves(value: String): List<Solve> = value.lineSequence().mapNotNull { line ->
    val fields = line.split('|', limit = 2)
    val millis = fields.firstOrNull()?.toLongOrNull()
    if (fields.size != 2 || millis == null || millis < 0) null else Solve(millis, fields[1])
}.toList()
