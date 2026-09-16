package com.one.utility.feature.privacy

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.one.utility.core.designsystem.*
import com.one.utility.core.processing.PasswordGeneratorEngine
import com.one.utility.core.processing.PasswordOptions

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PasswordGeneratorScreen(
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val engine = remember { PasswordGeneratorEngine() }

    var length by remember { mutableFloatStateOf(16f) }
    var includeUpper by remember { mutableStateOf(true) }
    var includeLower by remember { mutableStateOf(true) }
    var includeNumbers by remember { mutableStateOf(true) }
    var includeSymbols by remember { mutableStateOf(true) }
    var excludeAmbiguous by remember { mutableStateOf(true) }

    var generatedPassword by remember { mutableStateOf("") }
    var copiedMessage by remember { mutableStateOf<String?>(null) }

    fun generate() {
        val options = PasswordOptions(
            length = length.toInt(),
            includeUppercase = includeUpper,
            includeLowercase = includeLower,
            includeNumbers = includeNumbers,
            includeSymbols = includeSymbols,
            excludeAmbiguous = excludeAmbiguous
        )
        generatedPassword = engine.generatePassword(options)
        copiedMessage = null
    }

    LaunchedEffect(Unit) {
        generate()
    }

    Scaffold(
        containerColor = CanvasBackground,
        topBar = {
            TopAppBar(
                title = { Text("Password Generator", fontWeight = FontWeight.Bold, color = TextPrimary) },
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
            // 1. Password Display Box
            item {
                Surface(
                    shape = RoundedCornerShape(24.dp),
                    color = Color.White,
                    modifier = Modifier.fillMaxWidth().border(1.dp, BorderSubtle, RoundedCornerShape(24.dp))
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text("Generated Password", fontSize = 12.sp, color = TextSecondary)
                        Text(
                            text = generatedPassword,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                            color = DockObsidian,
                            lineHeight = 26.sp
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            TextButton(onClick = { generate() }) {
                                Icon(Icons.Default.Refresh, contentDescription = null, tint = DockObsidian, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(6.dp))
                                Text("Regenerate", color = DockObsidian, fontWeight = FontWeight.Bold)
                            }

                            Button(
                                onClick = {
                                    val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    cm.setPrimaryClip(ClipData.newPlainText("ONE Password", generatedPassword))
                                    copiedMessage = "Copied to clipboard!"
                                },
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = DockObsidian)
                            ) {
                                Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(6.dp))
                                Text("Copy")
                            }
                        }

                        copiedMessage?.let { msg ->
                            Text(msg, fontSize = 12.sp, color = BentoHoney, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // 2. Customization Controls
            item {
                Surface(
                    shape = RoundedCornerShape(24.dp),
                    color = Color.White,
                    modifier = Modifier.fillMaxWidth().border(1.dp, BorderSubtle, RoundedCornerShape(24.dp))
                ) {
                    Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text("Options", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = TextPrimary)

                        Column {
                            Text("Length: ${length.toInt()} characters", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                            Slider(
                                value = length,
                                onValueChange = {
                                    length = it
                                    generate()
                                },
                                valueRange = 8f..32f,
                                steps = 23
                            )
                        }

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text("Uppercase (A-Z)", fontSize = 14.sp, color = TextPrimary)
                            Switch(checked = includeUpper, onCheckedChange = { includeUpper = it; generate() })
                        }

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text("Lowercase (a-z)", fontSize = 14.sp, color = TextPrimary)
                            Switch(checked = includeLower, onCheckedChange = { includeLower = it; generate() })
                        }

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text("Numbers (0-9)", fontSize = 14.sp, color = TextPrimary)
                            Switch(checked = includeNumbers, onCheckedChange = { includeNumbers = it; generate() })
                        }

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text("Symbols (!@#$)", fontSize = 14.sp, color = TextPrimary)
                            Switch(checked = includeSymbols, onCheckedChange = { includeSymbols = it; generate() })
                        }

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text("Exclude Ambiguous (0, O, 1, l)", fontSize = 14.sp, color = TextPrimary)
                            Switch(checked = excludeAmbiguous, onCheckedChange = { excludeAmbiguous = it; generate() })
                        }
                    }
                }
            }
        }
    }
}
