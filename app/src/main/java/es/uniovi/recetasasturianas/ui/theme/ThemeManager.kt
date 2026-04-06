package es.uniovi.recetasasturianas.ui.theme

import android.content.Context
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatDelegate
import androidx.preference.PreferenceManager

/**
 * Gestor de temas de la aplicación.
 * Permite cambiar entre temas Claro, Oscuro y de Sistema.
 */
object ThemeManager {

    const val KEY_THEME = "app_theme"
    const val THEME_SYSTEM = "system"
    const val THEME_LIGHT = "light"
    const val THEME_DARK = "dark"

    /**
     * Aplica el tema guardado en las preferencias.
     */
    fun applyTheme(context: Context) {
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        val theme = prefs.getString(KEY_THEME, THEME_SYSTEM)
        applyThemeValue(theme)
    }

    /**
     * Aplica un valor de tema específico.
     * Usa un Handler para ejecutarlo en el siguiente ciclo del main loop
     * y evitar problemas de recreación de Activity inmediata.
     */
    fun applyThemeValue(theme: String?) {
        val mode = when (theme) {
            THEME_LIGHT -> AppCompatDelegate.MODE_NIGHT_NO
            THEME_DARK -> AppCompatDelegate.MODE_NIGHT_YES
            else -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
        }
        
        if (AppCompatDelegate.getDefaultNightMode() != mode) {
            
                AppCompatDelegate.setDefaultNightMode(mode)
            
        }
    }
}
