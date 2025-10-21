package ar.edu.unicen.tp_andiarena.data.model

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class Game(
    val id: Int,
    val name: String,
    @SerializedName("background_image")
    val backgroundImage: String?,
    val released: String?,
    val rating: Double,
    val platforms: List<GamePlatform>? = emptyList(),
    val genres: List<Genre>?,
    val stores: List<Store>?,
    @SerializedName("description_raw")
    val description: String?,
    @SerializedName("website")
    val website: String?
) : Parcelable

// Estructura para plataformas en juegos
@Parcelize
data class GamePlatform(
    val platform: Platform
) : Parcelable

// Estructura simple para lista de plataformas (filtros)
@Parcelize
data class Platform(
    val id: Int,
    val name: String
) : Parcelable

@Parcelize
data class Genre(
    val id: Int,
    val name: String
) : Parcelable

@Parcelize
data class Store(
    val store: StoreDetail
) : Parcelable

@Parcelize
data class StoreDetail(
    val id: Int,
    val name: String
) : Parcelable

data class GamesResponse(
    val results: List<Game>,
    val count: Int,
    val next: String?
)

data class PlatformResponse(
    val results: List<Platform> // Para filtros - estructura simple
)

data class GenreResponse(
    val results: List<Genre>
)