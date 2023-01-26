package app.cash.paykit.devapp.analytics

import android.os.Handler
import android.os.HandlerThread
import android.os.Looper
import android.util.Log
import app.cash.paykit.analytics.core.DeliveryHandler
import app.cash.paykit.analytics.core.DeliveryListener
import app.cash.paykit.analytics.persistence.AnalyticEntry
import kotlin.random.Random

class AnalyticEventsHandler : DeliveryHandler() {
  private val handler: Handler

  init {
    val handlerThread = HandlerThread("AnalyticsHandlerThread")
    handlerThread.start()
    val looper: Looper = handlerThread.looper
    handler = Handler(looper)
  }

  override val deliverableType = AnalyticEvent.TYPE

  override fun deliver(entries: List<AnalyticEntry>, deliveryListener: DeliveryListener) {
    val asyncDelay = 1000L * Random.Default.nextInt(1, 3)
    log(
      "Simulate async call by delaying response for $asyncDelay ms",
    )
    handler.postDelayed({
      val success: Boolean = Random.Default.nextBoolean()
      log(
        "Simulated async call was a " + (if (success) "success" else "failure") + ".",
      )
      if (success) {
        deliveryListener.onSuccess(entries)
        log("sent entries:")
        for (p in entries) {
          log("sent:" + p.content)
        }
      } else {
        deliveryListener.onError(entries)
        log("failed entries:")
        for (p in entries) {
          log("failed:" + p.content)
        }
      }
    }, asyncDelay)
  }

  private fun log(message: String) {
    Log.d("AnalyticEventsHandler", message)
  }
}
