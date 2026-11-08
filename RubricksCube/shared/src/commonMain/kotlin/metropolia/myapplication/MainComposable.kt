package metropolia.myapplication

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import org.jetbrains.compose.resources.StringResource
import rubrickscube.shared.generated.resources.Res
import rubrickscube.shared.generated.resources.camera
import rubrickscube.shared.generated.resources.main_menu

enum class AppScreen(val title: StringResource) {
    MainMenu(title = Res.string.main_menu),
    Camera(title = Res.string.camera)
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
                onNavigateToCamera = { navcontroller.navigate(AppScreen.Camera.name) }
            )
        }
        composable(AppScreen.Camera.name) {
            CameraScreen(
                onNavigateBack = { navcontroller.popBackStack() }
            )
        }
    }

}