package com.example.statussaver

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import com.example.statussaver.data.StatusModel
import com.example.statussaver.ui.PreviewScreen
import com.example.statussaver.viewmodel.MainUiState
import com.example.statussaver.viewmodel.MainViewModel
import dagger.hilt.android.AndroidEntryPoint

// Colors from Stitch Design
val BgColor = Color(0xFF131313)
val PrimaryGreen = Color(0xFF25D366)
val OnPrimaryGreen = Color(0xFF005523)
val SurfaceLow = Color(0xFF1C1B1B)
val OnSurfaceVariant = Color(0xFFBBCBB9)

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    
    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        installSplashScreen()
        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = BgColor) {
                    val navController = rememberNavController()
                    NavHost(navController = navController, startDestination = "main") {
                        composable("main") {
                            MainScreen(
                                viewModel = viewModel, 
                                activity = this@MainActivity, 
                                onNavigateToPreview = { filter, index ->
                                    navController.navigate("preview/$filter/$index")
                                }
                            )
                        }
                        composable("preview/{filter}/{index}") { backStackEntry ->
                            val filter = backStackEntry.arguments?.getString("filter") ?: "Images"
                            val index = backStackEntry.arguments?.getString("index")?.toIntOrNull() ?: 0
                            PreviewScreen(viewModel, filter, index, onNavigateBack = { navController.popBackStack() })
                        }
                    }
                }
            }
        }
    }

    fun takePersistableUriPermission(uri: Uri) {
        val takeFlags: Int = Intent.FLAG_GRANT_READ_URI_PERMISSION or
                Intent.FLAG_GRANT_WRITE_URI_PERMISSION
        contentResolver.takePersistableUriPermission(uri, takeFlags)
        viewModel.saveUri(uri.toString())
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    viewModel: MainViewModel, 
    activity: MainActivity, 
    onNavigateToPreview: (String, Int) -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var selectedFilter by remember { mutableStateOf("Images") }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocumentTree()
    ) { uri: Uri? ->
        uri?.let { activity.takePersistableUriPermission(it) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "Status Saver", 
                        color = PrimaryGreen,
                        fontWeight = FontWeight.Bold
                    ) 
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BgColor)
            )
        },

        bottomBar = {
            NavigationBar(
                containerColor = SurfaceLow,
                tonalElevation = 0.dp
            ) {
                NavigationBarItem(
                    icon = { Icon(Icons.Default.PhotoLibrary, contentDescription = "Statuses") },
                    label = { Text("Statuses") },
                    selected = true,
                    onClick = { /* TODO */ },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = OnPrimaryGreen,
                        selectedTextColor = PrimaryGreen,
                        indicatorColor = PrimaryGreen,
                        unselectedIconColor = OnSurfaceVariant,
                        unselectedTextColor = OnSurfaceVariant
                    )
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Star, contentDescription = "Saved") },
                    label = { Text("Saved") },
                    selected = false,
                    onClick = { /* TODO */ },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = OnPrimaryGreen,
                        selectedTextColor = PrimaryGreen,
                        indicatorColor = PrimaryGreen,
                        unselectedIconColor = OnSurfaceVariant,
                        unselectedTextColor = OnSurfaceVariant
                    )
                )
            }
        },
        containerColor = BgColor
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Filter Tabs
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .background(SurfaceLow, RoundedCornerShape(12.dp))
                    .padding(4.dp)
            ) {
                FilterButton(
                    text = "Images",
                    isSelected = selectedFilter == "Images",
                    modifier = Modifier.weight(1f)
                ) { selectedFilter = "Images" }
                
                FilterButton(
                    text = "Videos",
                    isSelected = selectedFilter == "Videos",
                    modifier = Modifier.weight(1f)
                ) { selectedFilter = "Videos" }
            }

            // Main Content
            Box(modifier = Modifier.fillMaxSize()) {
                when (val state = uiState) {
                    is MainUiState.Loading -> {
                        CircularProgressIndicator(
                            color = PrimaryGreen,
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }
                    is MainUiState.PermissionRequired -> {
                        Column(
                            modifier = Modifier
                                .align(Alignment.Center)
                                .padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("Permission required", color = Color.White)
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(
                                onClick = { 
                                    launcher.launch(Uri.parse("content://com.android.externalstorage.documents/tree/primary%3AAndroid%2Fmedia/document/primary%3AAndroid%2Fmedia%2Fcom.whatsapp%2FWhatsApp%2FMedia%2F.Statuses")) 
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen)
                            ) {
                                Text("Link WhatsApp", color = OnPrimaryGreen)
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(
                                onClick = { 
                                    launcher.launch(Uri.parse("content://com.android.externalstorage.documents/tree/primary%3AAndroid%2Fmedia/document/primary%3AAndroid%2Fmedia%2Fcom.whatsapp.w4b%2FWhatsApp%20Business%2FMedia%2F.Statuses")) 
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen)
                            ) {
                                Text("Link WA Business", color = OnPrimaryGreen)
                            }
                        }
                    }
                    is MainUiState.Success -> {
                        val filteredStatuses = remember(state.statuses, selectedFilter) {
                            state.statuses.filter { 
                                if (selectedFilter == "Images") !it.isVideo else it.isVideo 
                            }
                        }

                        if (filteredStatuses.isEmpty()) {
                            Text(
                                "No statuses found.",
                                color = OnSurfaceVariant,
                                modifier = Modifier.align(Alignment.Center)
                            )
                        } else {
                            LazyVerticalGrid(
                                columns = GridCells.Fixed(2),
                                contentPadding = PaddingValues(16.dp),
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp),
                                modifier = Modifier.fillMaxSize()
                            ) {
                                items(filteredStatuses.size) { index ->
                                    val status = filteredStatuses[index]
                                    StatusCard(status) { onNavigateToPreview(selectedFilter, index) }
                                }
                            }
                        }
                    }
                    is MainUiState.Error -> {
                        Text(
                            "Error: ${state.message}", 
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun FilterButton(
    text: String,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val bgColor = if (isSelected) PrimaryGreen else Color.Transparent
    val textColor = if (isSelected) OnPrimaryGreen else OnSurfaceVariant

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(bgColor)
            .clickable { onClick() }
            .padding(vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = textColor,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun StatusCard(status: StatusModel, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .aspectRatio(9f / 16f)
            .clip(RoundedCornerShape(16.dp))
            .background(SurfaceLow)
            .clickable { onClick() }
    ) {
        AsyncImage(
            model = status.uri,
            contentDescription = "Status Media",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
        
        // Glass gradient at bottom
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .height(60.dp)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.7f))
                    )
                )
        )
        
        if (status.isVideo) {
            Icon(
                Icons.Default.PlayCircle,
                contentDescription = "Play Video",
                tint = Color.White.copy(alpha = 0.9f),
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(48.dp)
            )
        }
        
        // Time or info
        Text(
            text = if (status.isVideo) "Video" else "Image",
            color = Color.White,
            style = MaterialTheme.typography.labelSmall,
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(8.dp)
        )
    }
}
