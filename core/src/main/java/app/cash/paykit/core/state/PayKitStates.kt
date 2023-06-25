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

package app.cash.paykit.core.state

import app.cash.paykit.core.models.response.CustomerResponseData
import app.cash.paykit.core.models.sdk.CashAppPayPaymentAction
import app.cash.paykit.core.state.ClientEventPayload.CreatingCustomerRequestData
import app.cash.paykit.core.state.ClientEventPayload.UpdateCustomerRequestData
import ru.nsk.kstatemachine.DefaultDataState
import ru.nsk.kstatemachine.DefaultFinalDataState
import ru.nsk.kstatemachine.DefaultFinalState
import ru.nsk.kstatemachine.DefaultState
import ru.nsk.kstatemachine.defaultDataExtractor

sealed interface PayKitMachineStates {

  object NotStarted : PayKitMachineStates, DefaultState("NotStarted")
  object CreatingCustomerRequest : PayKitMachineStates,
    DefaultDataState<CreatingCustomerRequestData>(
      "CreatingCustomerRequest",
      dataExtractor = defaultDataExtractor()
    )

  object ReadyToAuthorize : PayKitMachineStates, DefaultDataState<CustomerResponseData>(
    "ReadyToAuthorize", dataExtractor = defaultDataExtractor()
  )

  sealed interface Authorizing : PayKitMachineStates {
    object DeepLinking : Authorizing, DefaultState("DeepLinking")

    object Polling : Authorizing, DefaultState("Polling")
  }

  object UpdatingCustomerRequest : PayKitMachineStates,
    DefaultDataState<UpdateCustomerRequestData>(
      "UpdatingCustomerRequest",
      dataExtractor = defaultDataExtractor()
    )

  object StartingWithExistingRequest : PayKitMachineStates,
    DefaultDataState<String>(
      "StartingWithExistingRequest",
      dataExtractor = defaultDataExtractor()
    )

  sealed interface ErrorState : PayKitMachineStates {
    object ExceptionState : ErrorState,
      DefaultDataState<Exception>(
        "ExceptionState",
        dataExtractor = defaultDataExtractor()
      )
  }

  sealed interface DecidedState : PayKitMachineStates {
    object Approved : PayKitMachineStates, DefaultFinalDataState<CustomerResponseData>(
      "Approved", dataExtractor = defaultDataExtractor()
    )

    object Declined : DecidedState, DefaultFinalState("Declined")
  }
}