package app.cash.paykit.core.impl

import AnalyticsEvent
import AnalyticsRequest
import android.util.Log
import app.cash.paykit.core.NetworkManager
import app.cash.paykit.core.exceptions.PayKitApiNetworkException
import app.cash.paykit.core.exceptions.PayKitConnectivityNetworkException
import app.cash.paykit.core.impl.RequestType.GET
import app.cash.paykit.core.impl.RequestType.PATCH
import app.cash.paykit.core.impl.RequestType.POST
import app.cash.paykit.core.models.analytics.AnalyticsResponse
import app.cash.paykit.core.models.common.NetworkResult
import app.cash.paykit.core.models.common.NetworkResult.Failure
import app.cash.paykit.core.models.common.NetworkResult.Success
import app.cash.paykit.core.models.request.CreateCustomerRequest
import app.cash.paykit.core.models.request.CustomerRequestDataFactory
import app.cash.paykit.core.models.response.ApiErrorResponse
import app.cash.paykit.core.models.response.CustomerTopLevelResponse
import app.cash.paykit.core.models.sdk.PayKitPaymentAction
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.JsonEncodingException
import com.squareup.moshi.Moshi
import com.squareup.moshi.adapter
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException
import java.net.HttpURLConnection
import java.net.SocketTimeoutException
import java.util.UUID

enum class RequestType {
  GET,
  POST,
  PATCH,
}

