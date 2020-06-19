package nike.urbandict.ui

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import androidx.test.espresso.Espresso
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.TypeTextAction
import androidx.test.espresso.action.ViewActions.pressImeActionButton
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withResourceName
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SmallTest
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.rule.ActivityTestRule
import nike.urbandict.R
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@SmallTest
class MainActivityTest {

    @get:Rule
    val activityTestRule = ActivityTestRule<MainActivity>(MainActivity::class.java)

    @get:Rule
    val taskExecutorRule = InstantTaskExecutorRule()

    @Test
    fun testEvent() {
        val activity = activityTestRule.activity

        onView(withResourceName("search_src_text"))
            .perform(TypeTextAction("wat"), pressImeActionButton())

        waitUntilLoaded { activity.findViewById(R.id.definitionsView) }

        assertFalse(activity.findViewById<SwipeRefreshLayout>(R.id.swipeRefreshView).isRefreshing)

        onView(withId(R.id.definitionsView)).check { view, noViewFoundException ->
            if (view == null) {
                throw noViewFoundException
            }
            assertTrue((view as RecyclerView).childCount > 2)
        }
    }

    /**
     * Stop the test until RecyclerView's data gets loaded.
     *
     * Passed [recyclerProvider] will be activated in UI thread, allowing you to retrieve the View.
     *
     * Workaround for https://issuetracker.google.com/issues/123653014
     */
    inline fun waitUntilLoaded(crossinline recyclerProvider: () -> RecyclerView) {
        Espresso.onIdle()

        lateinit var recycler: RecyclerView

        InstrumentationRegistry.getInstrumentation().runOnMainSync {
            recycler = recyclerProvider()
        }

        while (recycler.hasPendingAdapterUpdates()) {
            Thread.sleep(10)
        }
    }

}