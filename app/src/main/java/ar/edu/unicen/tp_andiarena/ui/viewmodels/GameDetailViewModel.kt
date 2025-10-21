package ar.edu.unicen.tp_andiarena.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ar.edu.unicen.tp_andiarena.data.model.Game
import ar.edu.unicen.tp_andiarena.data.repository.GameRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class GameDetailViewModel @Inject constructor(
    private val gameRepository: GameRepository
) : ViewModel() {

    private val _game = MutableStateFlow<Game?>(null)
    val game: StateFlow<Game?> = _game.asStateFlow()

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    fun loadGameDetails(gameId: Int) {
        viewModelScope.launch {
            _loading.value = true
            _error.value = null
            try {
                val gameDetails = gameRepository.getGameDetails(gameId)
                _game.value = gameDetails
            } catch (e: Exception) {
                _error.value = "Error al cargar detalles: ${e.message}"
            } finally {
                _loading.value = false
            }
        }
    }
}