package com.khammin.core.presentation.components.text

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp

@Composable
fun WordleText(
    modifier: Modifier = Modifier,
    text: String,
    color: Color,
    fontSize: TextUnit = 14.sp,
    fontWeight: FontWeight = FontWeight.Normal,
    letterSpacing: TextUnit = 0.sp,
    textAlign: TextAlign = TextAlign.Start,
    lineHeight: TextUnit = TextUnit.Unspecified,
) {
    Text(
        text          = text,
        color         = color,
        fontSize      = fontSize,
        fontWeight    = fontWeight,
        letterSpacing = letterSpacing,
        textAlign     = textAlign,
        lineHeight    = lineHeight,
        modifier      = modifier,
    )
}
