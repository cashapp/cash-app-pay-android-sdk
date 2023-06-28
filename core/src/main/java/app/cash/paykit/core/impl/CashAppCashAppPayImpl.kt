/*
 * Copyright (C) 2023 Cash App
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package app.cash.paykit.core.impl

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.annotation.WorkerThread
import app.cash.paykit.core.BuildConfig
import app.cash.paykit.core.CashAppPay
import app.cash.paykit.core.CashAppPayFactory.TOKEN_REFRESH_WINDOW
import app.cash.paykit.core.CashAppPayLifecycleObserver
import app.cash.paykit.core.CashAppPayListener
import app.cash.paykit.core.CashAppPayState
import app.cash.paykit.core.CashAppPayState.Approved
import app.cash.paykit.core.CashAppPayState.Authorizing
import app.cash.paykit.core.CashAppPayState.CashAppPayExceptionState
import app.cash.paykit.core.CashAppPayState.CreatingCustomerRequest
import app.cash.paykit.core.CashAppPayState.Declined
import app.cash.paykit.core.CashAppPayState.NotStarted
import app.cash.paykit.core.CashAppPayState.PollingTransactionStatus
import app.cash.paykit.core.CashAppPayState.ReadyToAuthorize
import app.cash.paykit.core.CashAppPayState.Refreshing
import app.cash.paykit.core.CashAppPayState.RetrievingExistingCustomerRequest
import app.cash.paykit.core.CashAppPayState.UpdatingCustomerRequest
import app.cash.paykit.core.NetworkManager
import app.cash.paykit.core.analytics.PayKitAnalyticsEventDispatcher
import app.cash.paykit.core.android.ApplicationContextHolder
import app.cash.paykit.core.android.LOG_TAG
import app.cash.paykit.core.android.safeStart
import app.cash.paykit.core.exceptions.CashAppPayIntegrationException
import app.cash.paykit.core.exceptions.CashAppPayNetworkErrorType.CONNECTIVITY
import app.cash.paykit.core.exceptions.CashAppPayNetworkException
import app.cash.paykit.core.models.common.NetworkResult.Failure
import app.cash.paykit.core.models.common.NetworkResult.Success
import app.cash.paykit.core.models.response.CustomerResponseData
import app.cash.paykit.core.models.response.STATUS_APPROVED
import app.cash.paykit.core.models.response.STATUS_PENDING
import app.cash.paykit.core.models.response.STATUS_PROCESSING
import app.cash.paykit.core.models.sdk.CashAppPayPaymentAction
import app.cash.paykit.core.utils.orElse
import kotlinx.datetime.Clock
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

/**
 * @param clientId Client Identifier that should be provided by Cash PayKit integration.
 * @param useSandboxEnvironment Specify what development environment should be used.
 */
