// Share.kt
package com.example.spotted.pages

import OSMPlace
import android.content.Context
import android.location.Geocoder
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.spotted.data.model.Category
import com.example.spotted.data.view.ShareViewModel
import com.example.spotted.utils.PhotoPickerSection
import com.example.spotted.utils.rememberPermissionDeniedHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import org.koin.androidx.compose.koinViewModel
import searchAddressOSM
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShareScreen(
    innerPadding: PaddingValues
) {
    val viewModel: ShareViewModel = koinViewModel()
    val context = LocalContext.current
    val (snackbarHostState, onPermissionDenied) = rememberPermissionDeniedHandler()




    var title by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf<Category?>(null) }
    var description by remember { mutableStateOf("") }


    var selectedPhotoUri by remember { mutableStateOf<Uri?>(null) }


    var categoryDropdownExpanded by remember { mutableStateOf(false) }


    var address by remember { mutableStateOf("") }
    var coordinates by remember { mutableStateOf<Pair<Double, Double>?>(null) }
    var isSearchingLocation by remember { mutableStateOf(false) }
    var locationError by remember { mutableStateOf(false) }


    val isLoading  by viewModel.isLoading.collectAsState()
    val error      by viewModel.error.collectAsState()
    val isSuccess  by viewModel.isSuccess.collectAsState()
    val categories by viewModel.categories.collectAsState()


    var latitude by remember { mutableStateOf<Double?>(null) }
    var longitude by remember { mutableStateOf<Double?>(null) }
    var selectedAddressName by remember { mutableStateOf("") }


    LaunchedEffect(isSuccess) {
        if (isSuccess) {
            title            = ""
            selectedCategory = null
            description      = ""
            address          = ""
            coordinates      = null
            selectedPhotoUri = null
        }
    }


    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)
            .background(MaterialTheme.colorScheme.background)
    ) {

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {


            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 28.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                Text(
                    buildAnnotatedString {
                        withStyle(style = SpanStyle(color = MaterialTheme.colorScheme.secondary)) {
                            append("Nuovo")
                        }
                        withStyle(style = SpanStyle(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)) {
                            append("Spot")
                        }
                    },
                    fontSize = 36.sp
                )

                Spacer(Modifier.height(8.dp))

                Surface(
                    shape = RoundedCornerShape(50),
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                ) {
                    Text(
                        text = "Condividi con il tuo campus",
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }

            }


            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {


                Column {
                    SectionLabel("DETTAGLI SPOT")
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {

                            OutlinedTextField(
                                value = title,
                                onValueChange = { title = it },
                                label = { Text("Titolo") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                                enabled = !isLoading
                            )


                            ExposedDropdownMenuBox(
                                expanded = categoryDropdownExpanded && !isLoading,
                                onExpandedChange = {
                                    if (!isLoading) categoryDropdownExpanded = !categoryDropdownExpanded
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                OutlinedTextField(
                                    value = selectedCategory?.let { "${it.emoji}  ${it.name}" }
                                        ?: if (categories.isEmpty()) "Caricamento…" else "Seleziona categoria",
                                    onValueChange = {},
                                    readOnly = true,
                                    label = { Text("Categoria") },
                                    trailingIcon = {
                                        ExposedDropdownMenuDefaults.TrailingIcon(
                                            expanded = categoryDropdownExpanded
                                        )
                                    },
                                    colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                                    modifier = Modifier
                                        .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                                        .fillMaxWidth(),
                                    enabled = !isLoading && categories.isNotEmpty(),
                                    singleLine = true,
                                )

                                ExposedDropdownMenu(
                                    expanded = categoryDropdownExpanded && !isLoading,
                                    onDismissRequest = { categoryDropdownExpanded = false },
                                ) {
                                    if (categories.isEmpty()) {
                                        DropdownMenuItem(
                                            text = {
                                                Text(
                                                    "Nessuna categoria disponibile",
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            },
                                            onClick = { categoryDropdownExpanded = false }
                                        )
                                    } else {
                                        categories.forEach { category ->
                                            DropdownMenuItem(
                                                text = {
                                                    Row(
                                                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                                                        verticalAlignment = Alignment.CenterVertically
                                                    ) {
                                                        Text(
                                                            text     = category.emoji,
                                                            fontSize = 20.sp
                                                        )
                                                        Text(
                                                            text  = category.name,
                                                            style = MaterialTheme.typography.bodyMedium
                                                        )
                                                    }
                                                },
                                                onClick = {
                                                    selectedCategory = category
                                                    categoryDropdownExpanded = false
                                                },
                                                contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding,
                                            )
                                        }
                                    }
                                }
                            }


                            OutlinedTextField(
                                value = description,
                                onValueChange = { description = it },
                                label = { Text("Descrizione") },
                                modifier = Modifier.fillMaxWidth(),
                                minLines = 3,
                                maxLines = 5,
                                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                                enabled = !isLoading
                            )
                        }
                    }
                }


                OSMAddressAutocompleteField(
                    modifier = Modifier.fillMaxWidth(),
                    onLocationSelected = { lat, lon, addr ->
                        latitude = lat
                        longitude = lon
                        selectedAddressName = addr
                    }
                )

                if (latitude != null && longitude != null) {
                    Text(
                        text = "📍 Posizione agganciata correttamente!",
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.bodySmall
                    )
                }


                Column {
                    SectionLabel("MEDIA (OPZIONALE)")
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                    ) {
                        Box(modifier = Modifier.padding(16.dp)) {
                            PhotoPickerSection(
                                selectedUri        = selectedPhotoUri,
                                onPhotoSelected    = { uri -> selectedPhotoUri = uri },
                                onPhotoRemoved     = { selectedPhotoUri = null },
                                enabled            = !isLoading,
                                onPermissionDenied = onPermissionDenied
                            )
                        }
                    }
                }


                if (error != null) {
                    Text(
                        text = error ?: "Errore sconosciuto",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }
            }


            Spacer(modifier = Modifier.height(90.dp))
        }


        Surface(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth(),
            color = MaterialTheme.colorScheme.background
        ) {
            Button(
                onClick = {
                    val categoryId = selectedCategory?.id ?: return@Button
                    val lat = latitude ?: 0.0
                    val lng = longitude ?: 0.0

                    val photoBytes = selectedPhotoUri?.let { uri ->
                        context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
                    }
                    val photoMime = selectedPhotoUri
                        ?.let { context.contentResolver.getType(it) }
                        ?: "image/jpeg"

                    viewModel.publishSpot(
                        title       = title,
                        category    = categoryId,
                        description = description,
                        latitude    = lat,
                        longitude   = lng,
                        photoBytes  = photoBytes,
                        photoMime   = photoMime
                    )
                },
                modifier = Modifier
                    .padding(horizontal = 16.dp, vertical = 16.dp)
                    .fillMaxWidth()
                    .height(52.dp),
                enabled = !isLoading
                        && title.isNotBlank()
                        && description.isNotBlank()
                        && selectedCategory != null
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier    = Modifier.size(24.dp),
                        color       = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("Pubblica Spot", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                }
            }

        }
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 84.dp)
        )
    }
}




