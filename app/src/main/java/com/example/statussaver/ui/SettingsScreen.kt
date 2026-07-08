package com.example.statussaver.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.statussaver.BgColor
import com.example.statussaver.PrimaryGreen
import com.example.statussaver.SurfaceHigh
import com.example.statussaver.SurfaceLow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(onNavigateBack: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "Settings", 
                        color = PrimaryGreen, 
                        fontWeight = FontWeight.Bold,
                        fontSize = 22.sp
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = PrimaryGreen)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BgColor)
            )
        },
        containerColor = BgColor
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Storage Section
            SettingsSection(title = "STORAGE") {
                SettingsItem(
                    icon = Icons.Default.DataUsage,
                    title = "Storage Usage",
                    subtitle = "Statuses consume less than 1% of your total storage.",
                    trailing = { Text("1.2 GB", color = Color.Gray, fontSize = 12.sp) }
                )
                Divider(color = SurfaceHigh)
                SettingsItem(
                    icon = Icons.Default.AutoAwesome,
                    title = "Auto-Save Statuses",
                    subtitle = "Automatically save viewed statuses",
                    trailing = {
                        Switch(
                            checked = true, 
                            onCheckedChange = {},
                            colors = SwitchDefaults.colors(checkedThumbColor = PrimaryGreen, checkedTrackColor = SurfaceHigh)
                        )
                    }
                )
            }

            // Preferences Section
            SettingsSection(title = "PREFERENCES") {
                SettingsItem(
                    icon = Icons.Default.DarkMode,
                    title = "Appearance",
                    subtitle = "Dark Mode",
                    trailing = {
                        Switch(
                            checked = true, 
                            onCheckedChange = {},
                            colors = SwitchDefaults.colors(checkedThumbColor = PrimaryGreen, checkedTrackColor = SurfaceHigh)
                        )
                    }
                )
                Divider(color = SurfaceHigh)
                SettingsItem(
                    icon = Icons.Default.Help,
                    title = "Help & Support",
                    subtitle = "FAQs, Contact us",
                    trailing = { Icon(Icons.Default.OpenInNew, contentDescription = null, tint = Color.Gray) }
                )
                Divider(color = SurfaceHigh)
                SettingsItem(
                    icon = Icons.Default.Info,
                    title = "About Status Saver",
                    subtitle = "Version 2.4.1 (Stable)",
                    trailing = { Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color.Gray) }
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))

            // Developer Credit
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Develop by Tayyab",
                    color = PrimaryGreen,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Reset All Settings",
                    color = Color(0xFFFFB4AB), // Error color for reset
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.clickable {  }
                )
            }
        }
    }
}

@Composable
fun SettingsSection(title: String, content: @Composable ColumnScope.() -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = title,
            color = PrimaryGreen,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 1.sp,
            modifier = Modifier.padding(start = 4.dp)
        )
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(SurfaceLow)
        ) {
            content()
        }
    }
}

@Composable
fun SettingsItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    trailing: @Composable () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Icon(icon, contentDescription = null, tint = Color.Gray)
        Column(modifier = Modifier.weight(1f)) {
            Text(title, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Medium)
            Text(subtitle, color = Color.Gray, fontSize = 12.sp)
        }
        trailing()
    }
}
