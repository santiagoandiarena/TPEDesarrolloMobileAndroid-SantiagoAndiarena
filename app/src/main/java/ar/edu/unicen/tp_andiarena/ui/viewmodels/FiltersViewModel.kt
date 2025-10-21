package ar.edu.unicen.tp_andiarena.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ar.edu.unicen.tp_andiarena.data.model.GameFilters
import ar.edu.unicen.tp_andiarena.data.model.Genre
import ar.edu.unicen.tp_andiarena.data.model.Platform
import ar.edu.unicen.tp_andiarena.data.repository.GameRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FiltersViewModel @Inject constructor(
    private val gameRepository: GameRepository
) : ViewModel() {

    private val _platforms = MutableStateFlow<List<Platform>>(emptyList())
    val platforms: StateFlow<List<Platform>> = _platforms.asStateFlow()

    private val _genres = MutableStateFlow<List<Genre>>(emptyList())
    val genres: StateFlow<List<Genre>> = _genres.asStateFlow()

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _selectedFilters = MutableStateFlow<GameFilters>(GameFilters.DEFAULT)
    val selectedFilters: StateFlow<GameFilters> = _selectedFilters.asStateFlow()

    init {
        loadFiltersData()
    }

    fun loadFiltersData() {
        viewModelScope.launch {
            _loading.value = true
            _error.value = null
            try {
                val platformsJob = launch { loadPlatforms() }
                val genresJob = launch { loadGenres() }
                platformsJob.join()
                genresJob.join()
            } catch (e: Exception) {
                _error.value = e.message ?: "Error desconocido al cargar filtros"
            } finally {
                _loading.value = false
            }
        }
    }

    private suspend fun loadPlatforms() {
        try {
            println("DEBUG - Cargando plataformas...")
            val platformsList = gameRepository.getPlatforms()
            println("DEBUG - Plataformas recibidas: ${platformsList.size}")
            platformsList.forEach { p -> println("DEBUG - Platform: id=${p.id}, name=${p.name}") }

            _platforms.value = platformsList

        } catch (e: Exception) {
            println("DEBUG - Error cargando plataformas: ${e.message}")
            throw Exception("Error al cargar plataformas")
        }
    }

    private suspend fun loadGenres() {
        try {
            _genres.value = gameRepository.getGenres()
        } catch (e: Exception) {
            throw Exception("Error cargando géneros: ${e.message}")
        }
    }

    fun updatePlatforms(selectedPlatforms: List<Int>) {
        _selectedFilters.value = _selectedFilters.value.copy(
            platforms = if (selectedPlatforms.isNotEmpty()) selectedPlatforms else null
        )
    }

    fun updateGenres(selectedGenres: List<Int>) {
        _selectedFilters.value = _selectedFilters.value.copy(
            genres = if (selectedGenres.isNotEmpty()) selectedGenres else null
        )
    }

    fun updateOrdering(ordering: String?) {
        _selectedFilters.value = _selectedFilters.value.copy(ordering = ordering)
    }

    fun getCurrentFilters(): GameFilters {
        return _selectedFilters.value
    }

    fun retry() {
        loadFiltersData()
    }


}