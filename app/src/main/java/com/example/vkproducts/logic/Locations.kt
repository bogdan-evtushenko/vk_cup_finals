package com.example.vkproducts.logic

import org.json.JSONObject
import java.io.Serializable

data class Country(
    val id: Int,
    val title: String
) : Serializable {
    companion object {
        fun parse(country: JSONObject): Country {
            return Country(
                country.getInt("id"),
                country.getString("title")
            )
        }
    }
}

data class City(
    val id: Int,
    val title: String
) : Serializable {
    companion object {
        fun parse(city: JSONObject): City {
            return City(
                city.getInt("id"),
                city.getString("title")
            )
        }
    }
}