package metropolia.myapplication

import androidx.compose.foundation.Image
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
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import rubrickscube.shared.generated.resources.Res
import rubrickscube.shared.generated.resources.medal_gold_winner_2_svgrepo_com
import rubrickscube.shared.generated.resources.medal_silver_badge_svgrepo_com
import rubrickscube.shared.generated.resources.medal_bronze_prize_svgrepo_com

@Composable
fun BadgeCard(
    badgeName: String,
    badgeDescription: String,
    badgeIcon: DrawableResource,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Medal / Badge Image
            Image(
                painter = painterResource(badgeIcon),
                contentDescription = badgeName,
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(8.dp))
            )

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = badgeName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = badgeDescription,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun BadgeCardPreview() {
    MaterialTheme {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            BadgeCard(
                badgeName = "Gold Solver",
                badgeDescription = "Solved a 3x3 cube in under 30 seconds",
                badgeIcon = Res.drawable.medal_gold_winner_2_svgrepo_com
            )
            BadgeCard(
                badgeName = "Silver Solver",
                badgeDescription = "Solved 50 cubes total",
                badgeIcon = Res.drawable.medal_silver_badge_svgrepo_com
            )
            BadgeCard(
                badgeName = "Bronze Solver",
                badgeDescription = "Completed your first CFOP solve",
                badgeIcon = Res.drawable.medal_bronze_prize_svgrepo_com
            )
        }
    }
}
