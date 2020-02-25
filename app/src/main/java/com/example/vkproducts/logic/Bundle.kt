package com.example.vkproducts.logic

import android.os.Bundle

var Bundle.documentId: Int
    get() = getInt("document_id")
    set(value) = putInt("document_id", value)

var Bundle.documentTitle: String?
    get() = getString("document_title")
    set(value) = putString("document_title", value)
