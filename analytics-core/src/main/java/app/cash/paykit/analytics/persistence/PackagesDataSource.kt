package app.cash.paykit.analytics.persistence

import app.cash.paykit.analytics.AnalyticsOptions

internal abstract class PackagesDataSource(val options: AnalyticsOptions) {

  abstract fun insertPackage(type: String, content: String, metaData: String): Long

  /**
   * Deletes the package from the database.
   *
   * @param packages - packages to delete
   */
  abstract fun deletePackages(packages: List<AnalyticEntry>)

  /**
   * Updates existing packages to SYNC_PENDING state and sets provided processId where packages
   * are in states NEW or SYNC_FAILED. The packages that are in state SYNC_IN_PROGRESS are skipped
   * in the update. Packages in state SYNC_PENDING will be updated only if process id is NULL.
   * This is to ensure that in case that we have two sync processes overlapping we do not send
   * packages multiple times. The method will update only first MAX_SYNC_PACKAGE_COUNT_PER_PROCESS
   * number of rows.
   *
   * @param processId id of the process that is going to perform the sync operation
   * @param packageType type of the package to mark
   */
  abstract fun markPackagesForDelivery(processId: String, packageType: String)

  /**
   * Generates process id to be associated with the synchronization. This should be called at the
   * beginning of the synchronization task to obtain the process_id which should be used until
   * task ends its processing.
   *
   * @return process id associated with sync packages
   * @param packageType type of the package
   */
  @Synchronized
  fun generateProcessId(packageType: String): String {
    val procId = "proc-" + System.currentTimeMillis()
    markPackagesForDelivery(procId, packageType)
    return procId
  }

  /**
   * Returns the list of packages to send to the server that are in state SYNC_PENDING and belong
   * to the provided PROCESS_ID. The result is paginated based on the 0 for offset and AnalyticPackage.BATCH_SIZE
   * for limit
   *
   * @param processId id of the sync process
   * @param packageType type of the package
   * @return List of sync packages to send to the server
   */
  @Synchronized
  fun getPackagesForDelivery(
    processId: String,
    packageType: String,
  ): List<AnalyticEntry> {
    return getPackagesForDelivery(processId, packageType, 0, options.batchSize)
  }

  /**
   * Returns the list of packages to send to the server that are in state SYNC_PENDING and belong
   * to the provided PROCESS_ID. The result is paginated based on the provided offset and limit
   * parameters.
   *
   * @param processId id of the sync process
   * @param packageType type of the package
   * @param offset    results offset
   * @param limit     results limit
   * @return List of sync packages to send to the server
   */
  @Synchronized
  fun getPackagesForDelivery(
    processId: String,
    packageType: String,
    offset: Int,
    limit: Int,
  ): List<AnalyticEntry> {
    return getPackagesByProcessIdAndState(
      processId,
      packageType,
      AnalyticEntry.STATE_DELIVERY_PENDING,
      offset,
      limit,
    )
  }

  /**
   * Returns the list of packages to send to the server. It retrieves packages that are in a
   * provided state and matching the provided process id. The result is paginated based on the
   * provided offset and limit parameters.
   *
   * @param processId id of the working process
   * @param packageType type of the package
   * @param state  state of the packages to retrieve
   * @param offset results offset
   * @param limit  results limit
   * @return List of sync packages to send to the server
   */
  abstract fun getPackagesByProcessIdAndState(
    processId: String,
    packageType: String,
    state: Int,
    offset: Int,
    limit: Int,
  ): List<AnalyticEntry>

  /**
   * Updates all packages in the database to the status NEW and process_id to NULL
   */
  abstract fun resetPackages()

  /**
   * Updates the status of the sync package.
   *
   * @param packages  list of the sync packages to update
   * @param status    new status for the sync package
   */
  abstract fun updateStatuses(packages: List<AnalyticEntry>, status: Int)

  fun packagesList2CommaSeparatedIds(packages: List<AnalyticEntry>?): String {
    if (packages == null) {
      return ""
    }
    if (packages.isEmpty()) {
      return ""
    }
    val result = StringBuilder()
    for (pkg in packages) {
      result.append(pkg.id)
      result.append(",")
    }
    return if (result.isNotEmpty()) result.substring(0, result.length - 1) else ""
  }
}
