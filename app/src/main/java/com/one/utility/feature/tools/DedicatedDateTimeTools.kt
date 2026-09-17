package com.one.utility.feature.tools

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.one.utility.core.designsystem.*
import com.one.utility.core.processing.CurrencyAndTimeEngine
import kotlinx.coroutines.delay
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun DedicatedWorldClockView() {
    val engine = remember { CurrencyAndTimeEngine() }
    val cities = remember { engine.getWorldClockCities() }
    var currentTimeMillis by remember { mutableLongStateOf(System.currentTimeMillis()) }

    LaunchedEffect(Unit) {
        while (true) {
            currentTimeMillis = System.currentTimeMillis()
            delay(1000)
        }
    }

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        cities.forEach { city ->
            val zone = runCatching { ZoneId.of(city.zoneId) }.getOrNull() ?: ZoneId.of("UTC")
            val zonedDateTime = java.time.ZonedDateTime.now(zone)
            val timeStr = zonedDateTime.format(DateTimeFormatter.ofPattern("hh:mm:ss a"))
            val dateStr = zonedDateTime.format(DateTimeFormatter.ofPattern("EEE, MMM d"))

            Surface(
                shape = RoundedCornerShape(16.dp),
                color = AppTheme.colors.cardSurface,
                modifier = Modifier.fillMaxWidth().border(1.dp, AppTheme.colors.borderSubtle, RoundedCornerShape(16.dp))
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(city.city, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = AppTheme.colors.textPrimary)
                        Text("${city.country} • $dateStr", fontSize = 12.sp, color = AppTheme.colors.textSecondary)
                    }
                    Text(timeStr, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Black, fontSize = 18.sp, color = MaterialTheme.colorScheme.primary)
                }
            }
        }
    }
}

