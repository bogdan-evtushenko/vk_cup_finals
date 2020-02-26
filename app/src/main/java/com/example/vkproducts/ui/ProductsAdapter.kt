package com.example.vkproducts.ui

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.vkproducts.R
import com.example.vkproducts.logic.Product
import kotlinx.android.synthetic.main.view_product_item.view.*
import java.text.DecimalFormat

class ProductsAdapter(
    private val context: Context,
    private val itemClickListener: (Product) -> Unit,
    private val items: List<Product>
) : RecyclerView.Adapter<ProductsAdapter.ViewHolder>() {

    override fun getItemCount() = items.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layout =
            LayoutInflater.from(context).inflate(R.layout.view_product_item, parent, false)
        return ViewHolder(layout)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val product = items[position]
        with(holder) {
            Glide.with(itemView)
                .load(product.photoUrl)
                .into(imageView)

            tvTitle.text = product.title

            tvSubTitle.text = product.costInString
            onClickListener = { itemClickListener(product) }
        }
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imageView: ImageView = view.imageView
        val tvTitle: TextView = view.tvTitle
        val tvSubTitle: TextView = view.tvSubTitle

        var onClickListener: (() -> Unit)? = null

        init {
            view.setOnClickListener { onClickListener?.invoke() }
        }
    }
}
