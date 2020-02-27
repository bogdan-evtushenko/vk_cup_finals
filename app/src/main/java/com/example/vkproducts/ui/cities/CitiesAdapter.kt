package com.example.vkproducts.ui.cities

import android.content.Context
import android.graphics.Typeface
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.vkproducts.R
import com.example.vkproducts.logic.CitiesListItem
import kotlinx.android.synthetic.main.view_city_item.view.*

class CitiesAdapter(
    private val context: Context,
    private val itemClickListener: (CitiesListItem) -> Unit,
    private val items: List<CitiesListItem>,
    private var currentSelectingItemPosition: Int
) : RecyclerView.Adapter<CitiesAdapter.ViewHolder>() {
    override fun getItemCount() = items.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layout =
            LayoutInflater.from(context).inflate(R.layout.view_city_item, parent, false)
        return ViewHolder(layout)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val cityItem = items[position]
        with(holder) {
            itemView.isClickable = !cityItem.isCountry

            tvTitle.text = cityItem.title
            if (cityItem.isCountry) tvTitle.setTypeface(null, Typeface.BOLD)
            else tvTitle.setTypeface(null, Typeface.NORMAL)

            ivBadge.visibility = if (currentSelectingItemPosition == adapterPosition) View.VISIBLE else View.GONE
            onClickListener = {
                itemClickListener(cityItem)
                val prevPosition = currentSelectingItemPosition
                currentSelectingItemPosition = adapterPosition

                notifyItemChanged(prevPosition)
                notifyItemChanged(currentSelectingItemPosition)
            }
        }
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val ivBadge: ImageView = view.ivBadge
        val tvTitle: TextView = view.tvTitle

        var onClickListener: (() -> Unit)? = null

        init {
            view.setOnClickListener { onClickListener?.invoke() }
        }
    }
}
