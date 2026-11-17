package metropolia.myapplication.timer

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import platform.JavaScriptCore.JSContext
import rubrickscube.shared.generated.resources.Res
import kotlin.coroutines.coroutineContext

private val scrambleMutex = Mutex()
private var scrambleEngine: IosScrambleEngine? = null

actual suspend fun generate3x3Scramble(): String = withContext(Dispatchers.Default) {
    scrambleMutex.withLock {
        coroutineContext.ensureActive()
        val engine = scrambleEngine ?: IosScrambleEngine(
            solver = Res.readBytes("files/min2phase.js").decodeToString(),
            wrapper = Res.readBytes("files/scramble3x3.js").decodeToString()
        ).also { scrambleEngine = it }
        val scramble = engine.generate()
        coroutineContext.ensureActive()
        scramble
    }
}

/** Serialized access preserves the initialized solver tables without blocking the UI. */
@OptIn(ExperimentalForeignApi::class)
internal class IosScrambleEngine(solver: String, wrapper: String) {
    private val context = JSContext()

    init {
        evaluate(solver)
        evaluate(wrapper)
    }

    fun generate(): String = evaluate("qubeScrambler.generate()")
        .also { check(it.isNotBlank()) { "The scramble generator returned an empty sequence." } }

    private fun evaluate(script: String): String {
        context.exception = null
        val result = context.evaluateScript(script)
        check(context.exception == null) {
            "Could not generate a 3×3 scramble: ${context.exception?.toString()}"
        }
        return result?.toString().orEmpty()
    }
}
