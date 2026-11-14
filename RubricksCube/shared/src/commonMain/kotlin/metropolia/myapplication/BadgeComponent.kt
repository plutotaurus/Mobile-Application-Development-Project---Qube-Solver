package metropolia.myapplication

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource


data class Badge(
    val id: String,
    val name: String,
    val imageRes: DrawableResource?,
    val unlocked: Boolean,
    val rarity: BadgeRarity
)

enum class BadgeRarity(
    val borderColor: Color
) {
    COMMON(Color.Gray),
    RARE(Color(0xFF2196F3)),
    EPIC(Color(0xFF9C27B0)),
    LEGENDARY(Color(0xFFFFC107))
}

@Composable
fun BadgeComponent(
    badge: Badge,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .aspectRatio(1f)
            .clickable(onClick = onClick),
        border = BorderStroke(
            width = 2.dp,
            color = badge.rarity.borderColor
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceEvenly
        ) {

            if (badge.imageRes != null) {
                Image(
                    painter = painterResource(badge.imageRes),
                    contentDescription = badge.name,
                    modifier = Modifier
                        .fillMaxWidth(0.6f)
                        .aspectRatio(1f),
                    alpha = if (badge.unlocked) 1f else 0.4f
                )
            } else {
                Text(
                    text = "🏆",
                    fontSize = 28.sp
                )
            }

            Text(
                text = if (badge.unlocked) badge.name else "???",
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.bodySmall,
                maxLines = 2
            )
        }
    }
}

@Composable
fun BadgeGrid(
    badges: List<Badge>,
    onBadgeClick: (Badge) -> Unit,
    onMoreClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val previewBadges = badges.take(7)

    LazyVerticalGrid(
        columns = GridCells.Fixed(4),
        modifier = modifier
            .fillMaxWidth()
            .height(240.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        userScrollEnabled = false
    ) {

        items(previewBadges) { badge ->
            BadgeComponent(
                badge = badge,
                onClick = {
                    onBadgeClick(badge)
                }
            )
        }

        item {
            MoreBadgeCard(
                onClick = onMoreClick
            )
        }
    }
}

@Composable
fun MoreBadgeCard(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .aspectRatio(1f)
            .clickable(onClick = onClick)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "➔",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(Modifier.height(4.dp))

            Text(
                text = "More",
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}
