package com.example.vkproducts.ui.products

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.KeyEvent
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.example.vkproducts.R
import com.example.vkproducts.logic.*
import com.vk.api.sdk.VK
import com.vk.api.sdk.VKApiCallback
import com.vk.api.sdk.exceptions.VKApiExecutionException
import kotlinx.android.synthetic.main.activity_product.*

class ProductActivity : AppCompatActivity() {

    private val product: Product by lazy { intent?.extras?.product ?: throw IllegalStateException() }

    private val marketId: Int
        get() = intent?.extras?.marketId ?: throw  IllegalStateException()

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
            updateButtonVision(product.isFavorite)

            btnAddToFavorite.setOnClickListener {
                if (product.isFavorite) {
                    removeProductFromFavorite(product.id)
                } else {
                    addProductToFavorite(product.id)
                }
                isFavorite = !isFavorite
                updateButtonVision(isFavorite)
            }
        }
    }

    private fun addProductToFavorite(productId: Int) {
        VK.execute(VKRequests.AddProductToFavorite(productId, marketId), object : VKApiCallback<Boolean> {
            override fun success(result: Boolean) {
                println("Success in adding : $result")
            }

            override fun fail(error: VKApiExecutionException) {
                println("Here failure in adding to favorite : $error")
            }
        })
    }

    private fun removeProductFromFavorite(productId: Int) {
        VK.execute(VKRequests.RemoveProductFromFavorite(productId, marketId), object : VKApiCallback<Boolean> {
            override fun success(result: Boolean) {
                println("Success in removing : $result")
            }

            override fun fail(error: VKApiExecutionException) {
                println("Here failure in removing to favorite : $error")
            }
        })
    }

    private fun updateButtonVision(isFavorite: Boolean) {
        when (isFavorite) {
            true -> {
                btnAddToFavorite.background = getDrawable(R.drawable.button_gray_ripple_rounded)
                btnAddToFavorite.setTextColor(ContextCompat.getColor(this, R.color.blue))
                btnAddToFavorite.text = getString(R.string.remove_from_favorites)
            }
            false -> {
                btnAddToFavorite.background = getDrawable(R.drawable.button_blue_ripple_rounded)
                btnAddToFavorite.setTextColor(ContextCompat.getColor(this, R.color.white))
                btnAddToFavorite.text = getString(R.string.add_to_favorites)
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        setResultOK()
        onBackPressed()
        return true
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            setResultOK()
            finish()
        }
        return true
    }

    private fun setResultOK() {
        val resultIntent = Intent()
        resultIntent.putExtra("product", product)
        setResult(Activity.RESULT_OK, resultIntent)
    }

    companion object {
        fun newIntent(context: Context, product: Product, marketId: Int) =
            Intent(context, ProductActivity::class.java).apply {
                putExtras(Bundle().apply {
                    this.product = product
                    this.marketId = marketId
                })
            }
    }
}
