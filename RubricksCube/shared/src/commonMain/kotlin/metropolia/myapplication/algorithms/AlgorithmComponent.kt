package metropolia.myapplication

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import metropolia.myapplication.data.Cube

@Composable
fun AlgorithmComponent(
    algorithmName: String,
    algorithm: String,
    cube: Cube?,
    onImageClick: () -> Unit = {},
    modifier: Modifier = Modifier,
    onAlgorithmClick: (() -> Unit)? = null,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFBEC0AF)
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalArrangement = Arrangement.spacedBy(24.dp)
        ) {

            // 3D Cube Thumbnail Card (clickable to open animation)
            Card(
                modifier = Modifier
                    .size(120.dp)
                    .clickable(enabled = cube != null, onClick = onImageClick),
                colors = CardDefaults.cardColors(
                    containerColor = Color.LightGray
                ),
                shape = RoundedCornerShape(16.dp)
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
                    containerColor = Color.LightGray
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(12.dp)
                ) {
                    Text(
                        text = algorithmName,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = algorithm,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AlgorithmComponentPreview() {
    AlgorithmComponent(
        algorithmName = "T-Perm",
        algorithm = "R U R' U' D R2 U' R U' R' U R2 D'",
        cube = Cube.solved(),
        onImageClick = {}
    )
}
