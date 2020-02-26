package com.example.vkproducts.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.GridLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.vkproducts.R
import com.example.vkproducts.logic.Product
import com.example.vkproducts.logic.VKRequests
import com.example.vkproducts.logic.marketId
import com.example.vkproducts.logic.marketTitle
import com.vk.api.sdk.VK
import com.vk.api.sdk.VKApiCallback
import com.vk.api.sdk.exceptions.VKApiExecutionException
import kotlinx.android.synthetic.main.activity_market.*

class MarketActivity : AppCompatActivity() {

    private val marketId: Int
        get() = intent.extras?.marketId ?: throw IllegalStateException()

    private val marketTitle: String
        get() = intent.extras?.marketTitle ?: throw IllegalStateException()

    private val progressBarDialog by lazy { ProgressBarDialog(this) }
    private val products: MutableList<Product> = mutableListOf()
    private val productsAdapter by lazy { ProductsAdapter(this, ::adjustClickingOnProduct, products) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_market)
        setSupportActionBar(toolbar)

        supportActionBar?.run {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
        }

        pageTitle.text = marketTitle
        recyclerView.run {
            layoutManager = GridLayoutManager(this@MarketActivity, 2)
        }

        fetchProducts()
    }

    private fun fetchProducts(offset: Int = 0) {
        progressBarDialog.show()
        println("here owner_id : $marketId")
        VK.execute(VKRequests.FetchProducts(marketId), object : VKApiCallback<Pair<Int, List<Product>>> {
            override fun success(result: Pair<Int, List<Product>>) {
                products.addAll(result.second)
                showProducts()
            }

            override fun fail(error: VKApiExecutionException) {
                println("error1 : ${error.message}")
                showToast(error.message.orEmpty())
                showProducts()
            }
        })
    }

    private fun showProducts() {
        progressBarDialog.dismiss()
        println("Here products : $products")
        recyclerView.adapter = productsAdapter
        if (products.isEmpty()) showToast(getString(R.string.nothing_found))
    }

    private fun adjustClickingOnProduct(product: Product) {
        println("Clicking on product : $product")
        /*when (adapterMode) {
            AdapterMode.STANDARD -> startActivity(PhotoActivity.newIntent(this, photo.photoUrl))
            AdapterMode.REMOVING -> {
                if (photo.isSelected) {
                    removingPhotos.add(photo)
                } else {
                    photo.isSelected = true
                    removingPhotos.remove(photo)
                    photo.isSelected = false
                }
            }
        }*/
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    private fun showToast(message: String) = Toast.makeText(this@MarketActivity, message, Toast.LENGTH_SHORT).show()

    companion object {
        fun newIntent(context: Context, marketId: Int, marketTitle: String) =
            Intent(context, MarketActivity::class.java).apply {
                putExtras(Bundle().apply {
                    this.marketId = marketId
                    this.marketTitle = marketTitle
                })
            }

        const val REQUEST_PICK_IMAGE = 1
        const val REQUEST_PERMISSION = 2
    }
}
