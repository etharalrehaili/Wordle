package com.khammin.core.presentation.components.bottomsheets

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.ArrowForwardIos
import androidx.compose.material.icons.automirrored.outlined.HelpOutline
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.HelpOutline
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.khammin.core.R
import com.khammin.core.alias.Action
import com.khammin.core.presentation.components.enums.AppColorTheme
import com.khammin.core.presentation.components.enums.AppLanguage
import com.khammin.core.presentation.components.enums.DrawerScreen
import com.khammin.core.presentation.components.text.WordleText
import com.khammin.core.presentation.theme.GameDesignTheme
import com.khammin.core.presentation.theme.GameDesignTheme.colors
import com.khammin.core.presentation.theme.LocalWordleColors

@Composable
fun GameMenuDrawerSheet(
    selectedLanguage: AppLanguage = AppLanguage.ENGLISH,
    onClose: Action,
    onProfile: Action,
    onSupportClick: Action,
    onLanguageSelected: (AppLanguage) -> Unit
) {

    var currentScreen by remember { mutableStateOf(DrawerScreen.MENU) }
    var isNavigatingForward by remember { mutableStateOf(true) }

    BackHandler(enabled = currentScreen != DrawerScreen.MENU) {
        isNavigatingForward = false
        currentScreen = DrawerScreen.MENU
    }

    ModalDrawerSheet(
        drawerShape          = RectangleShape,
        drawerContainerColor = colors.background,
        modifier = Modifier
            .fillMaxWidth(0.78f)
            .fillMaxHeight()
    ) {
        AnimatedContent(
            targetState = currentScreen,
            transitionSpec = {
                if (isNavigatingForward)
                    slideInHorizontally { it } togetherWith slideOutHorizontally { -it }
                else
                    slideInHorizontally { -it } togetherWith slideOutHorizontally { it }
            },
            label = "drawerScreenTransition"
        ) { screen ->
            when (screen) {
                DrawerScreen.MENU -> MenuScreen(
                    onClose    = onClose,
                    onLanguage = { isNavigatingForward = true; currentScreen = DrawerScreen.LANGUAGE },
                    onProfile  = onProfile,
                    onSupport  = onSupportClick,
                )
                DrawerScreen.LANGUAGE -> LanguageScreen(
                    selectedLanguage = selectedLanguage,
                    onBack   = { isNavigatingForward = false; currentScreen = DrawerScreen.MENU },
                    onSelect = { lang ->
                        onLanguageSelected(lang)
                        isNavigatingForward = false
                        currentScreen = DrawerScreen.MENU
                    }
                )
            }
        }
    }
}

@Composable
private fun MenuScreen(
    onClose: Action,
    onLanguage: Action,
    onProfile: Action,
    onSupport: Action,
) {
    data class Entry(
        val icon: ImageVector,
        val label: String,
        val description: String,
        val accent: Color,
        val action: () -> Unit,
    )

    val items = buildList {
        add(Entry(
            icon        = Icons.Filled.Person,
            label       = stringResource(R.string.drawer_profile),
            description = stringResource(R.string.drawer_profile_desc),
            accent      = colors.logoBlue,
            action      = onProfile
        ))
        add(Entry(
            icon        = Icons.Filled.Language,
            label       = stringResource(R.string.drawer_language),
            description = stringResource(R.string.drawer_language_desc),
            accent      = colors.logoGreen,
            action      = onLanguage
        ))
        add(Entry(
            icon        = Icons.AutoMirrored.Outlined.HelpOutline,
            label       = stringResource(R.string.drawer_support),
            description = stringResource(R.string.drawer_support_desc),
            accent      = colors.logoPurple,
            action      = onSupport
        ))
    }

    DrawerColumn {

        // ── Branded header ────────────────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 24.dp, end = 12.dp, top = 48.dp, bottom = 24.dp)
        )

        // ── Menu items ────────────────────────────────────────────
        items.forEach { item ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(
                        indication        = null,
                        interactionSource = remember { MutableInteractionSource() },
                        onClick           = item.action
                    )
                    .padding(horizontal = 20.dp, vertical = 14.dp)
            ) {
                // Colored icon box
                Box(
                    modifier         = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(item.accent.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector        = item.icon,
                        contentDescription = item.label,
                        tint               = item.accent,
                        modifier           = Modifier.size(22.dp)
                    )
                }

                Spacer(Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    WordleText(
                        text       = item.label,
                        color      = colors.title,
                        fontSize   = GameDesignTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Spacer(Modifier.height(1.dp))
                    WordleText(
                        text     = item.description,
                        color    = colors.body.copy(alpha = 0.45f),
                        fontSize = GameDesignTheme.typography.labelSmall,
                    )
                }

                Icon(
                    imageVector        = Icons.AutoMirrored.Outlined.ArrowForwardIos,
                    contentDescription = null,
                    tint               = colors.body.copy(alpha = 0.20f),
                    modifier           = Modifier.size(14.dp)
                )
            }

            HorizontalDivider(
                color     = colors.divider.copy(alpha = 0.5f),
                thickness = 0.5.dp,
                modifier  = Modifier.padding(start = 80.dp, end = 20.dp)
            )
        }
    }
}

