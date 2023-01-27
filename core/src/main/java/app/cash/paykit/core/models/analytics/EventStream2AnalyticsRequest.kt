import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class EventStream2AnalyticsRequest(
  @Json(name = "events")
  val events: List<String>,
)
