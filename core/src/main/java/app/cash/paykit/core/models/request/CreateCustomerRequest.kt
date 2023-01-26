package app.cash.paykit.core.models.request

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class CreateCustomerRequest(
  @Json(name = "idempotency_key_")
  val idempotencyKey: String? = null,
  @Json(name = "request")
  val customerRequestData: CustomerRequestData,
)
