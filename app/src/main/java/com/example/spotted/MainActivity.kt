package com.example.spotted

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.example.spotted.common.FloatingProfile
import com.example.spotted.common.SectionsBar
import com.example.spotted.data.di.appModule
import com.example.spotted.pages.ChatScreen
import com.example.spotted.pages.FollowingScreen
import com.example.spotted.pages.LoginScreen
import com.example.spotted.pages.MapScreen
import com.example.spotted.pages.NoGpsMap
import com.example.spotted.pages.ProfileScreen
import com.example.spotted.pages.ShareScreen
import com.example.spotted.pages.SignupScreen
import com.example.spotted.ui.screens.FeedScreen
import com.example.spotted.ui.theme.SpottedTheme
import com.example.spotted.ui.theme.ThemeManager
import com.example.spotted.ui.theme.ThemeMode
import com.example.spotted.utils.NavigationRoute
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.HazeStyle
import dev.chrisbanes.haze.HazeTint
import dev.chrisbanes.haze.haze
import dev.chrisbanes.haze.hazeChild
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.GlobalContext.startKoin

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()

        super.onCreate(savedInstanceState)
        ThemeManager.init(applicationContext)

        enableEdgeToEdge()
        setContent {
            val themeMode by ThemeManager.themeMode.collectAsState()

            val isDarkTheme = when (themeMode) {
                ThemeMode.LIGHT  -> false
                ThemeMode.DARK   -> true
                ThemeMode.SYSTEM -> isSystemInDarkTheme()
            }

            SpottedTheme(darkTheme = isDarkTheme) {
                val navController = rememberNavController()
                val hazeState = remember { HazeState() }

                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination

                val showBackButton =
                    (currentDestination?.hasRoute(NavigationRoute.Chat::class) == true) or
                            (currentDestination?.hasRoute(NavigationRoute.Profile::class) == true)

                val showNavigationBar =
                    (currentDestination?.hasRoute(NavigationRoute.Login::class) == true) or
                            (currentDestination?.hasRoute(NavigationRoute.Signup::class) == true) or
                            (currentDestination?.hasRoute(NavigationRoute.Chat::class) == true) or
                            (currentDestination?.hasRoute(NavigationRoute.Profile::class) == true)

                val showTopBar =
                    (currentDestination?.hasRoute(NavigationRoute.Login::class) == true) or
                            (currentDestination?.hasRoute(NavigationRoute.Signup::class) == true)

                val showChatInput =
                    (currentDestination?.hasRoute(NavigationRoute.Chat::class) == true)

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    bottomBar = {
                        if (!showNavigationBar) {
                            SectionsBar(
                                navController = navController,
                                modifier = Modifier.hazeChild(
                                    state = hazeState,
                                    style = HazeStyle(
                                        backgroundColor = MaterialTheme.colorScheme.surfaceContainer.copy(alpha = 0.9f),
                                        tint = HazeTint(Color.White.copy(alpha = 0.05f)),
                                        blurRadius = 15.dp,
                                    )
                                )
                            )
                        } else if (showChatInput) {
                            // ChatInput()
                        }
                    },
                    topBar = {
                        if (!showTopBar) {
                            FloatingProfile(
                                modifier = Modifier.hazeChild(
                                    state = hazeState,
                                    style = HazeStyle(
                                        backgroundColor = MaterialTheme.colorScheme.surfaceContainer.copy(alpha = 1.9f),
                                        tint = HazeTint(Color.White.copy(alpha = 0.05f)),
                                        blurRadius = 15.dp,
                                    )
                                ),
                                onNavigateToProfile = { navController.navigate(NavigationRoute.Profile) },
                                onNavigateBack = { navController.popBackStack() },
                                show = showBackButton
                            )
                        }
                    }
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .haze(state = hazeState)
                    ) {
                        NavGraph(navController, innerPadding)
                    }
                }
            }
        }
    }

    @Composable
    fun NavGraph(navController: NavHostController, innerPadding: PaddingValues) {
        NavHost(
            navController    = navController,
            startDestination = NavigationRoute.Login
        ) {
            composable<NavigationRoute.Feed> { FeedScreen(innerPadding) }

            composable<NavigationRoute.Map> {
                MapScreen(
                    innerPadding      = innerPadding,
                    navigate          = { chatId -> navController.navigate(NavigationRoute.Chat(chatId)) },
                    onPermissionDenied = { permanentlyDenied ->
                        navController.navigate(NavigationRoute.NoGpsMap(permanentlyDenied))
                    }
                )
            }

            composable<NavigationRoute.NoGpsMap> { backStackEntry ->
                val route: NavigationRoute.NoGpsMap = backStackEntry.toRoute()
                NoGpsMap(
                    permanentlyDenied    = route.permanentlyDenied,
                    onRequestPermission  = {
                        navController.navigate(NavigationRoute.Map) {
                            popUpTo(NavigationRoute.NoGpsMap(route.permanentlyDenied)) { inclusive = true }
                        }
                    }
                )
            }

            composable<NavigationRoute.Profile> {
                ProfileScreen(innerPadding, { navController.navigate(NavigationRoute.Login) })
            }
            composable<NavigationRoute.Following> {
                FollowingScreen(
                    innerPadding = innerPadding,
                    navigate     = { chatId -> navController.navigate(NavigationRoute.Chat(chatId = chatId)) }
                )
            }

            composable<NavigationRoute.Login> {
                LoginScreen(
                    innerPadding    = innerPadding,
                    navigate        = { navController.navigate(NavigationRoute.Feed) },
                    navigateToSignup = { navController.navigate(NavigationRoute.Signup) }
                )
            }

            composable<NavigationRoute.Signup> {
                SignupScreen(
                    innerPadding       = innerPadding,
                    onNavigateSuccess  = {
                        navController.navigate(NavigationRoute.Feed) {
                            popUpTo(NavigationRoute.Login) { inclusive = true }
                        }
                    },
                    onNavigateToLogin  = { navController.popBackStack() }
                )
            }

            composable<NavigationRoute.Chat> { backStackEntry ->
                val chatRoute: NavigationRoute.Chat = backStackEntry.toRoute()
                ChatScreen(postId = chatRoute.chatId.toLong(), innerPadding = innerPadding)
            }

            composable<NavigationRoute.Share> { ShareScreen(innerPadding) }
        }
    }
}