package com.one.utility

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.one.utility.core.data.PreferencesManager
import com.one.utility.core.designsystem.ONETheme
import com.one.utility.core.designsystem.components.FloatingDock
import com.one.utility.core.designsystem.components.NavigationTab
import com.one.utility.feature.backgroundremover.BackgroundRemoverScreen
import com.one.utility.feature.calculator.CalculatorMode
import com.one.utility.feature.calculator.CalculatorScreen
import com.one.utility.feature.calculator.EverydayCalculatorsScreen
import com.one.utility.feature.calculator.TactileCalculatorScreen
import com.one.utility.feature.calculator.UnitConverterScreen
import com.one.utility.feature.compressor.CompressorScreen
import com.one.utility.feature.converter.ImageConverterScreen
import com.one.utility.feature.cropper.ImageCropperScreen
import com.one.utility.feature.currency.CurrencyAndTimeScreen
import com.one.utility.feature.developer.DeveloperToolsScreen
import com.one.utility.feature.files.BatchRenameScreen
import com.one.utility.feature.fonts.CoolFontsScreen
import com.one.utility.feature.home.HomeScreen
import com.one.utility.feature.imagetopdf.ImageToPdfScreen
import com.one.utility.feature.pdf.PdfToolboxScreen
import com.one.utility.feature.privacy.PasswordGeneratorScreen
import com.one.utility.feature.qr.QrScreen
import com.one.utility.feature.scanner.DocumentScannerScreen
import com.one.utility.feature.settings.SettingsScreen
import com.one.utility.feature.storage.StorageCleanerScreen
import com.one.utility.feature.text.TextToolsScreen
import com.one.utility.feature.tools.ToolsListScreen
import com.one.utility.feature.workflow.ChainedWorkflowScreen
import com.one.utility.core.designsystem.ScreenTransitions
import com.one.utility.feature.tools.DedicatedToolScreen
import java.net.URLDecoder
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val context = LocalContext.current
            val prefs = remember { PreferencesManager(context) }
            var currentThemeMode by remember { mutableStateOf(prefs.themeMode) }

            ONETheme(themeMode = currentThemeMode) {
                OneAppNavigation(
                    onThemeChanged = { newTheme ->
                        currentThemeMode = newTheme
                    }
                )
            }
        }
    }
}

