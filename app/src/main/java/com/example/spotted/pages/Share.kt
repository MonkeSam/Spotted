//package com.example.spotted.pages
//
//import android.graphics.drawable.Icon
//import androidx.compose.foundation.BorderStroke
//import androidx.compose.foundation.Image
//import androidx.compose.foundation.background
//import androidx.compose.foundation.border
//import androidx.compose.foundation.clickable
//import androidx.compose.foundation.horizontalScroll
//import androidx.compose.foundation.layout.*
//import androidx.compose.foundation.rememberScrollState
//import androidx.compose.foundation.shape.CircleShape
//import androidx.compose.foundation.shape.RoundedCornerShape
//import androidx.compose.foundation.text.BasicTextField
//import androidx.compose.foundation.verticalScroll
//import androidx.compose.material.icons.Icons
//import androidx.compose.material.icons.filled.AddCircle
//import androidx.compose.material.icons.filled.Close
//import androidx.compose.material.icons.filled.LocationOn
//import androidx.compose.material.icons.filled.Share
//import androidx.compose.material.icons.outlined.Info
//import androidx.compose.material3.*
//import androidx.compose.runtime.*
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.draw.clip
//import androidx.compose.ui.graphics.Color
//import androidx.compose.ui.graphics.SolidColor
//import androidx.compose.ui.text.font.FontWeight
//import androidx.compose.ui.text.style.TextAlign
//import androidx.compose.ui.unit.dp
//import androidx.compose.ui.unit.sp
//import com.google.android.engage.common.datamodel.Image
//
//private const val MAX_CHARS = 280
//
//@Composable
//fun ShareScreen(innerPadding: PaddingValues) {
//
//    // ── Stato UI (logica da implementare) ─────────────────────────────────
////    var selectedCategory by remember { mutableStateOf<SpotCategory?>(null) }
//    var textContent      by remember { mutableStateOf("") }
//    var locationText     by remember { mutableStateOf("") }
//    var photoSelected    by remember { mutableStateOf(false) }
//
////    val canPublish = selectedCategory != null && textContent.isNotBlank()
//
//    Box(
//        modifier = Modifier
//            .fillMaxSize()
//            .padding(innerPadding),
//    ) {
//        // ── Contenuto scrollabile ─────────────────────────────────────────
//        Column(
//            modifier = Modifier
//                .fillMaxSize()
//                .verticalScroll(rememberScrollState())
//                .padding(horizontal = 16.dp)
//                .padding(bottom = 88.dp),   // spazio per il bottone fisso
//            verticalArrangement = Arrangement.spacedBy(16.dp),
//        ) {
//
//            Spacer(Modifier.height(4.dp))
//
//            // ── Banner anonimato ─────────────────────────────────────────
//            AnonymousBanner()
//
//            // ── Selettore categoria ──────────────────────────────────────
//            SectionLabel("Categoria")
//            CategorySelector(
//                selected   = selectedCategory,
//                onSelect   = { selectedCategory = it },
//            )
//
//            // ── Campo testo ──────────────────────────────────────────────
//            SectionLabel("Cosa hai spottato?")
//            SpotTextField(
//                value      = textContent,
//                onValueChange = { if (it.length <= MAX_CHARS) textContent = it },
//            )
//
//            // ── Foto ─────────────────────────────────────────────────────
//            SectionLabel("Foto  •  opzionale")
//            PhotoPickerCard(
//                photoSelected = photoSelected,
//                onPickPhoto   = { /* TODO: aprire il picker */ photoSelected = true },
//                onRemovePhoto = { photoSelected = false },
//            )
//
//            // ── Posizione ────────────────────────────────────────────────
//            SectionLabel("Posizione  •  opzionale")
//            LocationPickerRow(
//                value         = locationText,
//                onValueChange = { locationText = it },
//                onPickLocation = { /* TODO: aprire la mappa */ },
//            )
//        }
//
//        // ── Bottone pubblica (fisso in basso) ─────────────────────────────
//        Button(
//            onClick  = { /* TODO: pubblicare lo spot */ },
//            enabled  = canPublish,
//            modifier = Modifier
//                .fillMaxWidth()
//                .align(Alignment.BottomCenter)
//                .padding(horizontal = 16.dp, vertical = 16.dp)
//                .height(52.dp),
//            shape  = RoundedCornerShape(14.dp),
//            colors = ButtonDefaults.buttonColors(
//                containerColor         = MaterialTheme.colorScheme.primary,
//                contentColor           = Color.White,
//                disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
//                disabledContentColor   = MaterialTheme.colorScheme.onSurfaceVariant,
//            ),
//        ) {
//            Text(
//                text       = "Pubblica spot",
//                fontSize   = 15.sp,
//                fontWeight = FontWeight.SemiBold,
//            )
//        }
//    }
//}
//
//// ─── AnonymousBanner ─────────────────────────────────────────────────────────
//
//@Composable
//fun AnonymousBanner() {
//    Surface(
//        shape = RoundedCornerShape(12.dp),
//        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f),
//    ) {
//        Row(
//            modifier          = Modifier
//                .fillMaxWidth()
//                .padding(horizontal = 14.dp, vertical = 10.dp),
//            verticalAlignment = Alignment.CenterVertically,
//            horizontalArrangement = Arrangement.spacedBy(10.dp),
//        ) {
//            Icon(
//                imageVector        = Icons.Outlined.Info,
//                contentDescription = null,
//                tint               = MaterialTheme.colorScheme.primary,
//                modifier           = Modifier.size(18.dp),
//            )
//            Text(
//                text     = "Stai pubblicando in modo anonimo. Il tuo nome non sarà visibile sullo spot.",
//                fontSize = 12.sp,
//                color    = MaterialTheme.colorScheme.onSurface,
//                lineHeight = 17.sp,
//            )
//        }
//    }
//}
//
//// ─── SectionLabel ────────────────────────────────────────────────────────────
//
//@Composable
//fun SectionLabel(text: String) {
//    Text(
//        text       = text,
//        fontSize   = 12.sp,
//        fontWeight = FontWeight.SemiBold,
//        letterSpacing = 0.3.sp,
//        color      = MaterialTheme.colorScheme.onSurfaceVariant,
//        modifier   = Modifier.padding(start = 2.dp),
//    )
//}
//
//// ─── CategorySelector ────────────────────────────────────────────────────────
//
//@Composable
//fun CategorySelector(
//    selected: SpotCategory?,
//    onSelect: (SpotCategory) -> Unit,
//) {
//    Row(
//        modifier              = Modifier
//            .fillMaxWidth()
//            .horizontalScroll(rememberScrollState()),
//        horizontalArrangement = Arrangement.spacedBy(8.dp),
//    ) {
//        SpotCategory.entries.forEach { category ->
//            val isSelected = category == selected
//            Surface(
//                modifier = Modifier.clickable { onSelect(category) },
//                shape    = RoundedCornerShape(12.dp),
//                color = if (isSelected) MaterialTheme.colorScheme.primaryContainer
//                else MaterialTheme.colorScheme.surfaceContainerHigh
//            ) {
//                Row(
//                    modifier          = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
//                    verticalAlignment = Alignment.CenterVertically,
//                    horizontalArrangement = Arrangement.spacedBy(6.dp),
//                ) {
//                    Text(category.emoji, fontSize = 16.sp)
//                    Text(
//                        text       = category.label,
//                        fontSize   = 13.sp,
//                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
//                        color      = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer
//                        else MaterialTheme.colorScheme.onSurfaceVariant,
//                    )
//                }
//            }
//        }
//    }
//}
//
//// ─── SpotTextField ───────────────────────────────────────────────────────────
//
//@Composable
//fun SpotTextField(
//    value:         String,
//    onValueChange: (String) -> Unit,
//) {
//    val remaining = MAX_CHARS - value.length
//
//    Surface(
//        shape = RoundedCornerShape(16.dp),
//        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
//        border = BorderStroke(
//            0.5.dp,
//            MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
//        ),
//    ) {
//        Column {
//            BasicTextField(
//                value         = value,
//                onValueChange = onValueChange,
//                modifier      = Modifier
//                    .fillMaxWidth()
//                    .defaultMinSize(minHeight = 120.dp)
//                    .padding(horizontal = 16.dp, vertical = 14.dp),
//                textStyle     = LocalTextStyle.current.copy(
//                    fontSize = 15.sp,
//                    color    = MaterialTheme.colorScheme.onSurface,
//                    lineHeight = 22.sp,
//                ),
//                cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
//                decorationBox = { innerField ->
//                    Box {
//                        if (value.isEmpty()) {
//                            Text(
//                                text     = "Es. Ho visto qualcuno dimenticare un libro in Aula",
//                                fontSize = 15.sp,
//                                color    = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
//                                lineHeight = 22.sp,
//                            )
//                        }
//                        innerField()
//                    }
//                },
//            )
//
//            // Counter
//            Row(
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .padding(end = 14.dp, bottom = 10.dp),
//                horizontalArrangement = Arrangement.End,
//            ) {
//                Text(
//                    text  = "$remaining",
//                    fontSize = 11.sp,
//                    color = when {
//                        remaining < 20  -> MaterialTheme.colorScheme.error
//                        remaining < 60  -> Color(0xFFB45309)
//                        else            -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
//                    },
//                )
//            }
//        }
//    }
//}
//
//// ─── PhotoPickerCard ─────────────────────────────────────────────────────────
//
//@Composable
//fun PhotoPickerCard(
//    photoSelected: Boolean,
//    onPickPhoto:   () -> Unit,
//    onRemovePhoto: () -> Unit,
//) {
//    if (photoSelected) {
//        // Stato: foto selezionata
//        Surface(
//            shape = RoundedCornerShape(16.dp),
//            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f),
//            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)),
//        ) {
//            Row(
//                modifier          = Modifier
//                    .fillMaxWidth()
//                    .padding(horizontal = 16.dp, vertical = 14.dp),
//                verticalAlignment = Alignment.CenterVertically,
//                horizontalArrangement = Arrangement.SpaceBetween,
//            ) {
//                Row(
//                    verticalAlignment = Alignment.CenterVertically,
//                    horizontalArrangement = Arrangement.spacedBy(10.dp),
//                ) {
//                    Icon(
//                        Icons.Default.Share,
//                        contentDescription = null,
//                        tint     = MaterialTheme.colorScheme.primary,
//                        modifier = Modifier.size(22.dp),
//                    )
//                    Text(
//                        text     = "1 foto selezionata",
//                        fontSize = 14.sp,
//                        fontWeight = FontWeight.Medium,
//                        color    = MaterialTheme.colorScheme.onSurface,
//                    )
//                }
//                IconButton(onClick = onRemovePhoto, modifier = Modifier.size(28.dp)) {
//                    Icon(
//                        Icons.Default.Close,
//                        contentDescription = "Rimuovi foto",
//                        tint     = MaterialTheme.colorScheme.onSurfaceVariant,
//                        modifier = Modifier.size(18.dp),
//                    )
//                }
//            }
//        }
//    } else {
//        // Stato: nessuna foto
//        Box(
//            modifier = Modifier
//                .fillMaxWidth()
//                .height(110.dp)
//                .clip(RoundedCornerShape(16.dp))
//                .border(
//                    width  = 1.5.dp,
//                    brush  = SolidColor(MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)),
//                    shape  = RoundedCornerShape(16.dp),
//                )
//                .clickable(onClick = onPickPhoto),
//            contentAlignment = Alignment.Center,
//        ) {
//            Column(
//                horizontalAlignment = Alignment.CenterHorizontally,
//                verticalArrangement = Arrangement.spacedBy(6.dp),
//            ) {
//                Icon(
//                    Icons.Default.AddCircle,
//                    contentDescription = "Aggiungi foto",
//                    tint     = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
//                    modifier = Modifier.size(28.dp),
//                )
//                Text(
//                    text     = "Tocca per aggiungere una foto",
//                    fontSize = 13.sp,
//                    color    = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
//                )
//            }
//        }
//    }
//}
//
//// ─── LocationPickerRow ────────────────────────────────────────────────────────
//
//@Composable
//fun LocationPickerRow(
//    value:           String,
//    onValueChange:   (String) -> Unit,
//    onPickLocation:  () -> Unit,
//) {
//    Surface(
//        shape  = RoundedCornerShape(16.dp),
//        color  = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
//        border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)),
//    ) {
//        Row(
//            modifier          = Modifier
//                .fillMaxWidth()
//                .padding(horizontal = 16.dp, vertical = 4.dp),
//            verticalAlignment = Alignment.CenterVertically,
//            horizontalArrangement = Arrangement.spacedBy(10.dp),
//        ) {
//            IconButton(
//                onClick  = onPickLocation,
//                modifier = Modifier.size(36.dp),
//            ) {
//                Icon(
//                    Icons.Default.LocationOn,
//                    contentDescription = "Scegli posizione sulla mappa",
//                    tint     = MaterialTheme.colorScheme.primary,
//                    modifier = Modifier.size(22.dp),
//                )
//            }
//
//            BasicTextField(
//                value         = value,
//                onValueChange = onValueChange,
//                modifier      = Modifier
//                    .weight(1f)
//                    .padding(vertical = 14.dp),
//                textStyle     = LocalTextStyle.current.copy(
//                    fontSize = 14.sp,
//                    color    = MaterialTheme.colorScheme.onSurface,
//                ),
//                cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
//                singleLine    = true,
//                decorationBox = { innerField ->
//                    Box {
//                        if (value.isEmpty()) {
//                            Text(
//                                text  = "Es. Piazza Verdi, Aula A, Biblioteca…",
//                                fontSize = 14.sp,
//                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
//                            )
//                        }
//                        innerField()
//                    }
//                },
//            )
//        }
//    }
//}