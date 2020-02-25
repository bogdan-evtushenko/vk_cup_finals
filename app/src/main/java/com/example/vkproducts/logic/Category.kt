package com.example.vkproducts.logic

import org.json.JSONObject
import java.io.Serializable

data class Category(
    val id: Int,
    val name: String
) : Serializable {
    companion object {
        fun parse(document: JSONObject): Category {
            return Category(
                document.getInt("id"),
                document.getString("name")
            )
        }
    }
}