# 1.0.7

 - Breaking change: `authorizeCustomerRequest` no longer requires a `context` to be passed as a parameter
 - Breaking change: several class names have changed to better align across platforms. The gist of it, is that `PayKit` becomes `CashAppPay`

Class renaming correspondence:

`PayKitState` -> `CashAppPayState`
`PayKitExceptionState` -> `CashAppPayExceptionState`
`PayKitCurrency` -> `CashAppPayCurrency`
`PayKitPaymentAction` -> `CashAppPayPaymentAction`
`CashAppPayKit` -> `CashAppPay`
`CashAppPayKitFactory` -> `CashAppPayFactory`
`CashAppPayKitListener` -> `CashAppPayListener`
`CashPayKitLightButton` -> `CashAppPayLightButton`
`CashPayKitDarkButton` -> `CashAppPayDarkButton`