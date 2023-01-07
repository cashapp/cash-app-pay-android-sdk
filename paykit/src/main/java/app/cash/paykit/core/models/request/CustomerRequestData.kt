package app.cash.paykit.core.models.request

import app.cash.paykit.core.models.common.Action
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class CustomerRequestData(
  @Json(name = "actions")
  val actions: List<Action>,
  @Json(name = "channel")
  val channel: String?,
  @Json(name = "redirect_url")
  val redirectUri: String?,
)
