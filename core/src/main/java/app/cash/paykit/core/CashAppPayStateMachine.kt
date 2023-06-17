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

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.util.Log
import app.cash.paykit.core.PayKitEvents.Authorize
import app.cash.paykit.core.PayKitEvents.CreateCustomerRequest
import app.cash.paykit.core.PayKitEvents.DeepLinkError
import app.cash.paykit.core.PayKitEvents.DeepLinkSuccess
import app.cash.paykit.core.PayKitEvents.GetCustomerRequest
import app.cash.paykit.core.PayKitEvents.InputEvents.IllegalArguments
import app.cash.paykit.core.PayKitMachineContext.CreateRequestParams
import app.cash.paykit.core.PayKitMachineStates.Authorizing
import app.cash.paykit.core.PayKitMachineStates.DecidedState.Approved
import app.cash.paykit.core.PayKitMachineStates.DecidedState.Declined
import app.cash.paykit.core.PayKitMachineStates.ErrorState.IllegalArgumentsState
import app.cash.paykit.core.android.ApplicationContextHolder
import app.cash.paykit.core.exceptions.CashAppPayIntegrationException
import app.cash.paykit.core.models.common.NetworkResult.Failure
import app.cash.paykit.core.models.common.NetworkResult.Success
import app.cash.paykit.core.models.request.CustomerRequestData
import app.cash.paykit.core.models.response.CustomerResponseData
import app.cash.paykit.core.models.response.STATUS_DECLINED
import app.cash.paykit.core.models.sdk.CashAppPayPaymentAction
import ru.nsk.kstatemachine.DataEvent
import ru.nsk.kstatemachine.DefaultFinalDataState
import ru.nsk.kstatemachine.DefaultState
import ru.nsk.kstatemachine.Event
import ru.nsk.kstatemachine.FinalState
import ru.nsk.kstatemachine.State
import ru.nsk.kstatemachine.StateMachine
import ru.nsk.kstatemachine.addFinalState
import ru.nsk.kstatemachine.addInitialState
import ru.nsk.kstatemachine.createStdLibStateMachine
import ru.nsk.kstatemachine.dataTransition
import ru.nsk.kstatemachine.defaultDataExtractor
import ru.nsk.kstatemachine.invoke
import ru.nsk.kstatemachine.noTransition
import ru.nsk.kstatemachine.onEntry
import ru.nsk.kstatemachine.onExit
import ru.nsk.kstatemachine.onTriggered
import ru.nsk.kstatemachine.processEventBlocking
import ru.nsk.kstatemachine.state
import ru.nsk.kstatemachine.targetState
import ru.nsk.kstatemachine.transition
import ru.nsk.kstatemachine.transitionConditionally
import ru.nsk.kstatemachine.visitors.exportToPlantUmlBlocking
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

typealias CreatingCustomerRequestDataState = Pair<List<CashAppPayPaymentAction>, String?>

/**
 * Mutable contextual data for use in the state machine.
 *
 * Acts as a sort of redux store in the state machine
 */
data class PayKitMachineContext(
  val createRequestParams: CreateRequestParams? = null,
  val customerRequestData: CustomerRequestData? = null,
  val customerResponseData: CustomerResponseData? = null,
) {

  data class CreateRequestParams(
    val actions: List<CashAppPayPaymentAction>,
    val redirectUri: String? = null
  )
}

sealed class PayKitMachineStates(name: String) : DefaultState(name) {

  object NotStarted : PayKitMachineStates("NotStarted")
  object CreatingCustomerRequest : PayKitMachineStates("CreatingCustomerRequest")

  object ReadyToAuthorize : PayKitMachineStates("ReadyToAuthorize")

  sealed interface Authorizing {
    // object Parent : Authorizing, DefaultState("Authorizing")
    object DeepLinking : Authorizing, PayKitMachineStates("DeepLinking")
    object Polling : Authorizing, PayKitMachineStates("Polling")
  }

  sealed interface ErrorState {
    class IllegalArgumentsState : ErrorState,
      DefaultFinalDataState<Exception>(
        "IllegalArgumentsState",
        dataExtractor = defaultDataExtractor()
      )
  }

  sealed interface DecidedState {
    object Approved : PayKitMachineStates("Approved"), FinalState
    object Declined : PayKitMachineStates("Declined"), FinalState
  }
}

