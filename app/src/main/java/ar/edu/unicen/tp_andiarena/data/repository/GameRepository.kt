package ar.edu.unicen.tp_andiarena.data.repository
import ar.edu.unicen.tp_andiarena.data.model.Game
import ar.edu.unicen.tp_andiarena.data.model.GameFilters
import ar.edu.unicen.tp_andiarena.data.model.GamesResponse
import ar.edu.unicen.tp_andiarena.data.model.Genre
import ar.edu.unicen.tp_andiarena.data.model.Platform
import ar.edu.unicen.tp_andiarena.data.remote.RawgApiService
import javax.inject.Inject
class GameRepository @Inject constructor(
    private val apiService: RawgApiService
) {
    suspend fun getGames(
        page: Int = 1,
        pageSize: Int = 20,
        filters: GameFilters? = null,
        search: String? = null
    ): GamesResponse {
        return apiService.getGames(
            page = page,
            pageSize = pageSize,
            platforms = filters?.platforms?.joinToString(","),
            genres = filters?.genres?.joinToString(","),
            ordering = filters?.ordering,
            search = search
        )
    }
    suspend fun getGameDetails(id: Int): Game {
        return apiService.getGameDetails(id)
    }
    suspend fun getPlatforms(): List<Platform> {
        return apiService.getPlatforms().results
    }
    suspend fun getGenres(): List<Genre> {
        return apiService.getGenres().results
    }
}