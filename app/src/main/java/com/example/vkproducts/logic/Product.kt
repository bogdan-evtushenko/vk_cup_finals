package com.example.vkproducts.logic

import org.json.JSONArray
import org.json.JSONObject
import java.io.Serializable

data class Product(
    val id: Int,
    val title: String,
    val description: String,
    val amount: Int,
    val currency: String,
    val isFavorite: Boolean,
    val photoUrl: String
) : Serializable {
    companion object {
        fun parse(product: JSONObject): Product {
            return Product(
                product.getInt("id"),
                product.getString("title"),
                product.getString("description"),
                product.getJSONObject("price").getInt("amount"),
                product.getJSONObject("price").getJSONObject("currency").getString("name"),
                if (!product.isNull("is_favorite")) product.getBoolean("is_favorite") else false,
                parsePhotoURL(product.getJSONArray("photos").getJSONObject(0).getJSONArray("sizes"))
            )
        }

        private fun parsePhotoURL(sizes: JSONArray): String {
            for (i in 0 until sizes.length()) {
                val size = sizes.getJSONObject(i)
                if (size.getString("type") == "x") {
                    return size.getString("url")
                }
            }
            return sizes.getJSONObject(0).getString("url")
        }
    }
}