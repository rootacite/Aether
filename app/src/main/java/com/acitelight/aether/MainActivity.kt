package com.acitelight.aether

import android.app.Activity
import android.content.Intent
import androidx.compose.material.icons.Icons
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.automirrored.filled.CompareArrows
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import com.acitelight.aether.ui.theme.AetherTheme
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.acitelight.aether.view.pages.ComicGridView
import com.acitelight.aether.view.pages.ComicPageView
import com.acitelight.aether.view.pages.ComicScreen
import com.acitelight.aether.view.pages.HomeScreen
import com.acitelight.aether.view.pages.MeScreen
import com.acitelight.aether.view.pages.TransmissionScreen
import com.acitelight.aether.view.pages.VideoPlayer
import com.acitelight.aether.view.pages.VideoScreen
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val app = application as AetherApp

        lifecycleScope.launch {
            app.isServiceInitialized?.filter { it }?.first()
            val intent = Intent(this@MainActivity, MainScreenActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
}

@AndroidEntryPoint
class MainScreenActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.attributes = window.attributes.apply {
            screenBrightness = WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_NONE
        }
        enableEdgeToEdge()
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContent {
            AetherTheme {
                AppNavigation()
            }
        }
    }
}


fun setFullScreen(view: View, isFullScreen: Boolean) {
    Global.isFullScreen = isFullScreen
    val window = (view.context as Activity).window
    val insetsController = WindowCompat.getInsetsController(window, view)

    if (isFullScreen) {
        insetsController.hide(WindowInsetsCompat.Type.systemBars())
        insetsController.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
    } else {
        insetsController.show(WindowInsetsCompat.Type.systemBars())
    }
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val hideBottomBarRoutes = listOf(
        Screen.VideoPlayer.route,
        Screen.ComicGrid.route,
        Screen.ComicPage.route
    )
    val shouldShowBottomBar = currentRoute !in hideBottomBarRoutes

    Scaffold(
        bottomBar = {
            AnimatedVisibility(
                visible = shouldShowBottomBar,
                enter = slideInVertically(initialOffsetY = { fullHeight -> fullHeight }),
                exit = slideOutVertically(targetOffsetY = { fullHeight -> fullHeight })
            ) {
                BottomNavigationBar(navController = navController)
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Me.route,
            modifier = if(!Global.isFullScreen) Modifier.padding(innerPadding) else Modifier.padding(0.dp)
        ) {
            composable(
                Screen.Home.route,
                enterTransition = { slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Start, animationSpec = tween(200)) },
                exitTransition = { slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Start, animationSpec = tween(200)) },
                popEnterTransition = { slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.End, animationSpec = tween(200)) },
                popExitTransition = { slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.End, animationSpec = tween(200)) }
            ) {
                CardPage(title = "Home") {
                    HomeScreen(navController = navController)
                }
            }
            composable(Screen.Video.route,
                enterTransition = { slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Start, animationSpec = tween(200)) },
                exitTransition = { slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Start, animationSpec = tween(200)) },
                popEnterTransition = { slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.End, animationSpec = tween(200)) },
                popExitTransition = { slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.End, animationSpec = tween(200)) }) {
                VideoScreen(navController = navController)
            }
            composable(Screen.Comic.route,
                enterTransition = { slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Start, animationSpec = tween(200)) },
                exitTransition = { slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Start, animationSpec = tween(200)) },
                popEnterTransition = { slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.End, animationSpec = tween(200)) },
                popExitTransition = { slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.End, animationSpec = tween(200)) }) {
                CardPage(title = "Comic") {
                    ComicScreen(navController = navController)
                }
            }

            composable(Screen.Transmission.route,
                enterTransition = { slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Start, animationSpec = tween(200)) },
                exitTransition = { slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Start, animationSpec = tween(200)) },
                popEnterTransition = { slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.End, animationSpec = tween(200)) },
                popExitTransition = { slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.End, animationSpec = tween(200)) }) {
                CardPage(title = "Tasks") {
                    TransmissionScreen(navigator = navController)
                }
            }
            composable(Screen.Me.route,
                enterTransition = { slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Start, animationSpec = tween(200)) },
                exitTransition = { slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Start, animationSpec = tween(200)) },
                popEnterTransition = { slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.End, animationSpec = tween(200)) },
                popExitTransition = { slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.End, animationSpec = tween(200)) }) {
                MeScreen();
            }

            composable(
                route = Screen.VideoPlayer.route,
                enterTransition = { slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Start, animationSpec = tween(200)) },
                exitTransition = { slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Start, animationSpec = tween(200)) },
                popEnterTransition = { slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.End, animationSpec = tween(200)) },
                popExitTransition = { slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.End, animationSpec = tween(200)) },
                arguments = listOf(navArgument("videoId") { type = NavType.StringType })
            ) {
                backStackEntry ->
                val videoId = backStackEntry.arguments?.getString("videoId")
                if (videoId != null) {
                    VideoPlayer(videoId = videoId, navController = navController)
                }
            }

            composable(
                route = Screen.ComicGrid.route,
                enterTransition = { slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Start, animationSpec = tween(200)) },
                exitTransition = { slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Start, animationSpec = tween(200)) },
                popEnterTransition = { slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.End, animationSpec = tween(200)) },
                popExitTransition = { slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.End, animationSpec = tween(200)) },
                arguments = listOf(navArgument("comicId") { type = NavType.StringType })
            ) {
                    backStackEntry ->
                val comicId = backStackEntry.arguments?.getString("comicId")
                if (comicId != null) {
                    ComicGridView(comicId = comicId, navController = navController)
                }
            }

            composable(
                route = Screen.ComicPage.route,
                enterTransition = { slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Start, animationSpec = tween(200)) },
                exitTransition = { slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Start, animationSpec = tween(200)) },
                popEnterTransition = { slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.End, animationSpec = tween(200)) },
                popExitTransition = { slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.End, animationSpec = tween(200)) },
                arguments = listOf(navArgument("comicId") { type = NavType.StringType }, navArgument("page") { type = NavType.StringType })
            ) {
                    backStackEntry ->
                val comicId = backStackEntry.arguments?.getString("comicId")
                val page = backStackEntry.arguments?.getString("page")
                if (comicId != null && page != null) {
                    ComicPageView(comicId = comicId, page = page, navController = navController)
                }
            }
        }
    }
}

