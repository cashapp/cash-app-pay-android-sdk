package com.squareup.cash.paykit.models.request


import com.squareup.cash.paykit.models.common.Action
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class CustomerRequestData(
    @Json(name = "actions")
    val actions: List<Action>,
    @Json(name = "channel")
    val channel: String
)