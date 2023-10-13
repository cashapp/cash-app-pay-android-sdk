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

package app.cash.paykit.core.impl

import android.util.Log
import app.cash.paykit.CleanupOnExitFunction
import app.cash.paykit.ClientEventPayload.CreatingCustomerRequestData
import app.cash.paykit.EventSender
import app.cash.paykit.NetworkingWorker
import app.cash.paykit.PayKitEvents.CreateCustomerRequest
import app.cash.paykit.core.NetworkManager
import app.cash.paykit.models.common.NetworkResult
import app.cash.paykit.models.common.NetworkResult.Failure

internal class NativeNetworkWorker(
  private val networkManager: NetworkManager,
  private val clientId: String
) : NetworkingWorker {
  override fun createCustomerRequest(
    data: CreatingCustomerRequestData,
    eventSender: EventSender
  ): CleanupOnExitFunction {

    Log.d("CRAIG", "creating CR $data")
    val thread = Thread {
      try {
        when (val networkResult =
          networkManager.createCustomerRequest(
            clientId = clientId,
            paymentActions = data.actions,
            redirectUri = data.redirectUri,
          ).also {
            Log.d("CRAIG", "got response $it")
          }
        ) {

          is Failure -> {
            eventSender.send(CreateCustomerRequest.Error(networkResult.exception))
          }

          is NetworkResult.Success -> {
            val customerResponseData = networkResult.data.customerResponseData
            eventSender.send(CreateCustomerRequest.Success(customerResponseData))
          }
        }
      } catch (e: Exception) {
        eventSender.send(CreateCustomerRequest.Error(e))
      }
    }.apply {
      name = "createCustomerRequest"
      start()
    }

    return {
      try {
        thread.interrupt()
      } catch (e: SecurityException) {

      }
    }
  }
}