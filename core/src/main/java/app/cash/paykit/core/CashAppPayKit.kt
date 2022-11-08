/*
 * Copyright (C) 2023 Cash App
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package app.cash.paykit.core

import android.content.Context
import android.os.Build
import android.util.Log
import androidx.annotation.WorkerThread
import app.cash.paykit.analytics.AnalyticsOptions
import app.cash.paykit.analytics.PayKitAnalytics
import app.cash.paykit.core.analytics.PayKitAnalyticsEventDispatcher
import app.cash.paykit.core.analytics.PayKitAnalyticsEventDispatcherImpl
import app.cash.paykit.core.android.ApplicationContextHolder
import app.cash.paykit.core.exceptions.PayKitIntegrationException
import app.cash.paykit.core.impl.CashAppPayKitImpl
import app.cash.paykit.core.impl.NetworkManagerImpl
import app.cash.paykit.core.impl.PayKitLifecycleObserverImpl
import app.cash.paykit.core.models.response.CustomerResponseData
import app.cash.paykit.core.models.sdk.PayKitPaymentAction
import app.cash.paykit.core.network.OkHttpProvider
import app.cash.paykit.core.utils.UserAgentProvider
import kotlin.time.Duration.Companion.seconds

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

  private val paykitAnalytics by lazy { buildPayKitAnalytics() }

  private fun buildPayKitAnalytics() =
    with(ApplicationContextHolder.applicationContext) {
      val info = packageManager.getPackageInfo(packageName, 0)

      @Suppress("DEPRECATION")
      val versionCode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
        info?.longVersionCode
      } else {
        info?.versionCode
      }

      PayKitAnalytics(
        context = ApplicationContextHolder.applicationContext,
        options = AnalyticsOptions(
          delay = 10.seconds,
          logLevel = Log.VERBOSE,
          isLoggerDisabled = !BuildConfig.DEBUG,
          applicationVersionCode = versionCode!!.toInt(), // casting as int gives us the "legacy" version code
        ),
      )
    }

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
    val analyticsEventDispatcher =
      buildPayKitAnalyticsEventDispatcher(clientId, networkManager, paykitAnalytics)

    return CashAppPayKitImpl(
      clientId = clientId,
      networkManager = networkManager,
      analyticsEventDispatcher = analyticsEventDispatcher,
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

    val analyticsEventDispatcher =
      buildPayKitAnalyticsEventDispatcher(clientId, networkManager, paykitAnalytics)

    return CashAppPayKitImpl(
      clientId = clientId,
      networkManager = networkManager,
      analyticsEventDispatcher = analyticsEventDispatcher,
      payKitLifecycleListener = payKitLifecycleObserver,
      useSandboxEnvironment = true,
    )
  }

  private fun buildPayKitAnalyticsEventDispatcher(
    clientId: String,
    networkManager: NetworkManager,
    eventsManager: PayKitAnalytics,
  ): PayKitAnalyticsEventDispatcher {
    val sdkVersion =
      ApplicationContextHolder.applicationContext.getString(R.string.cashpaykit_version)
    return PayKitAnalyticsEventDispatcherImpl(
      sdkVersion,
      clientId,
      getUserAgentValue(),
      eventsManager,
      networkManager,
    )
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
