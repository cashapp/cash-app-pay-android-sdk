package com.squareup.cash.paykit.models.response


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class RequesterProfile(
    @Json(name = "logo_url")
    val logoUrl: String,
    @Json(name = "name")
    val name: String
)