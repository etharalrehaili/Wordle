package com.khammin.game.presentation.settings.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.khammin.authentication.R
import com.khammin.core.alias.Action
import com.khammin.core.presentation.components.navigation.GameTopBar
import com.khammin.core.presentation.preview.GameDarkBackgroundPreview
import com.khammin.core.presentation.theme.GameDesignTheme.colors
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.khammin.core.presentation.components.buttons.GameButton
import com.khammin.core.presentation.components.text.WordleText
import com.khammin.core.presentation.theme.GameDesignTheme

@Composable
fun SupportScreen(onBack: Action) {
    SupportContent(onBack = onBack)
}

@Composable
fun SupportContent(onBack: Action) {
    val context = LocalContext.current
    val linkedInUrl = "https://www.linkedin.com/in/ethar-alrehaili/"

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {

            GameTopBar(
                title              = stringResource(R.string.support_title),
                startIcon          = Icons.AutoMirrored.Filled.ArrowBack,
                onStartIconClicked = onBack,
                showBackground     = false,
                modifier           = Modifier.fillMaxWidth().statusBarsPadding(),
                containerColor     = Color.Transparent,
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 28.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {

                Spacer(Modifier.height(8.dp))

                // ── Header card ───────────────────────────────────────
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(colors.buttonTeal.copy(alpha = 0.08f))
                        .border(
                            width = 1.dp,
                            color = colors.buttonTeal.copy(alpha = 0.20f),
                            shape = RoundedCornerShape(20.dp)
                        )
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    WordleText(
                        text = stringResource(R.string.support_header_title),
                        color      = colors.title,
                        fontSize   = GameDesignTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                    )
                    WordleText(
                        text = stringResource(R.string.support_header_body),
                        color = colors.body.copy(alpha = 0.7f),
                        fontSize = 14.sp,
                        lineHeight = 22.sp,
                    )
                }

                // ── LinkedIn card ─────────────────────────────────────
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(colors.buttonTaupe.copy(alpha = 0.10f))
                        .border(
                            width = 1.dp,
                            color = colors.border.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(16.dp)
                        )
                        .clickable {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(linkedInUrl))
                            context.startActivity(intent)
                        }
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // LinkedIn logo placeholder box
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF0A66C2)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text     = "in",
                            color    = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.ExtraBold,
                        )
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        WordleText(
                            text = stringResource(R.string.support_linkedin_name),
                            color      = colors.title,
                            fontSize   = GameDesignTheme.typography.labelLarge,
                            fontWeight = FontWeight.SemiBold,
                        )
                        WordleText(
                            text = stringResource(R.string.support_linkedin_handle),
                            color    = colors.body.copy(alpha = 0.45f),
                            fontSize = 12.sp,
                        )
                    }

                    Icon(
                        imageVector        = Icons.AutoMirrored.Filled.OpenInNew,
                        contentDescription = null,
                        tint               = colors.body.copy(alpha = 0.30f),
                        modifier           = Modifier.size(18.dp)
                    )
                }

                Spacer(Modifier.height(8.dp))

            }
        }
    }
}

@GameDarkBackgroundPreview
@Composable
private fun PreviewSupportScreenDark() {
    SupportScreen(onBack = {})
}