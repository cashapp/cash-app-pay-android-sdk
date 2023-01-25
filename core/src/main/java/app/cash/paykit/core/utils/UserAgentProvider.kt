package app.cash.paykit.core.utils

import android.content.Context
import android.os.Build
import app.cash.paykit.core.R
import java.util.Locale

internal object UserAgentProvider {

  fun provideUserAgent(context: Context): String {
    /**
     * User Agent:
     * BuildConfig.APPLICATION_ID
     * (Android XY
     * Build.VERSION.RELEASE
     * Build.MANUFACTURER
     * Build.BRAND
     * Build.MODEL
     * Locale.getDefault() )
     * PayKitVersion/[PayKit Version]
     */
    val stb = StringBuilder(context.packageName)
    stb.append(" (")
    stb.append("Android ")
    stb.append(Build.VERSION.RELEASE)
    stb.append("; ")
    stb.append(Build.MANUFACTURER)
    stb.append("; ")
    stb.append(Build.BRAND)
    stb.append("; ")
    stb.append(Build.MODEL)
    stb.append("; ")
    stb.append(Locale.getDefault())
    stb.append(") PayKitVersion/")
    stb.append(context.getString(R.string.cashpaykit_version))

    return stb.toString()
  }
}
