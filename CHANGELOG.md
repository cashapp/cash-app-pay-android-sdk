# NEXT RELEASE | WIP
## Breaking Changes
 - Our internal implementation no longer depends on `kotlinx-datetime`, and now uses Java 8 time classes,
namely `java.time.Instant`.
As a result, if your app or library supports Android versions below API 26, you must enable 
**[coreLibraryDesugaring](https://developer.android.com/studio/write/java8-support-table)** if it isn't already. In most cases, this will be evident at compile time, 
as the build will fail with an error similar to the following:

```
Dependency XYZ requires core library desugaring to be enabled for :your-app-module.
```

 - `CashAppPayButton` (the Cash App Pay–styled button) is **no longer** bundled with the core PayKit SDK.
It has been moved to a separate, **optional dependency**: `group = "app.cash.paykit", name = "ui-views"`.
Note that this is a View-based UI component. See below for the Compose alternative. If you do not use this component,
you do not need to include this dependency in your project.

The package has changed from `app.cash.paykit.core.ui.CashAppPayButton` to `app.cash.paykit.ui.views.CashAppPayButton`; update any import statements or XML references accordingly.

## New

 - Added a new **optional** dependency providing a **Jetpack Compose** version of the Cash App Pay–styled button, 
with the same supported variants: `group = "app.cash.paykit", name = "ui-compose"`. If you do not use this component, 
 you do not need to include this dependency in your project.

**Composable API:**

```kotlin
@Composable
fun CashAppPayButton(
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
  style: CashAppPayButtonStyle = CashAppPayButtonStyle.Default,
  enabled: Boolean = true,
)
```

 - Add new action `ON_FILE_PAYOUT`. A payout allows a merchant to send money to a user's Cash App account.

# 2.6.0
## Breaking Changes
- Introduces new button styles and deprecates existing styles. This is also replaces 
`FollowThemeButtonStyle` with two new options: `FollowThemeButtonStyle.Polychrome` and 
`FollowThemeButtonStyle.Monochrome`. To obtain different variants, 
developers should use the XML `style` attribute to specify the variant they want, as follows:

Polychrome Light Variant:
```xml
<app.cash.paykit.core.ui.CashAppPayButton
      style="@style/CAPButtonStyle.Alt"
      android:layout_height="54dp"
      android:layout_width="match_parent"/>
```

Polychrome Dark Variant:
```xml
<app.cash.paykit.core.ui.CashAppPayButton
      style="@style/CAPButtonStyle.Default"
      android:layout_height="54dp"
      android:layout_width="match_parent"/>
```

Monochrome Light Variant:
```xml
<app.cash.paykit.core.ui.CashAppPayButton
      style="@style/CAPButtonStyle.MonochromeLight"
      android:layout_height="54dp"
      android:layout_width="match_parent"/>
```

Monochrome Dark Variant:
```xml
<app.cash.paykit.core.ui.CashAppPayButton
      style="@style/CAPButtonStyle.MonochromeDark"
      android:layout_height="54dp"
      android:layout_width="match_parent"/>
```

# 2.5.0

 - Fix correct usage of `account_reference_id` in `OnFileAction`
 - Add `reference_id` as a parameter of creating a customer request. This is to bring the Android SDK to parity with iOS and Web.

# 2.4.0

 - Fix: `OnFileAction` param `accountReferenceId` is now properly being sent over the network
 - Update Android target SDK version to API 33
 - Update internal dependencies to recent versions, ensuring strong backward compatibility support

# 2.3.0

 - The class `CashAppPayInitializer` was made open, so that androidx.startup can be manually overridden.
 - This version bundles fixes for minify enabled builds.
 - Update internal dependency on `OkHttp` to version `4.11.0`.
 
## Breaking Changes
 
 - Renamed class `CashAppCashAppPayApiNetworkException` to `CashAppPayApiNetworkException`
 - This version contains a change to the bundled Cash App Pay button.
Previously, `light` and `dark` variants of the button were made possible by using 2 different
views, respectively `CashAppPayButtonLight` an `CashAppPayButtonDark`. As of this version, the
there will only be a single `CashAppPayButton` view, which has been updated to support both variants.
To obtain different variants, developers should use the XML `style` attribute to specify the variant they want, as follows:


Light Variant:
```xml
<app.cash.paykit.core.ui.CashAppPayButton
      style="@style/CAPButtonStyle.Light"
      android:layout_height="54dp"
      android:layout_width="match_parent"/>
```

Dark Variant:
```xml
<app.cash.paykit.core.ui.CashAppPayButton
      style="@style/CAPButtonStyle.Dark"
      android:layout_height="54dp"
      android:layout_width="match_parent"/>
```

This change makes it possible for developer to use the button in a more flexible way, such as using
a style that changes accordingly to the OS theme.

You should migrate any instances of `CashAppPayButtonLight` and `CashAppPayButtonDark` to `CashAppPayButton`.

# 2.2.1

Here's what has changed on this release:

 - Information that can be considered PII is now marked as such by implementing the interface `PiiContent`.
   - This is possibly a breaking change, but in practice for the majority of people out there, these shouldn't be properties that you're using.
  - Improved Thread management to prevent memory leaks and unexpected behavior.


# 2.2.0

In this release of our open source SDK, we've made a significant update that involves modifying the date types within our returned payloads from string to Instant. Please note that this modification is a breaking change. Here's what has been altered:

 - The dates in the models have been transitioned from `string` to `Instant`.
 - Metrics timestamps are now accurately recorded in `Epoch usec`.
 - In situations where the auth flow token has expired, it will now be automatically refreshed, thereby ensuring a successful request.
 - We've added a new state: `Refreshing`. This state is triggered exclusively when the auth flow token needs a refresh during a customer authorization attempt. Typically, this state acts as a bridge between `Authorizing` and `PollingTransactionStatus` states.


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
