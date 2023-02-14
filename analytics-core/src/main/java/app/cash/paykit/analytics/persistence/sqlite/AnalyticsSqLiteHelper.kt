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

package app.cash.paykit.analytics.persistence.sqlite

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import app.cash.paykit.analytics.AnalyticsOptions

class AnalyticsSqLiteHelper(context: Context, options: AnalyticsOptions) :
  SQLiteOpenHelper(context, options.databaseName, null, DATABASE_VERSION) {

  private var _database: SQLiteDatabase? = null

  init {
    ensureDatabaseIsInitialized()
  }

  override fun onCreate(sqLiteDatabase: SQLiteDatabase) {
    sqLiteDatabase.execSQL(AnalyticsSQLiteDataSource.SQL_CREATE_TABLE)
    sqLiteDatabase.execSQL(AnalyticsSQLiteDataSource.SQL_CREATE_INDEX_FOR_COLUMN_STATE)
    sqLiteDatabase.execSQL(AnalyticsSQLiteDataSource.SQL_CREATE_INDEX_FOR_COLUMN_TYPE)
    sqLiteDatabase.execSQL(AnalyticsSQLiteDataSource.SQL_CREATE_INDEX_FOR_COLUMN_PROCESS_ID)
  }

  override fun onUpgrade(sqLiteDatabase: SQLiteDatabase, oldVersion: Int, newVersion: Int) {}

  @get:Synchronized val database: SQLiteDatabase
    get() {
      ensureDatabaseIsInitialized()
      return _database!!
    }

  private fun ensureDatabaseIsInitialized() {
    if (!isDatabaseOpened) {
      _database = writableDatabase
      Log.d(TAG, "database opened.")
    }
  }

  private val isDatabaseOpened: Boolean
    get() = _database?.isOpen == true

  companion object {
    private const val TAG = "AnalyticsSQLiteHelper"
    const val DATABASE_VERSION = 1
  }
}
