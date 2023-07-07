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

import android.util.Log
import app.cash.paykit.core.android.LOG_TAG

internal class SingleThreadManagerImpl : SingleThreadManager {

  private val threads: MutableMap<ThreadPurpose, Thread?> = mutableMapOf()

  override fun createThread(purpose: ThreadPurpose, runnable: Runnable): Thread {
    // Before creating a new thread of a given type, make sure the last one is interrupted.
    interruptThread(purpose)

    val thread = Thread(runnable, purpose.name)
    threads[purpose] = thread
    return thread
  }

  override fun interruptThread(purpose: ThreadPurpose) {
    try {
      threads[purpose]?.interrupt()
    } catch (e: Exception) {
      Log.e(LOG_TAG, "Failed to interrupt thread: ${purpose.name}", e)
    } finally {
      threads[purpose] = null
    }
  }

  override fun interruptAllThreads() {
    threads.keys.forEach { interruptThread(it) }
  }
}
