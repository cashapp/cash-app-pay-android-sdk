/*
 * Copyright (C) 2023 Cash App
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package app.cash.paykit.core.utils

import android.content.Context
import android.os.Build
import app.cash.paykit.core.R
import java.util.*

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
    stb.append(context.getString(R.string.cap_version))

    return stb.toString()
  }
}
