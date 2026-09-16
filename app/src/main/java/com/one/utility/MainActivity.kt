package com.one.utility

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.one.utility.core.designsystem.ONETheme
import com.one.utility.feature.backgroundremover.BackgroundRemoverScreen
import com.one.utility.feature.calculator.CalculatorScreen
import com.one.utility.feature.calculator.EverydayCalculatorsScreen
import com.one.utility.feature.compressor.CompressorScreen
import com.one.utility.feature.converter.ImageConverterScreen
import com.one.utility.feature.cropper.ImageCropperScreen
import com.one.utility.feature.currency.CurrencyAndTimeScreen
import com.one.utility.feature.developer.DeveloperToolsScreen
import com.one.utility.feature.files.BatchRenameScreen
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
import java.net.URLDecoder
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ONETheme {
                OneAppNavigation()
            }
        }
    }
}

@Composable
fun OneAppNavigation() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "home"
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
            CalculatorScreen(
                initialExpression = exprParam,
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
            SettingsScreen(onNavigateBack = { navController.popBackStack() })
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
                onNavigateBack = { navController.popBackStack() }
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
    }
}
