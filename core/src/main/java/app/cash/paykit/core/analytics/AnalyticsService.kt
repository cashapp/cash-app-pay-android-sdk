package app.cash.paykit.core.analytics

import AnalyticsEvent
import app.cash.paykit.core.NetworkManager
import app.cash.paykit.core.models.analytics.payloads.AnalyticsInitializationPayload
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.adapter
import java.util.UUID

internal class AnalyticsService(
  private val sdkVersion: String,
  private val cliendId: String,
  private val userAgent: String,
  private val networkManager: NetworkManager,
) {

  @OptIn(ExperimentalStdlibApi::class)
  fun sendSdkInitializationAnalytics() {
    Thread {
      val initializationPayload =
        AnalyticsInitializationPayload(sdkVersion, userAgent, PLATFORM, cliendId)

      val moshi: Moshi = Moshi.Builder().build()
      val moshiAdapter: JsonAdapter<AnalyticsInitializationPayload> = moshi.adapter()
      val jsonData: String = moshiAdapter.toJson(initializationPayload)

      val analyticsEvent =
        AnalyticsEvent(
          appName = APP_NAME,
          catalogName = AnalyticsInitializationPayload.CATALOG,
          uuid = UUID.randomUUID().toString(),
          jsonData = jsonData,
          recordedAt = System.nanoTime() * 10,
        )

      networkManager.uploadAnalyticsEvents(cliendId, listOf(analyticsEvent))
    }.start()
  }
}

private const val APP_NAME = "paykitsdk-android"
private const val PLATFORM = "android"
