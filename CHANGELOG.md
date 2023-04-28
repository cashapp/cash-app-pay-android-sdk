# 2.1.0

This version introduces a concrete type for `GrantType` under the `Grant` class. Before this field was a `string`. 
This is a breaking change. The following has changed:

- `Grant.type` from `string` to `GrantType` <br/>
- Possible `GrantType` values: `ONE_TIME`, `EXTENDED`, `UNKNOWN`. These values match their spelling with what is described by our public API.
- For convenience, `ONE_TIME` applies to a grant can only be used once, where `EXTENDED` applies to grants that can be repeatedly used.
- `CashAppPayPaymentAction` no longer contains the `redirectUri` parameter. Instead, pass that value to the `createCustomerRequest` function.
- Fixes to the behavior of `startWithExistingCustomerRequest`


# 2.0.0

This version introduces support for multiple `CashAppPayPaymentAction` per `createCustomerRequest`.
This is a breaking change. The following functions have changed:

 - `createCustomerRequest(paymentAction: CashAppPayPaymentAction)` to `createCustomerRequest(paymentAction: CashAppPayPaymentAction, redirectUri: String?)` <br/>
 - `CashAppPayPaymentAction` no longer contains the `redirectUri` parameter. Instead pass that value to the `createCustomerRequest` function.

And the following functions were introduced:

 - `createCustomerRequest(paymentActions: List<CashAppPayPaymentAction>, redirectUri: String?)`
 - `updateCustomerRequest(requestId: String, paymentActions: List<CashAppPayPaymentAction>)`

# 1.0.8

 - Added the property `environment` to internal analytics.

# 1.0.7

 - Breaking change: `authorizeCustomerRequest` no longer requires a `context` to be passed as a parameter.
 - Breaking change: Several class names have changed to be consistent across platforms. Which means that we have changed `PayKit` to `CashAppPay`.

   Classes that are renamed are:

   `PayKitState` to `CashAppPayState` <br/>
   `PayKitExceptionState` to `CashAppPayExceptionState` <br/>
   `PayKitCurrency` to `CashAppPayCurrency` <br/>
   `PayKitPaymentAction` to `CashAppPayPaymentAction` <br/>
   `CashAppPayKit` to `CashAppPay` <br/>
   `CashAppPayKitFactory` to `CashAppPayFactory` <br/>
   `CashAppPayKitListener` to `CashAppPayListener` <br/>
   `CashPayKitLightButton` to `CashAppPayLightButton` <br/>
   `CashPayKitDarkButton` to `CashAppPayDarkButton` <br/>
