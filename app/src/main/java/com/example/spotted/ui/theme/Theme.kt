package com.example.spotted.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val LightColorScheme = lightColorScheme(
    primary                = UniboRed40,
    onPrimary              = Color.White,
    primaryContainer       = Color(0xFFFFDAD6),
    onPrimaryContainer     = Color(0xFF410001),
    secondary              = UniboGrey40,
    onSecondary            = Color.White,
    secondaryContainer     = Color(0xFFFFDAD6), // ← indicatore selected nav item
    onSecondaryContainer   = Color(0xFF410001), // ← icona sull'indicatore
    tertiary               = UniboGold40,
    onTertiary             = Color.White,
    tertiaryContainer      = Color(0xFFF9EDBB),
    onTertiaryContainer    = Color(0xFF2B2000),
    background             = Color(0xFFFFFBFE),
    onBackground           = Color(0xFF1C1B1F),
    surface                = Color(0xFFFFFBFE),
    onSurface              = Color(0xFF1C1B1F),
    onSurfaceVariant       = Color(0xFF534341), // ← icone/label non selezionati
    outline                = Color(0xFF857370),
)

private val DarkColorScheme = darkColorScheme(
    primary                = UniboRed80,
    onPrimary              = Color(0xFF410001),
    primaryContainer       = UniboRed40,
    onPrimaryContainer     = Color(0xFFFFDAD6),
    secondary              = UniboGrey80,
    onSecondary            = Color(0xFF1A1C1E),
    secondaryContainer     = UniboRed40,        // ← indicatore selected nav item (dark)
    onSecondaryContainer   = Color(0xFFFFDAD6), // ← icona sull'indicatore (dark)
    tertiary               = UniboGold80,
    onTertiary             = Color(0xFF2B2000),
    tertiaryContainer      = UniboGold40,
    onTertiaryContainer    = Color(0xFFF9EDBB),
    background             = Color(0xFF1C1B1F),
    onBackground           = Color(0xFFFFFBFE),
    surface                = Color(0xFF1C1B1F),
    onSurface              = Color(0xFFFFFBFE),
    onSurfaceVariant       = Color(0xFFF4DDDB), // ← icone/label non selezionati (dark)
)
@Composable
fun SpottedTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}