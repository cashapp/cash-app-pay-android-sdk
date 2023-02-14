/*
 * Copyright (C) 2023 Cash App
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package app.cash.paykit.core.android

import android.content.Context
import java.lang.ref.WeakReference

/**
 * Singleton that holds onto our application [Context] as a [WeakReference]
 */
internal object ApplicationContextHolder {
  private var isInitialized: Boolean = false

  private lateinit var applicationContextReference: WeakReference<Context>

  fun init(applicationContext: Context) {
    if (isInitialized) {
      return
    }
    isInitialized = true
    applicationContextReference = WeakReference(applicationContext.applicationContext)
  }

  val applicationContext: Context
    get() = applicationContextReference.get()!!
}
