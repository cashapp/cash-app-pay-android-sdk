package com.squareup.cash.paykit.models.response

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ApiError(
  @Json(name = "category")
  val category: String,
  @Json(name = "code")
  val code: String,
  @Json(name = "detail")
  val detail: String?,
  @Json(name = "field")
  val field_value: String?
)