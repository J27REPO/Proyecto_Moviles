package es.uniovi.recetasasturianas.ui.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.preference.ListPreference
import androidx.preference.PreferenceFragmentCompat
import es.uniovi.recetasasturianas.R
import es.uniovi.recetasasturianas.ui.theme.ThemeManager

/**
 * Fragment para las preferencias de la aplicación.
 *
 * Preferencias disponibles:
 * - Intervalo de actualización (cada cuántas horas refrescar)
 * - Orden por defecto (nombre / restaurante)
 * - Ocultar recetas sin tiempo de preparación
 */
class SettingsFragment : PreferenceFragmentCompat() {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preferences, rootKey)

        // Configurar summaries dinámicos
        setupPreferenceSummaries()
    }

    private fun setupPreferenceSummaries() {
        // Tema de la aplicación
        findPreference<ListPreference>(ThemeManager.KEY_THEME)?.setOnPreferenceChangeListener { preference, newValue ->
            val themeValue = newValue as String
            updateListPreferenceSummary(preference as ListPreference, themeValue)
            ThemeManager.applyThemeValue(themeValue)
            true
        }

        // Intervalo de actualización
        findPreference<ListPreference>("refresh_hours")?.setOnPreferenceChangeListener { preference, newValue ->
            updateListPreferenceSummary(preference as ListPreference, newValue as String)
            true
        }

        // Orden por defecto
        findPreference<ListPreference>("default_sort")?.setOnPreferenceChangeListener { preference, newValue ->
            updateListPreferenceSummary(preference as ListPreference, newValue as String)
            true
        }
    }

    private fun updateListPreferenceSummary(preference: ListPreference, newValue: String) {
        val index = preference.findIndexOfValue(newValue)
        if (index >= 0) {
            preference.summary = preference.entries[index]
        }
    }
}
