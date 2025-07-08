package com.example.politicalandroid.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.navigation.NavType
import com.example.politicalandroid.ui.screens.ArticleDetailScreen
import com.example.politicalandroid.ui.screens.DashboardScreen
import com.example.politicalandroid.ui.screens.HomeScreen
import com.example.politicalandroid.ui.screens.LoginScreen
import com.example.politicalandroid.ui.screens.ProfileScreen
import com.example.politicalandroid.viewmodel.AuthViewModel
import com.example.politicalandroid.viewmodel.ContactSubmissionsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val context = LocalContext.current
    val authViewModel: AuthViewModel = viewModel { AuthViewModel(context) }
    val contactSubmissionsViewModel: ContactSubmissionsViewModel = viewModel { ContactSubmissionsViewModel(context) }
    val authUiState by authViewModel.uiState.collectAsState()
    
    // Get current route
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    
    // Determine which screens to show in bottom nav based on auth state
    val screensToShow = remember(authUiState.isLoggedIn) {
        if (authUiState.isLoggedIn) {
            listOf(Screen.Home, Screen.Dashboard, Screen.Profile)
        } else {
            listOf(Screen.Home, Screen.Profile)
        }
    }
    
    // Check if current screen should show bottom nav
    val showBottomNav = currentDestination?.route in screensToShow.map { it.route }
    
    Scaffold(
        bottomBar = {
            if (showBottomNav) {
                NavigationBar {
                    screensToShow.forEach { screen ->
                        NavigationBarItem(
                            icon = {
                                Icon(
                                    imageVector = if (currentDestination?.hierarchy?.any { it.route == screen.route } == true) {
                                        screen.selectedIcon
                                    } else {
                                        screen.unselectedIcon
                                    },
                                    contentDescription = screen.title
                                )
                            },
                            label = { Text(screen.title) },
                            selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                            onClick = {
                                navController.navigate(screen.route) {
                                    // Pop up to the start destination of the graph to
                                    // avoid building up a large stack of destinations
                                    // on the back stack as users select items
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    // Avoid multiple copies of the same destination when
                                    // reselecting the same item
                                    launchSingleTop = true
                                    // Restore state when reselecting a previously selected item
                                    restoreState = true
                                }
                            }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Home.route) {
                HomeScreen(
                    onNavigateToLogin = { 
                        navController.navigate(Screen.Login.route)
                    },
                    onNavigateToDashboard = {
                        if (authUiState.isLoggedIn) {
                            navController.navigate(Screen.Dashboard.route)
                        } else {
                            navController.navigate(Screen.Login.route)
                        }
                    },
                    onNavigateToArticle = { articleId ->
                        navController.navigate(Screen.ArticleDetail.createRoute(articleId))
                    }
                )
            }
            
            composable(Screen.Login.route) {
                LoginScreen(
                    onNavigateBack = { 
                        navController.popBackStack() 
                    },
                    onLoginSuccess = { 
                        navController.navigate(Screen.Dashboard.route) {
                            popUpTo(Screen.Home.route) { inclusive = false }
                        }
                    },
                    viewModel = authViewModel
                )
            }
            
            composable(Screen.Dashboard.route) {
                // Check if user is still logged in
                if (authUiState.isLoggedIn) {
                    DashboardScreen(
                        onLogout = {
                            navController.navigate(Screen.Home.route) {
                                popUpTo(Screen.Dashboard.route) { inclusive = true }
                            }
                        },
                        viewModel = authViewModel
                    )
                } else {
                    // Redirect to login if not logged in
                    LaunchedEffect(Unit) {
                        navController.navigate(Screen.Login.route) {
                            popUpTo(Screen.Dashboard.route) { inclusive = true }
                        }
                    }
                }
            }
            
            composable(Screen.Profile.route) {
                ProfileScreen(
                    onNavigateToLogin = {
                        navController.navigate(Screen.Login.route)
                    },
                    onLogout = {
                        authViewModel.logout()
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Profile.route) { inclusive = false }
                        }
                    },
                    authViewModel = authViewModel,
                    contactSubmissionsViewModel = contactSubmissionsViewModel
                )
            }
            
            // Add ArticleDetail screen
            composable(
                route = Screen.ArticleDetail.route,
                arguments = listOf(navArgument("articleId") { type = NavType.IntType })
            ) { backStackEntry ->
                val articleId = backStackEntry.arguments?.getInt("articleId") ?: 0
                ArticleDetailScreen(
                    articleId = articleId,
                    onNavigateBack = {
                        navController.popBackStack()
                    },
                    authViewModel = authViewModel
                )
            }
        }
    }
}