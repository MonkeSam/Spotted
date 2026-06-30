package com.example.spotted.ui.theme

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.spotted.R

@Composable
fun AppLogo(
    modifier: Modifier = Modifier,
    iconSize: Int = 40,
    textSize: Int = 28,
    spacerWidth: Int = 8
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_logo),
            contentDescription = "Logo SpottedUnibo",
            modifier = Modifier.size(iconSize.dp),
            tint = Color.Unspecified // per mantenere i colori originali del vettoriale
        )
        androidx.compose.foundation.layout.Spacer(modifier = Modifier.size(spacerWidth.dp))
        Text(
            buildAnnotatedString {
                withStyle(style = SpanStyle(color = UniboGrey40)) {
                    append("Spotted")
                }
                withStyle(style = SpanStyle(color = UniboRed40, fontWeight = FontWeight.Bold)) {
                    append("Unibo")
                }
            },
            fontSize = textSize.sp,
            letterSpacing = 0.5.sp
        )
    }
}