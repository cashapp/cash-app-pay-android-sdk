package app.cash.paykit.analytics

import android.content.ContentValues
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.util.Log
import app.cash.paykit.analytics.persistence.AnalyticEntry
import app.cash.paykit.analytics.persistence.sqlite.AnalyticsSQLiteDataSource
import app.cash.paykit.analytics.persistence.sqlite.AnalyticsSqLiteHelper
import java.lang.reflect.Field
import java.lang.reflect.Method

internal object Utils {
  @Throws(Exception::class)
  fun invokePrivateMethod(
    targetObject: Any,
    methodName: String?,
    parameterTypes: Array<Class<*>?>,
    vararg args: Any?,
  ): Any {
    val method: Method = targetObject.javaClass.getDeclaredMethod(methodName, *parameterTypes)
    method.isAccessible = true
    return method.invoke(targetObject, args)
  }
  fun setPrivateStaticField(clazz: Class<*>, fieldName: String?, value: Any?) {
    try {
      val field: Field = clazz.getDeclaredField(fieldName)
      field.setAccessible(true)
      field.set(null, value)
    } catch (e: NoSuchFieldException) {
      e.printStackTrace()
    } catch (e: IllegalAccessException) {
      e.printStackTrace()
    }
  }

  fun setPrivateField(obj: Any, fieldName: String?, value: Any?) {
    try {
      val field: Field = obj.javaClass.getDeclaredField(fieldName)
      field.isAccessible = true
      field.set(obj, value)
    } catch (e: NoSuchFieldException) {
      e.printStackTrace()
    } catch (e: IllegalAccessException) {
      e.printStackTrace()
    }
  }

  fun getPrivateField(obj: Any, fieldName: String?): Any? {
    try {
      val field: Field = obj.javaClass.getDeclaredField(fieldName)
      field.isAccessible = true
      return field.get(obj)
    } catch (e: NoSuchFieldException) {
      e.printStackTrace()
    } catch (e: IllegalAccessException) {
      e.printStackTrace()
    }
    return null
  }

  fun createPackage(packageType: String?, packageState: Int): AnalyticEntry {
    return createPackage(
      "package.process.id",
      packageType,
      packageState,
      "package.load",
      "package.metdata",
      "v1",
    )
  }

  fun createPackage(processId: String?, packageType: String?, packageState: Int): AnalyticEntry {
    return createPackage(
      processId,
      packageType,
      packageState,
      "package.load",
      "package.metdata",
      "v1",
    )
  }

  fun createPackage(
    processId: String?,
    packageType: String?,
    packageState: Int,
    load: String?,
    metaData: String?,
    version: String?,
  ) = AnalyticEntry(
    id = System.currentTimeMillis(),
    processId = processId,
    type = packageType,
    state = packageState,
    content = load,
    metaData = metaData,
    version = version,
  )

  fun getPackagesToSync(count: Int): List<AnalyticEntry> {
    val packages = mutableListOf<AnalyticEntry>()
    for (i in 0 until count) {
      val p: AnalyticEntry = createPackage("TYPE_1", AnalyticEntry.STATE_NEW)
      packages.add(p)
    }
    return packages
  }

  /**
   * Returns all sync packages from the database.
   *
   * @return list of sync packages
   */
  fun getAllSyncPackages(sqLiteHelper: AnalyticsSqLiteHelper): List<AnalyticEntry> {
    val packages = mutableListOf<AnalyticEntry>()
    val database: SQLiteDatabase = sqLiteHelper.database
    var cursor: Cursor? = null
    try {
      // @formatter:off
      cursor = database.query(
        true,
        AnalyticsSQLiteDataSource.TABLE_SYNC_PACKAGES,
        null,
        null,
        null,
        null,
        null,
        "id ASC",
        null,
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

  /**
   * Deletes all packages from the database.
   */
  @Synchronized fun deleteAllPackages(sqLiteHelper: AnalyticsSqLiteHelper) {
    val database: SQLiteDatabase = sqLiteHelper.database
    try {
      database.delete(AnalyticsSQLiteDataSource.TABLE_SYNC_PACKAGES, null, null)
    } catch (e: Exception) {
      e.printStackTrace()
      Log.e("", "", e)
    }
  }

  fun insertSyncPackage(
    sqLiteHelper: AnalyticsSqLiteHelper,
    processId: String?,
    packageType: String?,
    packageState: Int,
    load: String?,
    metaData: String?,
    version: String?,
  ): Long {
    var insertId: Long = -1
    try {
      val database: SQLiteDatabase = sqLiteHelper.database
      val values = ContentValues()
      values.put(AnalyticsSQLiteDataSource.COLUMN_TYPE, packageType)
      values.put(AnalyticsSQLiteDataSource.COLUMN_PROCESS_ID, processId)
      values.put(AnalyticsSQLiteDataSource.COLUMN_CONTENT, load)
      values.put(AnalyticsSQLiteDataSource.COLUMN_STATE, packageState)
      values.put(AnalyticsSQLiteDataSource.COLUMN_META_DATA, metaData)
      values.put(AnalyticsSQLiteDataSource.COLUMN_VERSION, version)
      insertId = database.insert(AnalyticsSQLiteDataSource.TABLE_SYNC_PACKAGES, null, values)
      if (insertId < 0) {
        Log.e(
          "Utils",
          "Unable to insert record into the " + AnalyticsSQLiteDataSource.TABLE_SYNC_PACKAGES + ", values: " +
            values,
        )
      }
    } catch (e: Exception) {
      e.printStackTrace()
      Log.e("", "", e)
    }
    return insertId
  }
}
