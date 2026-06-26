package com.example.spotted.pages

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.spotted.R
import com.example.spotted.ui.theme.ThemeMode
import com.example.spotted.viewmodel.ProfileViewModel
import org.koin.androidx.compose.koinViewModel
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    innerPadding: PaddingValues,
    navigate: () -> Unit
) {
    val viewModel: ProfileViewModel = koinViewModel()
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    // Stati per la gestione della foto e dei permessi
    var showDialogChooser by remember { mutableStateOf(false) }
    var cameraImageUri by remember { mutableStateOf<Uri?>(null) }

    // Funzione helper per convertire Uri in ByteArray (MimeType fisso o dinamico)
    fun handleSelectedUri(uri: Uri) {
        try {
            val inputStream = context.contentResolver.openInputStream(uri)
            val bytes = inputStream?.readBytes()
            inputStream?.close()
            if (bytes != null) {
                viewModel.changeProfilePicture(bytes, "image/jpeg")
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // Launcher Galleria
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { handleSelectedUri(it) }
    }

    // Launcher Fotocamera
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success: Boolean ->
        if (success) {
            cameraImageUri?.let { handleSelectedUri(it) }
        }
    }

    // Richiesta permessi Fotocamera
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            val uri = createImageUri(context)
            cameraImageUri = uri
            cameraLauncher.launch(uri)
        }
    }

    if (state.isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)
            .verticalScroll(rememberScrollState())
    ) {
        // ── Sezione Avatar interattiva ───────────────────────────────
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Struttura a Box sovrapposti (Avatar + Bottone di Edit in basso a destra)
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .clickable { showDialogChooser = true },
                contentAlignment = Alignment.BottomEnd
            ) {
                // Cerchio Principale Avatar
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    if (!state.user?.profilePicture.isNullOrEmpty()) {
                        AsyncImage(
                            model = state.user?.profilePicture,
                            contentDescription = "Profile Picture",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Icon(
                            painter = painterResource(R.drawable.remove_24px),
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.6f)
                        )
                    }

                    // Loader in overlay se l'immagine si sta caricando
                    if (state.isImageUploading) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.Black.copy(alpha = 0.4f)),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(30.dp))
                        }
                    }
                }

                // Badge della Matita posizionato in basso a destra
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary)
                        .border(2.dp, MaterialTheme.colorScheme.surface, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Create,
                        contentDescription = "Modifica foto",
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(Modifier.height(14.dp))

            val displayName = state.user?.let { user ->
                listOfNotNull(user.name, user.surname).joinToString(" ")
                    .ifBlank { user.email }
            } ?: "Utente Sconosciuto"

            Text(
                text = displayName,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            Spacer(Modifier.height(8.dp))

            Surface(
                shape = RoundedCornerShape(50),
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
            ) {
                Text(
                    text = "Campus di Bologna",
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }



        Spacer(Modifier.height(24.dp))

        // ── Impostazioni app ─────────────────────────────────────
        MySectionLabel("IMPOSTAZIONI APP")

        SettingsCard {
            ThemeSelectorItem(
                currentTheme = state.currentTheme,
                onThemeSelected = { viewModel.updateTheme(it) }
            )
            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
            LinkItem(
                painter = Icons.AutoMirrored.Filled.ExitToApp,
                label = "Esci",
                onClick = {
                    viewModel.logout { navigate() }
                }
            )
        }

        // Messaggio di errore se presente
        state.errorMessage?.let { msg ->
            Text(
                text = msg,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(16.dp),
                style = MaterialTheme.typography.bodySmall
            )
        }
    }

    // Dialog Classico di scelta sorgente Foto (Galleria o Fotocamera)
    if (showDialogChooser) {
        AlertDialog(
            onDismissRequest = { showDialogChooser = false },
            title = { Text("Aggiorna foto profilo") },
            text = { Text("Scegli se scattare una nuova foto o selezionarla dalla galleria.") },
            confirmButton = {
                TextButton(onClick = {
                    showDialogChooser = false
                    galleryLauncher.launch("image/*")
                }) {
                    Text("Galleria")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showDialogChooser = false
                    val permissionCheck = ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA)
                    if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
                        val uri = createImageUri(context)
                        cameraImageUri = uri
                        cameraLauncher.launch(uri)
                    } else {
                        permissionLauncher.launch(Manifest.permission.CAMERA)
                    }
                }) {
                    Text("Fotocamera")
                }
            }
        )
    }
}

// Funzione di utilità per creare un URI temporaneo per la fotocamera
private fun createImageUri(context: Context): Uri {
    // Il file viene creato in cacheDir, che corrisponde perfettamente a <cache-path> nel tuo file_provider_paths.xml
    val tempFile = File.createTempFile("avatar_capture_", ".jpg", context.cacheDir).apply {
        createNewFile()
        deleteOnExit()
    }

    return FileProvider.getUriForFile(
        context,
        "${context.packageName}.provider", // <--- AGGIORNATO: ora coincide perfettamente con il tuo Manifest!
        tempFile
    )
}

// ── Componenti privati immutati ──────────────────────────────────

@Composable
private fun MySectionLabel(text: String) {
    Text(text = text, modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 8.dp), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, letterSpacing = 1.sp)
}

@Composable
private fun SettingsCard(content: @Composable () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp), shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)) { content() }
}

@Composable
private fun LinkItem(painter: ImageVector, label: String, onClick: () -> Unit) {
    Row(modifier = Modifier.fillMaxWidth().clickable(onClick = onClick).padding(horizontal = 16.dp, vertical = 16.dp), verticalAlignment = Alignment.CenterVertically) {
        Icon(painter, contentDescription = null)
        Spacer(Modifier.width(14.dp))
        Text(text = label, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
        Icon(imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun ThemeSelectorItem(currentTheme: ThemeMode, onThemeSelected: (ThemeMode) -> Unit) {
    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 14.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            Icon(imageVector = ImageVector.vectorResource(R.drawable.dark_mode_24px), contentDescription = null)
            Spacer(Modifier.width(14.dp))
            Text(text = "Tema Applicazione", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
        }
        Spacer(Modifier.height(12.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            ThemeOptionButton(modifier = Modifier.weight(1f), label = "Auto", isSelected = currentTheme == ThemeMode.SYSTEM, onClick = { onThemeSelected(ThemeMode.SYSTEM) })
            ThemeOptionButton(modifier = Modifier.weight(1f), label = "Chiaro", isSelected = currentTheme == ThemeMode.LIGHT, onClick = { onThemeSelected(ThemeMode.LIGHT) })
            ThemeOptionButton(modifier = Modifier.weight(1f), label = "Scuro", isSelected = currentTheme == ThemeMode.DARK, onClick = { onThemeSelected(ThemeMode.DARK) })
        }
    }
}

@Composable
private fun ThemeOptionButton(modifier: Modifier = Modifier, label: String, isSelected: Boolean, onClick: () -> Unit) {
    Surface(modifier = modifier.height(36.dp), shape = RoundedCornerShape(50), color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent, border = if (!isSelected) BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant) else null, onClick = onClick) {
        Box(contentAlignment = Alignment.Center) {
            Text(text = label, style = MaterialTheme.typography.bodyMedium, color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface)
        }
    }
}