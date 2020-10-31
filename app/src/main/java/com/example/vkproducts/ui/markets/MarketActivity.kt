package com.example.vkproducts.ui.markets

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.vkproducts.R
import com.example.vkproducts.logic.*
import com.example.vkproducts.ui.products.ProductActivity
import com.example.vkproducts.ui.products.ProductsAdapter
import com.example.vkproducts.ui.ProgressBarDialog
import com.vk.api.sdk.VK
import com.vk.api.sdk.VKApiCallback
import com.vk.api.sdk.exceptions.VKApiExecutionException
import kotlinx.android.synthetic.main.activity_market.*
import java.lang.Exception

class MarketActivity : AppCompatActivity() {

    private val marketId: Int
        get() = intent.extras?.marketId ?: throw IllegalStateException()

    private val marketTitle: String
        get() = intent.extras?.marketTitle ?: throw IllegalStateException()

    private val progressBarDialog by lazy { ProgressBarDialog(this) }
    private val products: MutableList<Product> = mutableListOf()
    private val productsAdapter by lazy {
        ProductsAdapter(
            this,
            ::adjustClickingOnProduct,
            products
        )
    }

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

    private fun fetchProducts() {
        progressBarDialog.show()
        VK.execute(VKRequests.FetchProducts(marketId), object : VKApiCallback<Pair<Int, List<Product>>> {
            override fun success(result: Pair<Int, List<Product>>) {
                products.addAll(result.second)
                configureLoadMore(result.first)
                showProducts()
            }

            override fun fail(error: Exception) {
                println("error1 : ${error.message}")
                showToast(error.message.orEmpty())
                showProducts()
            }
        })
    }

    private fun configureLoadMore(maxCount: Int) {
        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                if (dy > 0) {
                    val visibleThreshold = 2
                    val gridLayoutManager = recyclerView.layoutManager as GridLayoutManager
                    val lastItem = gridLayoutManager.findLastCompletelyVisibleItemPosition()
                    val currentTotalCount = gridLayoutManager.itemCount
                    if (currentTotalCount <= lastItem + visibleThreshold) {
                        if (currentTotalCount < maxCount) {
                            spinner.visibility = View.VISIBLE
                            fetchProductsWithOffset(currentTotalCount)
                        }
                    }
                }
            }
        })
    }

    private fun fetchProductsWithOffset(offset: Int) {
        VK.execute(VKRequests.FetchProducts(marketId, offset), object : VKApiCallback<Pair<Int, List<Product>>> {
            override fun success(result: Pair<Int, List<Product>>) {
                products.addAll(result.second)
                recyclerView.adapter?.notifyItemRangeInserted(
                    products.size - result.second.size,
                    result.second.size
                )
                spinner.visibility = View.GONE
            }

            override fun fail(error: Exception) {
                println("error in spinner : ${error.message}")
                showToast(error.message.orEmpty())
            }
        })
    }

    private fun showProducts() {
        progressBarDialog.dismiss()
        recyclerView.adapter = productsAdapter
        if (products.isEmpty()) showToast(getString(R.string.nothing_found))
    }

    private fun adjustClickingOnProduct(product: Product) {
        startActivityForResult(
            ProductActivity.newIntent(
                this,
                product,
                marketId
            ), REQUEST_CODE_PRODUCT
        )
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                REQUEST_CODE_PRODUCT -> {
                    val resultProduct = data?.extras?.product ?: throw IllegalStateException()
                    products.find { resultProduct.id == it.id }?.isFavorite = resultProduct.isFavorite
                }
            }
        }
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

        const val REQUEST_CODE_PRODUCT = 2
    }

}
