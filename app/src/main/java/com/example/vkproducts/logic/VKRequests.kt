package com.example.vkproducts.logic

import com.vk.api.sdk.requests.VKRequest
import org.json.JSONObject
import java.lang.IllegalStateException

object VKRequests {

    class FetchCategories :
        VKRequest<List<Product>>("groups.getCatalogInfo") {
        init {
            addParam("access_token", user?.accessToken)
            addParam("v", "5.103")
        }

        override fun parse(r: JSONObject): List<Product> {
            val response = r.getJSONObject("response")
            val categories = response.getJSONArray("categories")
            val result = ArrayList<Product>()

            for (i in 0 until categories.length()) {
                result.add(Product.parse(categories.getJSONObject(i)))
            }

            return result
        }
    }

    class FetchCategory(categoryId: Int) :
        VKRequest<List<Store>>("groups.getCatalog") {
        init {
            addParam("v", "5.103")
            addParam("access_token", user?.accessToken)
            addParam("category_id", categoryId)
        }

        override fun parse(r: JSONObject): List<Store> {
            val response = r.getJSONObject("response")
            val stores = response.getJSONArray("items")
            val result = ArrayList<Store>()

            for (i in 0 until stores.length()) {
                result.add(Store.parse(stores.getJSONObject(i)))
            }

            return result
        }
    }

    class DeleteDocument(documentId: Int) :
        VKRequest<Boolean>("docs.delete") {
        init {
            addParam("doc_id", documentId)
            addParam("v", "5.103")
            addParam("owner_id", user?.userId ?: throw IllegalStateException())
            addParam("access_token", user?.accessToken)
        }

        override fun parse(r: JSONObject): Boolean {
            return (r.getInt("response") == 1)
        }
    }

    class FetchCountries :
        VKRequest<List<Country>>("database.getCountries") {
        init {
            addParam("need_all", 1)
            addParam("count", 1000)
            addParam("access_token", user?.accessToken)
            addParam("v", "5.103")
        }

        override fun parse(r: JSONObject): List<Country> {
            val response = r.getJSONObject("response")
            val countries = response.getJSONArray("items")
            val result = ArrayList<Country>()

            for (i in 0 until countries.length()) {
                result.add(Country.parse(countries.getJSONObject(i)))
            }
            return result.sortedBy { it.id }.also { println(it.size) }.subList(0, 10)
        }
    }

    class FetchCities(countryId: Int) :
        VKRequest<List<City>>("database.getCities") {
        init {
            addParam("need_all", 0)
            addParam("country_id", countryId)
            addParam("count", 1000)
            addParam("access_token", user?.accessToken)
            addParam("v", "5.103")
        }

        override fun parse(r: JSONObject): List<City> {
            val response = r.getJSONObject("response")
            val cities = response.getJSONArray("items")
            val result = ArrayList<City>()

            for (i in 0 until cities.length()) {
                result.add(City.parse(cities.getJSONObject(i)))
            }
            return result
        }
    }

    class SearchGroupsByCity(cityId: Int) :
        VKRequest<List<Group>>("groups.search") {
        init {
            addParam("q", "*")
            addParam("city_id", cityId)
            addParam("sort", 0)
            addParam("count", 1000)
            addParam("market", 1)
            addParam("access_token", user?.accessToken)
            addParam("v", "5.103")
        }

        override fun parse(r: JSONObject): List<Group> {
            val response = r.getJSONObject("response")
            val groups = response.getJSONArray("items")
            val result = ArrayList<Group>()

            for (i in 0 until groups.length()) {
                result.add(Group.parse(groups.getJSONObject(i)))
            }
            return result
        }
    }

    class FetchProducts(marketId: Int) :
        VKRequest<Pair<Int, List<Product>>>("market.get") {
        init {
            addParam("owner_id", "-$marketId")
            addParam("count", 200)
            addParam("extended", 1)
            addParam("access_token", user?.accessToken)
            addParam("v", "5.103")
        }

        override fun parse(r: JSONObject): Pair<Int, List<Product>> {
            val response = r.getJSONObject("response")
            val products = response.getJSONArray("items")
            val result = ArrayList<Product>()

            for (i in 0 until products.length()) {
                result.add(Product.parse(products.getJSONObject(i)))
            }
            return Pair(response.getInt("count"), result)
        }
    }
}