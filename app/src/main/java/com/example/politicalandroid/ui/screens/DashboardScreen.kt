package com.example.politicalandroid.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.politicalandroid.viewmodel.AuthViewModel
import com.example.politicalandroid.viewmodel.DashboardViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    onLogout: () -> Unit,
    viewModel: AuthViewModel
) {
    val authUiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val dashboardViewModel = remember { DashboardViewModel(context) }
    val dashboardUiState by dashboardViewModel.uiState.collectAsState()
    
    // Form state
    var title by remember { mutableStateOf("") }
    var summary by remember { mutableStateOf("") }
    var articleText by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("General") }
    var featured by remember { mutableStateOf(false) }
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    
    // Image picker
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        imageUri = uri
        dashboardViewModel.clearMessages()
    }
    
    // Handle logout
    LaunchedEffect(authUiState.isLoggedIn) {
        if (!authUiState.isLoggedIn) {
            onLogout()
        }
    }
    
    // Clear form on successful submission
    LaunchedEffect(dashboardUiState.submitSuccess) {
        if (dashboardUiState.submitSuccess) {
            title = ""
            summary = ""
            articleText = ""
            category = "General"
            featured = false
            imageUri = null
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Admin Dashboard") },
                actions = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Icon(Icons.Default.Person, contentDescription = null)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Welcome, ${authUiState.user?.username ?: "User"}",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        IconButton(onClick = { viewModel.logout() }) {
                            Icon(Icons.Default.ExitToApp, contentDescription = "Logout")
                        }
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp)
                ) {
                    Text(
                        text = "Create New Article",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Error Message
                    dashboardUiState.errorMessage?.let { errorMsg ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.errorContainer
                            )
                        ) {
                            Text(
                                text = errorMsg,
                                modifier = Modifier.padding(16.dp),
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                    
                    // Success Message
                    dashboardUiState.submitMessage?.let { message ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer
                            )
                        ) {
                            Text(
                                text = message,
                                modifier = Modifier.padding(16.dp),
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                    
                    // Title Field
                    OutlinedTextField(
                        value = title,
                        onValueChange = { 
                            title = it
                            dashboardViewModel.clearMessages()
                        },
                        label = { Text("Title") },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !dashboardUiState.isSubmitting
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Summary Field
                    OutlinedTextField(
                        value = summary,
                        onValueChange = { 
                            summary = it
                            dashboardViewModel.clearMessages()
                        },
                        label = { Text("Summary") },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !dashboardUiState.isSubmitting,
                        minLines = 3,
                        maxLines = 5
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Article Content Field
                    OutlinedTextField(
                        value = articleText,
                        onValueChange = { 
                            articleText = it
                            dashboardViewModel.clearMessages()
                        },
                        label = { Text("Article Content") },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !dashboardUiState.isSubmitting,
                        minLines = 8,
                        maxLines = 15
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Image Upload Section
                    Column {
                        Text(
                            text = "Image Upload",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Button(
                            onClick = { imagePickerLauncher.launch("image/*") },
                            enabled = !dashboardUiState.isSubmitting,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Select Image")
                        }
                        
                        imageUri?.let { uri ->
                            Spacer(modifier = Modifier.height(8.dp))
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(200.dp)
                            ) {
                                AsyncImage(
                                    model = uri,
                                    contentDescription = "Selected image",
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Category and Featured Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Category Dropdown
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Category",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Medium
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            var expanded by remember { mutableStateOf(false) }
                            
                            ExposedDropdownMenuBox(
                                expanded = expanded,
                                onExpandedChange = { expanded = !expanded }
                            ) {
                                OutlinedTextField(
                                    value = category,
                                    onValueChange = {},
                                    readOnly = true,
                                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .menuAnchor(),
                                    enabled = !dashboardUiState.isSubmitting
                                )
                                
                                ExposedDropdownMenu(
                                    expanded = expanded,
                                    onDismissRequest = { expanded = false }
                                ) {
                                    listOf("General", "Political").forEach { option ->
                                        DropdownMenuItem(
                                            text = { Text(option) },
                                            onClick = {
                                                category = option
                                                expanded = false
                                                dashboardViewModel.clearMessages()
                                            }
                                        )
                                    }
                                }
                            }
                        }
                        
                        // Featured Checkbox
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Options",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Medium
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Checkbox(
                                    checked = featured,
                                    onCheckedChange = { 
                                        featured = it
                                        dashboardViewModel.clearMessages()
                                    },
                                    enabled = !dashboardUiState.isSubmitting
                                )
                                Text("Featured Article")
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    // Submit Button
                    Button(
                        onClick = {
                            when {
                                title.isBlank() -> dashboardViewModel.clearMessages()
                                summary.isBlank() -> dashboardViewModel.clearMessages()
                                articleText.isBlank() -> dashboardViewModel.clearMessages()
                                imageUri == null -> dashboardViewModel.clearMessages()
                                else -> {
                                    dashboardViewModel.createArticle(
                                        title = title,
                                        summary = summary,
                                        articleText = articleText,
                                        category = category,
                                        featured = featured,
                                        imageUri = imageUri!!
                                    )
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !dashboardUiState.isSubmitting && 
                                title.isNotBlank() && 
                                summary.isNotBlank() && 
                                articleText.isNotBlank() && 
                                imageUri != null
                    ) {
                        if (dashboardUiState.isSubmitting) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                        }
                        Text(if (dashboardUiState.isSubmitting) "Publishing..." else "Publish Article")
                    }
                }
            }
        }
    }
}