package es.uniovi.recetasasturianas

import androidx.recyclerview.widget.RecyclerView
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.swipeDown
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import com.adevinta.android.barista.assertion.BaristaVisibilityAssertions.assertDisplayed
import com.adevinta.android.barista.interaction.BaristaClickInteractions.clickOn
import com.adevinta.android.barista.interaction.BaristaEditTextInteractions.writeTo
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@LargeTest
class RecipeListFragmentTest {

    @get:Rule
    val activityRule = ActivityScenarioRule(MainActivity::class.java)

    private fun waitForListToLoad(timeoutMs: Long = 30000) {
        val startTime = System.currentTimeMillis()
        var loaded = false
        while (System.currentTimeMillis() - startTime < timeoutMs && !loaded) {
            try {
                onView(withId(R.id.recycler_view)).check { view, _ ->
                    loaded = view is RecyclerView && view.adapter?.itemCount ?: 0 > 0
                }
            } catch (_: Exception) {
                Thread.sleep(200)
            }
        }
        onView(withId(R.id.recycler_view)).check(matches(isDisplayed()))
    }

    @Test
    fun displayRecipeList() {
        waitForListToLoad()

        assertDisplayed(R.id.recycler_view)

        onView(withId(R.id.recycler_view)).check { view, _ ->
            assert((view as RecyclerView).adapter?.itemCount ?: 0 > 0) {
                "La lista de recetas no tiene elementos"
            }
        }
    }

    @Test
    fun clickRecipe_navigateToDetail() {
        waitForListToLoad()

        onView(withId(R.id.recycler_view))
            .perform(
                RecyclerViewActions.actionOnItemAtPosition<RecyclerView.ViewHolder>(
                    0,
                    click()
                )
            )

        assertDisplayed(R.id.text_name)
    }

    @Test
    fun searchRecipe_filtersList() {
        waitForListToLoad()

        clickOn(R.id.action_search)
        writeTo(androidx.appcompat.R.id.search_src_text, "fabada")

        assertDisplayed(R.id.recycler_view)
    }

    @Test
    fun filterByTime_filtersList() {
        waitForListToLoad()

        clickOn(R.id.chip_quick)

        assertDisplayed(R.id.recycler_view)
    }

    @Test
    fun pullToRefresh_refreshesList() {
        waitForListToLoad()

        onView(withId(R.id.swipe_refresh)).perform(swipeDown())

        assertDisplayed(R.id.recycler_view)
    }
}
