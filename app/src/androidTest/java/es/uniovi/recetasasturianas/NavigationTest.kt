package es.uniovi.recetasasturianas

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.platform.app.InstrumentationRegistry
import com.adevinta.android.barista.assertion.BaristaVisibilityAssertions.assertDisplayed
import com.adevinta.android.barista.interaction.BaristaClickInteractions.clickOn
import org.hamcrest.Matchers.allOf
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@LargeTest
class NavigationTest {

    @get:Rule
    val activityRule = ActivityScenarioRule(MainActivity::class.java)

    private val context = InstrumentationRegistry.getInstrumentation().targetContext

    private fun waitForListToLoad(timeoutMs: Long = 15000) {
        val startTime = System.currentTimeMillis()
        while (System.currentTimeMillis() - startTime < timeoutMs) {
            try {
                assertDisplayed(R.id.recycler_view)
                return
            } catch (e: Exception) {
                Thread.sleep(200)
            }
        }
        assertDisplayed(R.id.recycler_view)
    }

    @Test
    fun bottomNavigation_navigatesBetweenTabs() {
        waitForListToLoad()

        clickOn(R.id.favoritesFragment)
        clickOn(R.id.settingsFragment)
        assertDisplayed(context.getString(R.string.pref_refresh_title))

        clickOn(R.id.recipeListFragment)
        waitForListToLoad()
        assertDisplayed(R.id.recycler_view)
    }

    @Test
    fun appTitle_isDisplayed() {
        waitForListToLoad()

        onView(allOf(withText(R.string.nav_recipes), withParent(withId(R.id.toolbar))))
            .check(matches(isDisplayed()))
    }
}
