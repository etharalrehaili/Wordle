package com.khammin.core.presentation.components.text

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.khammin.core.presentation.theme.GameDesignTheme
import com.khammin.core.presentation.theme.GameDesignTheme.colors
import com.khammin.core.presentation.theme.WordleColors

@Composable
fun FieldLabel(text: String) {
    WordleText(
        text       = text,
        color      = colors.body.copy(alpha = 0.6f),
        fontSize   = GameDesignTheme.typography.labelSmall,
        fontWeight = FontWeight.SemiBold,
        letterSpacing = 0.5.sp,
    )
}

@Composable
fun FieldError(message: String) {
    Row(
        verticalAlignment     = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        modifier              = Modifier.padding(start = 4.dp, top = 6.dp)
    ) {
        Icon(
            imageVector        = Icons.Outlined.ErrorOutline,
            contentDescription = null,
            tint               = MaterialTheme.colorScheme.error,
            modifier           = Modifier.size(12.dp)
        )
        Text(
            text     = message,
            color    = MaterialTheme.colorScheme.error,
            fontSize = 12.sp,
        )
    }
}

@Composable
fun textFieldColors(colors: WordleColors) =
    OutlinedTextFieldDefaults.colors(
        focusedBorderColor          = colors.buttonTeal,
        unfocusedBorderColor        = colors.border,
        focusedTextColor            = colors.title,
        unfocusedTextColor          = colors.title,
        cursorColor                 = colors.buttonTeal,
        errorBorderColor            = MaterialTheme.colorScheme.error,
        focusedLeadingIconColor     = colors.buttonTeal,
        unfocusedLeadingIconColor   = colors.body.copy(alpha = 0.4f),
    )

@Composable
fun WordleText(
    text: String,
    color: Color,
    modifier: Modifier = Modifier,
    fontSize: TextUnit = 14.sp,
    fontWeight: FontWeight = FontWeight.Normal,
    letterSpacing: TextUnit = 0.sp,
    textAlign: TextAlign = TextAlign.Start,
    lineHeight: TextUnit = TextUnit.Unspecified,
) {
    Text(
        text = text,
        color = color,
        fontSize = fontSize,
        fontWeight = fontWeight,
        letterSpacing = letterSpacing,
        textAlign = textAlign,
        lineHeight = lineHeight,
        modifier = modifier
    )
}