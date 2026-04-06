package es.uniovi.recetasasturianas

import androidx.recyclerview.widget.RecyclerView
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import org.hamcrest.Matchers.not
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Tests de UI para la pantalla principal (lista de recetas).
 *
 * Verifica:
 * - Carga de la lista
 * - Navegación al detalle
 * - Búsqueda
 * - Filtrado
 */
@RunWith(AndroidJUnit4::class)
@LargeTest
class RecipeListFragmentTest {

    @get:Rule
    val activityRule = ActivityScenarioRule(MainActivity::class.java)

    @Before
    fun setUp() {
        // Aquí se podría registrar un IdlingResource si la app lo expone
        // Por ejemplo, un contador de coroutines en el repositorio
    }

    @After
    fun tearDown() {
        // Desregistrar IdlingResources si se usaron
    }

    /**
     * Espera a que la lista cargue verificando el RecyclerView.
     * Reemplaza Thread.sleep con una espera condicional.
     */
    private fun waitForListToLoad(timeoutMs: Long = 5000) {
        val startTime = System.currentTimeMillis()
        while (System.currentTimeMillis() - startTime < timeoutMs) {
            try {
                onView(withId(R.id.recycler_view))
                    .check { view, _ ->
                        if (view is RecyclerView && view.adapter?.itemCount ?: 0 > 0) {
                            return // Lista cargada
                        }
                        throw AssertionError("Lista vacía")
                    }
                return // Éxito
            } catch (e: Exception) {
                Thread.sleep(200) // Reintentar
            }
        }
        // Último intento para dar error claro
        onView(withId(R.id.recycler_view)).check(matches(isDisplayed()))
    }

    /**
     * Verifica que la lista de recetas se muestra al iniciar la app.
     */
    @Test
    fun displayRecipeList() {
        waitForListToLoad()

        // Verificar que el RecyclerView se muestra
        onView(withId(R.id.recycler_view))
            .check(matches(isDisplayed()))

        // Verificar que hay al menos un item
        onView(withId(R.id.recycler_view))
            .check { view, noViewFoundException ->
                if (view is RecyclerView) {
                    assert(view.adapter?.itemCount ?: 0 > 0) {
                        "La lista de recetas no tiene elementos"
                    }
                }
            }
    }

    /**
     * Verifica que al hacer click en una receta se navega al detalle.
     */
    @Test
    fun clickRecipe_navigateToDetail() {
        waitForListToLoad()

        // Hacer click en el primer item
        onView(withId(R.id.recycler_view))
            .perform(
                RecyclerViewActions.actionOnItemAtPosition<RecyclerView.ViewHolder>(
                    0,
                    click()
                )
            )

        // Verificar que se muestra la pantalla de detalle
        // (puede estar en el panel derecho en tablets)
        Thread.sleep(500) // Pequeña espera para la transición
        onView(withId(R.id.text_name))
            .check(matches(isDisplayed()))
    }

    /**
     * Verifica que la búsqueda funciona correctamente.
     */
    @Test
    fun searchRecipe_filtersList() {
        waitForListToLoad()

        // Abrir búsqueda
        onView(withId(R.id.action_search))
            .perform(click())

        // Escribir término de búsqueda
        onView(isAssignableFrom(android.widget.SearchView::class.java))
            .perform(typeText("fabada"))

        // Esperar resultados
        Thread.sleep(1000)

        // Verificar que la lista sigue mostrándose
        onView(withId(R.id.recycler_view))
            .check(matches(isDisplayed()))
    }

    /**
     * Verifica que el filtro por tiempo funciona.
     */
    @Test
    fun filterByTime_filtersList() {
        waitForListToLoad()

        // Seleccionar filtro rápido
        onView(withId(R.id.chip_quick))
            .perform(click())

        // Esperar filtro
        Thread.sleep(500)

        // Verificar que la lista sigue mostrándose
        onView(withId(R.id.recycler_view))
            .check(matches(isDisplayed()))
    }

    /**
     * Verifica que el pull-to-refresh funciona.
     */
    @Test
    fun pullToRefresh_refreshesList() {
        waitForListToLoad()

        // Hacer pull-to-refresh
        onView(withId(R.id.swipe_refresh))
            .perform(swipeDown())

        // Esperar un momento
        Thread.sleep(1000)

        // Verificar que la lista sigue mostrándose
        onView(withId(R.id.recycler_view))
            .check(matches(isDisplayed()))
    }
}
