package ar.edu.unicen.tp_andiarena.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import ar.edu.unicen.tp_andiarena.data.model.Game
import ar.edu.unicen.tp_andiarena.databinding.ItemGameBinding
import coil.load

class GamesAdapter(
    private val onItemClick: (Game) -> Unit,
    private val onLoadMore: () -> Unit
) : ListAdapter<Game, GamesAdapter.GameViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GameViewHolder {
        val binding = ItemGameBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return GameViewHolder(binding)
    }

    override fun onBindViewHolder(holder: GameViewHolder, position: Int) {
        val game = getItem(position)
        holder.bind(game)

        // Detectar cuando estamos cerca del final para cargar más juegos
        if (position >= itemCount - 5) {
            onLoadMore()
        }
    }

    inner class GameViewHolder(
        private val binding: ItemGameBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(game: Game) {
            binding.apply {
                textGameName.text = game.name
                textGameRating.text = "Rating: ${game.rating}"

                // Cargar imagen con Coil
                imageGame.load(game.backgroundImage) {
                    crossfade(true)
                    placeholder(android.R.drawable.ic_menu_gallery)
                    error(android.R.drawable.ic_menu_report_image)
                }

                // Mostrar plataformas si están disponibles
                game.platforms?.let { platforms ->
                    val platformNames = platforms.take(3).joinToString { it.platform.name }
                    textPlatforms.text = "Plataformas: $platformNames"
                    textPlatforms.visibility = android.view.View.VISIBLE
                } ?: run {
                    textPlatforms.visibility = android.view.View.GONE
                }

                root.setOnClickListener {
                    onItemClick(game)
                }
            }
        }
    }

    companion object DiffCallback : DiffUtil.ItemCallback<Game>() {
        override fun areItemsTheSame(oldItem: Game, newItem: Game): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Game, newItem: Game): Boolean {
            return oldItem == newItem
        }
    }
}