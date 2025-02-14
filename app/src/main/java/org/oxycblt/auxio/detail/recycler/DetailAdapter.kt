/*
 * Copyright (c) 2022 Auxio Project
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
 
package org.oxycblt.auxio.detail.recycler

import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.TooltipCompat
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import org.oxycblt.auxio.IntegerTable
import org.oxycblt.auxio.databinding.ItemSortHeaderBinding
import org.oxycblt.auxio.detail.SortHeader
import org.oxycblt.auxio.list.Header
import org.oxycblt.auxio.list.Item
import org.oxycblt.auxio.list.SelectableListListener
import org.oxycblt.auxio.list.recycler.*
import org.oxycblt.auxio.music.Music
import org.oxycblt.auxio.util.context
import org.oxycblt.auxio.util.inflater

/**
 * A [RecyclerView.Adapter] that implements behavior shared across each detail view's adapters.
 * @param listener A [Listener] to bind interactions to.
 * @param itemCallback A [DiffUtil.ItemCallback] to use with [AsyncListDiffer] when updating the
 * internal list.
 * @author Alexander Capehart (OxygenCobalt)
 */
abstract class DetailAdapter(
    private val listener: Listener<*>,
    itemCallback: DiffUtil.ItemCallback<Item>
) : SelectionIndicatorAdapter<RecyclerView.ViewHolder>(), AuxioRecyclerView.SpanSizeLookup {
    // Safe to leak this since the listener will not fire during initialization
    @Suppress("LeakingThis") protected val differ = AsyncListDiffer(this, itemCallback)

    override fun getItemViewType(position: Int) =
        when (differ.currentList[position]) {
            // Implement support for headers and sort headers
            is Header -> HeaderViewHolder.VIEW_TYPE
            is SortHeader -> SortHeaderViewHolder.VIEW_TYPE
            else -> super.getItemViewType(position)
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        when (viewType) {
            HeaderViewHolder.VIEW_TYPE -> HeaderViewHolder.from(parent)
            SortHeaderViewHolder.VIEW_TYPE -> SortHeaderViewHolder.from(parent)
            else -> error("Invalid item type $viewType")
        }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = differ.currentList[position]) {
            is Header -> (holder as HeaderViewHolder).bind(item)
            is SortHeader -> (holder as SortHeaderViewHolder).bind(item, listener)
        }
    }

    override fun isItemFullWidth(position: Int): Boolean {
        // Headers should be full-width in all configurations.
        val item = differ.currentList[position]
        return item is Header || item is SortHeader
    }

    override val currentList: List<Item>
        get() = differ.currentList

    /**
     * Asynchronously update the list with new items. Assumes that the list only contains data
     * supported by the concrete [DetailAdapter] implementation.
     * @param newList The new [Item]s for the adapter to display.
     */
    fun submitList(newList: List<Item>) {
        differ.submitList(newList)
    }

    /** An extended [SelectableListListener] for [DetailAdapter] implementations. */
    interface Listener<in T : Music> : SelectableListListener<T> {
        // TODO: Split off into sub-listeners if a collapsing toolbar is implemented.
        /**
         * Called when the play button in a detail header is pressed, requesting that the current
         * item should be played.
         */
        fun onPlay()

        /**
         * Called when the shuffle button in a detail header is pressed, requesting that the current
         * item should be shuffled
         */
        fun onShuffle()

        /**
         * Called when the button in a [SortHeader] item is pressed, requesting that the sort menu
         * should be opened.
         */
        fun onOpenSortMenu(anchor: View)
    }

    protected companion object {
        /** A comparator that can be used with DiffUtil. */
        val DIFF_CALLBACK =
            object : SimpleItemCallback<Item>() {
                override fun areContentsTheSame(oldItem: Item, newItem: Item): Boolean {
                    return when {
                        oldItem is Header && newItem is Header ->
                            HeaderViewHolder.DIFF_CALLBACK.areContentsTheSame(oldItem, newItem)
                        oldItem is SortHeader && newItem is SortHeader ->
                            SortHeaderViewHolder.DIFF_CALLBACK.areContentsTheSame(oldItem, newItem)
                        else -> false
                    }
                }
            }
    }
}

/**
 * A [RecyclerView.ViewHolder] that displays a [SortHeader], a variation on [Header] that adds a
 * button opening a menu for sorting. Use [from] to create an instance.
 * @author Alexander Capehart (OxygenCobalt)
 */
private class SortHeaderViewHolder(private val binding: ItemSortHeaderBinding) :
    RecyclerView.ViewHolder(binding.root) {
    /**
     * Bind new data to this instance.
     * @param sortHeader The new [SortHeader] to bind.
     * @param listener An [DetailAdapter.Listener] to bind interactions to.
     */
    fun bind(sortHeader: SortHeader, listener: DetailAdapter.Listener<*>) {
        binding.headerTitle.text = binding.context.getString(sortHeader.titleRes)
        binding.headerButton.apply {
            // Add a Tooltip based on the content description so that the purpose of this
            // button can be clear.
            TooltipCompat.setTooltipText(this, contentDescription)
            setOnClickListener(listener::onOpenSortMenu)
        }
    }

    companion object {
        /** A unique ID for this [RecyclerView.ViewHolder] type. */
        const val VIEW_TYPE = IntegerTable.VIEW_TYPE_SORT_HEADER

        /**
         * Create a new instance.
         * @param parent The parent to inflate this instance from.
         * @return A new instance.
         */
        fun from(parent: View) =
            SortHeaderViewHolder(ItemSortHeaderBinding.inflate(parent.context.inflater))

        /** A comparator that can be used with DiffUtil. */
        val DIFF_CALLBACK =
            object : SimpleItemCallback<SortHeader>() {
                override fun areContentsTheSame(oldItem: SortHeader, newItem: SortHeader) =
                    oldItem.titleRes == newItem.titleRes
            }
    }
}
