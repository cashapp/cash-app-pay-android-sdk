import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class AnalyticsEvent(
  @Json(name = "app_name")
  val appName: String,
  @Json(name = "catalog_name")
  val catalogName: String,
  @Json(name = "json_data")
  val jsonData: String,
  @Json(name = "recorded_at_usec")
  val recordedAt: Long,
  @Json(name = "uuid")
  val uuid: String,
)
