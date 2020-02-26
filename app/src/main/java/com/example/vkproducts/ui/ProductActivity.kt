package com.example.vkproducts.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.vkproducts.R
import com.example.vkproducts.logic.Product
import com.example.vkproducts.logic.product
import kotlinx.android.synthetic.main.activity_product.*

class ProductActivity : AppCompatActivity() {

    private val product: Product
        get() = intent?.extras?.product ?: throw IllegalStateException()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_product)
        setSupportActionBar(toolbar)
        supportActionBar?.run {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
        }

        initViews()
    }

    private fun initViews() {
        with(product) {
            Glide.with(ivPhoto)
                .load(photoUrl)
                .into(ivPhoto)
            pageTitle.text = title
            tvTitle.text = title
            tvSubTitle.text = costInString
            tvDescription.text = description
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    companion object {
        fun newIntent(context: Context, product: Product) =
            Intent(context, ProductActivity::class.java).apply {
                putExtras(Bundle().apply {
                    this.product = product
                })
            }
    }
}
