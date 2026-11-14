package metropolia.myapplication

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.painterResource
import rubrickscube.shared.generated.resources.Res
import rubrickscube.shared.generated.resources.medal_gold_winner_2_svgrepo_com
import rubrickscube.shared.generated.resources.medal_silver_badge_svgrepo_com
import rubrickscube.shared.generated.resources.medal_bronze_prize_svgrepo_com

@Composable
fun BadgeGrid(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Badges & Achievements",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "View All >",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    painter = painterResource(Res.drawable.medal_gold_winner_2_svgrepo_com),
                    contentDescription = "Gold Badge",
                    modifier = Modifier.size(64.dp).clip(RoundedCornerShape(8.dp))
                )
                Image(
                    painter = painterResource(Res.drawable.medal_silver_badge_svgrepo_com),
                    contentDescription = "Silver Badge",
                    modifier = Modifier.size(64.dp).clip(RoundedCornerShape(8.dp))
                )
                Image(
                    painter = painterResource(Res.drawable.medal_bronze_prize_svgrepo_com),
                    contentDescription = "Bronze Badge",
                    modifier = Modifier.size(64.dp).clip(RoundedCornerShape(8.dp))
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun BadgeGridPreview() {
    MaterialTheme {
        Box(modifier = Modifier.padding(16.dp)) {
            BadgeGrid(onClick = {})
        }
    }
}
