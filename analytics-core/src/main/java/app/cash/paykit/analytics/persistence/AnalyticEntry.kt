package app.cash.paykit.analytics.persistence

import android.database.Cursor

internal data class AnalyticEntry constructor(
  val id: Long = 0,
  val type: String? = null,
  val content: String? = null,
  val state: Int = 0,
  val metaData: String? = null,
  val processId: String? = null,
  val version: String? = null,
) {

  companion object {
    const val STATE_NEW = 0
    const val STATE_DELIVERY_PENDING = 1
    const val STATE_DELIVERY_IN_PROGRESS = 2
    const val STATE_DELIVERY_FAILED = 3

    fun from(cursor: Cursor) = AnalyticEntry(
      id = cursor.getLong(0),
      type = cursor.getString(1),
      content = cursor.getString(2),
      state = cursor.getInt(3),
      metaData = cursor.getString(4),
      processId = cursor.getString(5),
      version = cursor.getString(6),
    )
  }
}
