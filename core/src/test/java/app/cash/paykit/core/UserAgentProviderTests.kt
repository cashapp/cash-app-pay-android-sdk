package app.cash.paykit.core

import app.cash.paykit.core.utils.UserAgentProvider
import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment

@RunWith(RobolectricTestRunner::class)
class UserAgentProviderTests {

  @Test
  fun testUserAgentFormat() {
    val userAgent = UserAgentProvider.provideUserAgent(RuntimeEnvironment.getApplication())
    // Example of what Robolectric env. will produce: "app.cash.paykit.core.test (Android 12; robolectric; robolectric; robolectric; en_US) PayKitVersion/0.0.6-SNAPSHOT".
    assertThat(userAgent).contains("app.cash.paykit.core.test")
    assertThat(userAgent).containsMatch("Android ..")
    assertThat(userAgent).containsMatch("PayKitVersion/.")
    assertThat(userAgent).contains("robolectric")
    assertThat(userAgent).contains("en_US")
  }
}
