package com.squareup.cash.paykit

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.squareup.cash.paykit.PayKitState.StateCustomerCreated
import com.squareup.cash.paykit.PayKitState.StateFinished
import com.squareup.cash.paykit.PayKitState.StatePendingDeliveryTransactionStatus
import com.squareup.cash.paykit.PayKitState.StatePollingTransactionStatus
import com.squareup.cash.paykit.PayKitState.StateRequestAuthorization
import com.squareup.cash.paykit.PayKitState.StateStarted
import com.squareup.cash.paykit.models.response.CreateCustomerResponseData
import com.squareup.cash.paykit.utils.orElse
import java.util.concurrent.atomic.AtomicBoolean

/**
 * @param clientId Client Identifier that should be provided by Cash PayKit integration.
 */
class CashPayKit(private val clientId: String) : PayKitLifecycleListener {

  // TODO: Consider network errors.
  // TODO: Consider no internet available.

  private var callbackListener: CashPayKitListener? = null

  var customerResponseData: CreateCustomerResponseData? = null
    private set

  private var mainHandler: Handler = Handler(Looper.getMainLooper())

  private var currentState: PayKitState = StateStarted
  private var isPaused = AtomicBoolean(false)

  init {
    PayKitLifecycleObserver.register(this)
  }

  /**
   * @param brandId The Brand Identifier that was provided to you by the Cash Pay console.
   * @param redirectUri: The URI to deep link back into your application once the transaction is approved.
   */
  fun createCustomerRequest(brandId: String, redirectUri: String) {
    // TODO: print error if there is no listener registered.
    Thread {
      val customerData = NetworkManager.createCustomerRequest(clientId, brandId, redirectUri)

      // TODO For now resorting to simple callbacks and thread switching. Need to investigate pros/cons of using coroutines internally as the default.
      runOnUiThread(mainHandler) {
        currentState = StateCustomerCreated
        customerResponseData = customerData.customerResponseData
        callbackListener?.customerCreated(customerData.customerResponseData)
          .orElse {
            logError(
              "Created User with success, but callback wasn't registered. " +
                "Double check that you've used `registerListener`."
            )
          }
      }
    }.start()
  }

  fun authorizeCustomer(context: Context) {
    val customerData = customerResponseData
      ?: throw NullPointerException("Can't call authorize user before calling `createCustomerRequest`. Alternatively provide your own customerData")

    authorizeCustomer(context, customerData)
  }

  fun authorizeCustomer(context: Context, customerData: CreateCustomerResponseData) {
    // TODO: Check if Cash App is installed, otherwise send to Play Store. Handle deferred deep linking?
    val intent = Intent(Intent.ACTION_VIEW)
    intent.data = Uri.parse(customerData.authFlowTriggers?.mobileUrl)
    context.startActivity(intent)
    currentState = StateRequestAuthorization
  }

  /**
   *  Register listener to receive PayKit callbacks.
   */
  fun registerListener(listener: CashPayKitListener) {
    callbackListener = listener
  }

  fun unregisterListener() {
    callbackListener = null
    PayKitLifecycleObserver.unregister(this)
  }

  private fun checkTransactionStatus() {
    logError("Executing checkTransactionStatus")
    Thread {
      customerResponseData =
        NetworkManager.retrieveRequest(clientId, customerResponseData!!.id).customerResponseData
      runOnUiThread(mainHandler) {
        if (customerResponseData?.status == "APPROVED") {
          logError("Transaction Approved!")
          // Successful transaction.
          if (isPaused.get()) {
            currentState = StatePendingDeliveryTransactionStatus(true)
          } else {
            setStateFinished(true)
          }
        } else {
          // If status is pending, schedule to check again.
          if (customerResponseData?.status == "PENDING") {
            // TODO: Add exponential backoff strategy for long polling.
            Thread.sleep(500)
            checkTransactionStatus()
            return@runOnUiThread
          }

          // Unsuccessful transaction.
          logError("Transaction unsuccessful!")
          if (isPaused.get()) {
            currentState = StatePendingDeliveryTransactionStatus(false)
          } else {
            setStateFinished(false)
          }
        }

      }
    }.start()
  }

  private fun logError(errorMessage: String) {
    if (BuildConfig.DEBUG) {
      Log.e("PayKit", errorMessage)
    }
  }

  private fun setStateFinished(wasSuccessful: Boolean) {
    currentState = StateFinished(wasSuccessful)
    callbackListener?.authorizationResult(wasSuccessful).orElse {
      logError(
        "Created User with success, but callback wasn't registered. " +
          "Double check that you've used `registerListener`."
      )
    }
  }

  /**
   * Lifecycle callbacks.
   */

  override fun onApplicationForegrounded() {
    logError("onApplicationForegrounded")
    isPaused.set(false)
    when (currentState) {
      StateCustomerCreated -> {} // Ignored.
      is StateFinished -> {} // Ignored.
      StatePollingTransactionStatus -> {
        checkTransactionStatus()
      }
      StateRequestAuthorization -> {
        currentState = StatePollingTransactionStatus
        checkTransactionStatus()
      }
      StateStarted -> {}// Ignored.
      is StatePendingDeliveryTransactionStatus -> {
        // Was awaiting to deliver transaction status result, deliver now.
        val success = (currentState as StatePendingDeliveryTransactionStatus).isSuccessful
        setStateFinished(success)
      }
    }
  }

  override fun onApplicationBackgrounded() {
    isPaused.set(true)
    logError("onApplicationBackgrounded")
  }
}

interface CashPayKitListener {
  fun customerCreated(customerData: CreateCustomerResponseData)

  fun authorizationResult(wasSuccessful: Boolean)
}

fun runOnUiThread(mainHandler: Handler, action: Runnable) {
  mainHandler.post(action)
}