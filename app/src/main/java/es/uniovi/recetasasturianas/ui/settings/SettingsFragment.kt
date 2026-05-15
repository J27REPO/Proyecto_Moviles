package es.uniovi.recetasasturianas.ui.settings

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.preference.ListPreference
import androidx.preference.PreferenceFragmentCompat
import es.uniovi.recetasasturianas.R
import es.uniovi.recetasasturianas.ui.theme.ThemeManager

/**
 * Fragment para las preferencias de la aplicación.
 *
 * NOTA: Los summaries se actualizan automáticamente gracias a
 * `app:useSimpleSummaryProvider="true"` en el XML. No es necesario
 * llamar manualmente a updateListPreferenceSummary().
 */
class SettingsFragment : PreferenceFragmentCompat() {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preferences, rootKey)

        findPreference<ListPreference>(ThemeManager.KEY_THEME)?.setOnPreferenceChangeListener { _, newValue ->
            val themeValue = newValue as String
            // Aplicar el tema en el siguiente ciclo del loop para evitar
            // que la recreación de la Activity ocurra dentro del callback
            // del cambio de preferencia (lo que causaría crash).
            Handler(Looper.getMainLooper()).post {
                ThemeManager.applyThemeValue(themeValue)
            }
            true
        }
    }
}
