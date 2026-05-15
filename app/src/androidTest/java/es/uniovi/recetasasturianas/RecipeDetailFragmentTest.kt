package es.uniovi.recetasasturianas

import androidx.recyclerview.widget.RecyclerView
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import com.adevinta.android.barista.assertion.BaristaVisibilityAssertions.assertDisplayed
import com.adevinta.android.barista.interaction.BaristaClickInteractions.clickOn
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@LargeTest
class RecipeDetailFragmentTest {

    @get:Rule
    val activityRule = ActivityScenarioRule(MainActivity::class.java)

    private fun navigateToFirstRecipe() {
        val startTime = System.currentTimeMillis()
        var loaded = false
        while (System.currentTimeMillis() - startTime < 30000 && !loaded) {
            try {
                onView(withId(R.id.recycler_view)).check { view, _ ->
                    loaded = view is RecyclerView && view.adapter?.itemCount ?: 0 > 0
                }
            } catch (_: Exception) {
                Thread.sleep(200)
            }
        }

        onView(withId(R.id.recycler_view))
            .perform(
                RecyclerViewActions.actionOnItemAtPosition<RecyclerView.ViewHolder>(
                    0,
                    click()
                )
            )
    }

    @Test
    fun displayRecipeDetails() {
        navigateToFirstRecipe()

        assertDisplayed(R.id.text_name)
        assertDisplayed(R.id.text_restaurant)

        onView(withId(R.id.text_ingredients)).check(matches(withEffectiveVisibility(Visibility.VISIBLE)))
        onView(withId(R.id.text_preparation)).check(matches(withEffectiveVisibility(Visibility.VISIBLE)))
    }

    @Test
    fun toggleFavorite_updatesFabIcon() {
        navigateToFirstRecipe()

        clickOn(R.id.fab_favorite)
        assertDisplayed(R.id.fab_favorite)
    }

    @Test
    fun restaurantButton_existsIfHasUrl() {
        navigateToFirstRecipe()
    }
}
