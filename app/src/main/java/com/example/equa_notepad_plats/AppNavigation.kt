package com.example.equa_notepad_plats

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.example.equa_notepad_plats.Activities.BookScreen
import com.example.equa_notepad_plats.Activities.HomeScreen
import com.example.equa_notepad_plats.Activities.ProfileScreen
import com.example.equa_notepad_plats.components.BottomNavigationBar

@Composable
fun AppNavigation(
    navController: NavHostController = rememberNavController()
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val showBottomBar = remember(currentRoute) {
        currentRoute?.contains("Book") == false
    }

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                BottomNavigationBar(
                    currentRoute = when {
                        currentRoute?.contains("Home") == true -> "home"
                        currentRoute?.contains("Profile") == true -> "profile"
                        else -> "home"
                    },
                    onNavigate = { route ->
                        when (route) {
                            "home" -> {
                                navController.navigate(Route.Home) {
                                    popUpTo(Route.Home) { inclusive = true }
                                }
                            }
                            "profile" -> {
                                navController.navigate(Route.Profile) {
                                    popUpTo(Route.Home)
                                }
                            }
                        }
                    }
                )
            }
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = Route.Home,
            modifier = Modifier.padding(paddingValues)
        ) {
            composable<Route.Home> {
                HomeScreen(
                    onBookClick = { bookId ->
                        navController.navigate(Route.Book(bookId))
                    }
                )
            }

            composable<Route.Book> { backStackEntry ->
                val bookRoute: Route.Book = backStackEntry.toRoute()
                BookScreen(
                    bookId = bookRoute.bookId,
                    onNavigateBack = {
                        navController.navigateUp()
                    }
                )
            }

            composable<Route.Profile> {
                ProfileScreen()
            }
        }
    }
}