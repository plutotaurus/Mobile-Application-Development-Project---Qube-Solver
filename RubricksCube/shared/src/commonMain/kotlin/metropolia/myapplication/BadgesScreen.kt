package metropolia.myapplication

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import rubrickscube.shared.generated.resources.Res
import rubrickscube.shared.generated.resources.medal_bronze_prize_svgrepo_com
import rubrickscube.shared.generated.resources.medal_gold_winner_2_svgrepo_com
import rubrickscube.shared.generated.resources.medal_silver_badge_svgrepo_com

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BadgesScreen(
    onNavigateBack: () -> Unit
) {
    val allBadges = listOf(
        Badge("1", "Gold Solver", Res.drawable.medal_gold_winner_2_svgrepo_com, true, BadgeRarity.LEGENDARY),
        Badge("2", "Silver Solver", Res.drawable.medal_silver_badge_svgrepo_com, true, BadgeRarity.RARE),
        Badge("3", "Bronze Solver", Res.drawable.medal_bronze_prize_svgrepo_com, true, BadgeRarity.COMMON),
        Badge("4", "Speedster", Res.drawable.medal_gold_winner_2_svgrepo_com, false, BadgeRarity.COMMON),
        Badge("5", "Master", Res.drawable.medal_silver_badge_svgrepo_com, false, BadgeRarity.COMMON),
        Badge("6", "Expert", Res.drawable.medal_bronze_prize_svgrepo_com, false, BadgeRarity.COMMON),
        Badge("7", "Champion", Res.drawable.medal_gold_winner_2_svgrepo_com, false, BadgeRarity.COMMON),
        Badge("8", "Legend", Res.drawable.medal_silver_badge_svgrepo_com, false, BadgeRarity.LEGENDARY)
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("All Badges") },
                navigationIcon = {
                    TextButton(onClick = onNavigateBack) {
                        Text("Back")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(allBadges) { badge ->
                BadgeCard(
                    badgeName = "Badge Name",
                    badgeDescription = "Description of the Badge",
                    badgeIcon = badge.imageRes ?: Res.drawable.medal_gold_winner_2_svgrepo_com
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun BadgesScreenPreview() {
    MaterialTheme {
        BadgesScreen(onNavigateBack = {})
    }
}
