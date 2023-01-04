package com.squareup.cash.paykit

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.annotation.WorkerThread
import com.squareup.cash.paykit.PayKitState.Approved
import com.squareup.cash.paykit.PayKitState.Authorizing
import com.squareup.cash.paykit.PayKitState.Declined
import com.squareup.cash.paykit.PayKitState.NotStarted
import com.squareup.cash.paykit.PayKitState.PayKitException
import com.squareup.cash.paykit.PayKitState.PollingTransactionStatus
import com.squareup.cash.paykit.PayKitState.ReadyToAuthorize
import com.squareup.cash.paykit.exceptions.PayKitIntegrationException
import com.squareup.cash.paykit.models.common.NetworkResult.Failure
import com.squareup.cash.paykit.models.common.NetworkResult.Success
import com.squareup.cash.paykit.models.response.CustomerResponseData
import com.squareup.cash.paykit.models.sdk.PayKitPaymentAction
import com.squareup.cash.paykit.utils.orElse

private const val BASE_URL_SANDBOX = "https://sandbox.api.cash.app/customer-request/v1/"
private const val BASE_URL_PRODUCTION = "https://api.cash.app/customer-request/v1/"

/**
 * @param clientId Client Identifier that should be provided by Cash PayKit integration.
 * @param useSandboxEnvironment Specify what development environment should be used.
 */
class CashAppPayKit(
  private val clientId: String,
  private val useSandboxEnvironment: Boolean = false,
) : PayKitLifecycleListener {

  private var callbackListener: CashAppPayKitListener? = null

  private var customerResponseData: CustomerResponseData? = null

  private var currentState: PayKitState = NotStarted
    set(value) {
      field = value
      callbackListener?.payKitStateDidChange(value)
        .orElse {
          logError(
            "State changed to ${value.javaClass.simpleName}, but no listeners were notified." +
              "Make sure that you've used `registerForStateUpdates` to receive PayKit state updates.",
          )
        }
    }

  init {
    if (useSandboxEnvironment) {
      NetworkManager.baseUrl = BASE_URL_SANDBOX
    } else {
      NetworkManager.baseUrl = BASE_URL_PRODUCTION
    }
  }

  init {
    if (useSandboxEnvironment) {
      NetworkManager.baseUrl = BASE_URL_SANDBOX
    } else {
      NetworkManager.baseUrl = BASE_URL_PRODUCTION
    }
  }

  /**
   * Create customer request given a [PayKitPaymentAction].
   * Must be called from a background thread.
   *
   * @param paymentAction A wrapper class that contains all of the necessary ingredients for building a customer request.
   *                      Look at [PayKitPaymentAction] for more details.
   */
  @WorkerThread
  fun createCustomerRequest(paymentAction: PayKitPaymentAction) {
    enforceRegisteredStateUpdatesListener()
    val networkResult = NetworkManager.createCustomerRequest(clientId, paymentAction)
    when (networkResult) {
      is Failure -> {
        currentState = PayKitException(networkResult.exception)
      }
      is Success -> {
        customerResponseData = networkResult.data.customerResponseData
        currentState = ReadyToAuthorize(networkResult.data.customerResponseData)
      }
    }
  }

  /**
   * Update an existing customer request given its [requestId] an the updated definitions contained within [PayKitPaymentAction].
   * Must be called from a background thread.
   *
   * @param requestId ID of the request we intent do update.
   * @param paymentAction A wrapper class that contains all of the necessary ingredients for building a customer request.
   *                      Look at [PayKitPaymentAction] for more details.
   */
  @WorkerThread
  fun updateCustomerRequest(requestId: String, paymentAction: PayKitPaymentAction) {
    enforceRegisteredStateUpdatesListener()
    TODO("Implement updateCustomerRequest") // https://www.notion.so/cashappcash/Implement-updateCustomerRequest-c32a61dcdb3e49a8abd18119384492f0
  }

  /**
   * Authorize a customer request. This function must be called AFTER `createCustomerRequest`.
   * Not doing so will result in an Exception in sandbox mode, and a silent error log in production.
   *
   * @param context Android context class.
   */
  @Throws(IllegalArgumentException::class, PayKitIntegrationException::class)
  fun authorizeCustomerRequest(context: Context) {
    val customerData = customerResponseData

    if (customerData == null) {
      logAndSoftCrash(PayKitIntegrationException("Can't call authorizeCustomerRequest user before calling `createCustomerRequest`. Alternatively provide your own customerData"))
      return
    }

    authorizeCustomerRequest(context, customerData)
  }

  /**
   * Authorize a customer request with a previously created `customerData`.
   * This function will set this SDK instance internal state to the `customerData` provided here as a function parameter.
   *
   */
  @Throws(IllegalArgumentException::class)
  fun authorizeCustomerRequest(
    context: Context,
    customerData: CustomerResponseData,
  ) {
    enforceRegisteredStateUpdatesListener()

    if (customerData.authFlowTriggers?.mobileUrl.isNullOrEmpty()) {
      throw IllegalArgumentException("customerData is missing redirect url")
    }
    // Open Mobile URL provided by backend response.
    val intent = Intent(Intent.ACTION_VIEW)
    intent.data = try {
      Uri.parse(customerData.authFlowTriggers?.mobileUrl)
    } catch (error: NullPointerException) {
      throw IllegalArgumentException("Cannot parse redirect url")
    }

    // Replace internal state.
    customerResponseData = customerData

    // Register for process lifecycle updates.
    PayKitLifecycleObserver.register(this)

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
      logAndSoftCrash(PayKitIntegrationException("Shouldn't call this function before registering for state updates via `registerForStateUpdates`."))
    }
  }

  private fun poolTransactionStatus() {
    Thread {
      val networkResult = NetworkManager.retrieveUpdatedRequestData(
        clientId,
        customerResponseData!!.id,
      )
      if (networkResult is Failure) {
        currentState = PayKitException(networkResult.exception)
        return@Thread
      }
      customerResponseData = (networkResult as Success).data.customerResponseData

      if (customerResponseData?.status == "APPROVED") {
        // Successful transaction.
        setStateFinished(true)
      } else {
        // If status is pending, schedule to check again.
        if (customerResponseData?.status == "PENDING") {
          // TODO: Add backoff strategy for long polling. ( https://www.notion.so/cashappcash/Implement-Long-pooling-retry-logic-a9af47e2db9242faa5d64df2596fd78e )
          Thread.sleep(500)
          poolTransactionStatus()
          return@Thread
        }

        // Unsuccessful transaction.
        setStateFinished(false)
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
  private fun logAndSoftCrash(exception: Exception) {
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

  /**
   * Lifecycle callbacks.
   */

  override fun onApplicationForegrounded() {
    logError("onApplicationForegrounded")
    if (currentState is Authorizing) {
      currentState = PollingTransactionStatus
      poolTransactionStatus()
    }
  }

  override fun onApplicationBackgrounded() {
    logError("onApplicationBackgrounded")
  }
}

interface CashAppPayKitListener {
  fun payKitStateDidChange(newState: PayKitState)
}
