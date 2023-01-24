package app.cash.paykit.devapp.analytics

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import app.cash.paykit.analytics.AnalyticsOptions
import app.cash.paykit.analytics.PayKitAnalytics
import app.cash.paykit.devapp.R
import java.util.*
import kotlin.math.log
import kotlin.time.ExperimentalTime
import kotlin.time.Duration.Companion.seconds

class AnalyticsSample : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_analytics_sample)

        val analyticsOptions = AnalyticsOptions(
            logLevel = Log.VERBOSE,
            interval = 5.seconds
        )

        val analytics = PayKitAnalytics(applicationContext, analyticsOptions)
        analytics.registerDeliveryHandler(AnalyticEventsHandler())

        findViewById<Button>(R.id.scheduleBtn).setOnClickListener {
            analytics.scheduleForDelivery(
                AnalyticEvent("scheduled data to sync @" + Date().toString())
            )
        }

        findViewById<Button>(R.id.dispatchBtn).setOnClickListener {
            analytics.dispatch(
                AnalyticEvent("dispatched data to sync @" + Date().toString())
            )
        }

        findViewById<Button>(R.id.shutdownBtn).setOnClickListener {
            analytics.scheduleShutdown()
        }
    }

}