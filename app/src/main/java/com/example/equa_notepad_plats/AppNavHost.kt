package com.example.equa_notepad_plats

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.example.equa_notepad_plats.Activities.*
import com.example.equa_notepad_plats.data.DatabaseProvider
import com.example.equa_notepad_plats.data.repositories.*
import com.example.equa_notepad_plats.view_models.*
import androidx.compose.ui.platform.LocalContext
import com.example.equa_notepad_plats.data.SupabaseClientProvider
import com.example.equa_notepad_plats.components.BottomNavigationBar
import android.util.Log
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.coroutines.flow.collectLatest

@Composable
fun AppNavHost(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
    startDestination: Any = LoginRoute
) {
    val context = LocalContext.current
    val database = DatabaseProvider.getDatabase(context)
    val userRepository = UserRepository(database)

    // Store current user info
    var currentUserId by remember { mutableStateOf("default_user_id") }
    var currentUserName by remember { mutableStateOf("Usuario") }
    var currentUserEmail by remember { mutableStateOf("usuario@ejemplo.com") }
    var currentUserPhotoUrl by remember { mutableStateOf<String?>(null) }
    var isGuest by remember { mutableStateOf(false) }
    var userLoaded by remember { mutableStateOf(false) }

    // Load user data initially and whenever we navigate
    LaunchedEffect(Unit) {
        // Load user data from repository
        val user = userRepository.getUser()
        if (user != null) {
            currentUserId = user.id
            currentUserName = user.name
            currentUserEmail = user.email
            currentUserPhotoUrl = user.photoUrl
            isGuest = user.isGuest
        }
        userLoaded = true
    }

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // Reload user data when returning to HomeRoute (after login)
    LaunchedEffect(currentRoute) {
        if (currentRoute == HomeRoute::class.qualifiedName) {
            val user = userRepository.getUser()
            if (user != null) {
                currentUserId = user.id
                currentUserName = user.name
                currentUserEmail = user.email
                currentUserPhotoUrl = user.photoUrl
                isGuest = user.isGuest
            }
        }
    }

    // Define routes that should show bottom navigation
    val routesWithBottomNav = setOf(
        HomeRoute::class.qualifiedName,
        PracticeRoute::class.qualifiedName,
        ProfileRoute::class.qualifiedName
    )

    val showBottomNav = routesWithBottomNav.any { route ->
        currentRoute?.startsWith(route.toString()) == true
    }

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
                            "practice" -> navController.navigate(PracticeRoute(bookId = 0)) {
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
                    },
                    currentUserId = currentUserId,
                    currentUserName = currentUserName,
                    currentUserEmail = currentUserEmail,
                    currentUserPhotoUrl = currentUserPhotoUrl,
                    isGuest = isGuest
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
                    },
                    currentUserId = currentUserId,
                    isGuest = isGuest
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
                    },
                    currentUserId = currentUserId,
                    currentUserName = currentUserName,
                    currentUserEmail = currentUserEmail,
                    currentUserPhotoUrl = currentUserPhotoUrl,
                    isGuest = isGuest
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
                    }
                )
            }

            // Practice Screen
            composable<PracticeRoute> { backStackEntry ->
                val practiceRoute: PracticeRoute = backStackEntry.toRoute()
                val repository = FormulaRepository(database)
                val viewModel = PracticeViewModel(
                    repositoryFormula = repository,
                )

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