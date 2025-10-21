package ar.edu.unicen.tp_andiarena.ui.viewmodels
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ar.edu.unicen.tp_andiarena.data.model.Game
import ar.edu.unicen.tp_andiarena.data.model.GameFilters
import ar.edu.unicen.tp_andiarena.data.model.GamesResponse
import ar.edu.unicen.tp_andiarena.data.repository.GameRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
@HiltViewModel
class GamesViewModel @Inject constructor(
    private val gameRepository: GameRepository,
) : ViewModel() {
    private val _games = MutableStateFlow<List<Game>>(emptyList())
    val games: StateFlow<List<Game>> = _games.asStateFlow()
    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading.asStateFlow()
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()
    private val _currentFilters = MutableStateFlow<GameFilters?>(null)
    val currentFilters: StateFlow<GameFilters?> = _currentFilters.asStateFlow()
    private val _currentSearch = MutableStateFlow<String?>(null)
    val currentSearch: StateFlow<String?> = _currentSearch.asStateFlow()
    private val _canLoadMore = MutableStateFlow(true)
    val canLoadMore: StateFlow<Boolean> = _canLoadMore.asStateFlow()
    private var currentPage = 1
    private var isLoadingMore = false
    init {
        loadGames()
    }
    fun loadGames(
        filters: GameFilters? = null,
        loadMore: Boolean = false,
        search: String? = null
    ) {
        if (isLoadingMore) return
        viewModelScope.launch {
            if (!loadMore) {
                _loading.value = true
                currentPage = 1
                _games.value = emptyList()
            } else {
                isLoadingMore = true
            }
            _error.value = null
            try {
                val response = gameRepository.getGames(
                    page = currentPage,
                    pageSize = 20,
                    filters = filters,
                    search = search
                )
                if (loadMore) {
// Agregar nuevos juegos a la lista existente
                    _games.value = _games.value + response.results
                } else {
// Nueva lista de juegos
                    _games.value = response.results
                    if (search.isNullOrBlank()) {
                        _currentFilters.value = filters
                    }
                }
// Verificar si hay más páginas
                _canLoadMore.value = response.next != null
                currentPage++
            } catch (e: Exception) {
                _error.value = "Error al cargar juegos: ${e.message}"
            } finally {
                _loading.value = false
                isLoadingMore = false
            }
        }
    }
    fun loadMoreGames() {
        if (_canLoadMore.value && !isLoadingMore) {
            loadGames(
                filters = if (_currentSearch.value.isNullOrBlank()) _currentFilters.value else null,
                loadMore = true,
                search = _currentSearch.value
            )
        }
    }
    fun searchGames(query: String?) {
        _currentSearch.value = query
        loadGames(
            filters = if (query.isNullOrBlank()) _currentFilters.value else null,
            loadMore = false,
            search = if (query.isNullOrBlank()) null else query
        )
    }
    fun applyFilters(filters: GameFilters) {
        _currentSearch.value = null // Limpiar búsqueda al aplicar filtros
        loadGames(filters)
    }
    fun clearFilters() {
        loadGames(GameFilters.DEFAULT)
    }
    fun retry() {
        loadGames(
            filters = if (_currentSearch.value.isNullOrBlank()) _currentFilters.value else null,
            search = _currentSearch.value
        )
    }
}