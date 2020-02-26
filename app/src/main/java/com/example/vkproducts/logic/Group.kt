package com.example.vkproducts.logic

import org.json.JSONObject
import java.io.Serializable

data class Group(
    val id: Int,
    val name: String,
    val isClosed: Int,
    val photoUrl: String
) : Serializable {
    companion object {
        fun parse(group: JSONObject): Group {
            return Group(
                group.getInt("id"),
                group.getString("name"),
                group.getInt("is_closed"),
                group.getString("photo_50")
            )
        }
    }
}