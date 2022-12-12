package com.squareup.cash.paykit.models.response

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class CustomerTopLevelResponse(
  @Json(name = "request")
  val customerResponseData: CustomerResponseData
)