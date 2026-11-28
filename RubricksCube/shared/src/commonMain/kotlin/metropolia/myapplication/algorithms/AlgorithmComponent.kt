package metropolia.myapplication

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import metropolia.myapplication.data.Cube
import metropolia.myapplication.ui.QubeTheme

@Composable
fun AlgorithmComponent(
    algorithmName: String,
    algorithm: String,
    cube: Cube?,
    onImageClick: () -> Unit = {},
    modifier: Modifier = Modifier,
    onAlgorithmClick: (() -> Unit)? = null,
    statusChip: @Composable () -> Unit = {},
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        ),
        shape = MaterialTheme.shapes.large
    ) {
        Column(Modifier.fillMaxWidth().padding(16.dp)) {
            AlgorithmTitle(algorithmName, onAlgorithmClick, statusChip)
            Spacer(Modifier.height(8.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(24.dp)
            ) {

                // 3D Cube Thumbnail Card (clickable to open animation)
                Card(
                    modifier = Modifier
                        .size(120.dp)
                        .clickable(enabled = cube != null, onClick = onImageClick),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerHighest
                    ),
                    shape = MaterialTheme.shapes.large
                ) {
                    if (cube != null) {
                        CubeThumbnail(
                            cube = cube,
                            modifier = Modifier.fillMaxSize().padding(12.dp)
                        )
                    } else {
                        Box(Modifier.fillMaxSize().padding(12.dp), contentAlignment = Alignment.Center) {
                            Text("Preview unavailable")
                        }
                    }
                }

                // Algorithm information
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .then(if (onAlgorithmClick != null) Modifier.clickable(onClick = onAlgorithmClick) else Modifier),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerHighest
                    ),
                    shape = MaterialTheme.shapes.large
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp)
                    ) {
                        Text(
                            text = algorithm,
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            }
        }
    }
}

@Composable
internal fun AlgorithmTitle(
    name: String,
    onOpen: (() -> Unit)? = null,
    chip: @Composable () -> Unit,
) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            name,
            modifier = Modifier.weight(1f, fill = false)
                .then(if (onOpen == null) Modifier else Modifier.clickable(onClick = onOpen)),
            style = MaterialTheme.typography.titleLarge,
        )
        chip()
    }
}

@Composable
internal fun AlgorithmStatusChip(status: AlgorithmStatus, id: String, onCycle: () -> Unit) {
    val colors = when (status) {
        AlgorithmStatus.Unlearned -> AssistChipDefaults.assistChipColors()
        AlgorithmStatus.Learning -> AssistChipDefaults.assistChipColors(
            containerColor = Color(0xFFFFE79B), labelColor = Color(0xFF463500),
        )
        AlgorithmStatus.Learned -> AssistChipDefaults.assistChipColors(
            containerColor = Color(0xFFC6EFCE), labelColor = Color(0xFF123E20),
        )
    }
    AssistChip(
        onClick = onCycle,
        label = { Text(status.name) },
        colors = colors,
        modifier = Modifier.semantics {
            contentDescription = "$id, ${status.name}. Mark as ${status.next().name}"
        },
    )
}

@Preview(showBackground = true)
@Composable
fun AlgorithmComponentPreview() {
    QubeTheme {
        AlgorithmComponent(
            algorithmName = "T-Perm",
            algorithm = "R U R' U' D R2 U' R U' R' U R2 D'",
            cube = Cube.solved(),
            onImageClick = {}
        )
    }
}
