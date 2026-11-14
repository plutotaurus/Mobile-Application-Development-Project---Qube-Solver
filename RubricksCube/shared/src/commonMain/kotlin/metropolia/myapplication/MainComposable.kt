package metropolia.myapplication

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import org.jetbrains.compose.resources.StringResource
import rubrickscube.shared.generated.resources.Res
import rubrickscube.shared.generated.resources.algorithms
import rubrickscube.shared.generated.resources.badges
import rubrickscube.shared.generated.resources.camera
import rubrickscube.shared.generated.resources.main_menu
import rubrickscube.shared.generated.resources.profile
import rubrickscube.shared.generated.resources.racing
import rubrickscube.shared.generated.resources.timer

enum class AppScreen(val title: StringResource) {
    MainMenu(title = Res.string.main_menu),
    Camera(title = Res.string.camera),
    Racing(title = Res.string.racing),
    Timer(title = Res.string.timer),
    Algorithms(title = Res.string.algorithms),
    Profile(title = Res.string.profile),
    Badges(title = Res.string.badges)
}


@Composable
@Preview
fun QubeApp(
    navcontroller: NavHostController = rememberNavController()
) {
    NavHost(
        navController = navcontroller,
        startDestination = AppScreen.MainMenu.name
    ) {
        composable(AppScreen.MainMenu.name
        ) {
            MainMenuScreen(
                onNavigateToCamera = { navcontroller.navigate(AppScreen.Camera.name) },
                onNavigateToAlgorithms = { navcontroller.navigate(AppScreen.Algorithms.name) },
                onNavigateToTimer = { navcontroller.navigate(AppScreen.Timer.name) },
                onNavigateToRacing = { navcontroller.navigate(AppScreen.Racing.name) },
                onNavigateToProfile = { navcontroller.navigate(AppScreen.Profile.name) }
            )
        }
        composable(AppScreen.Camera.name) {
            CameraScreen(
                onNavigateBack = { navcontroller.popBackStack() }
            )
        }
        composable(AppScreen.Algorithms.name){
            AlgorithmsScreen (
                onNavigateBack = {navcontroller.popBackStack()}
            )
        }
        composable(AppScreen.Timer.name){
            TimerScreen (
                onNavigateBack = {navcontroller.popBackStack()}
            )
        }
        composable(AppScreen.Profile.name){
            ProfileScreen (
                onNavigateBack = {navcontroller.popBackStack()},
                onNavigateToBadges = { navcontroller.navigate(AppScreen.Badges.name) }
            )
        }
        composable(AppScreen.Badges.name) {
            BadgesScreen(
                onNavigateBack = { navcontroller.popBackStack() }
            )
        }
        composable(AppScreen.Racing.name){
            RacingScreen(
                onNavigateBack = {navcontroller.popBackStack()}
            )
        }
    }

}
