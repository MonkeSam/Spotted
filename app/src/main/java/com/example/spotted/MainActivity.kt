package com.example.spotted

import FeedScreen
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.spotted.common.FloatingProfile
import com.example.spotted.common.SectionsBar
import com.example.spotted.pages.FollowingScreen
import com.example.spotted.pages.MapScreen
import com.example.spotted.pages.ProfileScreen
import com.example.spotted.ui.theme.SpottedTheme
import com.example.spotted.utils.NavigationRoute

import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import dev.chrisbanes.haze.HazeDefaults
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.haze
import dev.chrisbanes.haze.hazeChild


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {

        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SpottedTheme {
                val navController = rememberNavController()

                // 1. CORREZIONE: Inizializziamo lo stato di Haze che prima mancava
                val hazeState = remember { HazeState() }

                // Osserva la destinazione corrente direttamente nella MainActivity
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination

                // Controlla se NON siamo nella pagina Profilo
                val showBottomBar = currentDestination?.hasRoute(NavigationRoute.Profile::class) == false

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    bottomBar = {
                        SectionsBar(
                            navController = navController,
                            modifier = Modifier.hazeChild(
                                state = hazeState,
                                style = HazeDefaults.style(
                                    backgroundColor = MaterialTheme.colorScheme.surfaceContainer.copy(alpha = 0.20f),
                                    blurRadius = 20.dp,
                                    noiseFactor = 0.1f,
                                    tint = Color.White.copy(alpha = 0.1f)  // ✅ Color, non lista
                                )
                            )
                        )
                    },
                    topBar = {
                        FloatingProfile(
                            modifier = Modifier.hazeChild(
                                state = hazeState,
                                style = HazeDefaults.style(
                                    backgroundColor = MaterialTheme.colorScheme.surfaceContainer.copy(alpha = 0.20f),
                                    blurRadius = 20.dp
                                    // tint omesso → usa il default della seconda overload (HazeTint)
                                    // più pulito di passare Color.Unspecified
                                )
                            ),
                            onNavigateToProfile = { navController.navigate(NavigationRoute.Profile) },
                            onNavigateBack = { navController.popBackStack() },
                            show = showBottomBar
                        )
                    }
                ) { innerPadding ->
                    // Ignoriamo 'innerPadding' per fare andare il NavGraph Edge-to-Edge (sotto le barre)
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .haze(state = hazeState) // Registriamo questo Box come sfondo da sfocare
                    ) {
                        NavGraph(navController,innerPadding)
                    }
                }
            }
        }
    }
}

@Composable
fun NavGraph(navController : NavHostController, innerPadding: PaddingValues){
    NavHost(
        navController = navController,
        startDestination = NavigationRoute.Feed
    ){
        composable<NavigationRoute.Feed> { FeedScreen(innerPadding)  }
        composable<NavigationRoute.Map> { MapScreen(innerPadding) }
        composable<NavigationRoute.Profile> { ProfileScreen(innerPadding) }
        composable<NavigationRoute.Following> { FollowingScreen(innerPadding) }
    }
}