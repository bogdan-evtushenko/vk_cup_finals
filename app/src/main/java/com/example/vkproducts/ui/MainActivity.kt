package com.example.vkproducts.ui

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.vkproducts.R
import com.example.vkproducts.logic.*
import com.example.vkproducts.ui.cities.CitiesBottomSheetDialog
import com.example.vkproducts.ui.markets.MarketActivity
import com.example.vkproducts.ui.markets.MarketsAdapter
import com.vk.api.sdk.VK
import com.vk.api.sdk.VKApiCallback
import com.vk.api.sdk.VKApiConfig
import com.vk.api.sdk.VKDefaultValidationHandler
import com.vk.api.sdk.auth.VKAccessToken
import com.vk.api.sdk.auth.VKAuthCallback
import com.vk.api.sdk.auth.VKScope
import com.vk.api.sdk.exceptions.VKApiExecutionException
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    private val progressBarDialog by lazy { ProgressBarDialog(this) }
    private val citiesListItems = mutableListOf<CitiesListItem>()
    private lateinit var currentSelectingItem: CitiesListItem
    private val markets: MutableList<Group> = mutableListOf()
    private var cityId: Int? = null

    private val marketsAdapter by lazy {
        MarketsAdapter(
            this,
            ::adjustClickingOnGroup
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        if (!VK.isLoggedIn()) {
            VK.login(this, arrayListOf(VKScope.WALL, VKScope.PHOTOS, VKScope.MARKET, VKScope.GROUPS))
        } else {
            fetchCountries()
        }

        VK.setConfig(
            VKApiConfig(
                context = this,
                appId = resources.getInteger(R.integer.com_vk_sdk_AppId),
                validationHandler = VKDefaultValidationHandler(this),
                version = "5.103"
            )
        )

        initViews()
    }

    private fun requestPermission() {
        val readExternalStoragePermission: Int =
            ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)

        val writeExternalStoragePermission: Int =
            ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)

        if (readExternalStoragePermission != PackageManager.PERMISSION_GRANTED || writeExternalStoragePermission != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE),
                REQUEST_PERMISSION
            )
        }
    }

    private fun initViews() {
        titleContainer.setOnClickListener {
            showBottomSheetDialog()
        }
        recyclerView.run {
            layoutManager = LinearLayoutManager(this@MainActivity, LinearLayoutManager.VERTICAL, false)
            adapter = marketsAdapter
        }
        ivSearch.setOnClickListener {
            cityId?.let {
                uploadMarketOneByOne(cityId!!, etComment.text.toString())
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        val callback = object : VKAuthCallback {
            override fun onLogin(token: VKAccessToken) {
                user = token
                fetchCountries()
                requestPermission()
            }

            override fun onLoginFailed(errorCode: Int) {
                Toast.makeText(
                    this@MainActivity,
                    getString(R.string.restart_app_and_login),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
        if (data == null || !VK.onActivityResult(requestCode, resultCode, data, callback)) {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_PERMISSION) {
            val grantResultsLength = grantResults.size
            if (!(grantResultsLength > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                Toast.makeText(
                    applicationContext,
                    "For posting on wall you need give us read external storage permission.",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    private fun fetchCountries() {
        progressBarDialog.show()
        VK.execute(VKRequests.FetchCountries(), object : VKApiCallback<List<Country>> {
            override fun success(result: List<Country>) {
                fetchCities(result)
            }

            override fun fail(error: Exception) {
                Toast.makeText(
                    this@MainActivity,
                    getString(R.string.something_went_wrong),
                    Toast.LENGTH_SHORT
                ).show()
                progressBarDialog.dismiss()
            }
        })
    }

    private fun fetchCities(countries: List<Country>, indexCountry: Int = 0) {
        if (indexCountry >= countries.size) {
            showCities()
            return
        } else {
            val country = countries[indexCountry]

            VK.execute(VKRequests.FetchCities(country.id), object : VKApiCallback<List<City>> {
                override fun success(result: List<City>) {
                    citiesListItems.add(CitiesListItem(country.id, country.title, true))
                    result.forEach { citiesListItems.add(CitiesListItem(it.id, it.title, false)) }
                    fetchCities(countries, indexCountry + 1)
                }

                override fun fail(error: Exception) {
                    println("here error in cities : ${error.message}")
                    fetchCities(countries, indexCountry + 1)
                }
            })
        }
    }

    private fun showCities() {
        progressBarDialog.dismiss()
        println("Here size ${citiesListItems.size}")
        currentSelectingItem = citiesListItems[1]
        updatePageTitle()
        fetchMarkets(currentSelectingItem)
    }

    private fun showBottomSheetDialog() {
        val bottomSheet = CitiesBottomSheetDialog(
            ::fetchMarkets,
            citiesListItems,
            currentSelectingItem
        )
        bottomSheet.show(supportFragmentManager, "shareBottomSheet")
    }

    private fun fetchMarkets(cityItem: CitiesListItem) {
        currentSelectingItem = cityItem
        updatePageTitle()
        markets.clear()
        uploadMarkets(cityItem.id)
    }

    private fun uploadMarkets(cityId: Int) {
        println("UploadMarkets by cityId : $cityId")
        uploadMarketOneByOne(cityId)
        this.cityId = cityId
    }

    private fun uploadMarketOneByOne(cityId: Int, query: String = "a") {
        progressBarDialog.show()
        markets.clear()
        VK.execute(VKRequests.SearchGroupsByCity(cityId, query), object : VKApiCallback<List<Group>> {
            override fun success(result: List<Group>) {
                println("Success : ${result}")
                markets.addAll(result)
                showMarkets()
            }

            override fun fail(error: Exception) {
                println("here error in groups : ${error.message}")
            }
        })
    }

    private fun showMarkets() {
        println("Here show Markets : $markets")
        progressBarDialog.dismiss()
        marketsAdapter.setItems(markets)
    }

    private fun adjustClickingOnGroup(group: Group) {
        startActivity(MarketActivity.newIntent(this, group.id, group.name))
    }

    private fun updatePageTitle() {
        pageTitle.text = getString(R.string.markets_in, currentSelectingItem.title)
    }

    companion object {
        const val REQUEST_PERMISSION = 2
    }
}