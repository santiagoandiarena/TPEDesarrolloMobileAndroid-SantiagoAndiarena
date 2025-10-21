package ar.edu.unicen.tp_andiarena.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import ar.edu.unicen.tp_andiarena.data.model.Platform
import ar.edu.unicen.tp_andiarena.databinding.ItemFilterBinding

class PlatformsAdapter(
    private val onItemClick: (Platform, Boolean) -> Unit
) : ListAdapter<Platform, PlatformsAdapter.PlatformViewHolder>(DiffCallback) {

    private val selectedItems = mutableSetOf<Int>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlatformViewHolder {
        val binding = ItemFilterBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return PlatformViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PlatformViewHolder, position: Int) {
        val platform = getItem(position)
        val isSelected = selectedItems.contains(platform.id)
        holder.bind(platform, isSelected)
    }

    fun getSelectedItems(): List<Int> {
        return selectedItems.toList()
    }

    fun clearSelection() {
        selectedItems.clear()
        notifyDataSetChanged()
    }

    inner class PlatformViewHolder(
        private val binding: ItemFilterBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(platform: Platform, isSelected: Boolean) {
            binding.apply {
                textFilterName.text = platform.name

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
                    val platformId = platform.id
                    if (selectedItems.contains(platformId)) {
                        selectedItems.remove(platformId)
                    } else {
                        selectedItems.add(platformId)
                    }
                    notifyItemChanged(adapterPosition)
                    onItemClick(platform, selectedItems.contains(platformId))
                }
            }
        }
    }

    companion object DiffCallback : DiffUtil.ItemCallback<Platform>() {
        override fun areItemsTheSame(oldItem: Platform, newItem: Platform): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Platform, newItem: Platform): Boolean {
            return oldItem == newItem
        }
    }
}