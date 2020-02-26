package com.example.vkproducts.logic

import android.os.Bundle

var Bundle.marketId: Int
    get() = getInt("market_id")
    set(value) = putInt("market_id", value)

var Bundle.marketTitle: String?
    get() = getString("market_title")
    set(value) = putString("market_title", value)
