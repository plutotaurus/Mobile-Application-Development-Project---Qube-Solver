package metropolia.myapplication

import androidx.compose.runtime.Composable

internal enum class AlgorithmStatus {
    Unlearned, Learning, Learned;

    fun next() = when (this) {
        Unlearned -> Learning
        Learning -> Learned
        Learned -> Unlearned
    }
}

@Composable
internal expect fun rememberLegacyProgress(profile: String): Map<String, AlgorithmStatus>
