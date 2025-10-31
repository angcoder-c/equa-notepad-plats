package com.example.equa_notepad_plats

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.example.equa_notepad_plats.Activities.*
import com.example.equa_notepad_plats.data.DatabaseProvider
import com.example.equa_notepad_plats.data.repositories.*
import com.example.equa_notepad_plats.view_models.*
import androidx.compose.ui.platform.LocalContext
import com.example.equa_notepad_plats.data.SupabaseClientProvider
import com.example.equa_notepad_plats.components.BottomNavigationBar

@Composable
fun AppNavHost(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
    startDestination: Any = LoginRoute
) {
    val context = LocalContext.current
    val database = DatabaseProvider.getDatabase(context)

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // Define routes that should show bottom navigation
    val routesWithBottomNav = setOf(
        HomeRoute::class.qualifiedName,
        PracticeRoute::class.qualifiedName,
        ProfileRoute::class.qualifiedName
    )

    val showBottomNav = currentRoute in routesWithBottomNav

    Scaffold(
        bottomBar = {
            if (showBottomNav) {
                BottomNavigationBar(
                    currentRoute = when (currentRoute) {
                        HomeRoute::class.qualifiedName -> "home"
                        PracticeRoute::class.qualifiedName -> "practice"
                        ProfileRoute::class.qualifiedName -> "profile"
                        else -> ""
                    },
                    onNavigate = { route ->
                        when (route) {
                            "home" -> navController.navigate(HomeRoute) {
                                popUpTo(HomeRoute) { inclusive = true }
                            }
                            "practice" -> navController.navigate(PracticeRoute(bookId = 1)) {
                                popUpTo(HomeRoute) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                            "profile" -> navController.navigate(ProfileRoute) {
                                popUpTo(HomeRoute) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    }
                )
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = modifier.padding(innerPadding)
        ) {
            composable<LoginRoute> {
                val repository = UserRepository(database)
                val viewModel = LoginViewModel(repository, SupabaseClientProvider.client)

                LoginScreen(
                    viewModel = viewModel,
                    onLoginSuccess = {
                        navController.navigate(HomeRoute) {
                            popUpTo(LoginRoute) { inclusive = true }
                        }
                    }
                )
            }

            // Home Screen
            composable<HomeRoute> {
                val repository = BookRepository(database)
                val viewModel = HomeViewModel(repository)

                HomeScreen(
                    viewModel = viewModel,
                    onBookClick = { bookId ->
                        navController.navigate(BookRoute(bookId))
                    },
                    onProfileClick = {
                        navController.navigate(ProfileRoute)
                    }
                )
            }

            // Book Screen
            composable<BookRoute> { backStackEntry ->
                val bookRoute: BookRoute = backStackEntry.toRoute()
                val repository = FormulaRepository(database)
                val repositoryBook = BookRepository(database)
                val viewModel = BookViewModel(repository, repositoryBook, bookRoute.bookId)

                BookScreen(
                    viewModel = viewModel,
                    onBackClick = {
                        navController.popBackStack()
                    },
                    onPracticeClick = {
                        navController.navigate(PracticeRoute(bookRoute.bookId))
                    },
                    onNewFormulaClick = {
                        navController.navigate(
                            FormulaDetailRoute(
                                formulaId = -1,
                                bookId = bookRoute.bookId
                            )
                        )
                    },
                    onFormulaClick = { formulaId ->
                        navController.navigate(
                            FormulaDetailRoute(
                                formulaId = formulaId,
                                bookId = bookRoute.bookId
                            )
                        )
                    }
                )
            }

            // Formula Detail Screen
            composable<FormulaDetailRoute> { backStackEntry ->
                val formulaRoute: FormulaDetailRoute = backStackEntry.toRoute()
                val repository = FormulaRepository(database)
                val viewModel = FormulaViewModel(
                    repository,
                    formulaRoute.bookId,
                    if (formulaRoute.formulaId != -1) formulaRoute.formulaId else null
                )

                FormulaScreen(
                    viewModel = viewModel,
                    onBackClick = {
                        navController.popBackStack()
                    },
                    onSaveSuccess = {
                        navController.popBackStack()
                    }
                )
            }

            // Profile Screen
            composable<ProfileRoute> {
                val repository = UserRepository(database)
                val viewModel = ProfileViewModel(repository)

                ProfileScreen(
                    viewModel = viewModel,
                    onBackClick = {
                        navController.popBackStack()
                    },
                    onLogoutSuccess = {
                        navController.navigate(LoginRoute) {
                            popUpTo(0) { inclusive = true }
                        }
                    },
                    onExerciseGeneratorClick = {
                        navController.navigate(ExerciseGeneratorRoute)
                    }
                )
            }

            // Practice Screen
            composable<PracticeRoute> { backStackEntry ->
                val practiceRoute: PracticeRoute = backStackEntry.toRoute()
                val repository = FormulaRepository(database)
                val repositoryBook = BookRepository(database)
                val viewModel = PracticeViewModel()

                PracticeScreen(
                    viewModel = viewModel,
                    bookId = practiceRoute.bookId.toString(),
                    onBackClick = {
                        navController.popBackStack()
                    }
                )
            }
        }
    }
}