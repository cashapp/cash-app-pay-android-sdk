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
import app.cash.paykit.core.CashAppPayState.UpdatingCustomerRequest
import app.cash.paykit.core.CashAppPayStateMachine
import app.cash.paykit.core.NetworkManager
import app.cash.paykit.core.PayKitEvents
import app.cash.paykit.core.PayKitMachineStates
import app.cash.paykit.core.PayKitMachineStates.Authorizing.DeepLinking
import app.cash.paykit.core.PayKitMachineStates.Authorizing.Polling
import app.cash.paykit.core.PayKitMachineStates.DecidedState
import app.cash.paykit.core.PayKitMachineStates.ErrorState.ExceptionState
import app.cash.paykit.core.analytics.PayKitAnalyticsEventDispatcher
import app.cash.paykit.core.android.ApplicationContextHolder
import app.cash.paykit.core.android.CAP_TAG
import app.cash.paykit.core.android.safeStart
import app.cash.paykit.core.exceptions.CashAppPayIntegrationException
import app.cash.paykit.core.models.response.CustomerResponseData
import app.cash.paykit.core.models.sdk.CashAppPayPaymentAction
import app.cash.paykit.core.utils.SingleThreadManager
import app.cash.paykit.core.utils.SingleThreadManagerImpl
import app.cash.paykit.core.utils.ThreadPurpose.CHECK_APPROVAL_STATUS
import app.cash.paykit.core.utils.ThreadPurpose.DEFERRED_REFRESH
import app.cash.paykit.core.utils.ThreadPurpose.REFRESH_AUTH_TOKEN
import app.cash.paykit.core.utils.orElse
import ru.nsk.kstatemachine.activeStates
import ru.nsk.kstatemachine.onStateEntry
import ru.nsk.kstatemachine.onStateExit
import ru.nsk.kstatemachine.onStateFinished
import ru.nsk.kstatemachine.onTransitionComplete
import ru.nsk.kstatemachine.onTransitionTriggered
import ru.nsk.kstatemachine.processEventBlocking
import ru.nsk.kstatemachine.startBlocking

/**
 * @param clientId Client Identifier that should be provided by Cash PayKit integration.
 * @param useSandboxEnvironment Specify what development environment should be used.
 */
