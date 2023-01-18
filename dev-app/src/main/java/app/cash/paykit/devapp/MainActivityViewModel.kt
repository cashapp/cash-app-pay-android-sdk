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
import app.cash.paykit.devapp.SDKEnvironments.PRODUCTION
import app.cash.paykit.devapp.SDKEnvironments.SANDBOX
import app.cash.paykit.devapp.SDKEnvironments.STAGING
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

const val sandboxClientID = "CASH_CHECKOUT_SANDBOX"
const val sandboxBrandID = "BRAND_9kx6p0mkuo97jnl025q9ni94t"

const val stagingClientID = "CASH_CHECKOUT"
const val stagingBrandID = "BRAND_4wv02dz5v4eg22b3enoffn6rt"

const val redirectURI = "cashapppaykit://checkout"

enum class SDKEnvironments {
  PRODUCTION,
  SANDBOX,
  STAGING,
}

class MainActivityViewModel : ViewModel(), CashAppPayKitListener {

  private val _payKitState = MutableStateFlow<PayKitState>(PayKitState.NotStarted)
  val payKitState: StateFlow<PayKitState> = _payKitState.asStateFlow()

  var currentRequestId: String? = null

  var currentEnvironment: SDKEnvironments = SANDBOX

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
    if (::payKitSdk.isInitialized) {
      payKitSdk.unregisterFromStateUpdates()
    }
    payKitSdk = when (currentEnvironment) {
      PRODUCTION -> CashAppPayKitFactory.create(stagingClientID)
      SANDBOX -> CashAppPayKitFactory.createSandbox(stagingClientID)
      STAGING -> CashAppPayKitFactory.createStaging(stagingClientID)
    }

    payKitSdk.registerForStateUpdates(this@MainActivityViewModel)
  }
}
