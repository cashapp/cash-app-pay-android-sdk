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

package app.cash.paykit.core.models.common

/**
 * This class wraps all I/O-related requests in one of 2 states: [Success] or [Failure].
 */
internal sealed interface NetworkResult<in T> {

  class Failure<T>(val exception: Exception) : NetworkResult<T>

  class Success<T>(val data: T) : NetworkResult<T>

  companion object {
    fun <T> success(data: T): NetworkResult<T> = Success(data)

    fun <T> failure(exception: Exception): NetworkResult<T> =
      Failure(exception)
  }
}
