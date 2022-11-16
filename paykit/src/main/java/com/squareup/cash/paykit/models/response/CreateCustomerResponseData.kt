package com.squareup.cash.paykit.models.response

import com.squareup.cash.paykit.models.common.Action
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class CreateCustomerResponseData(
  @Json(name = "actions")
  val actions: List<Action>,
  @Json(name = "auth_flow_triggers")
  val authFlowTriggers: AuthFlowTriggers?,
  @Json(name = "channel")
  val channel: String,
  @Json(name = "created_at")
  val createdAt: String,
  @Json(name = "expires_at")
  val expiresAt: String,
  @Json(name = "id")
  val id: String,
  @Json(name = "origin")
  val origin: Origin,
  @Json(name = "requester_profile")
  val requesterProfile: RequesterProfile,
  @Json(name = "status")
  val status: String,
  @Json(name = "updated_at")
  val updatedAt: String
)