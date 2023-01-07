package com.squareup.cash.paykit

import android.content.Context
import androidx.annotation.WorkerThread
import com.squareup.cash.paykit.exceptions.PayKitIntegrationException
import com.squareup.cash.paykit.impl.CashAppPayKitImpl
import com.squareup.cash.paykit.impl.NetworkManagerImpl
import com.squareup.cash.paykit.impl.PayKitLifecycleObserverImpl
import com.squareup.cash.paykit.models.response.CustomerResponseData
import com.squareup.cash.paykit.models.sdk.PayKitPaymentAction

interface CashAppPayKit {
  /**
   * Create customer request given a [PayKitPaymentAction].
   * Must be called from a background thread.
   *
   * @param paymentAction A wrapper class that contains all of the necessary ingredients for building a customer request.
   *                      Look at [PayKitPaymentAction] for more details.
   */
  @WorkerThread
  fun createCustomerRequest(paymentAction: PayKitPaymentAction)

  /**
   * Update an existing customer request given its [requestId] an the updated definitions contained within [PayKitPaymentAction].
   * Must be called from a background thread.
   *
   * @param requestId ID of the request we intent do update.
   * @param paymentAction A wrapper class that contains all of the necessary ingredients for building a customer request.
   *                      Look at [PayKitPaymentAction] for more details.
   */
  @WorkerThread
  fun updateCustomerRequest(
    requestId: String,
    paymentAction: PayKitPaymentAction,
  )

  /**
   * Authorize a customer request. This function must be called AFTER `createCustomerRequest`.
   * Not doing so will result in an Exception in sandbox mode, and a silent error log in production.
   *
   * @param context Android context class.
   */
  @Throws(IllegalArgumentException::class, PayKitIntegrationException::class)
  fun authorizeCustomerRequest(context: Context)

  /**
   * Authorize a customer request with a previously created `customerData`.
   * This function will set this SDK instance internal state to the `customerData` provided here as a function parameter.
   *
   */
  @Throws(IllegalArgumentException::class, RuntimeException::class)
  fun authorizeCustomerRequest(
    context: Context,
    customerData: CustomerResponseData,
  )

  /**
   *  Register a [CashAppPayKitListener] to receive PayKit callbacks.
   */
  fun registerForStateUpdates(listener: CashAppPayKitListener)

  /**
   *  Unregister any previously registered [CashAppPayKitListener] from PayKit updates.
   */
  fun unregisterFromStateUpdates()
}

object CashAppPayKitFactory {

  private val payKitLifecycleObserver: PayKitLifecycleObserver = PayKitLifecycleObserverImpl()

  /**
   * @param clientId Client Identifier that should be provided by Cash PayKit integration.
   */
  fun create(
    clientId: String,
  ): CashAppPayKit {
    return CashAppPayKitImpl(
      clientId = clientId,
      networkManager = NetworkManagerImpl(BASE_URL_PRODUCTION),
      payKitLifecycleListener = payKitLifecycleObserver,
      useSandboxEnvironment = false,
    )
  }

  /**
   * @param clientId Client Identifier that should be provided by Cash PayKit integration.
   */
  fun createSandbox(
    clientId: String,
  ): CashAppPayKit {
    return CashAppPayKitImpl(
      clientId = clientId,
      networkManager = NetworkManagerImpl(BASE_URL_SANDBOX),
      payKitLifecycleListener = payKitLifecycleObserver,
      useSandboxEnvironment = true,
    )
  }

  private const val BASE_URL_SANDBOX = "https://sandbox.api.cash.app/customer-request/v1/"
  private const val BASE_URL_PRODUCTION = "https://api.cash.app/customer-request/v1/"
}

interface CashAppPayKitListener {
  fun payKitStateDidChange(newState: PayKitState)
}
