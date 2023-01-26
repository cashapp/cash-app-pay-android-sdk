package app.cash.paykit.core.models.analytics

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class EventStream2Response(
  @Json(name = "failure_count")
  val failureCount: Int,
  @Json(name = "invalid_count")
  val invalidCount: Int,
  @Json(name = "success_count")
  val successCount: Int,
)
