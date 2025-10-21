package ar.edu.unicen.tp_andiarena.data.remote

import ar.edu.unicen.tp_andiarena.data.model.Game
import ar.edu.unicen.tp_andiarena.data.model.GamesResponse
import ar.edu.unicen.tp_andiarena.data.model.GenreResponse
import ar.edu.unicen.tp_andiarena.data.model.PlatformResponse
import retrofit2.http.GET
import retrofit2.http.Query
import retrofit2.http.Path

interface RawgApiService {
    @GET("games")
    suspend fun getGames(
        @Query("page") page: Int = 1,
        @Query("page_size") pageSize: Int = 20,
        @Query("platforms") platforms: String? = null,
        @Query("genres") genres: String? = null,
        @Query("ordering") ordering: String? = null,
        @Query("search") search: String? = null
    ): GamesResponse

    @GET("games/{id}")
    suspend fun getGameDetails(@Path("id") id: Int): Game

    @GET("platforms")
    suspend fun getPlatforms(): PlatformResponse

    @GET("genres")
    suspend fun getGenres(): GenreResponse
}