@Composable
fun OneAppNavigation(
    onThemeChanged: (String) -> Unit
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: "home"

    // Primary top-level destinations where the frosted glass dock remains visible
    val isMainTab = currentRoute == "home" ||
            currentRoute == "tools_list" ||
            currentRoute.startsWith("calculator") ||
            currentRoute == "settings"

    Box(modifier = Modifier.fillMaxSize()) {
        NavHost(
            navController = navController,
            startDestination = "home",
            modifier = Modifier.fillMaxSize(),
            enterTransition = { ScreenTransitions.enterTransition },
            exitTransition = { ScreenTransitions.exitTransition },
            popEnterTransition = { ScreenTransitions.popEnterTransition },
            popExitTransition = { ScreenTransitions.popExitTransition }
        ) {
            composable("home") {
                HomeScreen(
                    onNavigateToTool = { route -> navController.navigate(route) },
                    onNavigateToChainedWorkflow = { stepNames ->
                        val encoded = stepNames.joinToString(",")
                        navController.navigate("workflow/$encoded")
                    },
                    onNavigateToCalcWithExpression = { expr ->
                        val encoded = URLEncoder.encode(expr, StandardCharsets.UTF_8.toString())
                        navController.navigate("calculator?expr=$encoded")
                    }
                )
            }

            composable("cool_fonts") {
                CoolFontsScreen(onNavigateBack = { navController.popBackStack() })
            }

            composable("image_to_pdf") {
                ImageToPdfScreen(onNavigateBack = { navController.popBackStack() })
            }

            composable("compressor") {
                CompressorScreen(onNavigateBack = { navController.popBackStack() })
            }

            composable(
                route = "calculator?expr={expr}",
                arguments = listOf(navArgument("expr") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                })
            ) { backStackEntry ->
                val exprParam = backStackEntry.arguments?.getString("expr")?.let {
                    URLDecoder.decode(it, StandardCharsets.UTF_8.toString())
                }
                TactileCalculatorScreen(
                    initialExpression = exprParam,
                    onNavigateBack = {
                        if (navController.previousBackStackEntry != null) {
                            navController.popBackStack()
                        } else {
                            navController.navigate("home")
                        }
                    }
                )
            }

            composable("calculator") {
                TactileCalculatorScreen(
                    onNavigateBack = {
                        if (navController.previousBackStackEntry != null) {
                            navController.popBackStack()
                        } else {
                            navController.navigate("home")
                        }
                    }
                )
            }

            composable("unit_converter") {
                UnitConverterScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable("background_remover") {
                BackgroundRemoverScreen(onNavigateBack = { navController.popBackStack() })
            }

            composable("qr") {
                QrScreen(onNavigateBack = { navController.popBackStack() })
            }

            composable("settings") {
                SettingsScreen(
                    onNavigateBack = {
                        if (navController.previousBackStackEntry != null) {
                            navController.popBackStack()
                        } else {
                            navController.navigate("home")
                        }
                    },
                    onThemeChanged = onThemeChanged
                )
            }

            composable("pdf_toolbox") {
                PdfToolboxScreen(onNavigateBack = { navController.popBackStack() })
            }

            composable("text_tools") {
                TextToolsScreen(onNavigateBack = { navController.popBackStack() })
            }

            composable("dev_tools") {
                DeveloperToolsScreen(onNavigateBack = { navController.popBackStack() })
            }

            composable("password_generator") {
                PasswordGeneratorScreen(onNavigateBack = { navController.popBackStack() })
            }

            composable("storage_cleaner") {
                StorageCleanerScreen(onNavigateBack = { navController.popBackStack() })
            }

            composable("batch_rename") {
                BatchRenameScreen(onNavigateBack = { navController.popBackStack() })
            }

            composable("image_cropper") {
                ImageCropperScreen(onNavigateBack = { navController.popBackStack() })
            }

            composable("resizer") {
                ImageCropperScreen(onNavigateBack = { navController.popBackStack() })
            }

            composable("document_scanner") {
                DocumentScannerScreen(onNavigateBack = { navController.popBackStack() })
            }

            composable("everyday_calculators") {
                EverydayCalculatorsScreen(onNavigateBack = { navController.popBackStack() })
            }

            composable("image_converter") {
                ImageConverterScreen(onNavigateBack = { navController.popBackStack() })
            }

            composable("currency_time") {
                CurrencyAndTimeScreen(onNavigateBack = { navController.popBackStack() })
            }

            composable("tools_list") {
                ToolsListScreen(
                    onNavigateToTool = { route -> navController.navigate(route) },
                    onNavigateBack = {
                        if (navController.previousBackStackEntry != null) {
                            navController.popBackStack()
                        } else {
                            navController.navigate("home")
                        }
                    }
                )
            }

            composable(
                route = "workflow/{steps}",
                arguments = listOf(navArgument("steps") { type = NavType.StringType })
            ) { backStackEntry ->
                val stepsStr = backStackEntry.arguments?.getString("steps") ?: ""
                val stepNames = stepsStr.split(",").filter { it.isNotBlank() }
                ChainedWorkflowScreen(
                    stepNames = stepNames,
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable(
                route = "tool/{toolId}",
                arguments = listOf(navArgument("toolId") { type = NavType.StringType })
            ) { backStackEntry ->
                val toolId = backStackEntry.arguments?.getString("toolId") ?: ""
                DedicatedToolScreen(
                    toolId = toolId,
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToRoute = { route -> navController.navigate(route) }
                )
            }
        }

        // Persistent Frosted Glass Floating Dock across Home, Tools, Calculator, Settings
        if (isMainTab) {
            val selectedTab = when {
                currentRoute == "tools_list" -> NavigationTab.TOOLS
                currentRoute.startsWith("calculator") -> NavigationTab.CALCULATOR
                currentRoute == "settings" -> NavigationTab.SETTINGS
                else -> NavigationTab.HOME
            }

            FloatingDock(
                selectedTab = selectedTab,
                onTabSelected = { tab ->
                    val targetRoute = when (tab) {
                        NavigationTab.HOME -> "home"
                        NavigationTab.TOOLS -> "tools_list"
                        NavigationTab.CALCULATOR -> "calculator"
                        NavigationTab.SETTINGS -> "settings"
                    }

                    if (currentRoute != targetRoute) {
                        navController.navigate(targetRoute) {
                            popUpTo("home") {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .navigationBarsPadding()
                    .padding(bottom = 16.dp)
            )
        }
    }
}
