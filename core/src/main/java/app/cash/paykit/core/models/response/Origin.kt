package app.cash.paykit.core.models.response

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Origin(
  @Json(name = "id")
  val id: String?,

  @Json(name = "type")
  val type: String,
)
