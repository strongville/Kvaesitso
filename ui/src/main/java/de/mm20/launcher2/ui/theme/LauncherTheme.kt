package de.mm20.launcher2.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import de.mm20.launcher2.preferences.LauncherDataStore
import de.mm20.launcher2.preferences.Settings
import de.mm20.launcher2.preferences.Settings.AppearanceSettings
import de.mm20.launcher2.preferences.Settings.AppearanceSettings.Theme
import de.mm20.launcher2.ui.locals.LocalDarkTheme
import de.mm20.launcher2.ui.theme.colorscheme.*
import de.mm20.launcher2.ui.theme.typography.DefaultTypography
import kotlinx.coroutines.flow.map
import org.koin.androidx.compose.inject


@Composable
fun LauncherTheme(
    content: @Composable () -> Unit
) {

    val dataStore: LauncherDataStore by inject()

    val colorSchemePreference by remember {
        dataStore.data.map {
            if (it.easterEgg) Settings.AppearanceSettings.ColorScheme.EasterEgg
            else it.appearance.colorScheme
        }
    }.collectAsState(
        AppearanceSettings.ColorScheme.Default
    )

    val themePreference by remember { dataStore.data.map { it.appearance.theme } }.collectAsState(
        Theme.System
    )
    val darkTheme =
        themePreference == Theme.Dark || themePreference == Theme.System && isSystemInDarkTheme()

    val cornerRadius by remember {
        dataStore.data.map { it.cards.radius.dp }
    }.collectAsState(8.dp)

    val colorScheme by colorSchemeAsState(colorSchemePreference, darkTheme)

    CompositionLocalProvider(
        LocalDarkTheme provides darkTheme
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = DefaultTypography,
            shapes = Shapes(
                extraSmall = RoundedCornerShape(cornerRadius / 3f),
                small = RoundedCornerShape(cornerRadius / 3f * 2f),
                medium = RoundedCornerShape(cornerRadius),
                large = RoundedCornerShape(cornerRadius / 3f * 4f),
                extraLarge = RoundedCornerShape(cornerRadius / 3f * 7f),
            ),
            content = content
        )
    }
}

@Composable
fun colorSchemeAsState(colorScheme: AppearanceSettings.ColorScheme, darkTheme: Boolean): MutableState<ColorScheme> {
    val context = LocalContext.current
    val dataStore: LauncherDataStore by inject()

    when (colorScheme) {
        AppearanceSettings.ColorScheme.BlackAndWhite -> {
            return remember(darkTheme) {
                mutableStateOf(
                    if (darkTheme) DarkBlackAndWhiteColorScheme else LightBlackAndWhiteColorScheme
                )
            }
        }
        AppearanceSettings.ColorScheme.EasterEgg -> {
            return remember(darkTheme) {
                mutableStateOf(
                    if (darkTheme) DarkEasterEggColorScheme else LightEasterEggColorScheme
                )
            }
        }
        AppearanceSettings.ColorScheme.Custom -> {
            val colors by remember(darkTheme) {
                dataStore.data.map { if (darkTheme) it.appearance.customColors.darkScheme else it.appearance.customColors.lightScheme }
            }.collectAsState(null)
            val state = remember(colors, darkTheme) {
                mutableStateOf(
                    colors?.let { CustomColorScheme(it) }
                        ?: if (darkTheme) DarkDefaultColorScheme else LightDefaultColorScheme
                )
            }
            return state
        }
        else -> {
            if (Build.VERSION.SDK_INT >= 27 && (Build.VERSION.SDK_INT < 31 || colorScheme == AppearanceSettings.ColorScheme.DebugMaterialYouCompat)) {
                val wallpaperColors by wallpaperColorsAsState()
                val state = remember(wallpaperColors, darkTheme) {
                    mutableStateOf(
                        wallpaperColors?.let { MaterialYouCompatScheme(it, darkTheme) }
                            ?: if (darkTheme) DarkDefaultColorScheme else LightDefaultColorScheme
                    )
                }
                return state
            }
            if (Build.VERSION.SDK_INT >= 31) {
                return remember(darkTheme) {
                    mutableStateOf(
                        if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(
                            context
                        )
                    )
                }
            }

            return remember { mutableStateOf(if (darkTheme) DarkDefaultColorScheme else LightDefaultColorScheme) }

        }
    }

}
