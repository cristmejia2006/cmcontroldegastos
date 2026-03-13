package com.cristmejia2006.cmcontroldegastos

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.cristmejia2006.cmcontroldegastos.data.repository.SettingsRepository
import com.cristmejia2006.cmcontroldegastos.ui.home.AddTransactionScreen
import com.cristmejia2006.cmcontroldegastos.ui.home.HomeScreen
import com.cristmejia2006.cmcontroldegastos.ui.home.HomeViewModel
import com.cristmejia2006.cmcontroldegastos.ui.home.ReportsScreen
import com.cristmejia2006.cmcontroldegastos.ui.login.LoginScreen
import com.cristmejia2006.cmcontroldegastos.ui.login.RegisterScreen
import com.cristmejia2006.cmcontroldegastos.ui.theme.CMControlDeGastosTheme
import com.google.firebase.auth.FirebaseAuth
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        
        val settingsRepository = SettingsRepository(this)
        
        enableEdgeToEdge()
        setContent {
            val scope = rememberCoroutineScope()
            val isDarkModeSaved by settingsRepository.isDarkMode.collectAsState(initial = null)
            val systemDark = isSystemInDarkTheme()
            val darkMode = isDarkModeSaved ?: systemDark

            CMControlDeGastosTheme(darkTheme = darkMode) {
                AppNavigation(
                    isDarkMode = darkMode,
                    onThemeChange = { enabled ->
                        scope.launch { settingsRepository.setDarkMode(enabled) }
                    }
                )
            }
        }
    }
}

@Composable
fun AppNavigation(
    isDarkMode: Boolean,
    onThemeChange: (Boolean) -> Unit
) {
    val navController = rememberNavController()
    val auth = FirebaseAuth.getInstance()
    
    val startDestination = if (auth.currentUser != null) "main_content" else "login"

    NavHost(navController = navController, startDestination = startDestination) {
        composable("login") {
            LoginScreen(
                onLoginSuccess = { 
                    navController.navigate("main_content") {
                        popUpTo("login") { inclusive = true }
                    }
                },
                onNavigateToRegister = { navController.navigate("register") }
            )
        }
        composable("register") {
            RegisterScreen(
                onRegisterSuccess = { 
                    navController.navigate("main_content") {
                        popUpTo("register") { inclusive = true }
                    }
                },
                onNavigateToLogin = { navController.navigate("login") }
            )
        }
        composable("main_content") {
            MainContent(
                onLogout = {
                    navController.navigate("login") {
                        popUpTo("main_content") { inclusive = true }
                    }
                },
                onAddTransaction = { navController.navigate("add_transaction") },
                isDarkMode = isDarkMode,
                onThemeChange = onThemeChange
            )
        }
        composable("add_transaction") {
            AddTransactionScreen(
                onBack = { navController.popBackStack() }
            )
        }
    }
}

@Composable
fun MainContent(
    onLogout: () -> Unit,
    onAddTransaction: () -> Unit,
    isDarkMode: Boolean,
    onThemeChange: (Boolean) -> Unit,
    homeViewModel: HomeViewModel = viewModel()
) {
    val bottomNavController = rememberNavController()
    
    Scaffold(
        bottomBar = {
            NavigationBar {
                val navBackStackEntry by bottomNavController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination
                
                NavigationBarItem(
                    icon = { Icon(Icons.Default.AccountBalanceWallet, contentDescription = null) },
                    label = { Text("Inicio") },
                    selected = currentDestination?.hierarchy?.any { it.route == "home" } == true,
                    onClick = {
                        bottomNavController.navigate("home") {
                            popUpTo(bottomNavController.graph.findStartDestination().id) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.BarChart, contentDescription = null) },
                    label = { Text("Reportes") },
                    selected = currentDestination?.hierarchy?.any { it.route == "reports" } == true,
                    onClick = {
                        bottomNavController.navigate("reports") {
                            popUpTo(bottomNavController.graph.findStartDestination().id) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = bottomNavController, 
            startDestination = "home",
            modifier = androidx.compose.ui.Modifier.padding(innerPadding)
        ) {
            composable("home") {
                HomeScreen(
                    viewModel = homeViewModel,
                    onAddTransaction = onAddTransaction,
                    onLogout = onLogout,
                    isDarkMode = isDarkMode,
                    onThemeChange = onThemeChange
                )
            }
            composable("reports") {
                ReportsScreen(viewModel = homeViewModel)
            }
        }
    }
}
