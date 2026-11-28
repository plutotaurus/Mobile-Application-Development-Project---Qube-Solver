package metropolia.myapplication.profile

import androidx.compose.foundation.Canvas
import androidx.compose.ui.graphics.Color
import kotlin.math.roundToInt
import kotlinx.coroutines.flow.onStart
import metropolia.myapplication.rememberLegacyProgress
import metropolia.myapplication.data.local.seeders.seedStarterAlgorithms
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.key
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.catch
import metropolia.myapplication.data.local.rememberQubeDatabase
import metropolia.myapplication.timer.formatSolveTime
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import metropolia.myapplication.Badge
import metropolia.myapplication.BadgeGrid
import metropolia.myapplication.BadgeRarity
import metropolia.myapplication.ui.QubeButton
import metropolia.myapplication.ui.QubeTheme
import rubrickscube.shared.generated.resources.Res
import rubrickscube.shared.generated.resources.medal_bronze_prize_svgrepo_com
import rubrickscube.shared.generated.resources.medal_gold_winner_2_svgrepo_com
import rubrickscube.shared.generated.resources.medal_silver_badge_svgrepo_com

@Composable
fun ProfileScreen(
    onNavigateBack: () -> Unit = {},
    onNavigateToBadges: () -> Unit = {},
    isLoggedIn: Boolean = false,
    username: String = "Guest",
    onLogin: (String) -> Unit = {},
    onLogout: () -> Unit = {}
) {

    var showLoginDialog by remember { mutableStateOf(false) }
    var editRequested by remember { mutableStateOf(false) }
    var editAfterLogin by remember { mutableStateOf(false) }
    if (showLoginDialog) {
        LoginDialog(
            onDismiss = {
                showLoginDialog = false
                editAfterLogin = false
            },
            onLogin = { newUsername: String ->
                editRequested = editAfterLogin
                onLogin(newUsername)
                showLoginDialog = false
            }
        )
    }
    val database = rememberQubeDatabase()
    val account = username.takeIf { isLoggedIn }
    if (editRequested && account != null) {
        key(account) { EditProfileDialog(database.users(), account) { editRequested = false } }
    }
    val progressKey = account?.let { "user:$it" } ?: "guest"
    val legacyProgress = rememberLegacyProgress(progressKey)
    val profile by key(database, account) {
        remember(database, account, legacyProgress) {
            combine(database.users().observeProfileStats(account, progressKey),
                database.badges().observeProfileBadges(account)) { stats, badges ->
                Result.success(stats to badges)
            }.onStart {
                database.algorithms().seedStarterAlgorithms()
                database.algorithms().importLegacyProgress(progressKey, legacyProgress)
            }.catch { emit(Result.failure(it)) }
        }.collectAsState(initial = null)
    }
    val stats = profile?.getOrNull()?.first
    val earned = profile?.getOrNull()?.second.orEmpty()
    val profileBadges = remember(earned) {
        val catalog = listOf(
            Badge(
                "1",
                "Gold Solver",
                Res.drawable.medal_gold_winner_2_svgrepo_com,
                false,
                BadgeRarity.LEGENDARY
            ),
            Badge(
                "2",
                "Silver Solver",
                Res.drawable.medal_silver_badge_svgrepo_com,
                false,
                BadgeRarity.RARE
            ),
            Badge(
                "3",
                "Bronze Solver",
                Res.drawable.medal_bronze_prize_svgrepo_com,
                false,
                BadgeRarity.COMMON
            ),
            Badge("4", "Speedster", null, false, BadgeRarity.COMMON),
            Badge("5", "Master", null, false, BadgeRarity.COMMON),
            Badge("6", "Expert", null, false, BadgeRarity.COMMON),
            Badge("7", "Champion", null, false, BadgeRarity.COMMON),
            Badge("8", "Legend", null, false, BadgeRarity.LEGENDARY)
        )
        val unlocked = earned.map { record ->
            val template = catalog.find { it.name == record.title } ?: catalog.find { it.id == record.badgeId }
            template?.copy(id = record.badgeId, name = record.title, unlocked = true)
                ?: Badge(record.badgeId, record.title, null, true, BadgeRarity.COMMON)
        }
        unlocked + catalog.filter { badge -> unlocked.none { it.id == badge.id || it.name == badge.name } }
    }
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
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(onClick = {}) {
                        Text("3×3")
                    }

                    OutlinedButton(onClick = {
                        if (isLoggedIn) editRequested = true else {
                            editAfterLogin = true
                            showLoginDialog = true
                        }
                    }) { Text("Edit Profile") }

                    if (isLoggedIn) {
                        Button(
                            onClick = onLogout,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.errorContainer,
                                contentColor = MaterialTheme.colorScheme.onErrorContainer
                            )
                        ) {
                            Text("Log Out")
                        }
                    } else {
                        QubeButton("Log In", onClick = { showLoginDialog = true })
                    }
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
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 16.dp)
                )

                if (text == "Learning Progress" && stats != null) {
                    LearningProgressChart(stats.learning, stats.learned, stats.algorithmCount)
                } else {
                    val value = when {
                        profile == null -> "Loading…"
                        stats == null -> "Unavailable"
                        else -> when (text) {
                            "Solving Method" -> stats.solvingMethod ?: "Not set"
                            "Best Time" -> stats.bestTime?.let(::formatSolveTime) ?: "No recorded solves"
                            "Cube Brand" -> stats.cubeBrand ?: "Not set"
                            "Win Rate" -> if (stats.races == 0) "No ranked races" else
                                "${stats.wins * 100L / stats.races}% (${stats.wins} of ${stats.races})"
                            "Amount of Friends" -> stats.friends.toString()
                            else -> "${earned.size} earned"
                        }
                    }
                    Text(value, modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
                        style = MaterialTheme.typography.bodyLarge)
                }

                if (text == "Badges") {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp)
                    ) {
                        BadgeGrid(
                            badges = profileBadges,
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
                    val usernameSize = (14f + 4f * (1f - collapseFraction)).sp

                    Box(
                        modifier = Modifier
                            .size(avatarSize)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        val avatarSize = 40.dp + (100.dp * (1f - collapseFraction)) // shrinks from 140dp to 40dp
                        val fontSp = 20f + (12f * (1f - collapseFraction))
                        val textSize = fontSp.sp

                        Box(
                            modifier = Modifier
                                .size(avatarSize)
                                .clip(CircleShape)
                                .background(
                                    if (isLoggedIn) MaterialTheme.colorScheme.primaryContainer
                                    else MaterialTheme.colorScheme.surfaceVariant
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (isLoggedIn && username.isNotBlank()) username.take(1).uppercase() else "?",
                                fontSize = textSize,
                                fontWeight = FontWeight.Bold,
                                color = if (isLoggedIn) MaterialTheme.colorScheme.onPrimaryContainer
                                else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Spacer(Modifier.width(16.dp))

                    }
                    Spacer(Modifier.width(16.dp))
                    Text(
                        text = if (isLoggedIn) username else "Guest",
                        fontSize = usernameSize,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
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

private data class ProgressSlice(val label: String, val count: Int, val color: Color)

@Composable
private fun LearningProgressChart(learning: Int, learned: Int, total: Int) {
    val slices = listOf(
        ProgressSlice("Learning", learning, Color(0xFFE9B949)),
        ProgressSlice("Learned", learned, Color(0xFF56A36D)),
        ProgressSlice("Unlearned", total - learning - learned, Color(0xFF93979D)),
    )
    Column(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        if (total > 0) {
            Canvas(Modifier.size(144.dp).align(Alignment.CenterHorizontally)) {
                var start = -90f
                for (slice in slices) {
                    val sweep = 360f * slice.count / total
                    if (slice.count > 0) drawArc(slice.color, start, sweep, useCenter = true)
                    start += sweep
                }
            }
        } else {
            Text("No algorithms yet")
        }
        for (slice in slices) {
            val percent = if (total == 0) 0 else (100.0 * slice.count / total).roundToInt()
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Box(Modifier.size(12.dp).background(slice.color, CircleShape))
                Text("${slice.label}: ${slice.count} ($percent%)", style = MaterialTheme.typography.bodyMedium)
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
    QubeTheme {
        ProfileScreen()
    }
}
