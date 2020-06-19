package nike.urbandict.ui

import kotlinx.coroutines.Dispatchers
import nike.urbandict.di.CoroutineContextProvider
import kotlin.coroutines.CoroutineContext

open class TestCoroutineContextProvider : CoroutineContextProvider() {
    override val Main: CoroutineContext = Dispatchers.Unconfined
    override val IO: CoroutineContext = Dispatchers.Unconfined
}
