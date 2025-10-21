package ar.edu.unicen.tp_andiarena.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class GameFilters(
    val platforms: List<Int>? = null,
    val genres: List<Int>? = null,
    val ordering: String? = null
) : Parcelable {
    fun hasActiveFilters(): Boolean {
        return !platforms.isNullOrEmpty() || !genres.isNullOrEmpty() || !ordering.isNullOrEmpty()
    }

    companion object {
        val DEFAULT = GameFilters()
    }
}