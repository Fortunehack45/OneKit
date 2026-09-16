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
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val prefs = remember { PreferencesManager(context) }

    var isHistoryEnabled by remember { mutableStateOf(prefs.isHistoryEnabled) }
    var isHapticsEnabled by remember { mutableStateOf(prefs.isHapticsEnabled) }
    var isAutosaveEnabled by remember { mutableStateOf(prefs.isAutosaveEnabled) }
    var currentTheme by remember { mutableStateOf(prefs.themeMode) }

    var feedbackMessage by remember { mutableStateOf<String?>(null) }

    Scaffold(
        containerColor = CanvasBackground,
        topBar = {
            TopAppBar(
                title = { Text("Settings & Privacy", fontWeight = FontWeight.Bold, color = TextPrimary) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = TextPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = CanvasBackground)
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
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
                            Text("100% On-Device Privacy", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = TextPrimary)
                        }
                        Text(
                            text = "“ONE processes your files on your device whenever possible.”",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextPrimary
                        )
                        Text(
                            text = "• Zero account creation required\n• Zero remote databases\n• Files, photos, and PDFs never leave your phone\n• Completely offline by design",
                            fontSize = 13.sp,
                            lineHeight = 20.sp,
                            color = TextPrimary.copy(alpha = 0.85f)
                        )
                    }
                }
            }

            // 2. Privacy & History Preferences
            item {
                Surface(
                    shape = RoundedCornerShape(24.dp),
                    color = Color.White,
                    modifier = Modifier.fillMaxWidth().border(1.dp, BorderSubtle, RoundedCornerShape(24.dp))
                ) {
                    Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(Icons.Outlined.History, contentDescription = null, tint = DockObsidian)
                            Text("History & Recent Items", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = TextPrimary)
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Store Recent Tools Locally", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                                Text("Stored in on-device private preferences only", fontSize = 12.sp, color = TextSecondary)
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
                            colors = ButtonDefaults.buttonColors(containerColor = DockObsidian),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Clear Recent Tools History")
                        }
                    }
                }
            }

            // 3. Behavior & Appearance
            item {
                Surface(
                    shape = RoundedCornerShape(24.dp),
                    color = Color.White,
                    modifier = Modifier.fillMaxWidth().border(1.dp, BorderSubtle, RoundedCornerShape(24.dp))
                ) {
                    Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(Icons.Outlined.Tune, contentDescription = null, tint = DockObsidian)
                            Text("Preferences & Behavior", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = TextPrimary)
                        }

                        // Appearance row
                        Text("Appearance", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf("SYSTEM", "LIGHT", "DARK").forEach { mode ->
                                val isSelected = currentTheme == mode
                                FilterChip(
                                    selected = isSelected,
                                    onClick = {
                                        currentTheme = mode
                                        prefs.themeMode = mode
                                    },
                                    label = { Text(mode) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = DockObsidian,
                                        selectedLabelColor = Color.White
                                    )
                                )
                            }
                        }

                        // Haptic feedback
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Haptic Feedback", fontSize = 14.sp, color = TextPrimary)
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
                            Text("Auto-Save Generated Outputs", fontSize = 14.sp, color = TextPrimary)
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

            // 4. Temporary Cache Management
            item {
                Surface(
                    shape = RoundedCornerShape(24.dp),
                    color = Color.White,
                    modifier = Modifier.fillMaxWidth().border(1.dp, BorderSubtle, RoundedCornerShape(24.dp))
                ) {
                    Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(Icons.Outlined.CleaningServices, contentDescription = null, tint = DockObsidian)
                            Text("Storage & Cache", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = TextPrimary)
                        }
                        Text(
                            "Intermediate PDF and compressed image cache files are automatically deleted after 24 hours.",
                            fontSize = 13.sp,
                            color = TextSecondary
                        )

                        Button(
                            onClick = {
                                val files = context.cacheDir.listFiles()
                                val count = files?.size ?: 0
                                files?.forEach { it.delete() }
                                feedbackMessage = "Cleared $count temporary files."
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = DockObsidian),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Clear Temporary Cache Now")
                        }

                        feedbackMessage?.let { msg ->
                            Text(msg, fontSize = 12.sp, color = HeroLavenderDark, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // 5. About Box
            item {
                Surface(
                    shape = RoundedCornerShape(24.dp),
                    color = Color.White,
                    modifier = Modifier.fillMaxWidth().border(1.dp, BorderSubtle, RoundedCornerShape(24.dp))
                ) {
                    Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("ONE Utility App", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = TextPrimary)
                        Text("Version 1.0.0 • Production Offline Release", fontSize = 13.sp, color = TextSecondary)
                        Text("Open-Source Architecture • Built with Jetpack Compose", fontSize = 12.sp, color = TextMuted)
                    }
                }
                Spacer(Modifier.height(16.dp))
            }
        }
    }
}
