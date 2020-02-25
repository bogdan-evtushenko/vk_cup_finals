package com.example.vkproducts.logic

import org.json.JSONObject
import java.io.Serializable

data class Store(
    val id: Int,
    val name: String,
    val photo: String
) : Serializable {
    companion object {
        fun parse(store: JSONObject): Store {
            return Store(
                store.getInt("id"),
                store.getString("name"),
                store.getString("photo_50")
            )
        }
    }
}