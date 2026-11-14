package com.example.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.PlusOne
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.models.AzkarItem
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.GoldSecondary
import com.example.ui.viewmodels.AzkarViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AzkarToolsScreen(
    azkarViewModel: AzkarViewModel
) {
    val selectedCategory by azkarViewModel.selectedCategory.collectAsState()
    val azkarList by azkarViewModel.azkarList.collectAsState()
    val tasbeehCount by azkarViewModel.tasbeehCount.collectAsState()
    val tasbeehTarget by azkarViewModel.tasbeehTarget.collectAsState()

    var selectedMainTab by remember { mutableIntStateOf(0) } // 0 = Daily Azkar, 1 = Digital Tasbeeh

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Azkar & Digital Tasbeeh",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = EmeraldPrimary
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Main Section Tabs: Daily Azkar / Digital Tasbeeh
            TabRow(
                selectedTabIndex = selectedMainTab,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = GoldSecondary,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        modifier = Modifier.tabIndicatorOffset(tabPositions[selectedMainTab]),
                        color = GoldSecondary
                    )
                }
            ) {
                Tab(
                    selected = selectedMainTab == 0,
                    onClick = { selectedMainTab = 0 },
                    text = {
                        Text(
                            text = "Daily Azkar",
                            fontWeight = if (selectedMainTab == 0) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                )
                Tab(
                    selected = selectedMainTab == 1,
                    onClick = { selectedMainTab = 1 },
                    text = {
                        Text(
                            text = "Digital Tasbeeh",
                            fontWeight = if (selectedMainTab == 1) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                )
            }

            if (selectedMainTab == 0) {
                // Category Selector Chips
                TabRow(
                    selectedTabIndex = azkarViewModel.categories.indexOf(selectedCategory).coerceAtLeast(0),
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = EmeraldPrimary
                ) {
                    azkarViewModel.categories.forEach { category ->
                        Tab(
                            selected = selectedCategory == category,
                            onClick = { azkarViewModel.selectCategory(category) },
                            text = {
                                Text(
                                    text = category,
                                    fontSize = 13.sp,
                                    fontWeight = if (selectedCategory == category) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        )
                    }
                }

                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    items(azkarList) { azkar ->
                        AzkarItemCard(
                            azkar = azkar,
                            onIncrement = { azkarViewModel.incrementAzkarCount(azkar.id) },
                            onReset = { azkarViewModel.resetAzkarCount(azkar.id) }
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                    }
                }
            } else {
                // Digital Tasbeeh Counter View
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    // Target Selector
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        listOf(33, 100, 500).forEach { target ->
                            Surface(
                                modifier = Modifier
                                    .padding(horizontal = 6.dp)
                                    .clickable { azkarViewModel.setTasbeehTarget(target) },
                                shape = RoundedCornerShape(20.dp),
                                color = if (tasbeehTarget == target) EmeraldPrimary else MaterialTheme.colorScheme.surfaceVariant
                            ) {
                                Text(
                                    text = "$target Target",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = if (tasbeehTarget == target) GoldSecondary else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    // Interactive Circular Tasbeeh Counter Button
                    Card(
                        modifier = Modifier
                            .size(240.dp)
                            .clickable { azkarViewModel.incrementTasbeeh() },
                        shape = CircleShape,
                        colors = CardDefaults.cardColors(
                            containerColor = EmeraldPrimary
                        ),
                        border = androidx.compose.foundation.BorderStroke(4.dp, GoldSecondary),
                        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = tasbeehCount.toString(),
                                    style = MaterialTheme.typography.displayLarge,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 54.sp
                                )
                                Text(
                                    text = "/ $tasbeehTarget",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = GoldAccent
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "TAP TO COUNT",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = GoldSecondary,
                                    letterSpacing = 1.5.sp
                                )
                            }
                        }
                    }

                    // Reset Button
                    IconButton(
                        onClick = { azkarViewModel.resetTasbeeh() },
                        modifier = Modifier.size(56.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Reset Tasbeeh",
                            tint = EmeraldPrimary,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AzkarItemCard(
    azkar: AzkarItem,
    onIncrement: () -> Unit,
    onReset: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = azkar.arabicText,
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 20.sp,
                textAlign = TextAlign.Right,
                modifier = Modifier.fillMaxWidth(),
                lineHeight = 32.sp
            )

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = azkar.translation,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 18.sp
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Ref: ${azkar.reference}",
                style = MaterialTheme.typography.labelSmall,
                color = GoldSecondary
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onReset) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Reset",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Surface(
                    modifier = Modifier.clickable { onIncrement() },
                    shape = RoundedCornerShape(20.dp),
                    color = if (azkar.currentCount >= azkar.targetCount) GoldSecondary else EmeraldPrimary
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 18.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${azkar.currentCount} / ${azkar.targetCount}",
                            style = MaterialTheme.typography.titleMedium,
                            color = if (azkar.currentCount >= azkar.targetCount) EmeraldPrimary else Color.White,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Add",
                            tint = if (azkar.currentCount >= azkar.targetCount) EmeraldPrimary else GoldSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}
