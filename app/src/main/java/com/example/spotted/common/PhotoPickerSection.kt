// PhotoPickerSection.kt
package com.example.spotted.utils

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import coil.compose.AsyncImage
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.example.spotted.R
import java.io.File

/**
 * Componente per la selezione di una foto.
 *
 * Mostra un'anteprima dell'immagine selezionata (o un placeholder) e due
 * pulsanti che permettono di scattare una foto con la fotocamera o di
 * scegliere un'immagine dalla galleria.
 *
 * PREREQUISITI nel progetto:
 *  1. Aggiungere in AndroidManifest.xml dentro <application>:
 *       <provider
 *           android:name="androidx.core.content.FileProvider"
 *           android:authorities="${applicationId}.provider"
 *           android:exported="false"
 *           android:grantUriPermissions="true">
 *           <meta-data
 *               android:name="android.support.FILE_PROVIDER_PATHS"
 *               android:resource="@xml/file_provider_paths" />
 *       </provider>
 *
 *  2. Creare res/xml/file_provider_paths.xml:
 *       <?xml version="1.0" encoding="utf-8"?>
 *       <paths>
 *           <cache-path name="camera_photos" path="." />
 *       </paths>
 *
 *  3. Aggiungere in AndroidManifest.xml (fuori da <application>):
 *       <uses-permission android:name="android.permission.CAMERA" />
 *       <uses-permission android:name="android.permission.READ_MEDIA_IMAGES" />
 *       <uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE"
 *           android:maxSdkVersion="32" />
 *
 *  4. Dipendenza Coil in build.gradle (se non già presente):
 *       implementation("io.coil-kt:coil-compose:2.6.0")
 */
@Composable
fun PhotoPickerSection(
    selectedUri: Uri?,
    onPhotoSelected: (Uri) -> Unit,
    onPhotoRemoved: () -> Unit,
    enabled: Boolean = true
) {
    val context = LocalContext.current

    // URI temporaneo per la fotocamera: viene ricreato solo alla prima composizione
    val cameraUri: Uri = remember {
        val tempFile = File(context.cacheDir, "spotted_temp_photo.jpg")
        FileProvider.getUriForFile(context, "${context.packageName}.provider", tempFile)
    }

    // ── Launcher fotocamera ──────────────────────────────────────────────
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) onPhotoSelected(cameraUri)
    }

    // ── Launcher galleria ────────────────────────────────────────────────
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let { onPhotoSelected(it) }
    }

    // ── Launcher permesso fotocamera ─────────────────────────────────────
    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) cameraLauncher.launch(cameraUri)
    }

    // ── Launcher permesso storage (galleria) ─────────────────────────────
    val storagePermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) galleryLauncher.launch("image/*")
    }

    // ── Funzioni di avvio con controllo permessi ─────────────────────────
    fun launchCamera() {
        when {
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA)
                    == PackageManager.PERMISSION_GRANTED -> cameraLauncher.launch(cameraUri)
            else -> cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    fun launchGallery() {
        val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
            Manifest.permission.READ_MEDIA_IMAGES
        else
            Manifest.permission.READ_EXTERNAL_STORAGE

        when {
            ContextCompat.checkSelfPermission(context, permission)
                    == PackageManager.PERMISSION_GRANTED -> galleryLauncher.launch("image/*")
            else -> storagePermissionLauncher.launch(permission)
        }
    }

    // ── UI ───────────────────────────────────────────────────────────────
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {

        // Anteprima o placeholder
        if (selectedUri != null) {
            Box(modifier = Modifier.fillMaxWidth()) {
                AsyncImage(
                    model = selectedUri,
                    contentDescription = "Foto selezionata",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clip(RoundedCornerShape(12.dp)),
                    contentScale = ContentScale.Crop
                )
                // Pulsante rimozione foto in alto a destra
                IconButton(
                    onClick = onPhotoRemoved,
                    enabled = enabled,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Rimuovi foto",
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier
                            .size(20.dp)
                    )
                }
            }
        } else {
            // Placeholder quando non è selezionata nessuna foto
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .border(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
                        shape = RoundedCornerShape(12.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.AddCircle,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.35f),
                        modifier = Modifier.size(44.dp)
                    )
                    Text(
                        text = "Nessuna foto selezionata",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.55f)
                    )
                }
            }
        }

        // Pulsanti fotocamera / galleria
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = { launchCamera() },
                modifier = Modifier.weight(1f),
                enabled = enabled
            ) {
                Icon(
                    painterResource(R.drawable.add_a_photo_24px),
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(Modifier.width(6.dp))
                Text("Fotocamera")
            }
            OutlinedButton(
                onClick = { launchGallery() },
                modifier = Modifier.weight(1f),
                enabled = enabled
            ) {
                Icon(
                    painterResource(R.drawable.add_photo_alternate_24px),
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(Modifier.width(6.dp))
                Text("Galleria")
            }
        }
    }
}
