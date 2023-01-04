package com.squareup.cash.paykit.devapp

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.squareup.cash.paykit.CashAppPayKit
import com.squareup.cash.paykit.CashAppPayKitListener
import com.squareup.cash.paykit.PayKitState
import com.squareup.cash.paykit.models.sdk.PayKitPaymentAction
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

  private val payKitSdk = CashAppPayKit(sandboxClientID, useSandboxEnvironment = true)

  init {
    payKitSdk.registerForStateUpdates(this@MainActivityViewModel)
  }

  override fun onCleared() {
    super.onCleared()
    payKitSdk.unregisterFromStateUpdates()
  }

  override fun payKitStateDidChange(newState: PayKitState) {
    _payKitState.value = newState
  }

  fun createCustomerRequest(paymentAction: PayKitPaymentAction) {
    viewModelScope.launch(Dispatchers.IO) {
      payKitSdk.createCustomerRequest(paymentAction)
    }
  }

  fun authorizeCustomerRequest(context: Context) {
    payKitSdk.authorizeCustomerRequest(context)
  }
}
