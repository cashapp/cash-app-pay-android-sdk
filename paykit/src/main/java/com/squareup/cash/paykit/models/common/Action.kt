package com.squareup.cash.paykit.models.common

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Action(
  @Json(name = "amount")
  val amount_cents: Int?,
  @Json(name = "currency")
  val currency: String?,
  @Json(name = "scope_id")
  val scopeId: String,
  @Json(name = "type")
  val type: String
)