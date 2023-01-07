package app.cash.paykit.core

import app.cash.paykit.core.models.response.CustomerResponseData

/**
 * Various states Cash App PayKit SDK might be in depending the stage of the transaction process.
 *
 */
sealed interface PayKitState {

  /**
   * This is the initial PayKit State.
   */
  object NotStarted : PayKitState

  /**
   * This state denotes that a the Create Customer Request has been started. If successful, this state
   * will transition to [ReadyToAuthorize].
   */
  object CreatingCustomerRequest : PayKitState

  /**
   * This state denotes that a the Update Customer Request has been started. If successful, this state
   * will transition to [ReadyToAuthorize].
   */
  object UpdatingCustomerRequest : PayKitState

  /**
   * This state denotes that a valid Customer Request exists, and we're ready to authorize upon
   * user action.
   */
  class ReadyToAuthorize(val responseData: CustomerResponseData) : PayKitState

  /**
   * This state denotes that we've entered the process of authorizing an existing customer request.
   * This state will transition to [PollingTransactionStatus].
   */
  object Authorizing : PayKitState

  /**
   * This state denotes that we're actively polling for an authorization update. This state will
   * typically transition to either [Approved] or [Declined].
   */
  object PollingTransactionStatus : PayKitState

  /**
   * Terminal state denoting that the request authorization was approved.
   */
  class Approved(val responseData: CustomerResponseData) : PayKitState

  /**
   * Terminal state denoting that the request authorization was declined.
   */
  object Declined : PayKitState

  /**
   * Terminal state that can happen as a result of most states, indicates that an exception has
   * occurred.
   * This state is typically unrecoverable, and it is advised to restart the process from scratch in
   * case it is met.
   */
  class PayKitException(val exception: Exception) : PayKitState
}
