package metropolia.myapplication.renderingCube

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import metropolia.myapplication.data.Cube
import metropolia.myapplication.data.CubeColor
import metropolia.myapplication.data.Face

fun changeColor(color: CubeColor, selectedPosition: MutableState<Vector3i?>, selectedFace: MutableState<Face?>, showColorMenu: MutableState<Boolean>, cubeState: MutableState<Cube>) {

    val position = selectedPosition.value ?: return
    val face = selectedFace.value ?: return

    cubeState.value = cubeState.value.copy(
        cubelets = cubeState.value.cubelets.map { cubelet ->

            if (cubelet.position == position) {
                cubelet.copy(
                    stickers = cubelet.stickers + (face to color)
                )
            } else {
                cubelet
            }
        }
    )

    showColorMenu.value = false
}

fun isPointInsidePolygon(
    point: Offset,
    polygon: List<Offset>
): Boolean {

    var inside = false
    var j = polygon.lastIndex

    for (i in polygon.indices) {

        val xi = polygon[i].x
        val yi = polygon[i].y

        val xj = polygon[j].x
        val yj = polygon[j].y

        val intersects =
            ((yi > point.y) != (yj > point.y)) &&
                    (point.x < (xj - xi) * (point.y - yi) / (yj - yi) + xi)

        if (intersects) {
            inside = !inside
        }

        j = i
    }

    return inside
}


@Composable
fun CubeColorMenu(
    visible: Boolean,
    onColorSelected: (CubeColor) -> Unit
) {
    if (!visible) return

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Choose a colour",
            style = MaterialTheme.typography.titleMedium
        )

        CubeColor.entries.chunked(3).forEach { rowColors ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                rowColors.forEach { color ->
                    Button(
                        onClick = { onColorSelected(color) },
                        modifier = Modifier.size(64.dp),
                        contentPadding = PaddingValues(0.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = color.color
                        ),
                        border = BorderStroke(2.dp, Color.Black)
                    ){}
                }
            }
        }
    }
}
