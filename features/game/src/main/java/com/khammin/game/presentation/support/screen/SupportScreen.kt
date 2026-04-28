package com.khammin.game.presentation.support.screen

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.khammin.core.alias.Action
import com.khammin.core.presentation.components.navigation.GameTopBar
import com.khammin.core.presentation.components.text.WordleText
import com.khammin.core.presentation.preview.GameDarkBackgroundPreview
import com.khammin.core.presentation.theme.GameDesignTheme
import com.khammin.core.presentation.theme.GameDesignTheme.colors
import com.khammin.core.presentation.theme.GameDesignTheme.spacing
import com.khammin.game.R
import androidx.core.net.toUri

private val LinkedInBlue = Color(0xFF0A66C2)

@Composable
fun SupportScreen(onBack: Action) {
    SupportContent(onBack = onBack)
}

@Composable
private fun SupportContent(onBack: Action) {
    val context = LocalContext.current
    val linkedInUrl = stringResource(R.string.support_linkedin_url)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {

            // Top navigation bar with a back arrow and the screen title
            GameTopBar(
                title              = stringResource(R.string.support_title),
                startIcon          = Icons.AutoMirrored.Filled.ArrowBack,
                onStartIconClicked = onBack,
                modifier           = Modifier.fillMaxWidth().statusBarsPadding(),
            )

            // Scrollable body content
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 28.dp),
                verticalArrangement = Arrangement.spacedBy(spacing.md)
            ) {

                Spacer(Modifier.height(spacing.xs))

                // ── Header section ────────────────────────────────────
                // A short intro encouraging the user to share feedback
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(spacing.xs)
                ) {
                    WordleText(
                        text       = stringResource(R.string.support_header_title),
                        color      = colors.title,
                        fontSize   = GameDesignTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                    )
                    WordleText(
                        text       = stringResource(R.string.support_header_body),
                        color      = colors.body.copy(alpha = 0.7f),
                        fontSize   = 14.sp,
                        lineHeight = 22.sp,
                    )
                }

                // ── LinkedIn card ──────────────────────────────────────
                // Tapping this row opens the developer's LinkedIn profile
                // in the device's default browser or the LinkedIn app
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(colors.logoBlue.copy(alpha = 0.10f))
                        .border(
                            width = 1.dp,
                            color = colors.logoBlue.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(16.dp)
                        )
                        .clickable {
                            val intent = Intent(Intent.ACTION_VIEW, linkedInUrl.toUri())
                            context.startActivity(intent)
                        }
                        .padding(spacing.md),
                    verticalAlignment   = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(spacing.sm)
                ) {
                    // LinkedIn "in" logo badge using the brand's official blue
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(LinkedInBlue),
                        contentAlignment = Alignment.Center
                    ) {
                        WordleText(
                            text       = stringResource(R.string.support_linkedin_logo_text),
                            color      = Color.White,
                            fontSize   = 18.sp,
                            fontWeight = FontWeight.ExtraBold,
                        )
                    }

                    // Developer name and profile handle
                    Column(modifier = Modifier.weight(1f)) {
                        WordleText(
                            text       = stringResource(R.string.support_linkedin_name),
                            color      = colors.title,
                            fontSize   = GameDesignTheme.typography.labelLarge,
                            fontWeight = FontWeight.SemiBold,
                        )
                        WordleText(
                            text     = stringResource(R.string.support_linkedin_handle),
                            color    = colors.body.copy(alpha = 0.45f),
                            fontSize = 12.sp,
                        )
                    }

                    // "Open in new" arrow — indicates the link opens externally.
                    Icon(
                        imageVector        = Icons.AutoMirrored.Filled.OpenInNew,
                        contentDescription = null,
                        tint               = colors.body.copy(alpha = 0.30f),
                        modifier           = Modifier.size(18.dp)
                    )
                }

                Spacer(Modifier.height(spacing.xs))
            }
        }
    }
}

@GameDarkBackgroundPreview
@Composable
private fun PreviewSupportScreenDark() {
    SupportScreen(onBack = {})
}