internal class NetworkManagerImpl(
  private val baseUrl: String,
  private val userAgentValue: String,
  private val okHttpClient: OkHttpClient,
) : NetworkManager {

  // TODO: Generic network calls retry logic. ( https://www.notion.so/cashappcash/Generic-Retry-logic-for-all-network-requests-2fce583bb4154476835af908c8688995 )

  val CREATE_CUSTOMER_REQUEST_ENDPOINT: String
    get() = "${baseUrl}requests"

  val RETRIEVE_EXISTING_REQUEST_ENDPOINT: String
    get() = "${baseUrl}requests/"

  val UPDATE_CUSTOMER_REQUEST_ENDPOINT: String
    get() = "${baseUrl}requests/"

  @Throws(IOException::class)
  override fun createCustomerRequest(
    clientId: String,
    paymentAction: PayKitPaymentAction,
  ): NetworkResult<CustomerTopLevelResponse> {
    val customerRequestData = CustomerRequestDataFactory.build(clientId, paymentAction)
    val createCustomerRequest = CreateCustomerRequest(
      idempotencyKey = UUID.randomUUID().toString(),
      customerRequestData = customerRequestData,
    )

    return executeNetworkRequest(
      POST,
      CREATE_CUSTOMER_REQUEST_ENDPOINT,
      clientId,
      createCustomerRequest,
    )
  }

  @Throws(IOException::class)
  override fun updateCustomerRequest(
    clientId: String,
    requestId: String,
    paymentAction: PayKitPaymentAction,
  ): NetworkResult<CustomerTopLevelResponse> {
    val customerRequestData =
      CustomerRequestDataFactory.build(clientId, paymentAction, isRequestUpdate = true)
    val createCustomerRequest = CreateCustomerRequest(
      customerRequestData = customerRequestData,
    )
    return executeNetworkRequest(
      PATCH,
      UPDATE_CUSTOMER_REQUEST_ENDPOINT + requestId,
      clientId,
      createCustomerRequest,
    )
  }

  override fun retrieveUpdatedRequestData(
    clientId: String,
    requestId: String,
  ): NetworkResult<CustomerTopLevelResponse> {
    return executeNetworkRequest(
      GET,
      RETRIEVE_EXISTING_REQUEST_ENDPOINT + requestId,
      clientId,
      null,
    )
  }

  override fun uploadAnalyticsEvents(clientId: String, analyticEvents: List<AnalyticsEvent>) {
    // TODO: ClientId is not strictly necessary, remove it!
    val analyticsRequest = AnalyticsRequest(analyticEvents)
    val response: NetworkResult<AnalyticsResponse> = executeNetworkRequest(
      POST,
      ANALYTICS_PROD_ENDPOINT,
      clientId,
      analyticsRequest,
    )

    // TODO: Temporary log, will be removed.
    when (response) {
      is Failure -> Log.e("ANALYTICS", "Failed upload, got: ${response.exception}")
      is Success -> Log.v("ANALYTICS", "Success! Got: ${response.data}")
    }
  }

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
    requestPayload: In?,
  ): NetworkResult<Out> {
    val requestBuilder = Request.Builder()
      .url(endpointUrl)
      .addHeader("Content-Type", "application/json")
      .addHeader("Accept", "application/json")
      .addHeader("Authorization", "Client $clientId")
      .addHeader("User-Agent", userAgentValue)

    val moshi: Moshi = Moshi.Builder().build()
    val requestJsonAdapter: JsonAdapter<In> = moshi.adapter()
    val jsonData: String = requestJsonAdapter.toJson(requestPayload)

    when (requestType) {
      POST -> {
        val requestBody = jsonData.toRequestBody("application/json; charset=utf-8".toMediaType())
        requestBuilder.post(requestBody)
      }

      PATCH -> {
        val requestBody = jsonData.toRequestBody("application/json; charset=utf-8".toMediaType())
        requestBuilder.patch(requestBody)
      }

      GET -> {
        requestBuilder.get()
      }
    }

    okHttpClient.newCall(requestBuilder.build()).execute().use { response ->
      if (!response.isSuccessful) {
        // Unsuccessfully response is handled here.
        if (response.code != HttpURLConnection.HTTP_CREATED && response.code != HttpURLConnection.HTTP_OK) {
          // Under normal circumstances:
          //  - 3xx errors won’t have a payload.
          //  - 4xx are guaranteed to have a payload.
          //  - 5xx should have a payload, but there might be situations where they do not.
          //
          // So as a result our logic here is : use the payload if it exists, otherwise simply propagate the error code.
          val apiErrorResponse: NetworkResult<ApiErrorResponse> =
            deserializeResponse(response.body?.toString() ?: "", moshi)
          return when (apiErrorResponse) {
            is Failure -> NetworkResult.failure(
              PayKitConnectivityNetworkException(apiErrorResponse.exception),
            )

            is Success -> {
              val apiError = apiErrorResponse.data.apiErrors.first()
              val apiException = PayKitApiNetworkException(
                apiError.category,
                apiError.code,
                apiError.detail,
                apiError.field_value,
              )
              NetworkResult.failure(apiException)
            }
          }
        }
      }

      // Success continues here.
      return deserializeResponse(response.body!!.string(), moshi)
    }
  }

  @OptIn(ExperimentalStdlibApi::class)
  private inline fun <reified Out : Any> deserializeResponse(
    responseString: String,
    moshi: Moshi,
  ): NetworkResult<Out> {
    try {
      val jsonAdapterResponse: JsonAdapter<Out> = moshi.adapter()

      val responseModel = jsonAdapterResponse.fromJson(responseString)
      if (responseModel != null) {
        return NetworkResult.success(responseModel)
      }
      return NetworkResult.failure(IOException("Failed to deserialize response data."))
    } catch (e: SocketTimeoutException) {
      return NetworkResult.failure(PayKitConnectivityNetworkException(e))
    } catch (e: JsonEncodingException) {
      return NetworkResult.failure(e)
    } catch (e: Exception) {
      return NetworkResult.failure(e)
    }
  }

  companion object {
    private const val ANALYTICS_SERVICE_SUFFIX = "2.0/log/eventstream"
    const val ANALYTICS_STAGING_ENDPOINT =
      "https://api.squareupstaging.com/$ANALYTICS_SERVICE_SUFFIX"
    const val ANALYTICS_PROD_ENDPOINT = "https://api.squareup.com/$ANALYTICS_SERVICE_SUFFIX"
  }
}
