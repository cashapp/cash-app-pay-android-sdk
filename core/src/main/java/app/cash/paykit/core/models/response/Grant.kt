package app.cash.paykit.core.models.response

import app.cash.paykit.core.models.common.Action
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Grant(
  @Json(name = "id")
  val id: String,
  @Json(name = "status")
  val status: String,
  @Json(name = "type")
  val type: String,
  @Json(name = "action")
  val action: Action,
  @Json(name = "channel")
  val channel: String,
  @Json(name = "customer_id")
  val customerId: String,
  @Json(name = "updated_at")
  val updatedAt: String,
  @Json(name = "created_at")
  val createdAt: String,
  @Json(name = "expires_at")
  val expiresAt: String,
)
