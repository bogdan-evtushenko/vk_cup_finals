package com.example.vkproducts.ui

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.vkproducts.R
import com.example.vkproducts.logic.*
import com.vk.api.sdk.VK
import com.vk.api.sdk.VKApiCallback
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

    private val marketsAdapter by lazy { MarketsAdapter(this, ::adjustClickingOnGroup) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        if (!VK.isLoggedIn()) {
            VK.login(this, arrayListOf(VKScope.WALL, VKScope.PHOTOS, VKScope.MARKET))
        } else {
            fetchCountries()
        }

        initViews()
    }

    private fun initViews() {
        titleContainer.setOnClickListener { showBottomSheetDialog() }
        recyclerView.run {
            layoutManager = LinearLayoutManager(this@MainActivity, LinearLayoutManager.VERTICAL, false)
            adapter = marketsAdapter
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        val callback = object : VKAuthCallback {
            override fun onLogin(token: VKAccessToken) {
                user = token
                fetchCountries()
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

    override fun onStart() {
        super.onStart()
        if (user != null) {
            fetchCountries()
        }
    }

    private fun fetchCountries() {
        progressBarDialog.show()
        VK.execute(VKRequests.FetchCountries(), object : VKApiCallback<List<Country>> {
            override fun success(result: List<Country>) {
                println("here $result")
                fetchCities(result)
            }

            override fun fail(error: VKApiExecutionException) {
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

                override fun fail(error: VKApiExecutionException) {
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
        showBottomSheetDialog()
    }

    private fun showBottomSheetDialog() {
        val bottomSheet = CitiesBottomSheetDialog(::fetchMarkets, citiesListItems, currentSelectingItem)
        bottomSheet.show(supportFragmentManager, "shareBottomSheet")
    }

    private fun fetchMarkets(cityItem: CitiesListItem) {
        currentSelectingItem = cityItem
        // println("Here cityItem : $cityItem")
        updatePageTitle()
        markets.clear()
        uploadMarkets(cityItem.id)
    }

    private fun uploadMarkets(cityId: Int) {
        progressBarDialog.show()
        uploadMarketOneByOne(cityId)
    }

    private fun uploadMarketOneByOne(cityId: Int) {
        println("Upload market one by one")
        VK.execute(VKRequests.SearchGroupsByCity(cityId), object : VKApiCallback<List<Group>> {
            override fun success(result: List<Group>) {
                //println("Here result groups : $result")
                markets.addAll(result)
                showMarkets()
            }

            override fun fail(error: VKApiExecutionException) {
                println("here error in groups : ${error.message}")
            }
        })
    }

    private fun showMarkets() {
        progressBarDialog.dismiss()
        println("Markets : here : ${markets.size}")
        marketsAdapter.setItems(markets)
    }

    private fun adjustClickingOnGroup(group: Group) {
        startActivity(MarketActivity.newIntent(this, group.id, group.name))
    }

    private fun updatePageTitle() {
        pageTitle.text = getString(R.string.markets_in, currentSelectingItem.title)
    }
}