@Composable
private fun LanguageScreen(
    selectedLanguage: AppLanguage,
    onBack: Action,
    onSelect: (AppLanguage) -> Unit,
) {
    DrawerColumn {

        SelectionHeader(title = stringResource(R.string.drawer_language), onBack = onBack)

        AppLanguage.entries.forEach { lang ->
            SelectionRow(
                label = stringResource(
                    when (lang) {
                        AppLanguage.ARABIC   -> R.string.language_arabic
                        AppLanguage.ENGLISH  -> R.string.language_english
                    }
                ),
                isSelected = lang == selectedLanguage,
                accent     = colors.logoGreen,
                onClick    = { onSelect(lang) }
            )
        }
    }
}

@Composable
private fun SelectionHeader(title: String, onBack: () -> Unit) {

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier          = Modifier
            .fillMaxWidth()
            .padding(start = 8.dp, end = 16.dp, top = 48.dp, bottom = 16.dp)
    ) {
        IconButton(onClick = onBack, modifier = Modifier.size(40.dp)) {
            Icon(
                imageVector        = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                tint               = colors.body,
                modifier           = Modifier.size(20.dp)
            )
        }
        Spacer(Modifier.width(4.dp))
        WordleText(
            text       = title,
            color      = colors.title,
            fontSize   = GameDesignTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
        )
    }
    HorizontalDivider(color = colors.divider.copy(alpha = 0.5f), thickness = 0.5.dp)
}

@Composable
private fun SelectionRow(
    label: String,
    isSelected: Boolean,
    accent: Color,
    onClick: () -> Unit,
) {
    val colors = LocalWordleColors.current
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier          = Modifier
            .fillMaxWidth()
            .clickable(
                indication        = null,
                interactionSource = remember { MutableInteractionSource() },
                onClick           = onClick
            )
            .padding(horizontal = 24.dp, vertical = 18.dp)
    ) {
        WordleText(
            text       = label,
            color      = if (isSelected) accent else colors.title,
            fontSize   = GameDesignTheme.typography.labelLarge,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
            modifier   = Modifier.weight(1f)
        )
        if (isSelected) {
            Box(
                modifier         = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(accent.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector        = Icons.Filled.Check,
                    contentDescription = "Selected",
                    tint               = accent,
                    modifier           = Modifier.size(14.dp)
                )
            }
        }
    }
    HorizontalDivider(
        color     = colors.divider.copy(alpha = 0.5f),
        thickness = 0.5.dp,
        modifier  = Modifier.padding(horizontal = 24.dp)
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LanguageSelectionBottomSheet(
    selectedLanguage: AppLanguage,
    onLanguageSelected: (AppLanguage) -> Unit,
    onDismiss: () -> Unit,
    sheetState: SheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState       = sheetState,
        containerColor   = colors.background,
        dragHandle       = null,
        shape            = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .background(brush = colors.logoStripBrush)
            )
            Spacer(Modifier.height(16.dp))
            AppLanguage.entries.forEach { lang ->
                SelectionRow(
                    label = stringResource(
                        when (lang) {
                            AppLanguage.ARABIC  -> R.string.language_arabic
                            AppLanguage.ENGLISH -> R.string.language_english
                        }
                    ),
                    isSelected = lang == selectedLanguage,
                    accent     = colors.logoGreen,
                    onClick    = {
                        onLanguageSelected(lang)
                        onDismiss()
                    }
                )
            }
            Spacer(Modifier.height(8.dp))
        }
    }
}

@Composable
private fun DrawerColumn(content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth().fillMaxHeight(),
        content  = content
    )
}