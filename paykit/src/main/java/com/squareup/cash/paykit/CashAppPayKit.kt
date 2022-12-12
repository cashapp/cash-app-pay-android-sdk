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
import com.squareup.cash.paykit.exceptions.PayKitIntegrationException
import com.squareup.cash.paykit.models.response.CustomerResponseData
import com.squareup.cash.paykit.models.sdk.PayKitPaymentAction
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

  private var customerResponseData: CustomerResponseData? = null

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
   * @param paymentAction A wrapper class that contains all of the necessary ingredients for building a customer request.
   *                      Look at [PayKitPaymentAction] for more details.
   */
  fun createCustomerRequest(paymentAction: PayKitPaymentAction) {
    enforceRegisteredStateUpdatesListener()
    Thread {
      val customerData = NetworkManager.createCustomerRequest(clientId, paymentAction)

      // TODO For now resorting to simple callbacks and thread switching. Need to investigate pros/cons of using coroutines internally as the default.
      runOnUiThread(mainHandler) {
        customerResponseData = customerData.customerResponseData
        currentState = ReadyToAuthorize(customerData.customerResponseData)
      }
    }.start()
  }

  /**
   * @param requestId ID of the request we intent do update.
   * @param paymentAction A wrapper class that contains all of the necessary ingredients for building a customer request.
   *                      Look at [PayKitPaymentAction] for more details.
   */
  fun updateCustomerRequest(requestId: String, paymentAction: PayKitPaymentAction) {
    enforceRegisteredStateUpdatesListener()
    TODO("Implement updateCustomerRequest")
  }

  /**
   * Authorize a customer request. This function must be called AFTER `createCustomerRequest`.
   * Not doing so will result in an Exception in sandbox mode, and a silent error log in production.
   *
   * @param context Android context class.
   */
  fun authorizeCustomerRequest(context: Context) {
    val customerData = customerResponseData

    if (customerData == null) {
      logOrThrow(PayKitIntegrationException("Can't call authorizeCustomerRequest user before calling `createCustomerRequest`. Alternatively provide your own customerData"))
      return
    }

    authorizeCustomerRequest(context, customerData)
  }

  /**
   * Authorize a customer request with a previously created `customerData`.
   * This function will set this SDK instance internal state to the `customerData` provided here as a function parameter.
   *
   */
  fun authorizeCustomerRequest(context: Context, customerData: CustomerResponseData) {
    enforceRegisteredStateUpdatesListener()

    // Replace internal state.
    customerResponseData = customerData

    // Register for process lifecycle updates.
    PayKitLifecycleObserver.register(this)

    // Open Mobile URL provided by backend response.
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

  private fun enforceRegisteredStateUpdatesListener() {
    if (callbackListener == null) {
      logOrThrow(PayKitIntegrationException("Shouldn't call this function before registering for state updates via `registerForStateUpdates`."))
    }
  }

  private fun checkTransactionStatus() {
    logError("Executing checkTransactionStatus")
    Thread {
      customerResponseData =
        NetworkManager.retrieveUpdatedRequestData(
          clientId,
          customerResponseData!!.id
        ).customerResponseData
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
    Log.e("PayKit", errorMessage)
  }

  /**
   * This function will log in production, additionally it will throw an exception in sandbox or debug mode.
   */
  @Throws
  private fun logOrThrow(exception: Exception) {
    logError("Error occurred. E.: $exception")
    if (useSandboxEnvironment || BuildConfig.DEBUG) {
      throw exception
    }
  }

  private fun setStateFinished(wasSuccessful: Boolean) {
    PayKitLifecycleObserver.unregister(this)
    currentState = if (wasSuccessful) {
      Approved(customerResponseData!!)
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