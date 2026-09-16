package com.one.utility.feature.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.CleaningServices
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.one.utility.core.data.PreferencesManager
import com.one.utility.core.designsystem.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    onThemeChanged: (String) -> Unit = {}
) {
    val context = LocalContext.current
    val prefs = remember { PreferencesManager(context) }

    var isHistoryEnabled by remember { mutableStateOf(prefs.isHistoryEnabled) }
    var isHapticsEnabled by remember { mutableStateOf(prefs.isHapticsEnabled) }
    var isAutosaveEnabled by remember { mutableStateOf(prefs.isAutosaveEnabled) }
    var currentTheme by remember { mutableStateOf(prefs.themeMode) }

    var feedbackMessage by remember { mutableStateOf<String?>(null) }

    Scaffold(
        containerColor = AppTheme.colors.canvasBackground,
        topBar = {
            TopAppBar(
                title = { Text("Settings & Privacy", fontWeight = FontWeight.Bold, color = AppTheme.colors.textPrimary) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = AppTheme.colors.textPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = AppTheme.colors.canvasBackground)
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(bottom = 110.dp)
        ) {
            // 1. Privacy Banner Card
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(24.dp))
                        .background(HeroLavender)
                        .padding(22.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Outlined.Shield, contentDescription = null, tint = DockObsidian)
                            Text("100% On-Device Privacy", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = DockObsidian)
                        }
                        Text(
                            text = "“ONE processes all tasks on your device offline without remote tracking.”",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = DockObsidian
                        )
                        Text(
                            text = "• Zero account creation required\n• Zero remote databases\n• Files, photos, and PDFs never leave your phone\n• Completely offline by design",
                            fontSize = 12.sp,
                            lineHeight = 18.sp,
                            color = DockObsidian.copy(alpha = 0.85f)
                        )
                    }
                }
            }

            // 2. Feedback snackbar banner if triggered
            feedbackMessage?.let { msg ->
                item {
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = BentoMintLight,
                        modifier = Modifier.fillMaxWidth().border(1.dp, BentoMint, RoundedCornerShape(14.dp))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(msg, color = Color(0xFF0F5132), fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }

            // 3. Privacy & History Preferences
            item {
                Surface(
                    shape = RoundedCornerShape(24.dp),
                    color = AppTheme.colors.cardSurface,
                    modifier = Modifier.fillMaxWidth().border(1.dp, AppTheme.colors.borderSubtle, RoundedCornerShape(24.dp))
                ) {
                    Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(Icons.Outlined.History, contentDescription = null, tint = BentoHoney)
                            Text("History & Recent Items", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = AppTheme.colors.textPrimary)
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Store Recent Tools Locally", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = AppTheme.colors.textPrimary)
                                Text("Stored in on-device private preferences only", fontSize = 12.sp, color = AppTheme.colors.textSecondary)
                            }
                            Switch(
                                checked = isHistoryEnabled,
                                onCheckedChange = {
                                    isHistoryEnabled = it
                                    prefs.isHistoryEnabled = it
                                    feedbackMessage = if (it) "Local history enabled" else "Local history disabled"
                                }
                            )
                        }

                        Button(
                            onClick = {
                                prefs.clearRecentTools()
                                feedbackMessage = "Cleared recent tools history."
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = AppTheme.colors.surfaceVariant,
                                contentColor = AppTheme.colors.textPrimary
                            ),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Clear Recent Tools History", fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }

            // 4. Appearance & Behavior
            item {
                Surface(
                    shape = RoundedCornerShape(24.dp),
                    color = AppTheme.colors.cardSurface,
                    modifier = Modifier.fillMaxWidth().border(1.dp, AppTheme.colors.borderSubtle, RoundedCornerShape(24.dp))
                ) {
                    Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(Icons.Outlined.Tune, contentDescription = null, tint = HeroLavenderDark)
                            Text("Appearance & Theme", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = AppTheme.colors.textPrimary)
                        }

                        Text("Theme (Matte Slate Grey in Dark)", fontSize = 13.sp, fontWeight = FontWeight.Medium, color = AppTheme.colors.textSecondary)
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf("SYSTEM", "LIGHT", "DARK").forEach { mode ->
                                val isSelected = currentTheme.equals(mode, ignoreCase = true)
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(14.dp))
                                        .background(if (isSelected) AppTheme.colors.primaryButton else AppTheme.colors.surfaceVariant)
                                        .border(
                                            1.dp,
                                            if (isSelected) Color.Transparent else AppTheme.colors.borderSubtle,
                                            RoundedCornerShape(14.dp)
                                        )
                                        .clickable {
                                            currentTheme = mode
                                            prefs.themeMode = mode
                                            onThemeChanged(mode)
                                            feedbackMessage = "Theme set to $mode"
                                        }
                                        .padding(vertical = 10.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = mode,
                                        fontSize = 12.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isSelected) AppTheme.colors.onPrimaryButton else AppTheme.colors.textSecondary
                                    )
                                }
                            }
                        }

                        HorizontalDivider(color = AppTheme.colors.borderSubtle, modifier = Modifier.padding(vertical = 4.dp))

                        // Haptic feedback
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Haptic Feedback", fontSize = 14.sp, color = AppTheme.colors.textPrimary)
                            Switch(
                                checked = isHapticsEnabled,
                                onCheckedChange = {
                                    isHapticsEnabled = it
                                    prefs.isHapticsEnabled = it
                                }
                            )
                        }

                        // Auto-save outputs
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Auto-Save Outputs to Gallery", fontSize = 14.sp, color = AppTheme.colors.textPrimary)
                            Switch(
                                checked = isAutosaveEnabled,
                                onCheckedChange = {
                                    isAutosaveEnabled = it
                                    prefs.isAutosaveEnabled = it
                                }
                            )
                        }
                    }
                }
            }

            // 5. Temporary Cache Management
            item {
                Surface(
                    shape = RoundedCornerShape(24.dp),
                    color = AppTheme.colors.cardSurface,
                    modifier = Modifier.fillMaxWidth().border(1.dp, AppTheme.colors.borderSubtle, RoundedCornerShape(24.dp))
                ) {
                    Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(Icons.Outlined.CleaningServices, contentDescription = null, tint = BentoSky)
                            Text("Temporary App Cache", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = AppTheme.colors.textPrimary)
                        }

                        Text(
                            text = "Clear generated preview cache from conversions, PDFs, and background removal without affecting your original gallery photos.",
                            fontSize = 12.sp,
                            lineHeight = 18.sp,
                            color = AppTheme.colors.textSecondary
                        )

                        Button(
                            onClick = {
                                try {
                                    context.cacheDir.deleteRecursively()
                                    context.cacheDir.mkdirs()
                                    feedbackMessage = "Cache cleared successfully."
                                } catch (e: Exception) {
                                    feedbackMessage = "Error clearing cache."
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = BentoSky,
                                contentColor = TextPrimary
                            ),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Clear App Cache", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}