internal class CashAppPayImpl(
  private val clientId: String,
  private val networkManager: NetworkManager,
  private val analyticsEventDispatcher: PayKitAnalyticsEventDispatcher,
  private val payKitLifecycleListener: CashAppPayLifecycleObserver,
  private val useSandboxEnvironment: Boolean = false,
  private val logger: CashAppLogger,
  private val singleThreadManager: SingleThreadManager = SingleThreadManagerImpl(logger),
  initialState: CashAppPayState = NotStarted,
  initialCustomerResponseData: CustomerResponseData? = null,
) : CashAppPay, CashAppPayLifecycleListener {

  private var callbackListener: CashAppPayListener? = null

  private var customerResponseData: CustomerResponseData? = initialCustomerResponseData

  // TODO pass in initial state
  private val stateMachine = CashAppPayStateMachine(clientId, networkManager)
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
          logger.logError(
            CAP_TAG,
            "State changed to ${value.javaClass.simpleName}, but no listeners were notified." +
              "Make sure that you've used `registerForStateUpdates` to receive PayKit state updates.",
          )
        }
    }

  init {
    // Register for process lifecycle updates.
    payKitLifecycleListener.register(this)
    analyticsEventDispatcher.sdkInitialized()

    Thread {
      with(stateMachine.payKitMachine) {
        startBlocking()
        onTransitionTriggered {
          // Listen to all transitions in one place
          // instead of listening to each transition separately
          Log.d(
            name,
            "Transition triggered from ${it.transition.sourceState} to ${it.direction.targetState} " +
              "on ${it.event} with argument: ${it.argument}"
          )
        }
        onTransitionComplete { transitionParams, activeStates ->
          Log.d(
            name,
            "Transition complete from ${transitionParams.transition.sourceState}, active states: $activeStates"
          )

          val state = activeStates.last() as PayKitMachineStates
          val customerState = when (state) { // return the "deepest" child state
            PayKitMachineStates.NotStarted -> NotStarted
            PayKitMachineStates.CreatingCustomerRequest -> CreatingCustomerRequest
            is PayKitMachineStates.ReadyToAuthorize -> ReadyToAuthorize(stateMachine.context!!.customerResponseData!!)
            is DeepLinking -> Authorizing
            is Polling -> PollingTransactionStatus
            DecidedState.Approved -> Approved(stateMachine.context.customerResponseData!!)
            DecidedState.Declined -> Declined
            is ExceptionState -> CashAppPayExceptionState(error(state.data))
            PayKitMachineStates.UpdatingCustomerRequest -> UpdatingCustomerRequest
          }

          // Or we could do this in the individual state nodes
          analyticsEventDispatcher.genericStateChanged(
            state,
            stateMachine.context.customerResponseData
          )

          // Notify listener of State change.
          callbackListener?.cashAppPayStateDidChange(customerState)
            .orElse {
              logError(
                "State changed to ${customerState.javaClass.simpleName}, but no listeners were notified." +
                  "Make sure that you've used `registerForStateUpdates` to receive PayKit state updates.",
              )
            }

        }

        onStateEntry { state, _ -> Log.d(name, "Entered state $state") }
        onStateExit { state, _ -> Log.d(name, "Exit state $state") }
        onStateFinished { state, _ -> Log.d(name, "State finished $state") }
      }
    }.start()
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
  override fun createCustomerRequest(
    paymentActions: List<CashAppPayPaymentAction>,
    redirectUri: String?
  ) {
    enforceRegisteredStateUpdatesListener()

    // Validate [paymentActions] is not empty.
    if (paymentActions.isEmpty()) {
      val exceptionText = "paymentAction should not be empty"
      // TODO throw?
      stateMachine.payKitMachine.processEventBlocking(
        PayKitEvents.InputEvents.IllegalArguments(CashAppPayIntegrationException(exceptionText))
      )
      return
    }

    stateMachine.payKitMachine.processEventBlocking(
      PayKitEvents.CreateCustomerRequest(
        Pair(
          paymentActions,
          redirectUri
        )
      )
    )
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
  @Throws(IllegalArgumentException::class)
  override fun updateCustomerRequest(
    requestId: String,
    paymentActions: List<CashAppPayPaymentAction>,
  ) {
    enforceRegisteredStateUpdatesListener()

    // Validate [paymentActions] is not empty.
    if (paymentActions.isEmpty()) {
      val exceptionText = "paymentAction should not be empty"
      /*stateMachine.payKitMachine.processEventBlocking(
        PayKitEvents.InputEvents.IllegalArguments(CashAppPayIntegrationException(exceptionText))
      )*/
      throw IllegalArgumentException(exceptionText)
    }

    // TODO change to when extension fun
    if (stateMachine.payKitMachine.activeStates()
        .any { it is PayKitMachineStates.Authorizing || it is PayKitMachineStates.ReadyToAuthorize }
    ) {
      stateMachine.payKitMachine.processEventBlocking(
        PayKitEvents.UpdateCustomerRequestEvent.UpdateCustomerRequestAction(paymentActions to requestId)
      )
    } else {
      // TODO should we be including the customer response data in these exceptions, so they can do something with it?
      // stateMachine.payKitMachine.processEventBlocking(
      //   PayKitEvents.InputEvents.IllegalArguments(CashAppPayIntegrationException("Unable to update customer request. Not in the correct state"))
      // )
      val exceptionText = "Unable to update customer request. Not in the correct state"
      /*stateMachine.payKitMachine.processEventBlocking(
        PayKitEvents.InputEvents.IllegalArguments(CashAppPayIntegrationException(exceptionText))
      )*/
      throw IllegalArgumentException(exceptionText)
    }
  }

  @WorkerThread
  override fun startWithExistingCustomerRequest(requestId: String) {
    enforceRegisteredStateUpdatesListener()

    // TODO convert to state machine event

    /*currentState = RetrievingExistingCustomerRequest
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
      }
    }*/
  }

  /**
   * Authorize a customer request. This function must be called AFTER `createCustomerRequest`.
   * Not doing so will result in an Exception in sandbox mode, and a silent error log in production.
   */
  @Throws(IllegalArgumentException::class, CashAppPayIntegrationException::class)
  override fun authorizeCustomerRequest() {
    Log.d("CRAIG", "stateMachine.payKitMachine.states ${stateMachine.payKitMachine.activeStates()}")

    val readyState =
      stateMachine.payKitMachine.activeStates().last() as? PayKitMachineStates.ReadyToAuthorize
    if (readyState == null) {
      // TODO we are throwing here... should we throw in other methods?
      logAndSoftCrash(
        "No customer data found when attempting to authorize.",
        CashAppPayIntegrationException(
          "State machine is not ready to authorize",
        ),
      )
      return
    }

    authorizeCustomerRequest(stateMachine.context.customerResponseData!!)
  }

  /**
   * Deferred authorization of a customer request, when the auth token has expired.
   */
  private fun deferredAuthorizeCustomerRequest() {
    // Stop the thread that refreshes the customer request.
    singleThreadManager.interruptThread(REFRESH_AUTH_TOKEN)

    currentState = Refreshing

    logger.logVerbose(CAP_TAG, "Will refresh customer request before proceeding with authorization.")
    singleThreadManager.createThread(DEFERRED_REFRESH) {
      val networkResult = networkManager.retrieveUpdatedRequestData(
        clientId,
        customerResponseData!!.id,
      )
      if (networkResult is Failure) {
        logger.logError(CAP_TAG, "Failed to refresh expired auth token customer request.", networkResult.exception)
        currentState = CashAppPayExceptionState(networkResult.exception)
        return@createThread
      }
      logger.logVerbose(CAP_TAG, "Refreshed customer request with SUCCESS")
      customerResponseData = (networkResult as Success).data.customerResponseData

      if (currentState == Refreshing) {
        authorizeCustomerRequest(customerResponseData!!)
      }
    }.safeStart("Error while attempting to run deferred authorization.", logger, onError = {
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

    stateMachine.payKitMachine.processEventBlocking(
      PayKitEvents.Authorize(customerData)
    )
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
    logger.logVerbose(CAP_TAG, "Unregistering from state updates")
    callbackListener = null
    payKitLifecycleListener.unregister(this)
    analyticsEventDispatcher.eventListenerRemoved()
    analyticsEventDispatcher.shutdown()

    // Stop any polling operations that might be running.
    singleThreadManager.interruptAllThreads()
  }

  private fun enforceRegisteredStateUpdatesListener() {
    if (callbackListener == null) {
      logAndSoftCrash(
        "No listener registered for state updates.",
        CashAppPayIntegrationException(
          "Shouldn't call this function before registering for state updates via `registerForStateUpdates`.",
        ),
      )
    }
  }

  private fun logError(errorMessage: String) {
    Log.e("PayKit", errorMessage)
  }

  /**
   * This function will log in production, additionally it will throw an exception in sandbox or debug mode.
   */
  @Throws
  private fun logAndSoftCrash(msg: String, exception: Exception) {
    logger.logError(CAP_TAG, msg, exception)
    if (useSandboxEnvironment || BuildConfig.DEBUG) {
      throw exception
    }
  }

  /**
   * This function will throw the provided [exception] during development, or change the SDK state to [CashAppPayExceptionState] otherwise.
   */
  @Throws
  private fun softCrashOrStateException(msg: String, exception: Exception): CashAppPayExceptionState {
    logger.logError(CAP_TAG, msg, exception)
    if (useSandboxEnvironment || BuildConfig.DEBUG) {
      throw exception
    }
    return CashAppPayExceptionState(exception)
  }

  /**
   * Lifecycle callbacks.
   */

  override fun onApplicationForegrounded() {
    logError("onApplicationForegrounded")
    // TODO send message into machine so it starts polling
  }

  override fun onApplicationBackgrounded() {
    logger.logVerbose(CAP_TAG, "onApplicationBackgrounded")
  }
}
