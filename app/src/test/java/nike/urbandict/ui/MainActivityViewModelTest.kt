package nike.urbandict.ui

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.SavedStateHandle
import com.nhaarman.mockitokotlin2.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import nike.urbandict.api.DefineResponse
import nike.urbandict.api.Definition
import nike.urbandict.api.UrbanDictApi
import nike.urbandict.model.DefinitionsProcessor
import nike.urbandict.model.ProcessedDefinition
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
    private val definitionsProcessor = mock<DefinitionsProcessor>().apply {
        doAnswer {
            val raw = it.arguments.first() as Definition
            ProcessedDefinition(
                mock(),
                raw.word,
                raw.thumbsUp,
                raw.thumbsDown,
                raw.id
            )
        }.`when`(this).process(any())
    }
    private val coroutineContextProvider = TestCoroutineContextProvider()

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
    fun validTerm_searchThumpsUp_getDefinitionsSortedUp() {
        runBlocking {
            doReturn(response).`when`(api).define(eq("wat"))

            val viewModel = MainActivityViewModel(
                savedStateHandle,
                api,
                coroutineContextProvider,
                definitionsProcessor
            )

            viewModel.search("wat", SortOrder.THUMBS_UP)

            val value = viewModel.getDefinitions().getOrAwaitValue()
            assertNull(value.error)
            assertEquals(false, value.isLoading)
            assertEquals(response.list.size, value.data?.size)
            assertEquals(4L, value.data?.get(0)?.id)

            verify(api).define(eq("wat"))
        }
    }

    @Test
    fun validTerm_searchThumpsUp_getError() {
        runBlocking {
            doThrow(IllegalStateException()).`when`(api).define(any())

            val viewModel = MainActivityViewModel(
                savedStateHandle,
                api,
                coroutineContextProvider,
                definitionsProcessor
            )

            viewModel.search("wat", SortOrder.THUMBS_UP)

            val value = viewModel.getDefinitions().getOrAwaitValue()
            assertNotNull(value.error)

            verify(api).define(eq("wat"))
        }
    }

    @Test
    fun validTerm_search_getDefinitionsSorted(){
        runBlocking {
            doReturn(response).`when`(api).define(eq("wat"))

            val viewModel = MainActivityViewModel(
                savedStateHandle,
                api,
                coroutineContextProvider,
                definitionsProcessor
            )

            viewModel.search("wat", SortOrder.THUMBS_UP)

            var value = viewModel.getDefinitions().getOrAwaitValue()
            assertNull(value.error)
            assertEquals(false, value.isLoading)
            assertEquals(4L, value.data?.get(0)?.id)

            viewModel.sort(SortOrder.THUMBS_DOWN)

            value = viewModel.getDefinitions().getOrAwaitValue()
            assertNull(value.error)
            assertEquals(false, value.isLoading)
            assertEquals(2L, value.data?.get(0)?.id)

            verify(api).define(eq("wat"))
        }
    }

    @Test
    fun viewModel_getRefreshedData() {
        runBlocking {
            doReturn(response).`when`(api).define(eq("wat"))

            val viewModel = MainActivityViewModel(
                savedStateHandle,
                api,
                coroutineContextProvider,
                definitionsProcessor
            )

            viewModel.search("wat", SortOrder.THUMBS_UP)

            val value = viewModel.getDefinitions().getOrAwaitValue()
            assertNull(value.error)
            assertEquals(false, value.isLoading)
            assertEquals(4L, value.data?.get(0)?.id)

            viewModel.refresh(SortOrder.THUMBS_UP)
            viewModel.getDefinitions().getOrAwaitValue()
            verify(api, times(2)).define(eq("wat"))
        }
    }

    @Test
    fun validTerm_startWithLoading_getLoadedDefinitions() {
        runBlocking {
            doAnswer { CountDownLatch(1).await(5, TimeUnit.SECONDS) }.`when`(api)
                .define(eq("wat"))

            val viewModel = MainActivityViewModel(
                savedStateHandle,
                api,
                object : TestCoroutineContextProvider() {
                    override val IO: CoroutineContext
                        get() = Dispatchers.IO
                },
                definitionsProcessor
            )

            viewModel.search("wat", SortOrder.THUMBS_UP)

            val value = viewModel.getDefinitions().getOrAwaitValue()
            assertNull(value.error)
            assertEquals(true, value.isLoading)
        }
    }

    @Test
    fun viewModel_checkConsumedError() {
        val error = MainActivityViewModel.LoadingError(NullPointerException())
        assertFalse(error.consumed)
        error.consume()
        assertTrue(error.consumed)
    }
}