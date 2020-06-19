package nike.urbandict.ui

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.SavedStateHandle
import com.nhaarman.mockitokotlin2.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import nike.urbandict.api.DefineResponse
import nike.urbandict.api.Definition
import nike.urbandict.api.UrbanDictApi
import nike.urbandict.ui.MainActivityViewModel.SortOrder
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import kotlin.coroutines.CoroutineContext

class MainActivityViewModelTest {

    @Rule
    @JvmField
    val rule = InstantTaskExecutorRule()

    private val api = mock<UrbanDictApi>()
    private val savedStateHandle = mock<SavedStateHandle>()

    private val response = DefineResponse(
        listOf(
            Definition(
                "wat",
                "",
                "",
                1,
                2,
                "",
                1L,
                ""
            ),
            Definition(
                "wat",
                "",
                "",
                3,
                4,
                "",
                2L,
                ""
            ),
            Definition(
                "wat",
                "",
                "",
                5,
                3,
                "",
                3L,
                ""
            ),
            Definition(
                "wat",
                "",
                "",
                6,
                1,
                "",
                4L,
                ""
            )
        )
    )

    @Test
    fun searchTest() {
        runBlocking {
            doReturn(response).`when`(api).define(eq("wat"))

            val viewModel =
                MainActivityViewModel(savedStateHandle, api, TestCoroutineContextProvider())

            viewModel.search("wat", SortOrder.THUMBS_UP)

            val value = viewModel.definitions.getOrAwaitValue()
            assertNull(value.error)
            assertEquals(false, value.isLoading)
            assertEquals(response.list.size, value.data?.size)
            assertEquals(4L, value.data?.get(0)?.id)

            verify(api).define(eq("wat"))
        }
    }

    @Test
    fun searchError() {
        runBlocking {
            doThrow(IllegalStateException()).`when`(api).define(any())

            val viewModel =
                MainActivityViewModel(savedStateHandle, api, TestCoroutineContextProvider())

            viewModel.search("wat", SortOrder.THUMBS_UP)

            val value = viewModel.definitions.getOrAwaitValue()
            assertNotNull(value.error)

            verify(api).define(eq("wat"))
        }
    }

    @Test
    fun sortingTest() {
        runBlocking {
            doReturn(response).`when`(api).define(eq("wat"))

            val viewModel =
                MainActivityViewModel(savedStateHandle, api, TestCoroutineContextProvider())

            viewModel.search("wat", SortOrder.THUMBS_UP)

            var value = viewModel.definitions.getOrAwaitValue()
            assertNull(value.error)
            assertEquals(false, value.isLoading)
            assertEquals(4L, value.data?.get(0)?.id)

            viewModel.sort(SortOrder.THUMBS_DOWN)

            value = viewModel.definitions.getOrAwaitValue()
            assertNull(value.error)
            assertEquals(false, value.isLoading)
            assertEquals(2L, value.data?.get(0)?.id)

            verify(api).define(eq("wat"))
        }
    }

    @Test
    fun refreshTest() {
        runBlocking {
            doReturn(response).`when`(api).define(eq("wat"))

            val viewModel =
                MainActivityViewModel(savedStateHandle, api, TestCoroutineContextProvider())

            viewModel.search("wat", SortOrder.THUMBS_UP)

            val value = viewModel.definitions.getOrAwaitValue()
            assertNull(value.error)
            assertEquals(false, value.isLoading)
            assertEquals(4L, value.data?.get(0)?.id)

            viewModel.refresh(SortOrder.THUMBS_UP)
            viewModel.definitions.getOrAwaitValue()

            verify(api, times(2)).define(eq("wat"))
        }
    }

    @Test
    fun startWithLoading() {
        runBlocking {
            doAnswer { CountDownLatch(1).await(5, TimeUnit.SECONDS) }.`when`(api)
                .define(eq("wat"))

            val viewModel =
                MainActivityViewModel(savedStateHandle, api, object : TestCoroutineContextProvider() {
                    override val IO: CoroutineContext
                        get() = Dispatchers.IO
                })

            viewModel.search("wat", SortOrder.THUMBS_UP)

            val value = viewModel.definitions.getOrAwaitValue()
            assertNull(value.error)
            assertEquals(true, value.isLoading)
        }
    }

    @Test
    fun consumeError() {
        val error = MainActivityViewModel.LoadingError(NullPointerException())
        assertFalse(error.consumed)
        error.consume()
        assertTrue(error.consumed)
    }
}