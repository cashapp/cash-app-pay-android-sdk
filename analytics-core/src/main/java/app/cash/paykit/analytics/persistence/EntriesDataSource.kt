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

package app.cash.paykit.analytics.persistence

import android.os.SystemClock
import app.cash.paykit.analytics.AnalyticsOptions

abstract class EntriesDataSource(val options: AnalyticsOptions) {

  abstract fun insertEntry(type: String, content: String, metaData: String?): Long

  /**
   * Deletes the entries from the database.
   *
   * @param entries - entries to delete
   */
  abstract fun deleteEntry(entries: List<AnalyticEntry>)

  /**
   * Updates existing entries to SYNC_PENDING state and sets provided processId where entries
   * are in states NEW or SYNC_FAILED. The entries that are in state SYNC_IN_PROGRESS are skipped
   * in the update. entries in state SYNC_PENDING will be updated only if process id is NULL.
   * This is to ensure that in case that we have two sync processes overlapping we do not send
   * entries multiple times. The method will update only first MAX_SYNC_ENTRY_COUNT_PER_PROCESS
   * number of rows.
   *
   * @param processId id of the process that is going to perform the sync operation
   * @param entryType type of the entry to mark
   */
  abstract fun markEntriesForDelivery(processId: String, entryType: String)

  /**
   * Generates process id to be associated with the synchronization. This should be called at the
   * beginning of the synchronization task to obtain the process_id which should be used until
   * task ends its processing.
   *
   * @return process id associated with sync entries
   * @param entryType type of the entries
   */
  @Synchronized
  fun generateProcessId(entryType: String): String {
    val procId = "proc-" + SystemClock.elapsedRealtime()
    markEntriesForDelivery(procId, entryType)
    return procId
  }

  /**
   * Returns the list of entries to send to the server that are in state SYNC_PENDING and belong
   * to the provided PROCESS_ID. The result is paginated based on the 0 for offset and AnalyticEntry.BATCH_SIZE
   * for limit
   *
   * @param processId id of the sync process
   * @param entryType type of the entries
   * @return List of sync entries to send to the server
   */
  @Synchronized
  fun getEntriesForDelivery(
    processId: String,
    entryType: String,
  ): List<AnalyticEntry> {
    return getEntriesForDelivery(processId, entryType, 0, options.batchSize)
  }

  /**
   * Returns the list of entries to send to the server that are in state SYNC_PENDING and belong
   * to the provided PROCESS_ID. The result is paginated based on the provided offset and limit
   * parameters.
   *
   * @param processId id of the sync process
   * @param entryType type of the entry
   * @param offset    results offset
   * @param limit     results limit
   * @return List of sync entries to send to the server
   */
  @Synchronized
  fun getEntriesForDelivery(
    processId: String,
    entryType: String,
    offset: Int,
    limit: Int,
  ): List<AnalyticEntry> {
    return getEntriesByProcessIdAndState(
      processId,
      entryType,
      AnalyticEntry.STATE_DELIVERY_PENDING,
      offset,
      limit,
    )
  }

  /**
   * Returns the list of entries to send to the server. It retrieves entries that are in a
   * provided state and matching the provided process id. The result is paginated based on the
   * provided offset and limit parameters.
   *
   * @param processId id of the working process
   * @param entryType type of the entry
   * @param state  state of the entries to retrieve
   * @param offset results offset
   * @param limit  results limit
   * @return List of sync entries to send to the server
   */
  abstract fun getEntriesByProcessIdAndState(
    processId: String,
    entryType: String,
    state: Int,
    offset: Int,
    limit: Int,
  ): List<AnalyticEntry>

  /**
   * Updates all entries in the database to the status NEW and process_id to NULL
   */
  abstract fun resetEntries()

  /**
   * Updates the status of the sync entry.
   *
   * @param entries  list of the sync entries to update
   * @param status    new status for the sync entry
   */
  abstract fun updateStatuses(entries: List<AnalyticEntry>, status: Int)
}

fun List<AnalyticEntry>.toCommaSeparatedListIds() = joinToString(transform = {
  it.id.toString()
})