internal class CashAppPayStateMachine constructor(
  private val clientId: String,
  private val networkManager: NetworkManager,
) {

  var context: PayKitMachineContext = PayKitMachineContext()
    set(value) {
      field = value
      Log.d(payKitMachine.name, "Updating context: $value")
    }

  private fun State.poll(interval: Duration) {

    val thread = Thread {
      try {
        while (true) {
          Thread.sleep(interval.inWholeMilliseconds)
          val networkResult = networkManager.retrieveUpdatedRequestData(
            clientId,
            context.customerResponseData!!.id
          )
          when (networkResult) {
            is Failure -> {
              machine.processEventBlocking(GetCustomerRequest.Error(networkResult.exception))
            }

            is Success -> {
              val customerResponseData = networkResult.data.customerResponseData

              context = context.copy(
                customerResponseData = customerResponseData
              )
              machine.processEventBlocking(GetCustomerRequest.Success(customerResponseData))
            }
          }
        }
      } catch (e: InterruptedException) {
        Log.w("CRAIG", "InterruptedException getting customer request", e)
      }

    }.apply {
      name = "polling-thread"
    }

    onEntry {
      thread.start()
    }
    onExit {
      thread.interrupt()
      Log.e("CRAIG", "Stopping thread!!!")
    }
  }

  val payKitMachine = createStdLibStateMachine(
    name = "PayKit",
    start = false,
  ) {
    logger = StateMachine.Logger { Log.d(name, it()) }
    /*listenerExceptionHandler = StateMachine.ListenerExceptionHandler {
      Log.e("$name-Listener-error-Handler", "Exception: ${it}")
    }

    ignoredEventHandler = StateMachine.IgnoredEventHandler {
      Log.w(name, "unexpected ${it.event}")
    }

    ignoredEventHandler = StateMachine.IgnoredEventHandler {
      Log.w(name, "ignored event ${it.event}")
    }*/

    val errorState = addFinalState(IllegalArgumentsState()) {
    }

    dataTransition<IllegalArguments, Exception> {
      targetState = errorState
    }
    val authorizingState = state("Authorizing") {
      transitionConditionally<GetCustomerRequest.Success> {
        direction = {
          if (context.customerResponseData?.grants?.isNotEmpty() == true) {
            targetState(Approved)
          } else if (context.customerResponseData?.status.equals(STATUS_DECLINED)) {
            targetState(Declined)
          } else {
            noTransition()
          }
        }
      }
      addState(Authorizing.Polling) {
        poll(2.seconds)
      }

      addInitialState(Authorizing.DeepLinking) {
        // TODO abstract this to a worker
        onEntry {
          Thread {
            // Open Mobile URL provided by backend response.
            val intent = Intent(Intent.ACTION_VIEW)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            intent.data = try {
              Uri.parse(context.customerResponseData!!.authFlowTriggers?.mobileUrl)
            } catch (error: NullPointerException) {
              machine.processEventBlocking(DeepLinkError(IllegalArgumentException("Cannot parse redirect url")))
              return@Thread
            }

            try {
              ApplicationContextHolder.applicationContext.startActivity(intent)
              machine.processEventBlocking(DeepLinkSuccess)
            } catch (activityNotFoundException: ActivityNotFoundException) {
              machine.processEventBlocking(DeepLinkError(CashAppPayIntegrationException("Unable to open mobileUrl: ${context.customerResponseData!!.authFlowTriggers?.mobileUrl}")))
            }

          }.start()
        }
        transition<DeepLinkSuccess> {
          targetState = Authorizing.Polling
        }
        dataTransition<DeepLinkError, Exception> {
          targetState = errorState
        }
      }
    }

    addState(PayKitMachineStates.ReadyToAuthorize) {
      poll(10.seconds)

      transition<Authorize> {
        guard = { context.customerResponseData?.id != "bad" }
        targetState = authorizingState
      }
    }

    val creatingCustomerRequest =
      addState(PayKitMachineStates.CreatingCustomerRequest) {
        onEntry {
          when (val networkResult =
            networkManager.createCustomerRequest(
              clientId,
              context.createRequestParams!!.actions,
              context.createRequestParams!!.redirectUri,
            )
          ) {
            is Failure -> {
              machine.processEventBlocking(CreateCustomerRequest.Error(networkResult.exception))
            }

            is Success -> {
              val customerResponseData = networkResult.data.customerResponseData
              context = context.copy(customerResponseData = customerResponseData)
              machine.processEventBlocking(CreateCustomerRequest.Success(customerResponseData))
            }
          }
        }
        onExit {
          // TODO cancel network request?
        }
        transition<CreateCustomerRequest.Success> {
          targetState = PayKitMachineStates.ReadyToAuthorize
        }
        dataTransition<CreateCustomerRequest.Error, Exception> {
          targetState = errorState
        }
      }

    addInitialState(PayKitMachineStates.NotStarted) {
      transition<CreateCustomerRequest> {
        targetState = creatingCustomerRequest
        onTriggered {
          context = context.copy(
            createRequestParams = CreateRequestParams(
              it.event.data.first,
              it.event.data.second
            )
          )
        }
      }
    }

    addFinalState(Approved) {
    }

    addFinalState(Declined) {
    }
  }

  init {
    // www.plantuml.com/plantuml/png/nPRDIWCn58NtynHP2tq5QMatWeYqxiJ5q1ocmVp4v4QeuhlRknWOQBizcULilY_9ERbPLncBjE27axCm25dEng8UYylYKXmCuRiu2CmcvvA-OOd87OxUHIhooNjCez-KgGL4gGVzozpb9gTKt8-Ba_cTkIJsQ_B-mYLJTq1-Tl6JIQQ1tXy-p6hrs6EHIwOwK8_tZrYPFKPxVajpSq14Rmj6SN7QjYefeEpnrL25QF2IvA0ZWdH93iJIaDTsbIDrMDx738bxYLANAy7UGls6vI8QAjArX_ExI4KqkWpe_3AYGcyVzyA0Fz9k1t2DQ5lm_m00
    Log.d("CRAIG", payKitMachine.exportToPlantUmlBlocking())
  }
}

sealed interface PayKitEvents {
  data class CreateCustomerRequest(
    override val data: CreatingCustomerRequestDataState,
  ) : DataEvent<CreatingCustomerRequestDataState> {
    data class Success(override val data: CustomerResponseData) :
      DataEvent<CustomerResponseData>

    data class Error(override val data: Exception) : DataEvent<Exception>
  }

  sealed interface GetCustomerRequest {
    data class Success(override val data: CustomerResponseData) : DataEvent<CustomerResponseData>,
      GetCustomerRequest

    data class Error(override val data: Exception) : DataEvent<Exception>, GetCustomerRequest
  }

  data class Authorize(override val data: CustomerResponseData) : DataEvent<CustomerResponseData>
  object DeepLinkSuccess : Event

  data class DeepLinkError(override val data: Exception) : DataEvent<Exception>

  sealed interface InputEvents {
    data class IllegalArguments(override val data: CashAppPayIntegrationException) :
      DataEvent<CashAppPayIntegrationException>
  }
}


