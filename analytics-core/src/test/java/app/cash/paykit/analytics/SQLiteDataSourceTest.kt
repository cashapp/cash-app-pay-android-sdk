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
package app.cash.paykit.analytics

import app.cash.paykit.analytics.Utils.getAllSyncEntries
import app.cash.paykit.analytics.Utils.insertSyncEntry
import app.cash.paykit.analytics.persistence.AnalyticEntry
import app.cash.paykit.analytics.persistence.sqlite.AnalyticsSQLiteDataSource
import app.cash.paykit.analytics.persistence.sqlite.AnalyticsSqLiteHelper
import app.cash.paykit.logging.CashAppLogger
import io.mockk.mockk
import org.junit.After
import org.junit.Assert
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import kotlin.time.Duration.Companion.seconds

@RunWith(RobolectricTestRunner::class)
class SQLiteDataSourceTest {

  private lateinit var options: AnalyticsOptions
  private lateinit var helper: AnalyticsSqLiteHelper
  private val cashAppLogger: CashAppLogger = mockk(relaxed = true)
  private val app = RuntimeEnvironment.getApplication()

  @Before
  fun setup() {
    options = AnalyticsOptions(
      delay = 0.seconds,
      interval = 10.seconds,
      maxEntryCountPerProcess = 30,
      batchSize = 10,
      databaseName = "TEST",
    )
    app.deleteDatabase(options.databaseName)
    helper = AnalyticsSqLiteHelper(app, options)
  }

  @After
  fun tearDown() {
    Utils.deleteAllEntries(helper)
  }

  @Test
  fun testInsertEntries() {
    val dataSource = AnalyticsSQLiteDataSource(helper, options, cashAppLogger)
    val entryId: Long =
      dataSource.insertEntry("TYPE_1", "load.testInsertEntry", "metadata.testInsertEntry")
    assertTrue(entryId > 0)
    val entries: List<AnalyticEntry> = getAllSyncEntries(helper)
    Assert.assertNotNull(entries)
    assertTrue(entries.size == 1)
    Assert.assertNotNull(entries.first())
    with(entries.first()) {
      assertTrue("TYPE_1".equals(type, ignoreCase = true))
      assertTrue("load.testInsertEntry".equals(content, ignoreCase = true))
      assertTrue("metadata.testInsertEntry".equals(metaData, ignoreCase = true))
      assertEquals(state, AnalyticEntry.STATE_NEW)
      assertTrue(
        options.applicationVersionCode.toString().equals(version, ignoreCase = true),
      )
    }
  }

  @Test
  fun testDeleteEntries() {
    val dataSource = AnalyticsSQLiteDataSource(helper, options, cashAppLogger)
    val pkgId1: Long =
      dataSource.insertEntry("TYPE_1", "load.testInsertEntry.1", "metadata.testInsertEntry.1")
    val pkgId2: Long =
      dataSource.insertEntry("TYPE_2", "load.testInsertEntry.2", "metadata.testInsertEntry.2")
    val p1 = AnalyticEntry(id = pkgId1, content = "content")
    val p2 = AnalyticEntry(id = pkgId2, content = "content")
    val entries = listOf(p1, p2)

    dataSource.deleteEntry(entries)

    val allEntries = getAllSyncEntries(helper)
    Assert.assertNotNull(allEntries)
    assertTrue(allEntries.isEmpty())
  }

  @Test
  fun testGetEntriesByProcessIdAndState() {
    val dataSource = AnalyticsSQLiteDataSource(helper, options, cashAppLogger)
    // @formatter:off
    val p1 = insertSyncEntry(helper, "PROCESS_1", "TYPE_1", AnalyticEntry.STATE_NEW, "", "", "")
    val p2 = insertSyncEntry(helper, "PROCESS_1", "TYPE_1", AnalyticEntry.STATE_NEW, "", "", "")
    val p3 = insertSyncEntry(helper, "PROCESS_1", "TYPE_1", AnalyticEntry.STATE_NEW, "", "", "")

    val p20 =
      insertSyncEntry(helper, "PROCESS_2", "TYPE_2", AnalyticEntry.STATE_NEW, "", "", "")
    val p21 =
      insertSyncEntry(helper, "PROCESS_2", "TYPE_2", AnalyticEntry.STATE_NEW, "", "", "")
    // @formatter:on

    var entries = dataSource.getEntriesByProcessIdAndState(
      "PROCESS_1",
      "TYPE_1",
      AnalyticEntry.STATE_NEW,
      0,
      3,
    )
    Assert.assertNotNull(entries)
    assertEquals(entries.size, 3)
    assertEquals(entries[0].id, p1)
    assertEquals(entries[1].id, p2)
    assertEquals(entries[2].id, p3)
    entries =
      dataSource.getEntriesByProcessIdAndState(
        "PROCESS_2",
        "TYPE_2",
        AnalyticEntry.STATE_NEW,
        0,
        10,
      )
    Assert.assertNotNull(entries)
    assertTrue(entries.size == 2)
    assertEquals(entries[0].id, p20)
    assertEquals(entries[1].id, p21)
  }

