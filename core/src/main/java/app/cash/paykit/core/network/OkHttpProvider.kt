package app.cash.paykit.core.network

import okhttp3.OkHttpClient
import okhttp3.Protocol
import java.util.concurrent.TimeUnit.MILLISECONDS

internal object OkHttpProvider {

  fun provideOkHttpClient(): OkHttpClient {
    return OkHttpClient.Builder()
      .connectTimeout(DEFAULT_NETWORK_TIMEOUT_MILLISECONDS, MILLISECONDS)
      .callTimeout(DEFAULT_NETWORK_TIMEOUT_MILLISECONDS, MILLISECONDS)
      .readTimeout(DEFAULT_NETWORK_TIMEOUT_MILLISECONDS, MILLISECONDS)
      .writeTimeout(DEFAULT_NETWORK_TIMEOUT_MILLISECONDS, MILLISECONDS)
      .protocols(listOf(Protocol.HTTP_1_1))
      .followRedirects(true)
      .followSslRedirects(true)
      .build()
  }

  const val DEFAULT_NETWORK_TIMEOUT_MILLISECONDS = 60_000L
}
