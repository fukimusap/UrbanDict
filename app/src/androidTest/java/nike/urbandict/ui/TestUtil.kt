package nike.urbandict.ui

import androidx.recyclerview.widget.RecyclerView
import androidx.test.espresso.Espresso
import androidx.test.platform.app.InstrumentationRegistry

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