package com.example.vkproducts.ui.markets

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.vkproducts.R
import com.example.vkproducts.logic.Group
import kotlinx.android.synthetic.main.view_group_item.view.*

class MarketsAdapter(
    private val context: Context,
    private val itemClickListener: (Group) -> Unit
) : RecyclerView.Adapter<MarketsAdapter.ViewHolder>() {

    private var items: List<Group> = listOf()

    override fun getItemCount() = items.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layout =
            LayoutInflater.from(context).inflate(R.layout.view_group_item, parent, false)
        return ViewHolder(layout)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val group = items[position]
        with(holder) {
            Glide.with(itemView)
                .load(group.photoUrl)
                .apply(RequestOptions().centerCrop())
                .into(imageView)

            tvTitle.text = group.name
            tvSubTitle.text = when (group.isClosed) {
                0 -> "Open group"
                1 -> "Close group"
                2 -> "Private group"
                else -> "Open group"
            }

            onClickListener = { itemClickListener(group) }
        }
    }

    fun setItems(groupsList: List<Group>) {
        items = groupsList
        notifyDataSetChanged()
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imageView: ImageView = view.ivProfile
        val tvTitle: TextView = view.tvTitle
        val tvSubTitle: TextView = view.tvSubTitle

        var onClickListener: (() -> Unit)? = null

        init {
            view.setOnClickListener { onClickListener?.invoke() }
        }
    }
}
