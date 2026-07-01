package com.example.spotted.common

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.AlignmentLine
import androidx.navigation.NavController
import androidx.navigation.NavDestination.Companion.hasRoute // IMPORTANTE per il controllo type-safe
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.spotted.ui.theme.AppLogo
import com.example.spotted.utils.NavigationRoute
import com.example.spotted.utils.Sections


data class BarItem(
    val title: String,
    val icon: ImageVector,
    val route: Any
)

@Composable
fun SectionsBar(
    navController: NavController,
    modifier: Modifier = Modifier.fillMaxWidth()
) {

    val items = listOf(
        BarItem(Sections.FEED.name, Icons.Filled.Home, NavigationRoute.Feed),
        BarItem(Sections.FOLLOWING.name, Icons.Filled.Favorite, NavigationRoute.Following),
        BarItem(Sections.SHARE.name, Icons.Filled.Add, NavigationRoute.Share),
        BarItem(Sections.MAP.name, Icons.Filled.LocationOn, NavigationRoute.Map),

    )


    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    NavigationBar(
        modifier = modifier,
        containerColor = Color.Transparent
    ) {

        items.forEach { item ->

            val isSelected = currentDestination?.hasRoute(item.route::class) == true

            NavigationBarItem(
                selected = isSelected,
                onClick = {

                    if (!isSelected) {
                        navController.navigate(item.route) {

                            popUpTo(navController.graph.startDestinationId) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                },
                icon = { Icon(item.icon, contentDescription = item.title) },
                label = { Text(item.title) }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FloatingProfile(
    modifier: Modifier,
    onNavigateToProfile: () -> Unit,
    onNavigateBack: () -> Unit,
    show: Boolean
){
    CenterAlignedTopAppBar(
        title = {AppLogo(iconSize = 32, textSize = 20)},
        modifier,
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
            containerColor = Color.Transparent,
            actionIconContentColor = Color.Black,

        ),
        navigationIcon = {
            if (show){
                IconButton(onClick = onNavigateBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back_from_profile")
                }
            }

        },

        actions = {
                FilledIconButton(onClick =  onNavigateToProfile ) {
                    Icon(Icons.Filled.Person, Sections.PROFILE.name)
                }
        }
    )
}