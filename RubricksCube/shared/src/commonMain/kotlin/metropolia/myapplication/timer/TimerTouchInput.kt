package metropolia.myapplication.timer

import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput

/** Own the touch until release so small finger movements cannot become page scrolling. */
@Composable
internal fun Modifier.timerTouchInput(
    onPress: () -> Unit,
    onRelease: () -> Unit,
    onCancel: () -> Unit
): Modifier {
    val press by rememberUpdatedState(onPress)
    val release by rememberUpdatedState(onRelease)
    val cancel by rememberUpdatedState(onCancel)
    return pointerInput(Unit) {
        awaitEachGesture {
            val down = awaitFirstDown()
            down.consume()
            var released = false
            try {
                press()
                while (true) {
                    val event = awaitPointerEvent()
                    val finger = event.changes.firstOrNull { it.id == down.id } ?: break
                    val outside = finger.position.x < 0 || finger.position.x > size.width ||
                        finger.position.y < 0 || finger.position.y > size.height
                    val interrupted = event.changes.any { it.isConsumed || (it.id != down.id && it.pressed) }
                    event.changes.forEach { it.consume() }
                    if (outside || interrupted) break
                    if (!finger.pressed) {
                        released = true
                        release()
                        break
                    }
                }
            } finally {
                // Includes a pointer cancellation, leaving the pad, or leaving the screen.
                if (!released) cancel()
            }
        }
    }
}
