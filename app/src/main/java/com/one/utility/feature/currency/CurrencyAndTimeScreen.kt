package com.one.utility.feature.currency

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.one.utility.core.designsystem.*
import com.one.utility.core.processing.CurrencyAndTimeEngine
import java.time.LocalTime

enum class CurrencyTimeTab(val label: String) {
    CURRENCY("Offline Currency"),
    TIMEZONE("World Time Zones")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CurrencyAndTimeScreen(
    onNavigateBack: () -> Unit
) {
    val engine = remember { CurrencyAndTimeEngine() }
    var selectedTab by remember { mutableStateOf(CurrencyTimeTab.CURRENCY) }

    // Currency state
    var sourceAmount by remember { mutableStateOf("100") }
    var customRate by remember { mutableStateOf("1.08") }
    var fromCurrencyName by remember { mutableStateOf("EUR") }
    var toCurrencyName by remember { mutableStateOf("USD") }

    // Timezone state
    val timeZones = remember { engine.getMajorTimeZones() }
    var fromTz by remember { mutableStateOf("Africa/Lagos") }
    var toTz by remember { mutableStateOf("Asia/Tokyo") }

    Scaffold(
        containerColor = AppTheme.colors.canvasBackground,
        topBar = {
            TopAppBar(
                title = { Text("Currency & World Time", fontWeight = FontWeight.Bold, color = AppTheme.colors.textPrimary) },
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
            contentPadding = PaddingValues(bottom = 140.dp)
        ) {
            // Tab Selector Chips
            item {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    CurrencyTimeTab.values().forEach { tab ->
                        val isSelected = selectedTab == tab
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedTab = tab },
                            label = { Text(tab.label) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                                containerColor = AppTheme.colors.cardSurface,
                                labelColor = AppTheme.colors.textSecondary
                            )
                        )
                    }
                }
            }

            when (selectedTab) {
                CurrencyTimeTab.CURRENCY -> {
                    item {
                        Surface(
                            shape = RoundedCornerShape(24.dp),
                            color = AppTheme.colors.cardSurface,
                            modifier = Modifier.fillMaxWidth().border(1.dp, AppTheme.colors.borderSubtle, RoundedCornerShape(24.dp))
                        ) {
                            Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                                Text("Offline Currency Calculator", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = AppTheme.colors.textPrimary)

                                // Privacy Notice badge
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(HeroLavender)
                                        .padding(horizontal = 12.dp, vertical = 6.dp)
                                ) {
                                    Text("Rate entered manually • Zero external tracking", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = DockObsidian)
                                }

                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                    OutlinedTextField(
                                        value = sourceAmount,
                                        onValueChange = { sourceAmount = it },
                                        label = { Text("Amount ($fromCurrencyName)") },
                                        modifier = Modifier.weight(1.2f),
                                        shape = RoundedCornerShape(14.dp)
                                    )
                                    OutlinedTextField(
                                        value = customRate,
                                        onValueChange = { customRate = it },
                                        label = { Text("Exchange Rate") },
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(14.dp)
                                    )
                                }

                                val amount = sourceAmount.toDoubleOrNull() ?: 0.0
                                val rate = customRate.toDoubleOrNull() ?: 1.0
                                val converted = engine.convertCurrency(amount, rate)

                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(16.dp))
                                        .background(AppTheme.colors.bentoHoneySubtle)
                                        .padding(18.dp)
                                ) {
                                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                        Text("$amount $fromCurrencyName is equal to:", fontSize = 13.sp, color = AppTheme.colors.textSecondary)
                                        Text("${"%,.2f".format(converted)} $toCurrencyName", fontWeight = FontWeight.Bold, fontSize = 26.sp, color = AppTheme.colors.textPrimary)
                                        Text("Based on 1 $fromCurrencyName = $rate $toCurrencyName", fontSize = 11.sp, color = AppTheme.colors.textSecondary)
                                    }
                                }
                            }
                        }
                    }
                }

                CurrencyTimeTab.TIMEZONE -> {
                    item {
                        Surface(
                            shape = RoundedCornerShape(24.dp),
                            color = AppTheme.colors.cardSurface,
                            modifier = Modifier.fillMaxWidth().border(1.dp, AppTheme.colors.borderSubtle, RoundedCornerShape(24.dp))
                        ) {
                            Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                                Text("World Clock & Time Zones", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = AppTheme.colors.textPrimary)
                                Text("Calculated using Android's on-device time-zone database.", fontSize = 12.sp, color = AppTheme.colors.textSecondary)

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text("From Zone", fontSize = 11.sp, color = AppTheme.colors.textSecondary)
                                        Text(fromTz, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = AppTheme.colors.textPrimary)
                                    }
                                    Icon(Icons.Default.ArrowForward, contentDescription = null, tint = BentoHoney)
                                    Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.End) {
                                        Text("To Zone", fontSize = 11.sp, color = AppTheme.colors.textSecondary)
                                        Text(toTz, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = AppTheme.colors.textPrimary)
                                    }
                                }

                                val result = engine.convertTimeZone(
                                    time = LocalTime.now(),
                                    fromZoneIdStr = fromTz,
                                    toZoneIdStr = toTz
                                )

                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(16.dp))
                                        .background(AppTheme.colors.bentoSkySubtle)
                                        .padding(18.dp)
                                ) {
                                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                        Text("${result.fromZone}: ${result.fromTimeFormatted}", fontSize = 13.sp, color = AppTheme.colors.textSecondary)
                                        Text(
                                            "${result.toZone}: ${result.toTimeFormatted}",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 22.sp,
                                            color = AppTheme.colors.textPrimary
                                        )
                                        val aheadOrBehind = if (result.hourDifference >= 0) "+${result.hourDifference} hours ahead" else "${result.hourDifference} hours behind"
                                        Text(aheadOrBehind, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = HeroLavenderDark)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
