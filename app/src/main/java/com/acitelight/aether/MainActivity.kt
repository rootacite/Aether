package com.acitelight.aether

import android.app.Activity
import androidx.compose.material.icons.Icons
import android.graphics.drawable.Icon
import android.net.http.SslCertificate.saveState
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.automirrored.filled.CompareArrows
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButtonDefaults.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import com.acitelight.aether.ui.theme.AetherTheme
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.acitelight.aether.view.ComicScreen
import com.acitelight.aether.view.HomeScreen
import com.acitelight.aether.view.MeScreen
import com.acitelight.aether.view.VideoPlayer
import com.acitelight.aether.view.VideoScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContent {
            AetherTheme {
                AppNavigation()
            }
        }
    }
}


@Composable
fun ToggleFullScreen(isFullScreen: Boolean)
{
    val view = LocalView.current

    LaunchedEffect(isFullScreen) {
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
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val hideBottomBarRoutes = listOf(
        Screen.VideoPlayer.route,
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
            if(shouldShowBottomBar)
                ToggleFullScreen(false)
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = if(shouldShowBottomBar)Modifier.padding(innerPadding) else Modifier.padding(0.dp)
        ) {
            composable(Screen.Home.route) {
                HomeScreen()
            }
            composable(Screen.Video.route) {
                VideoScreen(navController = navController)
            }
            composable(Screen.Comic.route) {
                ComicScreen()
            }

            composable(Screen.Transmission.route) {
                // ComicScreen()
            }
            composable(Screen.Me.route) {
                MeScreen();
            }

            composable(
                route = Screen.VideoPlayer.route,
                arguments = listOf(navArgument("videoId") { type = NavType.StringType })
            ) {
                backStackEntry ->
                val videoId = backStackEntry.arguments?.getString("videoId")
                if (videoId != null) {
                    VideoPlayer(videoId = videoId, navController = navController)
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
        Screen.Home,
        Screen.Video,
        Screen.Comic,
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

sealed class Screen(val route: String, val icon: ImageVector, val title: String) {
    data object Home : Screen("home_route", Icons.Filled.Home, "Home")
    data object Video : Screen("video_route", Icons.Filled.VideoLibrary, "Video")
    data object Comic : Screen("comic_route", Icons.Filled.Image, "Comic")
    data object Transmission : Screen("transmission_route",
        Icons.AutoMirrored.Filled.CompareArrows, "Transmission")
    data object Me : Screen("me_route", Icons.Filled.AccountCircle, "me")
    data object VideoPlayer : Screen("video_player_route/{videoId}", Icons.Filled.PlayArrow, "VideoPlayer")
}