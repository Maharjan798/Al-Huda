package com.example.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CompassCalibration
import androidx.compose.material.icons.filled.EventNote
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.RadioButtonChecked
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.calendar.CalendarSyncManager
import com.example.calendar.CalendarSyncResult
import com.example.data.models.PrayerItem
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.GoldSecondary
import com.example.ui.viewmodels.PrayerUiState
import com.example.ui.viewmodels.PrayerViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrayersScreen(
    prayerViewModel: PrayerViewModel,
    onNavigateToQibla: () -> Unit
) {
    val context = LocalContext.current
    val prayerState by prayerViewModel.prayerUiState.collectAsState()
    val calcMethod by prayerViewModel.currentCalcMethod.collectAsState()
    val juristicSchool by prayerViewModel.currentJuristicSchool.collectAsState()
    val cityName by prayerViewModel.currentCityName.collectAsState()
    val isCalendarSyncEnabled by prayerViewModel.isCalendarSyncEnabled.collectAsState()

    var syncStatusMessage by remember { mutableStateOf<String?>(null) }

    val calendarPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allGranted = permissions.values.all { it }
        if (allGranted) {
            prayerViewModel.toggleCalendarSync(true, context) { result ->
                when (result) {
                    is CalendarSyncResult.Success -> {
                        syncStatusMessage = "Successfully synced ${result.count} prayer reminders to '${result.calendarName}'"
                    }
                    is CalendarSyncResult.Error -> {
                        syncStatusMessage = result.message
                    }
                    else -> {}
                }
            }
        } else {
            syncStatusMessage = "Calendar permission required to sync reminders."
            prayerViewModel.toggleCalendarSync(false, context)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Prayer Schedule & Qibla",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = EmeraldPrimary
                ),
                actions = {
                    IconButton(onClick = onNavigateToQibla) {
                        Icon(
                            imageVector = Icons.Default.CompassCalibration,
                            contentDescription = "Qibla Compass",
                            tint = GoldSecondary
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
                .padding(16.dp)
        ) {
            // Location Header
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    shape = RoundedCornerShape(28.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = EmeraldPrimary
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = "Location",
                                tint = GoldSecondary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = cityName,
                                style = MaterialTheme.typography.titleLarge,
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        when (val state = prayerState) {
                            is PrayerUiState.Success -> {
                                Text(
                                    text = "${state.schedule.hijriDateFormatted} • ${state.schedule.gregorianDateFormatted}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = GoldAccent
                                )
                            }
                            else -> {}
                        }
                    }
                }
            }

            // Qibla Navigation Tile
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                        .clickable { onNavigateToQibla() },
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color.White
                    ),
                    border = androidx.compose.foundation.BorderStroke(1.dp, GoldSecondary.copy(alpha = 0.3f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = EmeraldPrimary,
                            modifier = Modifier.size(44.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.CompassCalibration,
                                    contentDescription = "Qibla",
                                    tint = GoldSecondary
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Live Qibla Compass",
                                style = MaterialTheme.typography.titleMedium,
                                color = EmeraldPrimary,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Find exact Makkah heading direction",
                                style = MaterialTheme.typography.bodyMedium,
                                color = EmeraldPrimary.copy(alpha = 0.6f)
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = EmeraldPrimary
                        ) {
                            Text(
                                text = "Open",
                                style = MaterialTheme.typography.labelMedium,
                                color = GoldSecondary,
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // Prayer List
            when (val state = prayerState) {
                is PrayerUiState.Loading -> {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = GoldSecondary)
                        }
                    }
                }
                is PrayerUiState.Success -> {
                    items(state.prayerItems) { prayer ->
                        PrayerTimeRowItem(prayer = prayer)
                        Spacer(modifier = Modifier.height(10.dp))
                    }
                }
                is PrayerUiState.Error -> {
                    item {
                        Text(
                            text = state.message,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }
            }

            // System Calendar Sync Tile
            item {
                Spacer(modifier = Modifier.height(16.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    border = androidx.compose.foundation.BorderStroke(1.dp, GoldSecondary.copy(alpha = 0.4f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = EmeraldPrimary.copy(alpha = 0.1f),
                                    modifier = Modifier.size(42.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Default.CalendarToday,
                                            contentDescription = "Calendar Sync",
                                            tint = EmeraldPrimary
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                Column {
                                    Text(
                                        text = "System Calendar Sync",
                                        style = MaterialTheme.typography.titleMedium,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "Add reminders to Google / Device Calendar",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                    )
                                }
                            }

                            Switch(
                                checked = isCalendarSyncEnabled,
                                onCheckedChange = { checked ->
                                    if (checked) {
                                        if (CalendarSyncManager.hasCalendarPermission(context)) {
                                            prayerViewModel.toggleCalendarSync(true, context) { result ->
                                                when (result) {
                                                    is CalendarSyncResult.Success -> {
                                                        syncStatusMessage = "Synced ${result.count} prayers to '${result.calendarName}'"
                                                    }
                                                    is CalendarSyncResult.Error -> {
                                                        syncStatusMessage = result.message
                                                    }
                                                    else -> {}
                                                }
                                            }
                                        } else {
                                            calendarPermissionLauncher.launch(CalendarSyncManager.REQUIRED_PERMISSIONS)
                                        }
                                    } else {
                                        prayerViewModel.toggleCalendarSync(false, context)
                                        syncStatusMessage = "Calendar sync disabled"
                                    }
                                },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = EmeraldPrimary,
                                    uncheckedThumbColor = Color.Gray,
                                    uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant
                                )
                            )
                        }

                        if (isCalendarSyncEnabled) {
                            Spacer(modifier = Modifier.height(12.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Button(
                                    onClick = {
                                        if (CalendarSyncManager.hasCalendarPermission(context)) {
                                            prayerViewModel.toggleCalendarSync(true, context) { result ->
                                                when (result) {
                                                    is CalendarSyncResult.Success -> {
                                                        syncStatusMessage = "Synced ${result.count} prayers to '${result.calendarName}'"
                                                    }
                                                    is CalendarSyncResult.Error -> {
                                                        syncStatusMessage = result.message
                                                    }
                                                    else -> {}
                                                }
                                            }
                                        } else {
                                            calendarPermissionLauncher.launch(CalendarSyncManager.REQUIRED_PERMISSIONS)
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = EmeraldPrimary,
                                        contentColor = GoldSecondary
                                    ),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Sync,
                                        contentDescription = "Sync Now",
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(text = "Sync Now", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }

                                syncStatusMessage?.let { msg ->
                                    Text(
                                        text = msg,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = EmeraldPrimary,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        modifier = Modifier.padding(start = 8.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Calculation Method & School Settings Card
            item {
                Spacer(modifier = Modifier.height(16.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = "Settings",
                                tint = GoldSecondary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Calculation Settings",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurface,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Asr Juristic School Toggle
                        Text(
                            text = "Asr Juristic Method",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(6.dp))

                        Row(modifier = Modifier.fillMaxWidth()) {
                            Surface(
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { prayerViewModel.updateJuristicSchool(0) },
                                shape = RoundedCornerShape(8.dp),
                                color = if (juristicSchool == 0) EmeraldPrimary else MaterialTheme.colorScheme.surfaceVariant
                            ) {
                                Text(
                                    text = "Shafi / Hanbali / Maliki",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = if (juristicSchool == 0) GoldSecondary else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(12.dp),
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Surface(
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { prayerViewModel.updateJuristicSchool(1) },
                                shape = RoundedCornerShape(8.dp),
                                color = if (juristicSchool == 1) EmeraldPrimary else MaterialTheme.colorScheme.surfaceVariant
                            ) {
                                Text(
                                    text = "Hanafi",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = if (juristicSchool == 1) GoldSecondary else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(12.dp),
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PrayerTimeRowItem(prayer: PrayerItem) {
    val containerBg = when {
        prayer.isNext -> EmeraldPrimary
        prayer.isPassed -> Color.White.copy(alpha = 0.6f)
        else -> Color.White
    }

    val textColor = when {
        prayer.isNext -> Color.White
        else -> EmeraldPrimary
    }

    val subTextColor = when {
        prayer.isNext -> GoldAccent
        else -> EmeraldPrimary.copy(alpha = 0.6f)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = containerBg),
        border = if (prayer.isNext) androidx.compose.foundation.BorderStroke(1.5.dp, GoldSecondary) else androidx.compose.foundation.BorderStroke(1.dp, Color(0x0D0A3A2A)),
        elevation = CardDefaults.cardElevation(defaultElevation = if (prayer.isNext) 4.dp else 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = prayer.name,
                        style = MaterialTheme.typography.titleMedium,
                        color = textColor,
                        fontWeight = if (prayer.isNext) FontWeight.Bold else FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "(${prayer.nameArabic})",
                        style = MaterialTheme.typography.bodyMedium,
                        color = subTextColor,
                        fontSize = 13.sp
                    )
                }
                if (prayer.isNext) {
                    Text(
                        text = "UPCOMING PRAYER",
                        style = MaterialTheme.typography.labelSmall,
                        color = GoldSecondary,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                }
            }

            Text(
                text = prayer.time,
                style = MaterialTheme.typography.titleLarge,
                color = if (prayer.isNext) GoldSecondary else textColor,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
