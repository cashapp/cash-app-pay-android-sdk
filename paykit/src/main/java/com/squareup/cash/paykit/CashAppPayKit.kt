package com.squareup.cash.paykit

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.squareup.cash.paykit.PayKitState.Approved
import com.squareup.cash.paykit.PayKitState.Authorizing
import com.squareup.cash.paykit.PayKitState.Declined
import com.squareup.cash.paykit.PayKitState.NotStarted
import com.squareup.cash.paykit.PayKitState.PollingTransactionStatus
import com.squareup.cash.paykit.PayKitState.ReadyToAuthorize
import com.squareup.cash.paykit.models.response.CreateCustomerResponseData
import com.squareup.cash.paykit.utils.orElse
import java.util.concurrent.atomic.AtomicBoolean

/**
 * @param clientId Client Identifier that should be provided by Cash PayKit integration.
 * @param useSandboxEnvironment Specify what development environment should be used.
 */
class CashAppPayKit(
  private val clientId: String,
  private val useSandboxEnvironment: Boolean = false
) : PayKitLifecycleListener {

  // TODO: Consider network errors.
  // TODO: Consider no internet available.

  private var callbackListener: CashAppPayKitListener? = null

  var customerResponseData: CreateCustomerResponseData? = null
    private set

  private var mainHandler: Handler = Handler(Looper.getMainLooper())

  private var currentState: PayKitState = NotStarted
    set(value) {
      field = value
      callbackListener?.payKitStateDidChange(value)
        .orElse {
          logError(
            "State changed to ${value.javaClass.simpleName}, but no listeners were notified." +
              "Make sure that you've used `registerForStateUpdates` to receive PayKit state updates."
          )
        }
    }

  private var isPaused = AtomicBoolean(false)

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
        customerResponseData = customerData.customerResponseData
        currentState = ReadyToAuthorize(customerData.customerResponseData)
      }
    }.start()
  }

  fun authorizeCustomerRequest(context: Context) {
    // TODO: DO NOT THROW IN PROD.
    val customerData = customerResponseData
      ?: throw NullPointerException("Can't call authorize user before calling `createCustomerRequest`. Alternatively provide your own customerData")

    authorizeCustomerRequest(context, customerData)
  }

  fun authorizeCustomerRequest(context: Context, customerData: CreateCustomerResponseData) {
    PayKitLifecycleObserver.register(this)
    val intent = Intent(Intent.ACTION_VIEW)
    intent.data = Uri.parse(customerData.authFlowTriggers?.mobileUrl)
    context.startActivity(intent)
    currentState = Authorizing
  }

  /**
   *  Register a [CashAppPayKitListener] to receive PayKit callbacks.
   */
  fun registerForStateUpdates(listener: CashAppPayKitListener) {
    callbackListener = listener
  }

  /**
   *  Unregister any previously registered [CashAppPayKitListener] from PayKit updates.
   */
  fun unregisterFromStateUpdates() {
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
          setStateFinished(true)
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
          setStateFinished(false)
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
    PayKitLifecycleObserver.unregister(this)
    currentState = if (wasSuccessful) {
      // TODO: Expose Grants for successful state.
      Approved(null)
    } else {
      Declined
    }
  }

  private fun runOnUiThread(mainHandler: Handler, action: Runnable) {
    mainHandler.post(action)
  }

  /**
   * Lifecycle callbacks.
   */

  override fun onApplicationForegrounded() {
    logError("onApplicationForegrounded")
    isPaused.set(false)
    if (currentState is Authorizing) {
      currentState = PollingTransactionStatus
      checkTransactionStatus()
    }
  }

  override fun onApplicationBackgrounded() {
    isPaused.set(true)
    logError("onApplicationBackgrounded")
  }
}

interface CashAppPayKitListener {
  fun payKitStateDidChange(newState: PayKitState)
}