@Composable
private fun SectionLabel(text: String) {
    Text(
        text = text,
        modifier = Modifier.padding(start = 16.dp, bottom = 8.dp),
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        letterSpacing = 1.sp
    )
}

/**
 * Funzione helper per convertire un indirizzo in coordinate (Lat, Lon)
 * utilizzando il Geocoder nativo di Android.
 */
@Suppress("DEPRECATION")
suspend fun getCoordinatesFromAddress(context: Context, addressName: String): Pair<Double, Double>? {
    return withContext(Dispatchers.IO) {
        try {
            val geocoder = Geocoder(context, Locale.getDefault())
            val addresses = geocoder.getFromLocationName(addressName, 1)
            if (!addresses.isNullOrEmpty()) {
                val location = addresses[0]
                Pair(location.latitude, location.longitude)
            } else null
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OSMAddressAutocompleteField(
    modifier: Modifier = Modifier,
    onLocationSelected: (lat: Double, lon: Double, address: String) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var suggestions by remember { mutableStateOf<List<OSMPlace>>(emptyList()) }
    var isDropdownExpanded by remember { mutableStateOf(false) }
    var isSearching by remember { mutableStateOf(false) }


    LaunchedEffect(searchQuery) {
        if (searchQuery.length >= 3) {
            isSearching = true
            delay(500)
            suggestions = searchAddressOSM(searchQuery)
            isSearching = false
            isDropdownExpanded = suggestions.isNotEmpty()
        } else {
            suggestions = emptyList()
            isDropdownExpanded = false
        }
    }

    Box(modifier = modifier) {
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            label = { Text("Cerca Posizione / Indirizzo") },
            placeholder = { Text("Es: Piazza Verdi, Bologna") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            trailingIcon = {
                if (isSearching) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                }
            }
        )

        DropdownMenu(
            expanded = isDropdownExpanded,
            onDismissRequest = { isDropdownExpanded = false },
            modifier = Modifier.fillMaxWidth(0.9f)
        ) {
            suggestions.forEach { place ->
                DropdownMenuItem(
                    text = {
                        val parts = place.displayName.split(", ")
                        val primary = parts.take(2).joinToString(", ")
                        val secondary = parts.drop(2).joinToString(", ")

                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text(
                                text = primary,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium,
                                maxLines = 1
                            )
                            if (secondary.isNotBlank()) {
                                Text(
                                    text = secondary,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1
                                )
                            }
                        }
                    },
                    onClick = {
                        searchQuery = place.displayName
                        isDropdownExpanded = false
                        val latDouble = place.lat.toDoubleOrNull() ?: 0.0
                        val lonDouble = place.lon.toDoubleOrNull() ?: 0.0
                        onLocationSelected(latDouble, lonDouble, place.displayName)
                    }
                )
            }
        }
    }
}