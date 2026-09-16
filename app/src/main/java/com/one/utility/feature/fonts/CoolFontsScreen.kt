package com.one.utility.feature.fonts

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material.icons.outlined.TextFields
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
import com.one.utility.core.designsystem.*
import com.one.utility.core.processing.CoolFontsEngine
import com.one.utility.core.processing.FontStyleResult

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CoolFontsScreen(
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val engine = remember { CoolFontsEngine() }
    var inputText by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("All") }
    var copiedId by remember { mutableStateOf<String?>(null) }

    val categories = listOf("All", "Decorative", "Clean", "Symbols", "Stylized")
    val allStyles = remember(inputText) { engine.generateStyles(inputText) }
    val filteredStyles = remember(allStyles, selectedCategory) {
        if (selectedCategory == "All") allStyles else allStyles.filter { it.category == selectedCategory }
    }

    val clipboard = remember { context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager }

    fun copyToClipboard(style: FontStyleResult) {
        val clip = ClipData.newPlainText(style.styleName, style.previewText)
        clipboard.setPrimaryClip(clip)
        copiedId = style.id
        Toast.makeText(context, "Copied \"${style.styleName}\"!", Toast.LENGTH_SHORT).show()
    }

    fun shareText(text: String) {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, text)
        }
        context.startActivity(Intent.createChooser(intent, "Share Styled Font"))
    }

    Scaffold(
        containerColor = AppTheme.colors.canvasBackground,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Cool Fonts & Text Styler",
                        fontWeight = FontWeight.Bold,
                        color = AppTheme.colors.textPrimary
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = AppTheme.colors.textPrimary
                        )
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
            verticalArrangement = Arrangement.spacedBy(14.dp),
            contentPadding = PaddingValues(bottom = 90.dp)
        ) {
            // 1. Hero Input Card
            item {
                Surface(
                    shape = RoundedCornerShape(24.dp),
                    color = AppTheme.colors.cardSurface,
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, AppTheme.colors.borderSubtle, RoundedCornerShape(24.dp))
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                Icons.Outlined.TextFields,
                                contentDescription = null,
                                tint = HeroLavenderDark
                            )
                            Text(
                                "Enter Your Name or Text",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = AppTheme.colors.textPrimary
                            )
                        }

                        OutlinedTextField(
                            value = inputText,
                            onValueChange = { inputText = it },
                            placeholder = {
                                Text(
                                    "Type your name or bio here...",
                                    color = AppTheme.colors.textMuted
                                )
                            },
                            singleLine = true,
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = BentoHoney,
                                unfocusedBorderColor = AppTheme.colors.borderSubtle,
                                focusedTextColor = AppTheme.colors.textPrimary,
                                unfocusedTextColor = AppTheme.colors.textPrimary
                            )
                        )

                        // Quick suggestion chips
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf("Alex", "Champion", "ONE", "Cyber", "Queen").forEach { sample ->
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(AppTheme.colors.surfaceVariant)
                                        .clickable { inputText = sample }
                                        .padding(horizontal = 10.dp, vertical = 6.dp)
                                ) {
                                    Text(
                                        text = sample,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = AppTheme.colors.textSecondary
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // 2. Category Filter Pills
            item {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(categories) { cat ->
                        val isSelected = cat == selectedCategory
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(16.dp))
                                .background(if (isSelected) AppTheme.colors.primaryButton else AppTheme.colors.cardSurface)
                                .border(
                                    1.dp,
                                    if (isSelected) Color.Transparent else AppTheme.colors.borderSubtle,
                                    RoundedCornerShape(16.dp)
                                )
                                .clickable { selectedCategory = cat }
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = cat,
                                color = if (isSelected) AppTheme.colors.onPrimaryButton else AppTheme.colors.textSecondary,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                            )
                        }
                    }
                }
            }

            // 3. Render Font Cards
            items(filteredStyles, key = { it.id }) { style ->
                val isCopied = copiedId == style.id

                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = AppTheme.colors.cardSurface,
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, AppTheme.colors.borderSubtle, RoundedCornerShape(20.dp))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .padding(end = 12.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = style.styleName,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = AppTheme.colors.textSecondary
                            )
                            Text(
                                text = style.previewText,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Medium,
                                color = AppTheme.colors.textPrimary
                            )
                        }

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Copy Button
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(CircleShape)
                                    .background(if (isCopied) BentoMint else AppTheme.colors.surfaceVariant)
                                    .clickable { copyToClipboard(style) },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (isCopied) Icons.Outlined.Check else Icons.Outlined.ContentCopy,
                                    contentDescription = "Copy",
                                    tint = if (isCopied) Color(0xFF0F5132) else AppTheme.colors.textPrimary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }

                            // Share Button
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(CircleShape)
                                    .background(AppTheme.colors.surfaceVariant)
                                    .clickable { shareText(style.previewText) },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Share,
                                    contentDescription = "Share",
                                    tint = AppTheme.colors.textPrimary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
