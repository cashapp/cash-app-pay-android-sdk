package app.cash.paykit.core.network

import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit.MILLISECONDS

internal object OkHttpProvider {

  fun provideOkHttpClient(): OkHttpClient {
    return OkHttpClient.Builder()
      .connectTimeout(DEFAULT_NETWORK_TIMEOUT_MILLISECONDS, MILLISECONDS)
      .callTimeout(DEFAULT_NETWORK_TIMEOUT_MILLISECONDS, MILLISECONDS)
      .readTimeout(DEFAULT_NETWORK_TIMEOUT_MILLISECONDS, MILLISECONDS)
      .writeTimeout(DEFAULT_NETWORK_TIMEOUT_MILLISECONDS, MILLISECONDS)
      .build()
  }

  private const val DEFAULT_NETWORK_TIMEOUT_MILLISECONDS = 60_000L
}