  @Test
  fun testMarkEntriesForSynchronization() {
    val dataSource = AnalyticsSQLiteDataSource(helper, options, cashAppLogger)
    // @formatter:off
    // entries that are unassigned to sync process (2 types of entries, one entry for every possible state)
    val p1: Long = insertSyncEntry(helper, null, "TYPE_1", AnalyticEntry.STATE_NEW, "", "", "")
    val p2: Long =
      insertSyncEntry(helper, null, "TYPE_1", AnalyticEntry.STATE_DELIVERY_FAILED, "", "", "")
    val p4: Long = insertSyncEntry(
      helper,
      null,
      "TYPE_1",
      AnalyticEntry.STATE_DELIVERY_PENDING,
      "",
      "",
      "",
    )

    // entries assigned to PROCESS_1 sync process (2 types of entries, one entry for every possible state)
    val p9: Long = insertSyncEntry(helper, null, "TYPE_1", AnalyticEntry.STATE_NEW, "", "", "")
    val p10: Long = insertSyncEntry(
      helper,
      "PROCESS_1",
      "TYPE_1",
      AnalyticEntry.STATE_DELIVERY_FAILED,
      "",
      "",
      "",
    )
    val p12: Long = insertSyncEntry(
      helper,
      "PROCESS_1",
      "TYPE_1",
      AnalyticEntry.STATE_DELIVERY_PENDING,
      "",
      "",
      "",
    )

    // entries assigned to PROCESS_2 sync process (2 types of entries, one entry for every possible state)
    val p17: Long = insertSyncEntry(helper, null, "TYPE_1", AnalyticEntry.STATE_NEW, "", "", "")
    val p18: Long = insertSyncEntry(
      helper,
      "PROCESS_2",
      "TYPE_1",
      AnalyticEntry.STATE_DELIVERY_FAILED,
      "",
      "",
      "",
    )
    // @formatter:on

    dataSource.markEntriesForDelivery("PROCESS_1", "TYPE_1")
    val entries = dataSource.getEntriesByProcessIdAndState(
      "PROCESS_1",
      "TYPE_1",
      AnalyticEntry.STATE_DELIVERY_PENDING,
      0,
      10,
    )

    Assert.assertNotNull(entries)
    assertTrue(entries.size == 8)
    assertEquals(entries[0].id, p1)
    assertEquals(entries[1].id, p2)
    assertEquals(entries[2].id, p4)
    assertEquals(entries[3].id, p9)
    assertEquals(entries[4].id, p10)
    assertEquals(entries[5].id, p12)
    assertEquals(entries[6].id, p17)
    assertEquals(entries[7].id, p18)
  }

  @Test
  fun testUpdateStatuses() {
    val dataSource = AnalyticsSQLiteDataSource(helper, options, cashAppLogger)
    // @formatter:off
    val p1: Long = insertSyncEntry(helper, null, "TYPE_1", AnalyticEntry.STATE_NEW, "", "", "")
    val p2: Long = insertSyncEntry(
      helper,
      "PROCESS_1",
      "TYPE_1",
      AnalyticEntry.STATE_DELIVERY_FAILED,
      "",
      "",
      "",
    )
    val p3: Long = insertSyncEntry(
      helper,
      "PROCESS_1",
      "TYPE_1",
      AnalyticEntry.STATE_DELIVERY_IN_PROGRESS,
      "",
      "",
      "",
    )
    val p4: Long = insertSyncEntry(
      helper,
      "PROCESS_1",
      "TYPE_1",
      AnalyticEntry.STATE_DELIVERY_PENDING,
      "",
      "",
      "",
    )
    // @formatter:on

    val pkg1 = AnalyticEntry(id = p1, content = "content")
    val pkg2 = AnalyticEntry(id = p2, content = "content")
    val pkg3 = AnalyticEntry(id = p3, content = "content")
    val pkg4 = AnalyticEntry(id = p4, content = "content")

    val entriesToUpdate = listOf(pkg1, pkg2, pkg3, pkg4)
    dataSource.updateStatuses(entriesToUpdate, AnalyticEntry.STATE_NEW)

    val entries = getAllSyncEntries(helper)
    Assert.assertNotNull(entries)
    assertTrue(entries.size == 4)
    entries.forEach {
      assertEquals(it.state, AnalyticEntry.STATE_NEW)
    }
  }

  @Test
  fun testResetEntries() {
    val dataSource = AnalyticsSQLiteDataSource(helper, options, cashAppLogger)
    // @formatter:off
    insertSyncEntry(helper, null, "TYPE_1", AnalyticEntry.STATE_NEW, "", "", "")
    insertSyncEntry(
      helper,
      "PROCESS_1",
      "TYPE_1",
      AnalyticEntry.STATE_DELIVERY_FAILED,
      "",
      "",
      "",
    )
    insertSyncEntry(
      helper,
      "PROCESS_1",
      "TYPE_1",
      AnalyticEntry.STATE_DELIVERY_IN_PROGRESS,
      "",
      "",
      "",
    )
    insertSyncEntry(
      helper,
      "PROCESS_1",
      "TYPE_1",
      AnalyticEntry.STATE_DELIVERY_PENDING,
      "",
      "",
      "",
    )
    // @formatter:on

    dataSource.resetEntries()

    val entries = getAllSyncEntries(helper)
    Assert.assertNotNull(entries)
    assertTrue(entries.size == 4)
    entries.forEach {
      assertEquals(it.state, AnalyticEntry.STATE_NEW)
      assertTrue(it.processId == null)
    }
  }
}
