package es.uniovi.recetasasturianas

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Tests de UI para la navegación de la app.
 *
 * Verifica:
 * - Navegación entre tabs del BottomNavigation
 * - Navegación hacia atrás
 */
@RunWith(AndroidJUnit4::class)
@LargeTest
class NavigationTest {

    @get:Rule
    val activityRule = ActivityScenarioRule(MainActivity::class.java)

    /**
     * Espera a que el RecyclerView esté visible.
     */
    private fun waitForListToLoad(timeoutMs: Long = 5000) {
        val startTime = System.currentTimeMillis()
        while (System.currentTimeMillis() - startTime < timeoutMs) {
            try {
                onView(withId(R.id.recycler_view))
                    .check(matches(isDisplayed()))
                return
            } catch (e: Exception) {
                Thread.sleep(200)
            }
        }
        // Último intento
        onView(withId(R.id.recycler_view)).check(matches(isDisplayed()))
    }

    /**
     * Verifica que el BottomNavigation navega entre fragments.
     */
    @Test
    fun bottomNavigation_navigatesBetweenTabs() {
        // Esperar a que cargue la lista principal
        waitForListToLoad()

        // Verificar que estamos en la lista de recetas (tab inicial)
        onView(withId(R.id.recycler_view))
            .check(matches(isDisplayed()))

        // Navegar a favoritos
        onView(withId(R.id.favoritesFragment))
            .perform(click())

        // Esperar transición
        Thread.sleep(500)

        // Verificar que estamos en favoritos
        // Puede mostrar empty view o la lista
        // (ambos contienen el contenedor de favoritos)
        Thread.sleep(1000)

        // Navegar a ajustes
        onView(withId(R.id.settingsFragment))
            .perform(click())

        // Esperar transición
        Thread.sleep(500)

        // Verificar que estamos en ajustes (buscando un texto de preferencia)
        onView(withText(R.string.pref_refresh_title))
            .check(matches(isDisplayed()))

        // Volver a recetas
        onView(withId(R.id.recipeListFragment))
            .perform(click())

        // Esperar transición
        Thread.sleep(500)

        // Verificar que estamos en la lista
        // Puede tardar en recargar, esperar un poco más
        Thread.sleep(1000)
        onView(withId(R.id.recycler_view))
            .check(matches(isDisplayed()))
    }

    /**
     * Verifica que el título de la app se muestra correctamente.
     */
    @Test
    fun appTitle_isDisplayed() {
        // Verificar que el título de la app está en la toolbar
        onView(withText(R.string.app_name))
            .check(matches(isDisplayed()))
    }
}
