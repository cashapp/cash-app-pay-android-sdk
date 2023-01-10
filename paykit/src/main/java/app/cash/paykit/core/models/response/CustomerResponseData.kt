package app.cash.paykit.core.models.response

import app.cash.paykit.core.models.common.Action
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

const val STATUS_PENDING = "PENDING"
const val STATUS_APPROVED = "APPROVED"

@JsonClass(generateAdapter = true)
data class CustomerResponseData(
  @Json(name = "actions")
  val actions: List<Action>,
  @Json(name = "auth_flow_triggers")
  val authFlowTriggers: AuthFlowTriggers?,
  @Json(name = "channel")
  val channel: String,
  @Json(name = "id")
  val id: String,
  @Json(name = "origin")
  val origin: Origin,
  @Json(name = "requester_profile")
  val requesterProfile: RequesterProfile?,
  @Json(name = "status")
  val status: String,
  @Json(name = "updated_at")
  val updatedAt: String,
  @Json(name = "created_at")
  val createdAt: String,
  @Json(name = "expires_at")
  val expiresAt: String,
  @Json(name = "customer_profile")
  val customerProfile: CustomerProfile?,
  @Json(name = "grants")
  val grants: List<Grant>?,
)
