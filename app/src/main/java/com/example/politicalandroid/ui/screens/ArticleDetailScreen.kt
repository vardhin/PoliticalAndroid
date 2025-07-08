package com.example.politicalandroid.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.politicalandroid.viewmodel.ArticleDetailViewModel
import com.example.politicalandroid.viewmodel.AuthViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArticleDetailScreen(
    articleId: Int,
    onNavigateBack: () -> Unit,
    authViewModel: AuthViewModel
) {
    val context = LocalContext.current
    val viewModel = remember { ArticleDetailViewModel(context) }
    val uiState by viewModel.uiState.collectAsState()
    val authState by authViewModel.uiState.collectAsState()
    
    // Image picker launcher
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { viewModel.updateSelectedImage(it, context) }
    }
    
    // Handle navigation back on successful delete
    LaunchedEffect(uiState.deleteSuccess) {
        if (uiState.deleteSuccess) {
            onNavigateBack()
        }
    }
    
    LaunchedEffect(articleId) {
        viewModel.loadArticle(articleId)
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Article Details") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (authState.isLoggedIn && authState.user?.role == "admin") {
                        // Delete button
                        IconButton(
                            onClick = { viewModel.showDeleteConfirmation() },
                            enabled = !uiState.isDeleting
                        ) {
                            if (uiState.isDeleting) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Icon(
                                    Icons.Default.Delete,
                                    contentDescription = "Delete Article",
                                    tint = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                        
                        // Edit button
                        IconButton(
                            onClick = { viewModel.toggleEditMode() }
                        ) {
                            Icon(
                                imageVector = if (uiState.isEditMode) Icons.Default.Visibility else Icons.Default.Edit,
                                contentDescription = if (uiState.isEditMode) "View Mode" else "Edit Mode"
                            )
                        }
                    }
                }
            )
        }
    ) { innerPadding ->
        
        // Authentication Error Dialog
        if (uiState.requiresReauth) {
            AlertDialog(
                onDismissRequest = { viewModel.clearAuthError() },
                title = { 
                    Text(
                        "Authentication Required",
                        color = MaterialTheme.colorScheme.error
                    ) 
                },
                text = { 
                    Text("Your session has expired. Please login again to continue.")
                },
                confirmButton = {
                    Button(
                        onClick = { 
                            viewModel.clearAuthError()
                            authViewModel.logout()
                            onNavigateBack()
                        }
                    ) {
                        Text("Login Again")
                    }
                },
                dismissButton = {
                    OutlinedButton(
                        onClick = { viewModel.clearAuthError() }
                    ) {
                        Text("Cancel")
                    }
                }
            )
        }
        
        // Delete Confirmation Dialog
        if (uiState.showDeleteConfirm) {
            AlertDialog(
                onDismissRequest = { viewModel.hideDeleteConfirmation() },
                title = { 
                    Text(
                        "Delete Article",
                        color = MaterialTheme.colorScheme.error
                    ) 
                },
                text = { 
                    Column {
                        Text("Are you sure you want to delete this article?")
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "\"${uiState.article?.title ?: ""}\"",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "This action cannot be undone.",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = { viewModel.deleteArticle() },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Text("Delete", color = Color.White)
                    }
                },
                dismissButton = {
                    OutlinedButton(
                        onClick = { viewModel.hideDeleteConfirmation() }
                    ) {
                        Text("Cancel")
                    }
                }
            )
        }
        
        when {
            uiState.isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            
            uiState.errorMessage != null -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        )
                    ) {
                        Text(
                            text = uiState.errorMessage!!,
                            modifier = Modifier.padding(16.dp),
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }
            }
            
            uiState.article != null -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    val article = uiState.article!!
                    
                    // Image Section
                    Card(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column {
                            Box {
                                AsyncImage(
                                    model = uiState.selectedImageUri ?: article.imageUrl,
                                    contentDescription = "Article Image",
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(200.dp)
                                        .clip(RoundedCornerShape(8.dp)),
                                    contentScale = ContentScale.Crop
                                )
                                
                                if (uiState.isEditMode) {
                                    IconButton(
                                        onClick = { imagePickerLauncher.launch("image/*") },
                                        modifier = Modifier
                                            .align(Alignment.BottomEnd)
                                            .padding(8.dp)
                                    ) {
                                        Surface(
                                            color = MaterialTheme.colorScheme.primary,
                                            shape = RoundedCornerShape(50)
                                        ) {
                                            Icon(
                                                Icons.Default.PhotoCamera,
                                                contentDescription = "Change Image",
                                                modifier = Modifier.padding(8.dp),
                                                tint = MaterialTheme.colorScheme.onPrimary
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                    
                    // Category and Date
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (uiState.isEditMode) {
                            OutlinedTextField(
                                value = uiState.editableArticle?.category ?: article.category,
                                onValueChange = { viewModel.updateCategory(it) },
                                label = { Text("Category") },
                                modifier = Modifier.weight(1f)
                            )
                        } else {
                            AssistChip(
                                onClick = { },
                                label = { Text(article.category) }
                            )
                        }
                        
                        Spacer(modifier = Modifier.width(8.dp))
                        
                        if (uiState.isEditMode) {
                            OutlinedTextField(
                                value = uiState.editableArticle?.date ?: article.date,
                                onValueChange = { viewModel.updateDate(it) },
                                label = { Text("Date") },
                                modifier = Modifier.weight(1f)
                            )
                        } else {
                            Text(
                                text = article.date,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    
                    // Featured Status Section
                    if (uiState.isEditMode) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.secondaryContainer
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text(
                                        text = "Featured Article",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Text(
                                        text = "Show this article in featured section",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f)
                                    )
                                }
                                Switch(
                                    checked = uiState.editableArticle?.featured ?: article.featured,
                                    onCheckedChange = { viewModel.updateFeatured(it) }
                                )
                            }
                        }
                    } else if (article.featured) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer
                            )
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.Star,
                                    contentDescription = "Featured",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Featured Article",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }
                    }
                    
                    // Title Section
                    if (uiState.isEditMode) {
                        OutlinedTextField(
                            value = uiState.editableArticle?.title ?: article.title,
                            onValueChange = { viewModel.updateTitle(it) },
                            label = { Text("Title") },
                            modifier = Modifier.fillMaxWidth(),
                            textStyle = MaterialTheme.typography.headlineSmall.copy(
                                fontWeight = FontWeight.Bold
                            )
                        )
                    } else {
                        Text(
                            text = article.title,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    
                    // Summary Section
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "Summary",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            if (uiState.isEditMode) {
                                OutlinedTextField(
                                    value = uiState.editableArticle?.summary ?: article.excerpt,
                                    onValueChange = { viewModel.updateSummary(it) },
                                    modifier = Modifier.fillMaxWidth(),
                                    minLines = 3,
                                    maxLines = 6
                                )
                            } else {
                                Text(
                                    text = article.excerpt,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                    }
                    
                    // Content Section
                    Card(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "Article Content",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            if (uiState.isEditMode) {
                                OutlinedTextField(
                                    value = uiState.editableArticle?.content ?: article.content,
                                    onValueChange = { viewModel.updateContent(it) },
                                    modifier = Modifier.fillMaxWidth(),
                                    minLines = 10,
                                    maxLines = 20,
                                    placeholder = { Text("Enter the full article content here...") }
                                )
                            } else {
                                Text(
                                    text = article.content,
                                    style = MaterialTheme.typography.bodyMedium,
                                    lineHeight = MaterialTheme.typography.bodyMedium.lineHeight
                                )
                            }
                        }
                    }
                    
                    // Action Buttons for Edit Mode
                    if (uiState.isEditMode) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = { viewModel.resetChanges() },
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.Refresh, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Reset")
                            }
                            
                            Button(
                                onClick = { viewModel.saveChanges() },
                                modifier = Modifier.weight(1f),
                                enabled = uiState.hasChanges && !uiState.isSaving
                            ) {
                                if (uiState.isSaving) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(16.dp),
                                        strokeWidth = 2.dp
                                    )
                                } else {
                                    Icon(Icons.Default.CloudUpload, contentDescription = null)
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(if (uiState.isSaving) "Saving..." else "Save & Upload")
                            }
                        }
                    }
                    
                    // Success/Error Messages
                    uiState.saveMessage?.let { message ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = if (uiState.saveSuccess) 
                                    MaterialTheme.colorScheme.primaryContainer 
                                else 
                                    MaterialTheme.colorScheme.errorContainer
                            )
                        ) {
                            Text(
                                text = message,
                                modifier = Modifier.padding(16.dp),
                                color = if (uiState.saveSuccess) 
                                    MaterialTheme.colorScheme.onPrimaryContainer 
                                else 
                                    MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                    }
                }
            }
        }
    }
}