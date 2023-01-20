package app.cash.paykit.analytics.persistence.sqlite

import android.content.ContentValues
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteStatement
import android.util.Log
import app.cash.paykit.analytics.AnalyticsOptions
import app.cash.paykit.analytics.persistence.AnalyticEntry
import app.cash.paykit.analytics.persistence.PackagesDataSource

internal class AnalyticsSQLiteDataSource(
  private val sqLiteHelper: AnalyticsSqLiteHelper,
  options: AnalyticsOptions,
) :
  PackagesDataSource(options) {

  @Synchronized
  override fun insertPackage(type: String, content: String, metaData: String): Long {
    var insertId: Long = -1
    try {
      val database: SQLiteDatabase = sqLiteHelper.database
      val values = ContentValues()
      values.put(COLUMN_TYPE, type)
      values.put(COLUMN_CONTENT, content)
      values.put(COLUMN_STATE, AnalyticEntry.STATE_NEW)
      values.put(COLUMN_META_DATA, metaData)
      values.put(COLUMN_VERSION, java.lang.String.valueOf(options.applicationVersionCode))
      insertId = database.insert(TABLE_SYNC_PACKAGES, null, values)
      if (insertId < 0) {
        Log.e(TAG, "Unable to insert record into the $TABLE_SYNC_PACKAGES, values: $content")
      }
    } catch (e: Exception) {
      e.printStackTrace()
      Log.e("", "", e)
    }
    return insertId
  }

  @Synchronized
  override fun deletePackages(packages: List<AnalyticEntry>) {
    val database: SQLiteDatabase = sqLiteHelper.database
    try {
      val whereClauseForDelete =
        COLUMN_ID + " IN (" + packagesList2CommaSeparatedIds(packages) + ")"
      database.delete(TABLE_SYNC_PACKAGES, whereClauseForDelete, null)
    } catch (e: Exception) {
      e.printStackTrace()
      Log.e("", "", e)
    }
  }

  @Synchronized
  override fun markPackagesForDelivery(processId: String, packageType: String) {
    try {
      sqLiteHelper.database.use { database ->
        // @formatter:off
        database.query(
          true, // distinct
          TABLE_SYNC_PACKAGES,
          arrayOf(COLUMN_ID), // columns
          "(" +
            COLUMN_STATE + "=? OR " +
            COLUMN_STATE + "=? OR " +
            "(" + COLUMN_STATE + "=? AND " + COLUMN_PROCESS_ID + " IS NULL) " +
            ") AND " +
            COLUMN_TYPE + "=?",
          arrayOf(
            java.lang.String.valueOf(AnalyticEntry.STATE_NEW),
            java.lang.String.valueOf(AnalyticEntry.STATE_DELIVERY_FAILED),
            java.lang.String.valueOf(AnalyticEntry.STATE_DELIVERY_PENDING),
            packageType,
          ), //
          // selection args
          null, // group by
          null, // having
          "id ASC", // order by
          java.lang.String.valueOf(options.maxPackageCountPerProcess), // limit
        )?.use { cursor ->
          val query = (
            (
              "UPDATE " + TABLE_SYNC_PACKAGES +
                " SET " +
                COLUMN_STATE + "=" + AnalyticEntry.STATE_DELIVERY_PENDING
              ) + ", " +
              COLUMN_PROCESS_ID + "='" + processId + "'" +
              " WHERE " + COLUMN_ID + "=?;"
            )
          // @formatter:on
          val stmt: SQLiteStatement = database.compileStatement(query)
          cursor.moveToFirst()
          while (!cursor.isAfterLast) {
            val id: Long = cursor.getLong(0)
            stmt.bindLong(1, id)
            stmt.execute()
            cursor.moveToNext()
          }
        }
      }
    } catch (e: Exception) {
      e.printStackTrace()
      Log.e("", "", e)
    }
  }

  @Synchronized override fun getPackagesByProcessIdAndState(
    processId: String,
    packageType: String,
    state: Int,
    offset: Int,
    limit: Int,
  ): List<AnalyticEntry> {
    val packages = mutableListOf<AnalyticEntry>()
    var cursor: Cursor? = null
    try {
      val database: SQLiteDatabase = sqLiteHelper.database

      // @formatter:off
      cursor = database.query(
        true,
        TABLE_SYNC_PACKAGES,
        null,
        "$COLUMN_STATE=? AND $COLUMN_PROCESS_ID=? AND $COLUMN_TYPE=?",
        arrayOf(state.toString(), processId, packageType),
        null,
        null,
        "id ASC",
        if ((offset > 0)) "$offset,$limit" else limit.toString(),
      )
      // @formatter:on
      cursor.moveToFirst()
      while (!cursor.isAfterLast) {
        packages.add(AnalyticEntry.from(cursor))
        cursor.moveToNext()
      }
    } catch (e: Exception) {
      e.printStackTrace()
      Log.e("", "", e)
    } finally {
      cursor?.close()
    }
    return packages
  }

  @Synchronized
  override fun updateStatuses(packages: List<AnalyticEntry>, status: Int) {
    val database: SQLiteDatabase = sqLiteHelper.database
    try {
      val query =
        (
          "UPDATE " + TABLE_SYNC_PACKAGES + " SET " + COLUMN_STATE + "=" + status + " WHERE id IN (" +
            packagesList2CommaSeparatedIds(packages) + ");"
          )
      database.execSQL(query)
    } catch (e: Exception) {
      e.printStackTrace()
      Log.e("", "", e)
    }
  }

  @Synchronized override fun resetPackages() {
    val database: SQLiteDatabase = sqLiteHelper.database
    try {
      val query =
        (
          ("UPDATE " + TABLE_SYNC_PACKAGES + " SET " + COLUMN_STATE + "=" + AnalyticEntry.STATE_NEW) + ", " +
            COLUMN_PROCESS_ID +
            "=NULL;"
          )
      database.execSQL(query)
    } catch (e: Exception) {
      e.printStackTrace()
      Log.e("", "", e)
    }
  }

  companion object {
    private const val TAG = "PackagesDataSource"
    const val TABLE_SYNC_PACKAGES = "packages"
    const val COLUMN_ID = "id"
    const val COLUMN_TYPE = "type"
    const val COLUMN_CONTENT = "content"
    const val COLUMN_STATE = "state"
    const val COLUMN_META_DATA = "meta_data"
    const val COLUMN_PROCESS_ID = "process_id"
    const val COLUMN_VERSION = "version"

    // @formatter:off
    const val SQL_CREATE_TABLE = (
      "CREATE TABLE '" + TABLE_SYNC_PACKAGES + "' (" +
        "'" + COLUMN_ID + "' INTEGER NOT NULL," +
        "'" + COLUMN_TYPE + "' TEXT," +
        "'" + COLUMN_CONTENT + "' TEXT," +
        "'" + COLUMN_STATE + "' INTEGER," +
        "'" + COLUMN_META_DATA + "' TEXT," +
        "'" + COLUMN_PROCESS_ID + "' TEXT," +
        "'" + COLUMN_VERSION + "' TEXT," +
        " PRIMARY KEY ('" + COLUMN_ID + "'));"
      )

    // @formatter:on
    const val SQL_CREATE_INDEX_FOR_COLUMN_STATE = (
      "CREATE INDEX '" + COLUMN_STATE +
        "_index' ON " +
        TABLE_SYNC_PACKAGES + " ('" + COLUMN_STATE + "');"
      )
    const val SQL_CREATE_INDEX_FOR_COLUMN_TYPE = (
      "CREATE INDEX '" + COLUMN_TYPE +
        "_index' ON " + TABLE_SYNC_PACKAGES + " ('" + COLUMN_TYPE + "');"
      )
    const val SQL_CREATE_INDEX_FOR_COLUMN_PROCESS_ID =
      (
        "CREATE INDEX '" + COLUMN_PROCESS_ID + "_index' ON " +
          TABLE_SYNC_PACKAGES + " ('" + COLUMN_PROCESS_ID + "');"
        )
  }
}
