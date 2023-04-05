# 2.0.0

 - 

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
