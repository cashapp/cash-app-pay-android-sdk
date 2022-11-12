package com.squareup.cash.paykit

import com.squareup.cash.paykit.models.common.Action
import com.squareup.cash.paykit.models.request.CreateCustomerRequest
import com.squareup.cash.paykit.models.request.CustomerRequestData
import com.squareup.cash.paykit.models.response.CreateCustomerResponse
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
private const val REQUESTS_ENDPOINT = "${BASE_URL}requests"

object NetworkManager {

  @Throws(IOException::class)
  @OptIn(ExperimentalStdlibApi::class) fun createCustomerRequest(
    clientId: String,
    scopeId: String
  ): CreateCustomerResponse {
    // Create request data.
    val requestAction =
      Action(amount_cents = 500, currency = "USD", scopeId = scopeId, type = "ONE_TIME_PAYMENT")
    val requestData = CustomerRequestData(actions = listOf(requestAction), channel = "IN_APP")
    val createCustomerRequest = CreateCustomerRequest(
      idempotencyKey = UUID.randomUUID().toString(),
      customerRequestData = requestData
    )

    val url = URL(REQUESTS_ENDPOINT)
    val urlConnection: HttpURLConnection = url.openConnection() as HttpURLConnection
    urlConnection.requestMethod = "POST"
    urlConnection.setRequestProperty("Content-Type", "application/json")
    urlConnection.setRequestProperty("Authorization", "Client $clientId")
    urlConnection.doOutput = true
    urlConnection.setChunkedStreamingMode(0)

    try {
      val out: OutputStream = BufferedOutputStream(urlConnection.outputStream)
      val writer = BufferedWriter(
        OutputStreamWriter(
          out, "UTF-8"
        )
      )

      val moshi: Moshi = Moshi.Builder().build()
      val requestJsonAdapter: JsonAdapter<CreateCustomerRequest> = moshi.adapter()
      val jsonData: String = requestJsonAdapter.toJson(createCustomerRequest)

      writer.write(jsonData)
      writer.flush()

      val code = urlConnection.responseCode
      if (code != HttpURLConnection.HTTP_CREATED) {
        throw IOException("Invalid response code from server: $code")
      }

      // TODO: Could probably leverage OKIO to make this better.
      urlConnection.inputStream.use { inputStream ->
        inputStream.bufferedReader().use { buffered ->
          val responseLines = buffered.readLines()
          val sb = StringBuilder()
          responseLines.forEach { sb.append(it) }
          val responseJson = sb.toString()

          val jsonAdapterResponse: JsonAdapter<CreateCustomerResponse> = moshi.adapter()

          val responseModel = jsonAdapterResponse.fromJson(responseJson)
          return responseModel ?: throw IOException("Failed to deserialize response data")
        }
      }
    } finally {
      urlConnection.disconnect()
    }
  }
}