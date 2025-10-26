package com.example.equa_notepad_plats

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.example.equa_notepad_plats.Activities.*
import com.example.equa_notepad_plats.data.DatabaseProvider
import com.example.equa_notepad_plats.data.repositories.*
import com.example.equa_notepad_plats.view_models.*
import androidx.compose.ui.platform.LocalContext

@Composable
fun AppNavHost(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
    startDestination: Any = LoginRoute
) {
    val context = LocalContext.current
    val database = DatabaseProvider.getDatabase(context)

    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {
        composable<LoginRoute> {
            val repository = UserRepository(database)
            val viewModel = LoginViewModel(repository)

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
            val viewModel = BookViewModel(repository, repositoryBook,bookRoute.bookId)

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