@Composable
fun BottomNavigationBar(navController: NavController) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val items = if(Global.loggedIn) listOf(
        Screen.Home,
        Screen.Video,
        Screen.Comic,
        Screen.Transmission,
        Screen.Me
    ) else  listOf(
        Screen.Video,
        Screen.Transmission,
        Screen.Me
    )

    NavigationBar( modifier = Modifier.height(60.dp)) {
        items.forEach { screen ->
            NavigationBarItem(
                icon = { Icon(imageVector = screen.icon, contentDescription = null) },
                selected = currentRoute == screen.route,
                onClick = {
                    navController.navigate(screen.route) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                modifier = Modifier.padding(vertical = 2.dp).height(25.dp)
            )
        }
    }
}

@Composable
fun CardPage(
    title: String,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Box(Modifier.background(if (isSystemInDarkTheme()) {
        Color.Black
    } else {
        Color.White
    }).fillMaxSize())
    {
        val colorScheme = MaterialTheme.colorScheme
        Card(
            modifier = Modifier
                .fillMaxSize()
                .padding(6.dp),
            elevation = CardDefaults.cardElevation(4.dp),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = colorScheme.background)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
            ) {
                content()
            }
        }
    }
}

sealed class Screen(val route: String, val icon: ImageVector, val title: String) {
    data object Home : Screen("home_route", Icons.Filled.Home, "Home")
    data object Video : Screen("video_route", Icons.Filled.VideoLibrary, "Video")
    data object Comic : Screen("comic_route", Icons.Filled.Image, "Comic")
    data object Transmission : Screen("transmission_route",
        Icons.AutoMirrored.Filled.CompareArrows, "Transmission")
    data object Me : Screen("me_route", Icons.Filled.AccountCircle, "me")
    data object VideoPlayer : Screen("video_player_route/{videoId}", Icons.Filled.PlayArrow, "VideoPlayer")
    data object ComicGrid : Screen("comic_grid_route/{comicId}", Icons.Filled.PlayArrow, "ComicGrid")
    data object ComicPage : Screen("comic_page_route/{comicId}/{page}", Icons.Filled.PlayArrow, "ComicPage")
}