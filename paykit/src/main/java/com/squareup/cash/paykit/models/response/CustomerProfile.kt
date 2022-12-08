package com.squareup.cash.paykit.models.response

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class CustomerProfile(
  @Json(name = "id")
  val id: String,
  @Json(name = "cashtag")
  val cashTag: String
)