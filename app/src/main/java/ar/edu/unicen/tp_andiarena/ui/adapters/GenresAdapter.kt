package ar.edu.unicen.tp_andiarena.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import ar.edu.unicen.tp_andiarena.data.model.Genre
import ar.edu.unicen.tp_andiarena.databinding.ItemFilterBinding

class GenresAdapter(
    private val onItemClick: (Genre, Boolean) -> Unit
) : ListAdapter<Genre, GenresAdapter.GenreViewHolder>(DiffCallback) {

    private val selectedItems = mutableSetOf<Int>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GenreViewHolder {
        val binding = ItemFilterBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return GenreViewHolder(binding)
    }

    override fun onBindViewHolder(holder: GenreViewHolder, position: Int) {
        val genre = getItem(position)
        val isSelected = selectedItems.contains(genre.id) // Genre tiene id directo
        holder.bind(genre, isSelected)
    }

    fun getSelectedItems(): List<Int> {
        return selectedItems.toList()
    }

    fun clearSelection() {
        selectedItems.clear()
        notifyDataSetChanged()
    }

    inner class GenreViewHolder(
        private val binding: ItemFilterBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(genre: Genre, isSelected: Boolean) {
            binding.apply {
                textFilterName.text = genre.name

                // Cambiar apariencia según selección
                if (isSelected) {
                    root.setBackgroundColor(ContextCompat.getColor(root.context, android.R.color.holo_blue_light))
                    textFilterName.setTextColor(ContextCompat.getColor(root.context, android.R.color.white))
                    checkFilter.isChecked = true
                } else {
                    root.setBackgroundColor(ContextCompat.getColor(root.context, android.R.color.transparent))
                    textFilterName.setTextColor(ContextCompat.getColor(root.context, android.R.color.black))
                    checkFilter.isChecked = false
                }

                root.setOnClickListener {
                    val genreId = genre.id
                    if (selectedItems.contains(genreId)) {
                        selectedItems.remove(genreId)
                    } else {
                        selectedItems.add(genreId)
                    }
                    notifyItemChanged(adapterPosition)
                    onItemClick(genre, selectedItems.contains(genreId))
                }
            }
        }
    }

    companion object DiffCallback : DiffUtil.ItemCallback<Genre>() {
        override fun areItemsTheSame(oldItem: Genre, newItem: Genre): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Genre, newItem: Genre): Boolean {
            return oldItem == newItem
        }
    }
}