internal class CashAppCashAppPayImpl(
  private val clientId: String,
  private val networkManager: NetworkManager,
  private val analyticsEventDispatcher: PayKitAnalyticsEventDispatcher,
  private val payKitLifecycleListener: CashAppPayLifecycleObserver,
  private val useSandboxEnvironment: Boolean = false,
  initialState: CashAppPayState = NotStarted,
  initialCustomerResponseData: CustomerResponseData? = null,
) : CashAppPay, CashAppPayLifecycleListener {

  private var callbackListener: CashAppPayListener? = null

  private var customerResponseData: CustomerResponseData? = initialCustomerResponseData

  private var checkForApprovalThread: Thread? = null
  private var refreshUnauthorizedThread: Thread? = null

  private var currentState: CashAppPayState = initialState
    set(value) {
      field = value
      // Track Analytics for various state changes.
      when (value) {
        is Approved -> analyticsEventDispatcher.stateApproved(value)
        is CashAppPayExceptionState -> analyticsEventDispatcher.exceptionOccurred(
          value,
          customerResponseData,
        )
        Authorizing -> analyticsEventDispatcher.genericStateChanged(value, customerResponseData)
        Refreshing -> analyticsEventDispatcher.genericStateChanged(value, customerResponseData)
        Declined -> analyticsEventDispatcher.genericStateChanged(value, customerResponseData)
        NotStarted -> analyticsEventDispatcher.genericStateChanged(value, customerResponseData)
        PollingTransactionStatus -> analyticsEventDispatcher.genericStateChanged(value, customerResponseData)
        is ReadyToAuthorize -> analyticsEventDispatcher.genericStateChanged(value, customerResponseData)
        RetrievingExistingCustomerRequest -> analyticsEventDispatcher.genericStateChanged(value, customerResponseData)
        CreatingCustomerRequest -> { } // Handled separately.
        UpdatingCustomerRequest -> { } // Handled separately.
      }

      // Notify listener of State change.
      callbackListener?.cashAppPayStateDidChange(value)
        .orElse {
          logError(
            "State changed to ${value.javaClass.simpleName}, but no listeners were notified." +
              "Make sure that you've used `registerForStateUpdates` to receive PayKit state updates.",
          )
        }
    }

  init {
    // Register for process lifecycle updates.
    payKitLifecycleListener.register(this)
    analyticsEventDispatcher.sdkInitialized()
  }

  override fun createCustomerRequest(paymentAction: CashAppPayPaymentAction, redirectUri: String?) {
    createCustomerRequest(listOf(paymentAction), redirectUri)
  }

  /**
   * Create customer request given a [CashAppPayPaymentAction].
   * Must be called from a background thread.
   *
   * @param paymentActions A wrapper class that contains all of the necessary ingredients for building a customer request.
   *                      Look at [PayKitPaymentAction] for more details.
   */
  @WorkerThread
  override fun createCustomerRequest(paymentActions: List<CashAppPayPaymentAction>, redirectUri: String?) {
    enforceRegisteredStateUpdatesListener()

    // Validate [paymentActions] is not empty.
    if (paymentActions.isEmpty()) {
      val exceptionText = "paymentAction should not be empty"
      currentState = softCrashOrStateException(CashAppPayIntegrationException(exceptionText))
      return
    }

    currentState = CreatingCustomerRequest

    // Network call.
    val networkResult = networkManager.createCustomerRequest(clientId, paymentActions, redirectUri)
    when (networkResult) {
      is Failure -> {
        currentState = CashAppPayExceptionState(networkResult.exception)
      }

      is Success -> {
        customerResponseData = networkResult.data.customerResponseData
        currentState = ReadyToAuthorize(networkResult.data.customerResponseData)
        scheduleUnauthorizedCustomerRequestRefresh(networkResult.data.customerResponseData)
      }
    }
  }

  override fun updateCustomerRequest(requestId: String, paymentAction: CashAppPayPaymentAction) {
    updateCustomerRequest(requestId, listOf(paymentAction))
  }

  /**
   * Update an existing customer request given its [requestId] an the updated definitions contained within [CashAppPayPaymentAction].
   * Must be called from a background thread.
   *
   * @param requestId ID of the request we intent do update.
   * @param paymentActions A wrapper class that contains all of the necessary ingredients for building a customer request.
   *                      Look at [PayKitPaymentAction] for more details.
   */
  @WorkerThread
  override fun updateCustomerRequest(
    requestId: String,
    paymentActions: List<CashAppPayPaymentAction>,
  ) {
    enforceRegisteredStateUpdatesListener()

    // Validate [paymentActions] is not empty.
    if (paymentActions.isEmpty()) {
      val exceptionText = "paymentAction should not be empty"
      currentState = softCrashOrStateException(CashAppPayIntegrationException(exceptionText))
      return
    }

    currentState = UpdatingCustomerRequest

    // Network request.
    val networkResult = networkManager.updateCustomerRequest(clientId, requestId, paymentActions)
    when (networkResult) {
      is Failure -> {
        currentState = CashAppPayExceptionState(networkResult.exception)
      }

      is Success -> {
        customerResponseData = networkResult.data.customerResponseData
        currentState = ReadyToAuthorize(networkResult.data.customerResponseData)
      }
    }
  }

  @WorkerThread
  override fun startWithExistingCustomerRequest(requestId: String) {
    enforceRegisteredStateUpdatesListener()
    currentState = RetrievingExistingCustomerRequest
    val networkResult = networkManager.retrieveUpdatedRequestData(clientId, requestId)
    when (networkResult) {
      is Failure -> {
        currentState = CashAppPayExceptionState(networkResult.exception)
      }

      is Success -> {
        customerResponseData = networkResult.data.customerResponseData

        // Determine what kind of status we got.
        currentState = when (customerResponseData?.status) {
          STATUS_PROCESSING -> {
            Authorizing
          }

          STATUS_PENDING -> {
            scheduleUnauthorizedCustomerRequestRefresh(networkResult.data.customerResponseData)
            ReadyToAuthorize(customerResponseData!!)
          }

          STATUS_APPROVED -> {
            Approved(networkResult.data.customerResponseData)
          }

          else -> {
            Declined
          }
        }

        updateStateAndPoolForTransactionStatus()
      }
    }
  }

  /**
   * Authorize a customer request. This function must be called AFTER `createCustomerRequest`.
   * Not doing so will result in an Exception in sandbox mode, and a silent error log in production.
   */
  @Throws(IllegalArgumentException::class, CashAppPayIntegrationException::class)
  override fun authorizeCustomerRequest() {
    val customerData = customerResponseData

    if (customerData == null) {
      logAndSoftCrash(
        CashAppPayIntegrationException(
          "Can't call authorizeCustomerRequest user before calling `createCustomerRequest`. Alternatively provide your own customerData",
        ),
      )
      return
    }

    if (customerData.isAuthTokenExpired()) {
      logInfo("Auth token expired when attempting to authenticate, refreshing before proceeding.")
      deferredAuthorizeCustomerRequest()
      return
    }

    authorizeCustomerRequest(customerData)
  }

  /**
   * Deferred authorization of a customer request, when the auth token has expired.
   */
  private fun deferredAuthorizeCustomerRequest() {
    // Stop the thread that refreshes the customer request.
    try {
      refreshUnauthorizedThread?.interrupt()
    } catch (e: Exception) {
      logError("Error while interrupting previous thread. Exception: $e")
    }

    currentState = Refreshing

    logInfo("Will refresh customer request before proceeding with authorization.")
    Thread {
      val networkResult = networkManager.retrieveUpdatedRequestData(
        clientId,
        customerResponseData!!.id,
      )
      if (networkResult is Failure) {
        logError("Failed to refresh expired auth token customer request.")
        currentState = CashAppPayExceptionState(networkResult.exception)
        return@Thread
      }
      logInfo("Refreshed customer request with SUCCESS")
      customerResponseData = (networkResult as Success).data.customerResponseData

      if (currentState == Refreshing) {
        authorizeCustomerRequest(customerResponseData!!)
      }
    }.safeStart("Error while attempting to run deferred authorization.", onError = {
      if (currentState == Refreshing) {
        currentState = CashAppPayExceptionState(CashAppPayNetworkException(CONNECTIVITY))
      }
    })
  }

  /**
   * Authorize a customer request with a previously created `customerData`.
   * This function will set this SDK instance internal state to the `customerData` provided here as a function parameter.
   *
   */
  @Throws(IllegalArgumentException::class, RuntimeException::class)
  override fun authorizeCustomerRequest(
    customerData: CustomerResponseData,
  ) {
    enforceRegisteredStateUpdatesListener()

    if (customerData.authFlowTriggers?.mobileUrl.isNullOrEmpty()) {
      throw IllegalArgumentException("customerData is missing redirect url")
    }
    // Open Mobile URL provided by backend response.
    val intent = Intent(Intent.ACTION_VIEW)
    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
    intent.data = try {
      Uri.parse(customerData.authFlowTriggers?.mobileUrl)
    } catch (error: NullPointerException) {
      throw IllegalArgumentException("Cannot parse redirect url")
    }

    // Replace internal state.
    customerResponseData = customerData

    if (customerData.isAuthTokenExpired()) {
      logInfo("Auth token expired when attempting to authenticate, refreshing before proceeding.")
      deferredAuthorizeCustomerRequest()
      return
    }

    currentState = Authorizing
    try {
      ApplicationContextHolder.applicationContext.startActivity(intent)
    } catch (activityNotFoundException: ActivityNotFoundException) {
      currentState = CashAppPayExceptionState(CashAppPayIntegrationException("Unable to open mobileUrl: ${customerData.authFlowTriggers?.mobileUrl}"))
      return
    }
  }

  /**
   *  Register a [CashAppPayListener] to receive PayKit callbacks.
   */
  override fun registerForStateUpdates(listener: CashAppPayListener) {
    callbackListener = listener
    analyticsEventDispatcher.eventListenerAdded()
  }

  /**
   *  Unregister any previously registered [CashAppPayListener] from PayKit updates.
   */
  override fun unregisterFromStateUpdates() {
    logInfo("Unregistering from state updates")
    callbackListener = null
    payKitLifecycleListener.unregister(this)
    analyticsEventDispatcher.eventListenerRemoved()
    analyticsEventDispatcher.shutdown()

    // Stop any polling operations that might be running.
    try {
      refreshUnauthorizedThread?.interrupt()
      checkForApprovalThread?.interrupt()
    } catch (e: Exception) {
      logError("Error while interrupting threads. Exception: $e")
    }
  }

  private fun enforceRegisteredStateUpdatesListener() {
    if (callbackListener == null) {
      logAndSoftCrash(
        CashAppPayIntegrationException(
          "Shouldn't call this function before registering for state updates via `registerForStateUpdates`.",
        ),
      )
    }
  }

  private fun poolTransactionStatus() {
    checkForApprovalThread = Thread {
      val networkResult = networkManager.retrieveUpdatedRequestData(
        clientId,
        customerResponseData!!.id,
      )
      if (networkResult is Failure) {
        currentState = CashAppPayExceptionState(networkResult.exception)
        return@Thread
      }
      customerResponseData = (networkResult as Success).data.customerResponseData

      if (customerResponseData?.status == STATUS_APPROVED) {
        // Successful transaction.
        setStateFinished(true)
      } else {
        // If status is pending, schedule to check again.
        if (customerResponseData?.status == STATUS_PENDING) {
          // TODO: Add backoff strategy for long polling. ( https://www.notion.so/cashappcash/Implement-Long-pooling-retry-logic-a9af47e2db9242faa5d64df2596fd78e )
          try {
            Thread.sleep(500)
          } catch (e: InterruptedException) {
            return@Thread
          }

          poolTransactionStatus()
          return@Thread
        }

        // Unsuccessful transaction.
        setStateFinished(false)
      }
    }
    checkForApprovalThread?.safeStart(errorMessage = "Could not start checkForApprovalThread.")
  }

  private fun refreshUnauthorizedCustomerRequest(delay: Duration) {
    // Before starting a new thread, cancel any previous one.
    try {
      refreshUnauthorizedThread?.interrupt()
    } catch (e: Exception) {
      logError("Error while interrupting previous thread. Exception: $e")
    }

    refreshUnauthorizedThread = Thread {
      try {
        Thread.sleep(delay.inWholeMilliseconds)
      } catch (e: InterruptedException) {
        return@Thread
      }

      // Stop refreshing if the request has expired.
      val currentTime = Clock.System.now()
      val hasExpired = customerResponseData?.expiresAt?.let { expiresAt -> currentTime > expiresAt } ?: false
      if (hasExpired) {
        logError("Customer request has expired. Stopping refresh.")
        return@Thread
      }

      if (currentState !is ReadyToAuthorize) {
        // In this case, we don't want to retry since we're in a state that doesn't allow it.
        logError("Not refreshing unauthorized customer request because state is not ReadyToAuthorize")
        return@Thread
      }

      val networkResult = networkManager.retrieveUpdatedRequestData(
        clientId,
        customerResponseData!!.id,
      )
      if (networkResult is Failure) {
        logError("Failed to refresh expiring auth token customer request.")

        // Retry refreshing unauthorized customer request.
        refreshUnauthorizedCustomerRequest(delay)
        return@Thread
      }
      logInfo("Refreshed customer request with SUCCESS")
      customerResponseData = (networkResult as Success).data.customerResponseData
      refreshUnauthorizedCustomerRequest(delay)
    }

    refreshUnauthorizedThread?.safeStart("Could not start refreshUnauthorizedThread.", onError = {
      refreshUnauthorizedCustomerRequest(delay)
    })
  }

  /**
   * Given a `customerResponseData` object, this function will schedule a refresh of the customer request
   * so that the auth flow trigger is refreshed before it expires.
   */
  private fun scheduleUnauthorizedCustomerRequestRefresh(customerResponseData: CustomerResponseData) {
    if (customerResponseData.authFlowTriggers?.refreshesAt == null) {
      logError("Unable to schedule unauthorized customer request refresh. RefreshesAt is null.")
      return
    }

    val ttlSeconds = customerResponseData.authFlowTriggers.refreshesAt.minus(customerResponseData.createdAt)

    val refreshDelay = ttlSeconds.inWholeSeconds.minus(TOKEN_REFRESH_WINDOW.inWholeSeconds)
    logInfo("Scheduling unauthorized customer request refresh in $refreshDelay seconds.")
    refreshUnauthorizedCustomerRequest(refreshDelay.seconds)
  }

  private fun logError(errorMessage: String) {
    Log.e(LOG_TAG, errorMessage)
  }

  private fun logInfo(errorMessage: String) {
    Log.i(LOG_TAG, errorMessage)
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

  /**
   * This function will throw the provided [exception] during development, or change the SDK state to [CashAppPayExceptionState] otherwise.
   */
  @Throws
  private fun softCrashOrStateException(exception: Exception): CashAppPayExceptionState {
    logError("Error occurred. E.: $exception")
    if (useSandboxEnvironment || BuildConfig.DEBUG) {
      throw exception
    }
    return CashAppPayExceptionState(exception)
  }

  private fun setStateFinished(wasSuccessful: Boolean) {
    currentState = if (wasSuccessful) {
      Approved(customerResponseData!!)
    } else {
      Declined
    }
  }

  private fun updateStateAndPoolForTransactionStatus() {
    if (currentState is Authorizing) {
      currentState = PollingTransactionStatus
      poolTransactionStatus()
    }
  }

  /**
   * Lifecycle callbacks.
   */

  override fun onApplicationForegrounded() {
    logInfo("onApplicationForegrounded")
    updateStateAndPoolForTransactionStatus()
  }

  override fun onApplicationBackgrounded() {
    logInfo("onApplicationBackgrounded")
  }
}