@Composable
fun DedicatedStopwatchView() {
    var isRunning by remember { mutableStateOf(false) }
    var elapsedTimeMs by remember { mutableLongStateOf(0L) }
    val laps = remember { mutableStateListOf<Long>() }

    LaunchedEffect(isRunning) {
        val start = System.currentTimeMillis() - elapsedTimeMs
        while (isRunning) {
            elapsedTimeMs = System.currentTimeMillis() - start
            delay(30)
        }
    }

    val minutes = (elapsedTimeMs / 60000) % 60
    val seconds = (elapsedTimeMs / 1000) % 60
    val millis = (elapsedTimeMs % 1000) / 10

    Column(verticalArrangement = Arrangement.spacedBy(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Surface(
            shape = RoundedCornerShape(22.dp),
            color = AppTheme.colors.cardSurface,
            modifier = Modifier.fillMaxWidth().border(1.dp, AppTheme.colors.borderSubtle, RoundedCornerShape(22.dp))
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = String.format("%02d:%02d.%02d", minutes, seconds, millis),
                    fontFamily = FontFamily.Monospace,
                    fontSize = 46.sp,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.primary
                )

                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Button(
                        onClick = { isRunning = !isRunning },
                        modifier = Modifier.weight(1f).height(50.dp).pressFeedback(),
                        shape = RoundedCornerShape(14.dp),
                        colors = if (isRunning) accentButtonColors(MaterialTheme.colorScheme.error) else accentButtonColors(BentoEmerald)
                    ) {
                        Icon(if (isRunning) Icons.Default.Pause else Icons.Default.PlayArrow, contentDescription = null, tint = Color.White)
                        Spacer(Modifier.width(6.dp))
                        Text(if (isRunning) "Pause" else "Start", color = Color.White, fontWeight = FontWeight.Bold)
                    }

                    if (isRunning) {
                        Button(
                            onClick = { laps.add(0, elapsedTimeMs) },
                            modifier = Modifier.weight(1f).height(50.dp).pressFeedback(),
                            shape = RoundedCornerShape(14.dp),
                            colors = obsidianButtonColors()
                        ) {
                            Text("Lap", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    } else if (elapsedTimeMs > 0) {
                        Button(
                            onClick = { elapsedTimeMs = 0L; laps.clear() },
                            modifier = Modifier.weight(1f).height(50.dp).pressFeedback(),
                            shape = RoundedCornerShape(14.dp),
                            colors = obsidianButtonColors()
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = null, tint = Color.White)
                            Spacer(Modifier.width(6.dp))
                            Text("Reset", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        if (laps.isNotEmpty()) {
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = AppTheme.colors.cardSurface,
                modifier = Modifier.fillMaxWidth().border(1.dp, AppTheme.colors.borderSubtle, RoundedCornerShape(18.dp))
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Lap Splits (${laps.size})", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = AppTheme.colors.textPrimary)
                    HorizontalDivider(color = AppTheme.colors.borderSubtle)
                    laps.forEachIndexed { idx, lapMs ->
                        val lapMin = (lapMs / 60000) % 60
                        val lapSec = (lapMs / 1000) % 60
                        val lapMil = (lapMs % 1000) / 10
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Lap ${laps.size - idx}", fontSize = 13.sp, color = AppTheme.colors.textSecondary)
                            Text(String.format("%02d:%02d.%02d", lapMin, lapSec, lapMil), fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, color = AppTheme.colors.textPrimary)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DedicatedUnixTimestampView(copyAction: (String) -> Unit) {
    var timestampInput by remember { mutableStateOf((System.currentTimeMillis() / 1000).toString()) }

    val epochSeconds = timestampInput.toLongOrNull() ?: (System.currentTimeMillis() / 1000)
    val instant = runCatching { Instant.ofEpochSecond(epochSeconds) }.getOrNull() ?: Instant.now()
    val utcStr = instant.atZone(ZoneId.of("UTC")).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss 'UTC'"))
    val localStr = instant.atZone(ZoneId.systemDefault()).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss (z)"))

    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        OutlinedTextField(
            value = timestampInput,
            onValueChange = { timestampInput = it },
            label = { Text("Unix Timestamp (Seconds)") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp)
        )

        Button(
            onClick = { timestampInput = (System.currentTimeMillis() / 1000).toString() },
            modifier = Modifier.fillMaxWidth().height(48.dp).pressFeedback(),
            shape = RoundedCornerShape(12.dp),
            colors = obsidianButtonColors()
        ) {
            Icon(Icons.Default.Refresh, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(8.dp))
            Text("Set to Current Epoch Time", color = Color.White, fontWeight = FontWeight.Bold)
        }

        Surface(
            shape = RoundedCornerShape(18.dp),
            color = AppTheme.colors.cardSurface,
            modifier = Modifier.fillMaxWidth().border(1.dp, AppTheme.colors.borderSubtle, RoundedCornerShape(18.dp))
        ) {
            Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Human-Readable Dates:", fontSize = 13.sp, color = AppTheme.colors.textSecondary)

                Column {
                    Text("UTC Standard:", fontSize = 12.sp, color = AppTheme.colors.textSecondary)
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text(utcStr, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = MaterialTheme.colorScheme.primary)
                        IconButton(onClick = { copyAction(utcStr) }) {
                            Icon(Icons.Default.ContentCopy, contentDescription = "Copy", tint = AppTheme.colors.textPrimary, modifier = Modifier.size(16.dp))
                        }
                    }
                }

                HorizontalDivider(color = AppTheme.colors.borderSubtle)

                Column {
                    Text("Device Local Time:", fontSize = 12.sp, color = AppTheme.colors.textSecondary)
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text(localStr, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = BentoEmerald)
                        IconButton(onClick = { copyAction(localStr) }) {
                            Icon(Icons.Default.ContentCopy, contentDescription = "Copy", tint = AppTheme.colors.textPrimary, modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DedicatedBusinessDaysView(copyAction: (String) -> Unit) {
    val engine = remember { CurrencyAndTimeEngine() }
    var startYear by remember { mutableStateOf("2026") }
    var startMonth by remember { mutableStateOf("1") }
    var startDay by remember { mutableStateOf("1") }

    var endYear by remember { mutableStateOf("2026") }
    var endMonth by remember { mutableStateOf("12") }
    var endDay by remember { mutableStateOf("31") }

    val start = runCatching { LocalDate.of(startYear.toInt(), startMonth.toInt(), startDay.toInt()) }.getOrNull() ?: LocalDate.now()
    val end = runCatching { LocalDate.of(endYear.toInt(), endMonth.toInt(), endDay.toInt()) }.getOrNull() ?: LocalDate.now().plusMonths(1)
    val businessDays = engine.calculateBusinessDays(start, end)
    val totalDays = java.time.temporal.ChronoUnit.DAYS.between(start, end)

    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Text("Start Date:", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = AppTheme.colors.textPrimary)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(value = startYear, onValueChange = { startYear = it }, label = { Text("Year") }, modifier = Modifier.weight(1.2f), shape = RoundedCornerShape(12.dp))
            OutlinedTextField(value = startMonth, onValueChange = { startMonth = it }, label = { Text("Month") }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp))
            OutlinedTextField(value = startDay, onValueChange = { startDay = it }, label = { Text("Day") }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp))
        }

        Text("End Date:", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = AppTheme.colors.textPrimary)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(value = endYear, onValueChange = { endYear = it }, label = { Text("Year") }, modifier = Modifier.weight(1.2f), shape = RoundedCornerShape(12.dp))
            OutlinedTextField(value = endMonth, onValueChange = { endMonth = it }, label = { Text("Month") }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp))
            OutlinedTextField(value = endDay, onValueChange = { endDay = it }, label = { Text("Day") }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp))
        }

        Surface(
            shape = RoundedCornerShape(18.dp),
            color = AppTheme.colors.cardSurface,
            modifier = Modifier.fillMaxWidth().border(1.dp, AppTheme.colors.borderSubtle, RoundedCornerShape(18.dp))
        ) {
            Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Total Working / Business Days:", fontSize = 13.sp, color = AppTheme.colors.textSecondary)
                val fmt = "$businessDays days"
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text(text = fmt, fontSize = 32.sp, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary)
                    IconButton(onClick = { copyAction(fmt) }) {
                        Icon(Icons.Default.ContentCopy, contentDescription = "Copy", tint = AppTheme.colors.textPrimary, modifier = Modifier.size(18.dp))
                    }
                }
                HorizontalDivider(color = AppTheme.colors.borderSubtle)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Total Calendar Days:", fontSize = 13.sp, color = AppTheme.colors.textSecondary)
                    Text("$totalDays days", fontWeight = FontWeight.Bold, color = AppTheme.colors.textPrimary)
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Weekend Days Excluded:", fontSize = 13.sp, color = AppTheme.colors.textSecondary)
                    Text("${totalDays - businessDays} days", fontWeight = FontWeight.Bold, color = BentoEmerald)
                }
            }
        }
    }
}

@Composable
fun DedicatedTimeCountdownView() {
    var targetYear by remember { mutableStateOf((LocalDate.now().year + 1).toString()) }
    var targetMonth by remember { mutableStateOf("1") }
    var targetDay by remember { mutableStateOf("1") }
    var eventTitle by remember { mutableStateOf("New Year's Day") }
    var nowMillis by remember { mutableLongStateOf(System.currentTimeMillis()) }

    LaunchedEffect(Unit) {
        while (true) {
            nowMillis = System.currentTimeMillis()
            delay(1000)
        }
    }

    val targetDate = runCatching {
        LocalDate.of(targetYear.toInt(), targetMonth.toInt(), targetDay.toInt())
            .atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
    }.getOrNull() ?: System.currentTimeMillis()

    val diffMs = (targetDate - nowMillis).coerceAtLeast(0L)
    val days = diffMs / (1000 * 60 * 60 * 24)
    val hours = (diffMs / (1000 * 60 * 60)) % 24
    val minutes = (diffMs / (1000 * 60)) % 60
    val seconds = (diffMs / 1000) % 60

    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        OutlinedTextField(
            value = eventTitle,
            onValueChange = { eventTitle = it },
            label = { Text("Event Name") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp)
        )

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(value = targetYear, onValueChange = { targetYear = it }, label = { Text("Year") }, modifier = Modifier.weight(1.2f), shape = RoundedCornerShape(12.dp))
            OutlinedTextField(value = targetMonth, onValueChange = { targetMonth = it }, label = { Text("Month") }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp))
            OutlinedTextField(value = targetDay, onValueChange = { targetDay = it }, label = { Text("Day") }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp))
        }

        Surface(
            shape = RoundedCornerShape(22.dp),
            color = AppTheme.colors.cardSurface,
            modifier = Modifier.fillMaxWidth().border(1.dp, AppTheme.colors.borderSubtle, RoundedCornerShape(22.dp))
        ) {
            Column(modifier = Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(eventTitle, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = AppTheme.colors.textPrimary)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    listOf("DAYS" to days, "HOURS" to hours, "MINS" to minutes, "SECS" to seconds).forEach { (label, value) ->
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
                            modifier = Modifier.weight(1f).padding(vertical = 4.dp)
                        ) {
                            Column(modifier = Modifier.padding(10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(String.format(Locale.US, "%02d", value), fontSize = 24.sp, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary)
                                Text(label, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = AppTheme.colors.textSecondary)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DedicatedTimerView() {
    var totalSeconds by remember { mutableIntStateOf(300) } // default 5 mins
    var isRunning by remember { mutableStateOf(false) }
    var secondsRemaining by remember { mutableIntStateOf(300) }

    LaunchedEffect(isRunning) {
        while (isRunning && secondsRemaining > 0) {
            delay(1000)
            secondsRemaining--
        }
        if (secondsRemaining == 0) isRunning = false
    }

    val mins = secondsRemaining / 60
    val secs = secondsRemaining % 60

    Column(verticalArrangement = Arrangement.spacedBy(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            listOf("1m" to 60, "3m" to 180, "5m" to 300, "10m" to 600, "15m" to 900).forEach { (label, duration) ->
                FilterChip(
                    selected = totalSeconds == duration,
                    onClick = {
                        totalSeconds = duration
                        secondsRemaining = duration
                        isRunning = false
                    },
                    label = { Text(label) },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        Surface(
            shape = RoundedCornerShape(24.dp),
            color = AppTheme.colors.cardSurface,
            modifier = Modifier.fillMaxWidth().border(1.dp, AppTheme.colors.borderSubtle, RoundedCornerShape(24.dp))
        ) {
            Column(modifier = Modifier.padding(28.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Text(
                    text = String.format(Locale.US, "%02d:%02d", mins, secs),
                    fontSize = 54.sp,
                    fontWeight = FontWeight.Black,
                    fontFamily = FontFamily.Monospace,
                    color = if (secondsRemaining > 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                )

                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Button(
                        onClick = { isRunning = !isRunning },
                        modifier = Modifier.height(48.dp).pressFeedback(),
                        shape = RoundedCornerShape(12.dp),
                        colors = if (isRunning) accentButtonColors(MaterialTheme.colorScheme.error) else obsidianButtonColors()
                    ) {
                        Icon(if (isRunning) Icons.Default.Pause else Icons.Default.PlayArrow, contentDescription = null, tint = Color.White)
                        Spacer(Modifier.width(6.dp))
                        Text(if (isRunning) "Pause" else "Start Timer", color = Color.White, fontWeight = FontWeight.Bold)
                    }

                    OutlinedButton(
                        onClick = {
                            isRunning = false
                            secondsRemaining = totalSeconds
                        },
                        modifier = Modifier.height(48.dp).pressFeedback(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = null, tint = AppTheme.colors.textPrimary)
                        Spacer(Modifier.width(6.dp))
                        Text("Reset", color = AppTheme.colors.textPrimary)
                    }
                }
            }
        }
    }
}

@Composable
fun DedicatedWeekNumberView(copyAction: (String) -> Unit) {
    var yearInput by remember { mutableStateOf(LocalDate.now().year.toString()) }
    var monthInput by remember { mutableStateOf(LocalDate.now().monthValue.toString()) }
    var dayInput by remember { mutableStateOf(LocalDate.now().dayOfMonth.toString()) }

    val date = runCatching { LocalDate.of(yearInput.toInt(), monthInput.toInt(), dayInput.toInt()) }.getOrNull() ?: LocalDate.now()
    val weekNumber = date.get(java.time.temporal.IsoFields.WEEK_OF_WEEK_BASED_YEAR)
    val dayOfYear = date.dayOfYear
    val daysRemaining = (if (date.isLeapYear) 366 else 365) - dayOfYear

    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(value = yearInput, onValueChange = { yearInput = it }, label = { Text("Year") }, modifier = Modifier.weight(1.2f), shape = RoundedCornerShape(12.dp))
            OutlinedTextField(value = monthInput, onValueChange = { monthInput = it }, label = { Text("Month") }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp))
            OutlinedTextField(value = dayInput, onValueChange = { dayInput = it }, label = { Text("Day") }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp))
        }

        Surface(
            shape = RoundedCornerShape(18.dp),
            color = AppTheme.colors.cardSurface,
            modifier = Modifier.fillMaxWidth().border(1.dp, AppTheme.colors.borderSubtle, RoundedCornerShape(18.dp))
        ) {
            Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("ISO Week Number:", fontSize = 13.sp, color = AppTheme.colors.textSecondary)
                val fmt = "Week $weekNumber"
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text(text = fmt, fontSize = 36.sp, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary)
                    IconButton(onClick = { copyAction(fmt) }) {
                        Icon(Icons.Default.ContentCopy, contentDescription = "Copy", tint = AppTheme.colors.textPrimary, modifier = Modifier.size(18.dp))
                    }
                }
                HorizontalDivider(color = AppTheme.colors.borderSubtle)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Day of Year:", fontSize = 13.sp, color = AppTheme.colors.textSecondary)
                    Text("Day $dayOfYear of ${if (date.isLeapYear) 366 else 365}", fontWeight = FontWeight.Bold, color = AppTheme.colors.textPrimary)
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Days Remaining in Year:", fontSize = 13.sp, color = AppTheme.colors.textSecondary)
                    Text("$daysRemaining days", fontWeight = FontWeight.Bold, color = BentoEmerald)
                }
            }
        }
    }
}

@Composable
fun DedicatedAlarmShortcutsView() {
    val context = androidx.compose.ui.platform.LocalContext.current

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Surface(
            shape = RoundedCornerShape(18.dp),
            color = AppTheme.colors.cardSurface,
            modifier = Modifier.fillMaxWidth().border(1.dp, AppTheme.colors.borderSubtle, RoundedCornerShape(18.dp))
        ) {
            Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("System Clock & Alarm Shortcuts", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = AppTheme.colors.textPrimary)
                Text("Direct 1-tap shortcuts to your device's native clock, alarms, and timers without navigating through system settings.", fontSize = 13.sp, color = AppTheme.colors.textSecondary)

                Button(
                    onClick = {
                        runCatching {
                            val intent = Intent(android.provider.AlarmClock.ACTION_SHOW_ALARMS)
                            context.startActivity(intent)
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(48.dp).pressFeedback(),
                    shape = RoundedCornerShape(12.dp),
                    colors = obsidianButtonColors()
                ) {
                    Text("Open System Alarms", color = Color.White, fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = {
                        runCatching {
                            val intent = Intent(android.provider.AlarmClock.ACTION_SET_TIMER)
                            context.startActivity(intent)
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(48.dp).pressFeedback(),
                    shape = RoundedCornerShape(12.dp),
                    colors = accentButtonColors(MaterialTheme.colorScheme.primary)
                ) {
                    Text("Open System Timers", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun DedicatedTimeZoneConverterView() {
    DedicatedWorldClockView()
}

@Composable
fun DedicatedDateCalculatorView(copyAction: (String) -> Unit) {
    DedicatedBusinessDaysView(copyAction = copyAction)
}

