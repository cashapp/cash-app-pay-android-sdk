package app.cash.paykit.analytics.persistence.sqlite

import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase
import android.util.Log
import app.cash.paykit.analytics.AnalyticsOptions
import app.cash.paykit.analytics.persistence.AnalyticEntry
import app.cash.paykit.analytics.persistence.EntriesDataSource
import app.cash.paykit.analytics.persistence.toCommaSeparatedListIds

class AnalyticsSQLiteDataSource(
  private val sqLiteHelper: AnalyticsSqLiteHelper,
  options: AnalyticsOptions,
) : EntriesDataSource(options) {

  @Synchronized
  override fun insertEntry(type: String, content: String, metaData: String?): Long {
    var insertId: Long = -1
    try {
      val values = ContentValues().apply {
        put(COLUMN_TYPE, type)
        put(COLUMN_CONTENT, content)
        put(COLUMN_STATE, AnalyticEntry.STATE_NEW)
        metaData?.let {
          put(COLUMN_META_DATA, it)
        }
      }
      values.put(COLUMN_VERSION, options.applicationVersionCode.toString())
      insertId = sqLiteHelper.database.insert(TABLE_SYNC_ENTRIES, null, values)
      if (insertId < 0) {
        Log.e(TAG, "Unable to insert record into the $TABLE_SYNC_ENTRIES, values: $content")
      }
    } catch (e: Exception) {
      Log.e("", "", e)
    }
    return insertId
  }

  @Synchronized
  override fun deleteEntry(entries: List<AnalyticEntry>) {
    val database: SQLiteDatabase = sqLiteHelper.database
    try {
      val whereClauseForDelete = "$COLUMN_ID IN (${entries.toCommaSeparatedListIds()})"
      database.delete(TABLE_SYNC_ENTRIES, whereClauseForDelete, null)
    } catch (e: Exception) {
      Log.e("", "", e)
    }
  }

  @Synchronized
  override fun markEntriesForDelivery(processId: String, entryType: String) {
    try {
      sqLiteHelper.database.query(
        true, // distinct
        TABLE_SYNC_ENTRIES,
        arrayOf(COLUMN_ID), // columns
        "($COLUMN_STATE=? OR $COLUMN_STATE=? OR ($COLUMN_STATE=? AND $COLUMN_PROCESS_ID IS NULL)) AND $COLUMN_TYPE=?",
        arrayOf(
          AnalyticEntry.STATE_NEW.toString(),
          AnalyticEntry.STATE_DELIVERY_FAILED.toString(),
          AnalyticEntry.STATE_DELIVERY_PENDING.toString(),
          entryType,
        ),
        null,
        null,
        "id ASC",
        options.maxEntryCountPerProcess.toString(), // limit
      )?.use { cursor ->
        val query =
          "UPDATE $TABLE_SYNC_ENTRIES SET $COLUMN_STATE=${AnalyticEntry.STATE_DELIVERY_PENDING}, $COLUMN_PROCESS_ID='$processId' WHERE $COLUMN_ID=?;"

        with(sqLiteHelper.database.compileStatement(query)) {
          cursor.moveToFirst()
          while (!cursor.isAfterLast) {
            val id: Long = cursor.getLong(0)
            bindLong(1, id)
            execute()
            cursor.moveToNext()
          }
        }
      }
    } catch (e: Exception) {
      Log.e("", "", e)
    }
  }

  @Synchronized
  override fun getEntriesByProcessIdAndState(
    processId: String,
    entryType: String,
    state: Int,
    offset: Int,
    limit: Int,
  ): List<AnalyticEntry> {
    val entries = mutableListOf<AnalyticEntry>()
    try {
      sqLiteHelper.database.query(
        true,
        TABLE_SYNC_ENTRIES,
        null,
        "$COLUMN_STATE=? AND $COLUMN_PROCESS_ID=? AND $COLUMN_TYPE=?",
        arrayOf(state.toString(), processId, entryType),
        null,
        null,
        "id ASC",
        if ((offset > 0)) "$offset,$limit" else limit.toString(),
      ).use { cursor ->
        cursor.moveToFirst()
        while (!cursor.isAfterLast) {
          entries.add(AnalyticEntry.from(cursor))
          cursor.moveToNext()
        }
      }
    } catch (e: Exception) {
      Log.e("", "", e)
    }
    return entries
  }

  @Synchronized
  override fun updateStatuses(entries: List<AnalyticEntry>, status: Int) {
    val database: SQLiteDatabase = sqLiteHelper.database
    try {
      val query =
        "UPDATE $TABLE_SYNC_ENTRIES SET $COLUMN_STATE=$status WHERE id IN (" + entries.toCommaSeparatedListIds() + ");"
      database.execSQL(query)
    } catch (e: Exception) {
      Log.e("", "", e)
    }
  }

  @Synchronized
  override fun resetEntries() {
    val database: SQLiteDatabase = sqLiteHelper.database
    try {
      val query =
        """UPDATE $TABLE_SYNC_ENTRIES SET $COLUMN_STATE=${AnalyticEntry.STATE_NEW}, $COLUMN_PROCESS_ID=NULL;"""
      database.execSQL(query)
    } catch (e: Exception) {
      Log.e("", "", e)
    }
  }

  companion object {
    private const val TAG = "EntriesDataSource"
    const val TABLE_SYNC_ENTRIES = "entries"
    const val COLUMN_ID = "id"
    const val COLUMN_TYPE = "type"
    const val COLUMN_CONTENT = "content"
    const val COLUMN_STATE = "state"
    const val COLUMN_META_DATA = "meta_data"
    const val COLUMN_PROCESS_ID = "process_id"
    const val COLUMN_VERSION = "version"

    // @formatter:off
    const val SQL_CREATE_TABLE =
      "CREATE TABLE '$TABLE_SYNC_ENTRIES' ('$COLUMN_ID' INTEGER NOT NULL,'$COLUMN_TYPE' TEXT,'$COLUMN_CONTENT' TEXT,'$COLUMN_STATE' INTEGER,'$COLUMN_META_DATA' TEXT,'$COLUMN_PROCESS_ID' TEXT,'$COLUMN_VERSION' TEXT, PRIMARY KEY ('$COLUMN_ID'));"

    // @formatter:on
    const val SQL_CREATE_INDEX_FOR_COLUMN_STATE =
      "CREATE INDEX '" + COLUMN_STATE + "_index' ON " + TABLE_SYNC_ENTRIES + " ('" + COLUMN_STATE + "');"
    const val SQL_CREATE_INDEX_FOR_COLUMN_TYPE =
      "CREATE INDEX '" + COLUMN_TYPE + "_index' ON " + TABLE_SYNC_ENTRIES + " ('" + COLUMN_TYPE + "');"
    const val SQL_CREATE_INDEX_FOR_COLUMN_PROCESS_ID =
      "CREATE INDEX '" + COLUMN_PROCESS_ID + "_index' ON " + TABLE_SYNC_ENTRIES + " ('" + COLUMN_PROCESS_ID + "');"
  }
}
