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
package app.cash.paykit.core.android

import android.util.Log

/**
 * This class is used to wrap a thread start operation in a way that allows for smooth degradation on exception, as well as convenient and consistent error handling.
 */
fun Thread.safeStart(errorMessage: String?, onError: () -> Unit? = {}) {
  try {
    start()
  } catch (e: IllegalThreadStateException) {
    // This can happen if the thread is already started.
    Log.e("CAP", errorMessage, e)
    onError()
  } catch (e: InterruptedException) {
    Log.e("CAP", errorMessage, e)
    onError()
  }
}
