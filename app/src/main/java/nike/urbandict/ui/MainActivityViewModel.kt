package nike.urbandict.ui

import androidx.hilt.Assisted
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.*
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import nike.urbandict.api.UrbanDictApi
import nike.urbandict.di.CoroutineContextProvider
import nike.urbandict.model.DefinitionsProcessor
import nike.urbandict.model.ProcessedDefinition

class MainActivityViewModel @ViewModelInject constructor(
    @Assisted private val stateHandle: SavedStateHandle,
    private val api: UrbanDictApi,
    private val coroutineContextProvider: CoroutineContextProvider,
    private val definitionsProcessor: DefinitionsProcessor
) : ViewModel() {

    val definitions: LiveData<Result<List<ProcessedDefinition>>> = MutableLiveData()

    private var searchTerm: String? = null
    private var currentJob: Job? = null

    init {
        searchTerm = stateHandle[STATE_TERM]
        if (!searchTerm.isNullOrBlank()) {
            val sortOrder =
                if (stateHandle.get<Int>(STATE_SORT_ORDER) == SortOrder.THUMBS_DOWN.ordinal) {
                    SortOrder.THUMBS_DOWN
                } else {
                    SortOrder.THUMBS_UP
                }
            search(searchTerm.orEmpty(), sortOrder)
        }
    }

    fun sort(sortOrder: SortOrder) {
        stateHandle[STATE_SORT_ORDER] = sortOrder.ordinal
        val mutableLiveData = definitions as MutableLiveData
        currentJob = viewModelScope.launch(coroutineContextProvider.Main) {
            val currentValue = definitions.value
            if (currentValue?.data.isNullOrEmpty()) {
                mutableLiveData.value =
                    Result(
                        data = null,
                        error = null,
                        isLoading = false
                    )
            } else {
                mutableLiveData.value =
                    Result(
                        data = currentValue?.data,
                        error = currentValue?.error,
                        isLoading = true
                    )
                val sortedList = withContext(coroutineContextProvider.IO) {
                    ArrayList(currentValue?.data.orEmpty()).sortedByDescending {
                        if (sortOrder == SortOrder.THUMBS_UP) {
                            it.thumbsUp
                        } else {
                            it.thumbsDown
                        }
                    }
                }
                mutableLiveData.value =
                    Result(
                        data = sortedList,
                        error = null,
                        isLoading = false
                    )
            }
        }
    }

    fun refresh(sort: SortOrder) {
        stateHandle[STATE_SORT_ORDER] = sort.ordinal
        search(searchTerm.orEmpty(), sort)
    }

    fun search(term: String, sortOrder: SortOrder) {
        this.searchTerm = term
        stateHandle[STATE_TERM] = term
        stateHandle[STATE_SORT_ORDER] = sortOrder.ordinal

        val mutableLiveData = definitions as MutableLiveData

        if (term.isBlank()) {
            mutableLiveData.value =
                Result(
                    data = emptyList(),
                    error = null,
                    isLoading = false
                )
            return
        }

        currentJob = viewModelScope.launch(coroutineContextProvider.Main) {
            val currentValue = definitions.value
            mutableLiveData.value =
                Result(
                    data = currentValue?.data,
                    error = currentValue?.error,
                    isLoading = true
                )

            try {
                val searchResult = withContext(coroutineContextProvider.IO) {
                    api.define(term).list
                        .map { definitionsProcessor.process(it) }
                        .sortedByDescending {
                            if (sortOrder == SortOrder.THUMBS_UP) {
                                it.thumbsUp
                            } else {
                                it.thumbsDown
                            }
                        }
                }
                mutableLiveData.value =
                    Result(
                        data = searchResult,
                        error = null,
                        isLoading = false
                    )
            } catch (ignore: CancellationException) {
                // Coroutine was canceled.
            } catch (t: Throwable) {
                mutableLiveData.value =
                    Result(
                        data = null,
                        error = LoadingError(
                            throwable = t
                        ),
                        isLoading = false
                    )
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        currentJob?.cancel()
    }
    companion object {
        private const val STATE_TERM = "term"
        private const val STATE_SORT_ORDER = "sort"
    }

    data class Result<out T>(
        val data: T?,
        val error: LoadingError?,
        val isLoading: Boolean = false
    )

    data class LoadingError(val throwable: Throwable, var consumed: Boolean = false) {
        fun consume(): Throwable {
            consumed = true
            return throwable
        }
    }

    enum class SortOrder {
        THUMBS_UP,
        THUMBS_DOWN
    }
}