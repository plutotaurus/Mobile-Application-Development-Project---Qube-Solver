package metropolia.myapplication

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import rubrickscube.shared.generated.resources.Res
import rubrickscube.shared.generated.resources.medal_gold_winner_2_svgrepo_com
import rubrickscube.shared.generated.resources.medal_silver_badge_svgrepo_com
import rubrickscube.shared.generated.resources.medal_bronze_prize_svgrepo_com

@Composable
fun ProfileScreen(
    onNavigateBack: () -> Unit = {},
    onNavigateToBadges: () -> Unit = {}
) {
    val lazyListState = rememberLazyListState()

    // Calculate scroll offset for collapsing header
    val scrollOffset = if (lazyListState.firstVisibleItemIndex == 0) {
        lazyListState.firstVisibleItemScrollOffset.toFloat()
    } else {
        Float.MAX_VALUE // fully collapsed when scrolled past header
    }

    val maxHeaderHeight = 220.dp
    val minHeaderHeight = 64.dp
    val scrollRange = 150f // pixels over which it collapses
    val collapseFraction = (scrollOffset / scrollRange).coerceIn(0f, 1f)

    val currentHeaderHeight = minHeaderHeight + (maxHeaderHeight - minHeaderHeight) * (1f - collapseFraction)

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        // Main scrollable content
        LazyColumn(
            state = lazyListState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(top = maxHeaderHeight),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            item {
                Button(onClick = {}) {
                    Text("3×3")
                }
                Spacer(Modifier.height(16.dp))
            }

            items(
                listOf(
                    "Solving Method",
                    "Best Time",
                    "Cube Brand",
                    "Learning Progress",
                    "Win Rate",
                    "Amount of Friends",
                    "Badges"
                )
            ) { text ->
                Text(
                    text = text,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 16.dp)
                )

                if (text == "Badges") {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp)
                    ) {
                        BadgeGrid(
                            badges = listOf(
                                Badge("1", "Gold Solver", Res.drawable.medal_gold_winner_2_svgrepo_com, true, BadgeRarity.LEGENDARY),
                                Badge("2", "Silver Solver", Res.drawable.medal_silver_badge_svgrepo_com, true, BadgeRarity.RARE),
                                Badge("3", "Bronze Solver", Res.drawable.medal_bronze_prize_svgrepo_com, true, BadgeRarity.COMMON),
                                Badge("4", "Speedster", null, false, BadgeRarity.COMMON),
                                Badge("5", "Master", null, false, BadgeRarity.COMMON),
                                Badge("6", "Expert", null, false, BadgeRarity.COMMON),
                                Badge("7", "Champion", null, false, BadgeRarity.COMMON),
                                Badge("8", "Legend", null, false, BadgeRarity.LEGENDARY)
                            ),
                            onBadgeClick = {},
                            onMoreClick = onNavigateToBadges
                        )
                    }
                    Spacer(Modifier.height(16.dp))
                }

                Spacer(Modifier.height(28.dp))
            }
        }

        // Custom Collapsing Header
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(currentHeaderHeight),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = if (collapseFraction > 0.5f) 4.dp else 0.dp
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val avatarSize = 40.dp + (100.dp * (1f - collapseFraction)) // shrinks from 140dp to 40dp
                    val usernameSize = (20f + 12f * (1f - collapseFraction)).sp
                    val fontSp = 20f + (12f * (1f - collapseFraction))
                    val textSize = fontSp.sp

                    Box(
                        modifier = Modifier
                            .size(avatarSize)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "U",
                            fontSize = textSize,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                    Spacer(Modifier.width(16.dp))
                    Text(
                        text = "Username",
                        fontSize = usernameSize,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // Back button overlay (Bottom right)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            contentAlignment = Alignment.BottomEnd
        ) {
            FloatingActionButton(
                onClick = onNavigateBack
            ) {
                Text("Back")
            }
        }
    }
}

@Preview(
    showBackground = true,
    showSystemUi = true
)
@Composable
fun ProfileScreenPreview() {
    MaterialTheme {
        ProfileScreen()
    }
}
