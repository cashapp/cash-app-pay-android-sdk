package app.cash.paykit.core

import android.content.Context
import androidx.annotation.WorkerThread
import app.cash.paykit.core.analytics.PayKitAnalyticsEvents
import app.cash.paykit.core.analytics.PayKitAnalyticsEventsImpl
import app.cash.paykit.core.android.ApplicationContextHolder
import app.cash.paykit.core.exceptions.PayKitIntegrationException
import app.cash.paykit.core.impl.CashAppPayKitImpl
import app.cash.paykit.core.impl.NetworkManagerImpl
import app.cash.paykit.core.impl.PayKitLifecycleObserverImpl
import app.cash.paykit.core.models.response.CustomerResponseData
import app.cash.paykit.core.models.sdk.PayKitPaymentAction
import app.cash.paykit.core.network.OkHttpProvider
import app.cash.paykit.core.utils.UserAgentProvider

interface CashAppPayKit {
  /**
   * Create customer request given a [PayKitPaymentAction].
   *
   * Must be called from a background thread.
   *
   * @param paymentAction A wrapper class that contains all of the necessary ingredients for building a customer request.
   *                      Look at [PayKitPaymentAction] for more details.
   */
  @WorkerThread
  fun createCustomerRequest(paymentAction: PayKitPaymentAction)

  /**
   * Update an existing customer request given its [requestId] an the updated definitions contained within [PayKitPaymentAction].
   *
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
   * Retrieve an existing customer request, provided its [requestId]. This should be used as a
   * starting point, for cases where you want to recover the state of an existing or in-flight customer
   * request.
   *
   * Must be called from a background thread.
   *
   * @param requestId ID of the request we intent do retrieve.
   */
  @WorkerThread
  fun startWithExistingCustomerRequest(
    requestId: String,
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

  private fun getUserAgentValue(): String {
    return UserAgentProvider.provideUserAgent(ApplicationContextHolder.applicationContext)
  }

  /**
   * @param clientId Client Identifier that should be provided by Cash PayKit integration.
   */
  fun create(
    clientId: String,
  ): CashAppPayKit {
    val networkManager = NetworkManagerImpl(
      BASE_URL_PRODUCTION,
      ANALYTICS_BASE_URL,
      userAgentValue = getUserAgentValue(),
      okHttpClient = defaultOkHttpClient,
    )
    val paykitAnalytics = buildPayKitAnalytics(clientId, networkManager)

    return CashAppPayKitImpl(
      clientId = clientId,
      networkManager = networkManager,
      analytics = paykitAnalytics,
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
    val networkManager = NetworkManagerImpl(
      BASE_URL_SANDBOX,
      ANALYTICS_BASE_URL,
      userAgentValue = getUserAgentValue(),
      okHttpClient = defaultOkHttpClient,
    )

    val payKitAnalytics = buildPayKitAnalytics(clientId, networkManager)

    return CashAppPayKitImpl(
      clientId = clientId,
      networkManager = networkManager,
      analytics = payKitAnalytics,
      payKitLifecycleListener = payKitLifecycleObserver,
      useSandboxEnvironment = true,
    )
  }

  private fun buildPayKitAnalytics(
    clientId: String,
    networkManager: NetworkManager,
  ): PayKitAnalyticsEvents {
    val sdkVersion =
      ApplicationContextHolder.applicationContext.getString(R.string.cashpaykit_version)
    return PayKitAnalyticsEventsImpl(sdkVersion, clientId, getUserAgentValue(), networkManager)
  }

  private val defaultOkHttpClient = OkHttpProvider.provideOkHttpClient()

  // Do NOT add `const` to these, as it will invalidate reflection for our Dev App.
  private val BASE_URL_SANDBOX = "https://sandbox.api.cash.app/customer-request/v1/"
  private val BASE_URL_PRODUCTION = "https://api.cash.app/customer-request/v1/"
  private val ANALYTICS_BASE_URL = "https://api.squareup.com/"
}

interface CashAppPayKitListener {
  fun payKitStateDidChange(newState: PayKitState)
}
