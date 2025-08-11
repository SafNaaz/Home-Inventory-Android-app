package com.homeinventory.app.presentation.ui.theme

import android.app.Activity
import android.os.Build
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import androidx.hilt.navigation.compose.hiltViewModel
import com.homeinventory.app.presentation.viewmodel.InventoryViewModel

private val DarkColorScheme = darkColorScheme(
    primary = Blue,
    secondary = Purple,
    tertiary = Pink,
    background = SystemBackgroundDark,
    surface = SystemGray6Dark,
    onPrimary = SystemBackground,
    onSecondary = SystemBackground,
    onTertiary = SystemBackground,
    onBackground = LabelDark,
    onSurface = LabelDark,
)

private val LightColorScheme = lightColorScheme(
    primary = Blue,
    secondary = Purple,
    tertiary = Pink,
    background = SystemBackground,
    surface = SystemGray6,
    onPrimary = SystemBackground,
    onSecondary = SystemBackground,
    onTertiary = SystemBackground,
    onBackground = Label,
    onSurface = Label,
)

@Composable
fun HomeInventoryTheme(
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val viewModel: InventoryViewModel = hiltViewModel()
    val settings by viewModel.settings.collectAsState()
    val darkTheme = settings.isDarkMode
    
    // Add debugging
    LaunchedEffect(darkTheme) {
        println("DEBUG: Theme recomposing with darkTheme = $darkTheme")
        // Apply theme change to AppCompatDelegate
        AppCompatDelegate.setDefaultNightMode(
            if (darkTheme) AppCompatDelegate.MODE_NIGHT_YES 
            else AppCompatDelegate.MODE_NIGHT_NO
        )
    }
    
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.primary.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
