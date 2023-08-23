package de.mm20.launcher2.themes

import android.content.Context
import android.util.Log
import de.mm20.launcher2.database.AppDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.util.UUID

class ThemeRepository(
    private val context: Context,
    private val database: AppDatabase,
) {
    private val scope = CoroutineScope(Dispatchers.IO + Job())

    fun getThemes(): Flow<List<Theme>> {
        return database.themeDao().getAll().map {
            getBuiltInThemes() + it.map { Theme(it) }
        }
    }

    fun getTheme(id: UUID): Flow<Theme?> {
        if (id == DefaultThemeId) return flowOf(getDefaultTheme())
        if (id == BlackAndWhiteThemeId) return flowOf(getBlackAndWhiteTheme())
        return database.themeDao().get(id).map { it?.let { Theme(it) } }.flowOn(Dispatchers.Default)
    }

    fun createTheme(theme: Theme) {
        scope.launch {
            database.themeDao().insert(theme.toEntity())
        }
    }

    fun updateTheme(theme: Theme) {
        scope.launch {
            database.themeDao().update(theme.toEntity())
        }
    }

    fun getThemeOrDefault(id: UUID?): Flow<Theme> {
        if (id == null) return flowOf(getDefaultTheme())
        return getTheme(id).map { it ?: getDefaultTheme() }
    }

    private fun getBuiltInThemes(): List<Theme> {
        return listOf(
            getDefaultTheme(),
            getBlackAndWhiteTheme(),
        )
    }

    fun getDefaultTheme(): Theme {
        return Theme(
            id = DefaultThemeId,
            builtIn = true,
            name = context.getString(R.string.preference_colors_default),
            corePalette = EmptyCorePalette,
            lightColorScheme = DefaultLightColorScheme,
            darkColorScheme = DefaultDarkColorScheme,
        )
    }

    private fun getBlackAndWhiteTheme(): Theme {
        return Theme(
            id = BlackAndWhiteThemeId,
            builtIn = true,
            name = context.getString(R.string.preference_colors_bw),
            corePalette = EmptyCorePalette,
            lightColorScheme = BlackAndWhiteLightColorScheme,
            darkColorScheme = BlackAndWhiteDarkColorScheme,
        )
    }

}