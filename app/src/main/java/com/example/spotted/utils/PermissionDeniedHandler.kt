package com.example.spotted.utils

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.launch

data class PermissionDeniedHandler(
    val snackbarHostState: SnackbarHostState,
    val onPermissionDenied: (permanentlyDenied: Boolean) -> Unit
)

@Composable
fun rememberPermissionDeniedHandler(): PermissionDeniedHandler {
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val onPermissionDenied: (Boolean) -> Unit = { permanentlyDenied ->
        scope.launch {
            val result = snackbarHostState.showSnackbar(
                message = if (permanentlyDenied)
                    "Permesso negato permanentemente. Abilitalo dalle impostazioni."
                else
                    "Permesso negato",
                actionLabel = if (permanentlyDenied) "Impostazioni" else null,
                duration = SnackbarDuration.Long
            )
            if (result == SnackbarResult.ActionPerformed) {
                Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                    data = Uri.fromParts("package", context.packageName, null)
                    context.startActivity(this)
                }
            }
        }
    }

    return PermissionDeniedHandler(snackbarHostState, onPermissionDenied)
}