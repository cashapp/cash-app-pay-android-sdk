package app.cash.paykit.devapp

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.cash.paykit.core.CashAppPayKit
import app.cash.paykit.core.CashAppPayKitFactory
import app.cash.paykit.core.CashAppPayKitListener
import app.cash.paykit.core.PayKitState
import app.cash.paykit.core.PayKitState.ReadyToAuthorize
import app.cash.paykit.core.models.sdk.PayKitPaymentAction
import app.cash.paykit.devapp.SDKEnvironments.SANDBOX
import app.cash.paykit.devapp.SDKEnvironments.STAGING
import com.chuckerteam.chucker.api.ChuckerInterceptor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient

private const val sandboxClientID = "CASH_CHECKOUT_SANDBOX"
private const val sandboxBrandID = "BRAND_9kx6p0mkuo97jnl025q9ni94t"

private const val stagingClientID = "CASH_CHECKOUT"
private const val stagingBrandID = "BRAND_4wv02dz5v4eg22b3enoffn6rt"

const val redirectURI = "cashapppaykit://checkout"

enum class SDKEnvironments {
  SANDBOX,
  STAGING,
}

class MainActivityViewModel : ViewModel(), CashAppPayKitListener {

  private val BASE_URL_STAGING = "https://api.cashstaging.app/customer-request/v1/"

  private val _payKitState = MutableStateFlow<PayKitState>(PayKitState.NotStarted)
  val payKitState: StateFlow<PayKitState> = _payKitState.asStateFlow()

  var currentRequestId: String? = null

  var currentEnvironment: SDKEnvironments = SANDBOX

  lateinit var clientId: String
  var brandId: String? = null

  private lateinit var payKitSdk: CashAppPayKit

  init {
    setupNewSdk()
  }

  override fun onCleared() {
    super.onCleared()
    payKitSdk.unregisterFromStateUpdates()
  }

  override fun payKitStateDidChange(newState: PayKitState) {
    if (newState is ReadyToAuthorize) {
      currentRequestId = newState.responseData.id
    }
    _payKitState.value = newState
  }

  fun createCustomerRequest(paymentAction: PayKitPaymentAction) {
    viewModelScope.launch(Dispatchers.IO) {
      payKitSdk.createCustomerRequest(paymentAction)
    }
  }

  fun updateCustomerRequest(paymentAction: PayKitPaymentAction) {
    val requestId = currentRequestId ?: return
    viewModelScope.launch(Dispatchers.IO) {
      payKitSdk.updateCustomerRequest(requestId, paymentAction)
    }
  }

  fun authorizeCustomerRequest(context: Context) {
    payKitSdk.authorizeCustomerRequest(context)
  }

  fun retrieveExistingRequest(requestId: String) {
    viewModelScope.launch(Dispatchers.IO) {
      payKitSdk.startWithExistingCustomerRequest(requestId)
    }
  }

  fun resetSDK() {
    setupNewSdk()
    _payKitState.value = PayKitState.NotStarted
  }

  private fun setupNewSdk() {
    // Override SDK's OkHttpClient to inject Chucker Interceptor.
    val chuckerInterceptor = ChuckerInterceptor.Builder(DevApplication.instance).build()
    val okHttpClient = OkHttpClient.Builder()
      .addInterceptor(chuckerInterceptor)
    val defaultOkHttpClientField =
      CashAppPayKitFactory::class.java.getDeclaredField("defaultOkHttpClient")
    defaultOkHttpClientField.isAccessible = true
    defaultOkHttpClientField.set(CashAppPayKitFactory, okHttpClient.build())

    if (::payKitSdk.isInitialized) {
      payKitSdk.unregisterFromStateUpdates()
    }
    payKitSdk = when (currentEnvironment) {
      SANDBOX -> {
        clientId = sandboxClientID
        brandId = sandboxBrandID
        setAnalyticsEndpoint("https://api.squareup.com/")
        CashAppPayKitFactory.createSandbox(clientId)
      }

      STAGING -> {
        // Change internal production URL via Reflection, to act as staging.
        val baseUrlProd = CashAppPayKitFactory::class.java.getDeclaredField("BASE_URL_PRODUCTION")
        baseUrlProd.isAccessible = true
        baseUrlProd.set(CashAppPayKitFactory, BASE_URL_STAGING)

        // Change Analytics endpoint to Staging.
        setAnalyticsEndpoint("https://api.squareupstaging.com/")

        clientId = stagingClientID
        brandId = stagingBrandID
        CashAppPayKitFactory.create(clientId)
      }
    }

    payKitSdk.registerForStateUpdates(this@MainActivityViewModel)
  }

  private fun setAnalyticsEndpoint(analyticsBaseUrl: String) {
    val baseUrlProd = CashAppPayKitFactory::class.java.getDeclaredField("ANALYTICS_BASE_URL")
    baseUrlProd.isAccessible = true
    baseUrlProd.set(CashAppPayKitFactory, analyticsBaseUrl)
  }
}
