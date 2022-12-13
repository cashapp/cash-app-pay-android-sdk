package com.squareup.cash.paykit

import com.squareup.cash.paykit.RequestType.GET
import com.squareup.cash.paykit.RequestType.POST
import com.squareup.cash.paykit.models.common.Action
import com.squareup.cash.paykit.models.request.CreateCustomerRequest
import com.squareup.cash.paykit.models.request.CustomerRequestData
import com.squareup.cash.paykit.models.response.CustomerTopLevelResponse
import com.squareup.cash.paykit.models.sdk.PayKitPaymentAction
import com.squareup.cash.paykit.models.sdk.PayKitPaymentAction.OnFileAction
import com.squareup.cash.paykit.models.sdk.PayKitPaymentAction.OneTimeAction
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.adapter
import java.io.BufferedOutputStream
import java.io.BufferedWriter
import java.io.IOException
import java.io.OutputStream
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import java.util.UUID

private const val BASE_URL_SANDBOX = "https://sandbox.api.cash.app/customer-request/v1/"
private const val BASE_URL_RELEASE = "https://api.cash.app/customer-request/v1/"
private const val BASE_URL = BASE_URL_SANDBOX
private const val CREATE_CUSTOMER_REQUEST_ENDPOINT = "${BASE_URL}requests"
private const val RETRIEVE_EXISTING_REQUEST_ENDPOINT = "${BASE_URL}requests/"

enum class RequestType {
  GET,
  POST,
  PATCH
}

// TODO: Define more sensible network timeouts. ( https://www.notion.so/cashappcash/Define-network-timeouts-19ca85a1f3d7496bad8174be612304e9 )

object NetworkManager {

  private const val CHANNEL_IN_APP = "IN_APP"
  private const val PAYMENT_TYPE_ONE_TIME = "ONE_TIME_PAYMENT"
  private const val PAYMENT_TYPE_ON_FILE = "ON_FILE_PAYMENT"

  @Throws(IOException::class)
  fun createCustomerRequest(
    clientId: String,
    paymentAction: PayKitPaymentAction
  ): CustomerTopLevelResponse {
    return when (paymentAction) {
      is OnFileAction -> onFilePaymentCustomerRequest(clientId, paymentAction)
      is OneTimeAction -> oneTimePaymentCustomerRequest(clientId, paymentAction)
    }
  }

  fun retrieveUpdatedRequestData(clientId: String, requestId: String): CustomerTopLevelResponse {
    return executeNetworkRequest(
      GET,
      RETRIEVE_EXISTING_REQUEST_ENDPOINT + requestId,
      clientId,
      null
    )
  }

  private fun onFilePaymentCustomerRequest(
    clientId: String,
    paymentAction: OnFileAction
  ): CustomerTopLevelResponse {
    // Create request data.
    val scopeIdOrClientId = paymentAction.scopeId ?: clientId
    val requestAction =
      Action(
        scopeId = scopeIdOrClientId,
        type = PAYMENT_TYPE_ON_FILE
      )
    val requestData = CustomerRequestData(
      actions = listOf(requestAction),
      channel = CHANNEL_IN_APP,
      redirectUri = paymentAction.redirectUri
    )
    val createCustomerRequest = CreateCustomerRequest(
      idempotencyKey = UUID.randomUUID().toString(),
      customerRequestData = requestData
    )

    return executeNetworkRequest(
      POST,
      CREATE_CUSTOMER_REQUEST_ENDPOINT,
      clientId,
      createCustomerRequest
    )
  }

  private fun oneTimePaymentCustomerRequest(
    clientId: String,
    paymentAction: OneTimeAction
  ): CustomerTopLevelResponse {
    // Create request data.
    val scopeIdOrClientId = paymentAction.scopeId ?: clientId
    val requestAction =
      Action(
        amount_cents = paymentAction.amount,
        currency = paymentAction.currency?.backendValue,
        scopeId = scopeIdOrClientId,
        type = PAYMENT_TYPE_ONE_TIME
      )
    val requestData = CustomerRequestData(
      actions = listOf(requestAction),
      channel = CHANNEL_IN_APP,
      redirectUri = paymentAction.redirectUri
    )
    val createCustomerRequest = CreateCustomerRequest(
      idempotencyKey = UUID.randomUUID().toString(),
      customerRequestData = requestData
    )

    return executeNetworkRequest(
      POST,
      CREATE_CUSTOMER_REQUEST_ENDPOINT,
      clientId,
      createCustomerRequest
    )
  }

  @Throws(IOException::class)
  @OptIn(ExperimentalStdlibApi::class)
  /**
   * POST Request.
   * @param In Class for serializing the request
   * @param Out Class for deserializing the response
   * @param clientId Client ID for authenticating the request
   * @param requestPayload Request payload, an instance of the `In` class.
   */
  private inline fun <reified In : Any, reified Out : Any> executeNetworkRequest(
    requestType: RequestType,
    endpointUrl: String,
    clientId: String,
    requestPayload: In?
  ): Out {
    val url = URL(endpointUrl)
    val urlConnection: HttpURLConnection = url.openConnection() as HttpURLConnection
    urlConnection.requestMethod = requestType.name
    urlConnection.setRequestProperty("Content-Type", "application/json")
    urlConnection.setRequestProperty("Authorization", "Client $clientId")

    if (requestType == POST) {
      urlConnection.doOutput = true
      urlConnection.setChunkedStreamingMode(0)
    }

    try {
      val moshi: Moshi = Moshi.Builder().build()

      if (requestPayload != null) {
        val outStream: OutputStream = BufferedOutputStream(urlConnection.outputStream)
        val writer = BufferedWriter(
          OutputStreamWriter(
            outStream, "UTF-8"
          )
        )

        val requestJsonAdapter: JsonAdapter<In> = moshi.adapter()
        val jsonData: String = requestJsonAdapter.toJson(requestPayload)
        writer.write(jsonData)
        writer.flush()
      }

      val code = urlConnection.responseCode
      if (code != HttpURLConnection.HTTP_CREATED && code != HttpURLConnection.HTTP_OK) {
        throw IOException("Invalid response code from server: $code")
      }

      // TODO: Could probably leverage OKIO to improve this code. ( https://www.notion.so/cashappcash/Would-okio-benefit-the-low-level-network-handling-b8f55044c1e249a995f544f1f9de3c4a )
      urlConnection.inputStream.use { inputStream ->
        inputStream.bufferedReader().use { buffered ->
          val responseLines = buffered.readLines()
          val sb = StringBuilder()
          responseLines.forEach { sb.append(it) }
          val responseJson = sb.toString()

          val jsonAdapterResponse: JsonAdapter<Out> = moshi.adapter()

          val responseModel = jsonAdapterResponse.fromJson(responseJson)
          return responseModel ?: throw IOException("Failed to deserialize response data")
        }
      }
    } finally {
      urlConnection.disconnect()
    }
  }
}
