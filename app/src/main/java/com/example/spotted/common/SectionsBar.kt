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
import com.example.spotted.utils.NavigationRoute
import com.example.spotted.utils.Sections

// 1. Definiamo la struttura dati per le nostre schede della barra
data class BarItem(
    val title: String,
    val icon: ImageVector,
    val route: Any // Accetta le tue classi/oggetti di NavigationRoute
)

@Composable
fun SectionsBar(
    navController: NavController, // Passiamo direttamente il navController
    modifier: Modifier = Modifier.fillMaxWidth()
) {
    // 2. Creiamo la lista degli elementi associati alle rotte reali
    val items = listOf(
        BarItem(Sections.FEED.name, Icons.Filled.Home, NavigationRoute.Feed),
        BarItem(Sections.FOLLOWING.name, Icons.Filled.Favorite, NavigationRoute.Following),
        BarItem(Sections.SHARE.name, Icons.Filled.Add, NavigationRoute.Share),
        BarItem(Sections.MAP.name, Icons.Filled.LocationOn, NavigationRoute.Map),

    )

    // 3. Osserviamo la rotta corrente in modo reattivo
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    NavigationBar(
        modifier = modifier,
        containerColor = Color.Transparent
    ) {
        // 4. Iteriamo sulla lista per creare i NavigationBarItem
        items.forEach { item ->
            // Controlla dinamicamente se la destinazione corrente corrisponde alla rotta dell'item
            val isSelected = currentDestination?.hasRoute(item.route::class) == true

            NavigationBarItem(
                selected = isSelected,
                onClick = {
                    // Evita di ricaricare la pagina se siamo già lì
                    if (!isSelected) {
                        navController.navigate(item.route) {
                            // Riconduce la navigazione alla destinazione iniziale per non accumulare stack
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
        title = {},
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