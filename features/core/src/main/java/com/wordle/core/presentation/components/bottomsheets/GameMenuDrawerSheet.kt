package com.wordle.core.presentation.components.bottomsheets

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wordle.core.alias.Action
import com.wordle.core.presentation.components.enums.AppColorTheme
import com.wordle.core.presentation.components.enums.AppLanguage
import com.wordle.core.presentation.components.enums.DrawerScreen
import com.wordle.core.presentation.theme.LocalWordleColors

@Composable
fun GameMenuDrawerSheet(
    selectedLanguage: AppLanguage = AppLanguage.ENGLISH,
    selectedTheme: AppColorTheme = AppColorTheme.DARK,
    isLoggedIn: Boolean = true,
    onClose: Action,
    onProfile: Action,
    onLanguageSelected: (AppLanguage) -> Unit,
    onThemeSelected: (AppColorTheme) -> Unit
) {
    var currentScreen by remember { mutableStateOf(DrawerScreen.MENU) }
    var isNavigatingForward by remember { mutableStateOf(true) }
    val colors = LocalWordleColors.current

    ModalDrawerSheet(
        drawerShape          = RectangleShape,
        drawerContainerColor = colors.surface,
        modifier = Modifier
            .fillMaxWidth(0.75f)
            .fillMaxHeight()
    ) {
        AnimatedContent(
            targetState = currentScreen,
            transitionSpec = {
                if (isNavigatingForward) {
                    slideInHorizontally { it } togetherWith slideOutHorizontally { -it }
                } else {
                    slideInHorizontally { -it } togetherWith slideOutHorizontally { it }
                }
            },
            label = "drawerScreenTransition"
        ) { screen ->
            when (screen) {
                DrawerScreen.MENU -> MenuScreen(
                    onClose    = onClose,
                    onTheme    = {
                        isNavigatingForward = true
                        currentScreen = DrawerScreen.THEME
                    },
                    onLanguage = {
                        isNavigatingForward = true
                        currentScreen = DrawerScreen.LANGUAGE
                    },
                    onProfile  = onProfile,
                    isLoggedIn  = isLoggedIn,
                    )

                DrawerScreen.LANGUAGE -> LanguageScreen(
                    selectedLanguage = selectedLanguage,
                    onBack = {
                        isNavigatingForward = false
                        currentScreen = DrawerScreen.MENU
                    },
                    onSelect = { lang ->
                        onLanguageSelected(lang)
                        isNavigatingForward = false
                        currentScreen = DrawerScreen.MENU
                    }
                )

                DrawerScreen.THEME -> ThemeScreen(
                    selectedTheme = selectedTheme,
                    onBack   = { isNavigatingForward = false; currentScreen = DrawerScreen.MENU },
                    onSelect = { theme ->
                        onThemeSelected(theme)
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
    isLoggedIn: Boolean,
    onTheme: Action,
    onLanguage: Action,
    onProfile: Action,
) {
    data class Entry(val icon: ImageVector, val label: String, val action: () -> Unit)

    val items = buildList {
        if (isLoggedIn) add(Entry(Icons.Filled.Person, "Profile", onProfile))
        add(Entry(Icons.Filled.Palette,  "Theme",    onTheme))
        add(Entry(Icons.Filled.Language, "Language", onLanguage))
    }

    val colors = LocalWordleColors.current

    DrawerColumn {
        DrawerHeader(title = "Menu", onActionClick = onClose) {
            Icon(
                imageVector = Icons.Filled.Close,
                contentDescription = "Close",
                tint = colors.body,
                modifier = Modifier.size(20.dp)
            )
        }

        HorizontalDivider(color = colors.divider, thickness = 1.dp)

        items.forEach { item ->
            DrawerRow(
                onClick = item.action,
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 20.dp)
            ) {
                Icon(
                    imageVector = item.icon,
                    contentDescription = item.label,
                    tint = colors.body,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(16.dp))
                Text(text = item.label, color = colors.title, fontSize = 16.sp)
            }
            HorizontalDivider(color = colors.divider, thickness = 1.dp)
        }
    }
}

@Composable
private fun LanguageScreen(
    selectedLanguage: AppLanguage,
    onBack: Action,
    onSelect: (AppLanguage) -> Unit,
) {
    val colors = LocalWordleColors.current

    DrawerColumn {
        DrawerHeader(title = "Language", onActionClick = onBack) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                tint = colors.body,
                modifier = Modifier.size(20.dp)
            )
        }

        HorizontalDivider(color = colors.divider, thickness = 1.dp)

        AppLanguage.entries.forEach { lang ->
            val isSelected = lang == selectedLanguage

            DrawerRow(
                onClick = { onSelect(lang) },
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 20.dp)
            ) {
                Text(
                    text = lang.name.lowercase().replaceFirstChar { it.uppercase() },
                    color = colors.title,
                    fontSize = 16.sp,
                    modifier = Modifier.weight(1f)
                )
                if (isSelected) {
                    Icon(
                        imageVector = Icons.Filled.Check,
                        contentDescription = "Selected",
                        tint = colors.correct,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
            HorizontalDivider(color = colors.divider, thickness = 1.dp)
        }
    }
}

@Composable
private fun DrawerColumn(content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(),
        content = content
    )
}

@Composable
private fun DrawerHeader(
    title: String,
    onActionClick: () -> Unit,
    actionIcon: @Composable () -> Unit,
) {

    val colors = LocalWordleColors.current

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 24.dp, end = 8.dp, top = 16.dp, bottom = 16.dp)
    ) {
        Text(
            text = title,
            color = colors.title,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(1f)
        )
        IconButton(onClick = onActionClick, modifier = Modifier.size(40.dp)) {
            actionIcon()
        }
    }
}

@Composable
private fun DrawerRow(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable RowScope.() -> Unit,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() },
                onClick = onClick
            )
            .then(modifier),
        content = content
    )
}

@Composable
private fun ThemeScreen(
    selectedTheme: AppColorTheme,
    onBack: () -> Unit,
    onSelect: (AppColorTheme) -> Unit,
) {
    val colors = LocalWordleColors.current

    DrawerColumn {
        DrawerHeader(title = "Theme", onActionClick = onBack) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                tint = colors.body,
                modifier = Modifier.size(20.dp)
            )
        }

        HorizontalDivider(color = colors.divider, thickness = 1.dp)

        AppColorTheme.entries.forEach { theme ->
            val isSelected = theme == selectedTheme

            DrawerRow(
                onClick = { onSelect(theme) },
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 20.dp)
            ) {
                Text(
                    text = theme.name.lowercase().replaceFirstChar { it.uppercase() },
                    color = colors.title,
                    fontSize = 16.sp,
                    modifier = Modifier.weight(1f)
                )
                if (isSelected) {
                    Icon(
                        imageVector = Icons.Filled.Check,
                        contentDescription = "Selected",
                        tint = colors.correct,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
            HorizontalDivider(color = colors.divider, thickness = 1.dp)
        }
    }
}