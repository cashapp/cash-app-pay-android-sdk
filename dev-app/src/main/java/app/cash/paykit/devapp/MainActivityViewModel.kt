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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

const val sandboxClientID = "CASH_CHECKOUT_SANDBOX"
const val sandboxBrandID = "BRAND_9kx6p0mkuo97jnl025q9ni94t"

const val redirectURI = "cashapppaykit://checkout"

class MainActivityViewModel : ViewModel(), CashAppPayKitListener {

  private val _payKitState = MutableStateFlow<PayKitState>(PayKitState.NotStarted)
  val payKitState: StateFlow<PayKitState> = _payKitState.asStateFlow()

  var currentRequestId: String? = null

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

  fun resetSDK() {
    setupNewSdk()
    _payKitState.value = PayKitState.NotStarted
  }

  private fun setupNewSdk() {
    if (::payKitSdk.isInitialized) {
      payKitSdk.unregisterFromStateUpdates()
    }
    payKitSdk = CashAppPayKitFactory.createSandbox(sandboxClientID)
    payKitSdk.registerForStateUpdates(this@MainActivityViewModel)
  }
}
