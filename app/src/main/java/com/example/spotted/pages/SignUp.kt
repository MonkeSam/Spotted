package com.example.spotted.pages

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.LocalActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import coil.compose.AsyncImage
import com.example.spotted.data.view.SignupViewModel
import com.example.spotted.ui.theme.AppLogo
import com.example.spotted.utils.rememberPermissionDeniedHandler
import org.koin.androidx.compose.koinViewModel
import java.io.File

@Composable
fun SignupScreen(
    innerPadding: PaddingValues,
    onNavigateSuccess: () -> Unit,
    onNavigateToLogin: () -> Unit
) {
    val context = LocalContext.current
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    var surname by remember { mutableStateOf("") }
    val activity = LocalActivity.current!!
    val (snackbarHostState, onPermissionDenied) = rememberPermissionDeniedHandler()
    // Stati per la gestione della foto e dei permessi
    var showDialogChooser by remember { mutableStateOf(false) }
    var cameraImageUri by remember { mutableStateOf<Uri?>(null) }
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    var localError by remember { mutableStateOf<String?>(null) }

    val viewModel: SignupViewModel = koinViewModel()
    val isLoading by viewModel.isLoading.collectAsState()
    val serverError by viewModel.error.collectAsState()
    val user by viewModel.user.collectAsState()
    var emailError by remember { mutableStateOf(false) }
    var imageError by remember { mutableStateOf(false) }

    // ── Launchers ────────────────────────────────────────────────────────
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            imageUri = uri
        }
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success: Boolean ->
        if (success && cameraImageUri != null) {
            imageUri = cameraImageUri
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            val uri = createSignupImageUri(context) // createImageUri in Profile.kt
            cameraImageUri = uri
            cameraLauncher.launch(uri)
        } else {
            val permanentlyDenied = !activity.shouldShowRequestPermissionRationale(
                Manifest.permission.CAMERA
            )
            onPermissionDenied(permanentlyDenied) // ← una sola riga
        }
    }

    LaunchedEffect(user) {
        if (user != null) onNavigateSuccess()
    }

    Box(modifier = Modifier.fillMaxSize()){
        Box(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
            contentAlignment = Alignment.Center
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 30.dp)
            ) {
                Column(
                    modifier = Modifier
                        .background(MaterialTheme.colorScheme.primaryContainer)
                        .padding(16.dp)
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    AppLogo(
                        iconSize = 42,
                        textSize = 30,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    // 📸 Componente per selezionare l'immagine del profilo
                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .clip(CircleShape)
                            .background(
                                if (imageError) MaterialTheme.colorScheme.errorContainer
                                else MaterialTheme.colorScheme.surfaceVariant
                            )
                            .border( // ← bordo rosso se errore
                                width = 2.dp,
                                color = if (imageError) MaterialTheme.colorScheme.error
                                else Color.Transparent,
                                shape = CircleShape
                            )
                            .clickable {
                                imageError = false // reset errore al click
                                showDialogChooser = true
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        if (imageUri != null) {
                            AsyncImage(
                                model = imageUri,
                                contentDescription = "Profile Picture",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = "Placeholder",
                                modifier = Modifier.size(50.dp),
                                tint = if (imageError) MaterialTheme.colorScheme.error
                                else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Text(
                        text = if (imageError) "Foto profilo mancante"
                        else "Tocca per aggiungere una foto",
                        fontSize = 12.sp,
                        color = if (imageError) MaterialTheme.colorScheme.error
                        else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Nome") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = surname,
                        onValueChange = { surname = it },
                        label = { Text("Cognome") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = email,
                        onValueChange = {
                            email = it
                            emailError = it.isNotBlank() && !isValidEmail(it) // valida mentre scrivi
                        },
                        label = { Text("Email") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("nome.cognomeNN@studio.unibo.it") },
                        isError = emailError,
                        supportingText = {
                            if (emailError) {
                                Text(
                                    text = "Formato email non valido",
                                    color = MaterialTheme.colorScheme.error
                                )
                            }
                        },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Email // mostra la tastiera email
                        )
                    )

                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("Password") },
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = confirmPassword,
                        onValueChange = { confirmPassword = it },
                        label = { Text("Conferma Password") },
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth()
                    )

                    val displayError = localError ?: serverError
                    if (displayError != null) {
                        Text(
                            text = displayError,
                            color = MaterialTheme.colorScheme.error,
                            fontSize = 14.sp,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    Button(
                        onClick = {
                            localError = null
                            if (imageUri == null) { // ← nuovo controllo
                                imageError = true
                                localError = "Carica una foto profilo per continuare"
                            } else if (email.isBlank() || password.isBlank() || name.isBlank() || surname.isBlank()) {
                                localError = "Compila tutti i campi obbligatori"
                            }else if (!isValidEmail(email)) {
                                emailError = true
                                localError = "Inserisci un'email valida"
                            } else if (password != confirmPassword) {
                                localError = "Le password non coincidono"
                            } else if (password.length < 6) {
                                localError = "La password deve avere almeno 6 caratteri"
                            } else {
                                var imageBytes: ByteArray? = null
                                var mimeType = "image/jpeg"

                                if (imageUri != null) {
                                    try {
                                        val contentResolver = context.contentResolver
                                        mimeType = contentResolver.getType(imageUri!!) ?: "image/jpeg"
                                        val inputStream = contentResolver.openInputStream(imageUri!!)
                                        imageBytes = inputStream?.readBytes()
                                        inputStream?.close()
                                    } catch (e: Exception) {
                                        localError = "Errore durante il caricamento dell'immagine."
                                        return@Button
                                    }
                                }

                                viewModel.register(email, password, name, surname, imageBytes, mimeType)
                            }
                        },
                        enabled = !isLoading,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                    ) {
                        Text(if (isLoading) "Creazione account..." else "Registrati")
                    }

                    TextButton(onClick = onNavigateToLogin) {
                        Text("Hai già un account? Accedi", color = MaterialTheme.colorScheme.secondary)
                    }
                }
            }

        }
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = innerPadding.calculateBottomPadding())
        )
    }

    // ── Dialog Scelta Foto ───────────────────────────────────────────────
    if (showDialogChooser) {
        AlertDialog(
            onDismissRequest = { showDialogChooser = false },
            title = { Text("Scegli foto profilo") },
            text = { Text("Vuoi scattare una nuova foto o selezionarla dalla galleria?") },
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
                        val uri = createSignupImageUri(context)
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

// Funzione di utilità per creare un URI temporaneo per la fotocamera.
// Rinominata per evitare conflitti con quella in Profile.kt
private fun createSignupImageUri(context: Context): Uri {
    val tempFile = File.createTempFile("signup_capture_", ".jpg", context.cacheDir).apply {
        createNewFile()
        deleteOnExit()
    }

    return FileProvider.getUriForFile(
        context,
        "${context.packageName}.provider",
        tempFile
    )
}
fun isValidEmail(email: String): Boolean {
    return email.endsWith("@studio.unibo.it") &&
            android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
}