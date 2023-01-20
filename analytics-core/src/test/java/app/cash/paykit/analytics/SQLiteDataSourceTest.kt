package app.cash.paykit.analytics

import app.cash.paykit.analytics.Utils.getAllSyncPackages
import app.cash.paykit.analytics.Utils.insertSyncPackage
import app.cash.paykit.analytics.persistence.AnalyticEntry
import app.cash.paykit.analytics.persistence.sqlite.AnalyticsSQLiteDataSource
import app.cash.paykit.analytics.persistence.sqlite.AnalyticsSqLiteHelper
import org.junit.After
import org.junit.Assert
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import kotlin.Long
import kotlin.time.Duration.Companion.seconds
import kotlin.with

@RunWith(RobolectricTestRunner::class)
class SQLiteDataSourceTest {

  private lateinit var options: AnalyticsOptions
  private lateinit var helper: AnalyticsSqLiteHelper
  private val app = RuntimeEnvironment.getApplication()

  @Before
  fun setup() {
    options =
      AnalyticsOptions.build {
        delay = 0.seconds
        interval = 10.seconds
        maxPackageCountPerProcess = 30
        batchSize = 10
        databaseName = "TEST"
      }
    app.deleteDatabase(options.databaseName)
    helper = AnalyticsSqLiteHelper(app, options)
  }

  @After
  fun tearDown() {
    Utils.deleteAllPackages(helper)
  }

  @Test
  fun testInsertPackage() {
    val dataSource = AnalyticsSQLiteDataSource(helper, options)
    val pkgId: Long =
      dataSource.insertPackage("TYPE_1", "load.testInsertPackage", "metadata.testInsertPackage")
    assertTrue(pkgId > 0)
    val packages: List<AnalyticEntry> = getAllSyncPackages(helper)
    Assert.assertNotNull(packages)
    assertTrue(packages.size == 1)
    Assert.assertNotNull(packages.first())
    with(packages.first()) {
      assertTrue("TYPE_1".equals(type, ignoreCase = true))
      assertTrue("load.testInsertPackage".equals(content, ignoreCase = true))
      assertTrue("metadata.testInsertPackage".equals(metaData, ignoreCase = true))
      assertEquals(state, AnalyticEntry.STATE_NEW)
      assertTrue(
        options.applicationVersionCode.toString().equals(version, ignoreCase = true),
      )
    }
  }

  @Test
  fun testDeletePackages() {
    val dataSource = AnalyticsSQLiteDataSource(helper, options)
    val pkgId1: Long =
      dataSource.insertPackage("TYPE_1", "load.testInsertPackage.1", "metadata.testInsertPackage.1")
    val pkgId2: Long =
      dataSource.insertPackage("TYPE_2", "load.testInsertPackage.2", "metadata.testInsertPackage.2")
    val p1 = AnalyticEntry(id = pkgId1)
    val p2 = AnalyticEntry(id = pkgId2)
    val packages = listOf(p1, p2)

    dataSource.deletePackages(packages)

    val allAnalyticPackages = getAllSyncPackages(helper)
    Assert.assertNotNull(allAnalyticPackages)
    assertTrue(allAnalyticPackages.isEmpty())
  }

  @Test
  fun testGetPackagesByProcessIdAndState() {
    val dataSource = AnalyticsSQLiteDataSource(helper, options)
    // @formatter:off
    val p1 = insertSyncPackage(helper, "PROCESS_1", "TYPE_1", AnalyticEntry.STATE_NEW, "", "", "")
    val p2 = insertSyncPackage(helper, "PROCESS_1", "TYPE_1", AnalyticEntry.STATE_NEW, "", "", "")
    val p3 = insertSyncPackage(helper, "PROCESS_1", "TYPE_1", AnalyticEntry.STATE_NEW, "", "", "")
    val p4 = insertSyncPackage(helper, "PROCESS_1", "TYPE_1", AnalyticEntry.STATE_NEW, "", "", "")
    val p5: Long = insertSyncPackage(helper, "PROCESS_1", "TYPE_1", AnalyticEntry.STATE_NEW, "", "", "")
    val p6: Long = insertSyncPackage(helper, "PROCESS_1", "TYPE_1", AnalyticEntry.STATE_NEW, "", "", "")
    val p7: Long = insertSyncPackage(helper, "PROCESS_1", "TYPE_1", AnalyticEntry.STATE_NEW, "", "", "")
    val p8: Long = insertSyncPackage(helper, "PROCESS_1", "TYPE_2", AnalyticEntry.STATE_NEW, "", "", "")
    val p9: Long = insertSyncPackage(helper, "PROCESS_1", "TYPE_2", AnalyticEntry.STATE_NEW, "", "", "")
    val p10: Long = insertSyncPackage(helper, "PROCESS_1", "TYPE_1", AnalyticEntry.STATE_DELIVERY_PENDING, "", "", "")
    val p11: Long = insertSyncPackage(helper, "PROCESS_1", "TYPE_1", AnalyticEntry.STATE_DELIVERY_IN_PROGRESS, "", "", "")
    val p12: Long = insertSyncPackage(helper, "PROCESS_1", "TYPE_1", AnalyticEntry.STATE_DELIVERY_FAILED, "", "", "")
    val p13: Long = insertSyncPackage(helper, "PROCESS_2", "TYPE_1", AnalyticEntry.STATE_NEW, "", "", "")
    val p14: Long = insertSyncPackage(helper, "PROCESS_2", "TYPE_1", AnalyticEntry.STATE_NEW, "", "", "")
    val p15: Long = insertSyncPackage(helper, "PROCESS_2", "TYPE_1", AnalyticEntry.STATE_NEW, "", "", "")
    val p16: Long = insertSyncPackage(helper, "PROCESS_2", "TYPE_1", AnalyticEntry.STATE_NEW, "", "", "")
    val p17: Long = insertSyncPackage(helper, "PROCESS_2", "TYPE_1", AnalyticEntry.STATE_NEW, "", "", "")
    val p18: Long = insertSyncPackage(helper, "PROCESS_2", "TYPE_1", AnalyticEntry.STATE_NEW, "", "", "")
    val p19: Long = insertSyncPackage(helper, "PROCESS_2", "TYPE_1", AnalyticEntry.STATE_NEW, "", "", "")
    val p20 = insertSyncPackage(helper, "PROCESS_2", "TYPE_2", AnalyticEntry.STATE_NEW, "", "", "")
    val p21 = insertSyncPackage(helper, "PROCESS_2", "TYPE_2", AnalyticEntry.STATE_NEW, "", "", "")
    val p22 = insertSyncPackage(helper, "PROCESS_2", "TYPE_1", AnalyticEntry.STATE_DELIVERY_PENDING, "", "", "")
    val p23 = insertSyncPackage(helper, "PROCESS_2", "TYPE_1", AnalyticEntry.STATE_DELIVERY_IN_PROGRESS, "", "", "")
    val p24 = insertSyncPackage(helper, "PROCESS_2", "TYPE_1", AnalyticEntry.STATE_DELIVERY_FAILED, "", "", "")
    // @formatter:on

    var packages = dataSource.getPackagesByProcessIdAndState(
      "PROCESS_1",
      "TYPE_1",
      AnalyticEntry.STATE_NEW,
      0,
      3,
    )
    Assert.assertNotNull(packages)
    assertEquals(packages.size, 3)
    assertEquals(packages[0].id, p1)
    assertEquals(packages[1].id, p2)
    assertEquals(packages[2].id, p3)
    packages =
      dataSource.getPackagesByProcessIdAndState(
        "PROCESS_2",
        "TYPE_2",
        AnalyticEntry.STATE_NEW,
        0,
        10,
      )
    Assert.assertNotNull(packages)
    assertTrue(packages.size == 2)
    assertEquals(packages[0].id, p20)
    assertEquals(packages[1].id, p21)
  }

  @Test
  fun testMarkPackagesForSynchronization() {
    val dataSource = AnalyticsSQLiteDataSource(helper, options)
    // @formatter:off
    // packages that are unassigned to sync process (2 types of packages, one package for every possible state)
    val p1: Long = insertSyncPackage(helper, null, "TYPE_1", AnalyticEntry.STATE_NEW, "", "", "")
    val p2: Long = insertSyncPackage(helper, null, "TYPE_1", AnalyticEntry.STATE_DELIVERY_FAILED, "", "", "")
    val p3: Long = insertSyncPackage(helper, null, "TYPE_1", AnalyticEntry.STATE_DELIVERY_IN_PROGRESS, "", "", "")
    val p4: Long = insertSyncPackage(helper, null, "TYPE_1", AnalyticEntry.STATE_DELIVERY_PENDING, "", "", "")
    val p5: Long = insertSyncPackage(helper, null, "TYPE_2", AnalyticEntry.STATE_NEW, "", "", "")
    val p6: Long = insertSyncPackage(helper, null, "TYPE_2", AnalyticEntry.STATE_DELIVERY_FAILED, "", "", "")
    val p7: Long = insertSyncPackage(helper, null, "TYPE_2", AnalyticEntry.STATE_DELIVERY_IN_PROGRESS, "", "", "")
    val p8: Long = insertSyncPackage(helper, null, "TYPE_2", AnalyticEntry.STATE_DELIVERY_PENDING, "", "", "")

    // packages assigned to PROCESS_1 sync process (2 types of packages, one package for every possible state)
    val p9: Long = insertSyncPackage(helper, null, "TYPE_1", AnalyticEntry.STATE_NEW, "", "", "")
    val p10: Long = insertSyncPackage(helper, "PROCESS_1", "TYPE_1", AnalyticEntry.STATE_DELIVERY_FAILED, "", "", "")
    val p11: Long = insertSyncPackage(helper, "PROCESS_1", "TYPE_1", AnalyticEntry.STATE_DELIVERY_IN_PROGRESS, "", "", "")
    val p12: Long = insertSyncPackage(helper, "PROCESS_1", "TYPE_1", AnalyticEntry.STATE_DELIVERY_PENDING, "", "", "")
    val p13: Long = insertSyncPackage(helper, "PROCESS_1", "TYPE_2", AnalyticEntry.STATE_NEW, "", "", "")
    val p14: Long = insertSyncPackage(helper, "PROCESS_1", "TYPE_2", AnalyticEntry.STATE_DELIVERY_FAILED, "", "", "")
    val p15: Long = insertSyncPackage(helper, "PROCESS_1", "TYPE_2", AnalyticEntry.STATE_DELIVERY_IN_PROGRESS, "", "", "")
    val p16: Long = insertSyncPackage(helper, "PROCESS_1", "TYPE_2", AnalyticEntry.STATE_DELIVERY_PENDING, "", "", "")

    // packages assigned to PROCESS_2 sync process (2 types of packages, one package for every possible state)
    val p17: Long = insertSyncPackage(helper, null, "TYPE_1", AnalyticEntry.STATE_NEW, "", "", "")
    val p18: Long = insertSyncPackage(helper, "PROCESS_2", "TYPE_1", AnalyticEntry.STATE_DELIVERY_FAILED, "", "", "")
    val p19: Long = insertSyncPackage(helper, "PROCESS_2", "TYPE_1", AnalyticEntry.STATE_DELIVERY_IN_PROGRESS, "", "", "")
    val p20: Long = insertSyncPackage(helper, "PROCESS_2", "TYPE_1", AnalyticEntry.STATE_DELIVERY_PENDING, "", "", "")
    val p21: Long = insertSyncPackage(helper, "PROCESS_2", "TYPE_2", AnalyticEntry.STATE_NEW, "", "", "")
    val p22: Long = insertSyncPackage(helper, "PROCESS_2", "TYPE_2", AnalyticEntry.STATE_DELIVERY_FAILED, "", "", "")
    val p23: Long = insertSyncPackage(helper, "PROCESS_2", "TYPE_2", AnalyticEntry.STATE_DELIVERY_IN_PROGRESS, "", "", "")
    val p24: Long = insertSyncPackage(helper, "PROCESS_2", "TYPE_2", AnalyticEntry.STATE_DELIVERY_PENDING, "", "", "")
    // @formatter:on

    dataSource.markPackagesForDelivery("PROCESS_1", "TYPE_1")
    val packages = dataSource.getPackagesByProcessIdAndState(
      "PROCESS_1",
      "TYPE_1",
      AnalyticEntry.STATE_DELIVERY_PENDING,
      0,
      10,
    )

    Assert.assertNotNull(packages)
    assertTrue(packages.size == 8)
    assertEquals(packages[0].id, p1)
    assertEquals(packages[1].id, p2)
    assertEquals(packages[2].id, p4)
    assertEquals(packages[3].id, p9)
    assertEquals(packages[4].id, p10)
    assertEquals(packages[5].id, p12)
    assertEquals(packages[6].id, p17)
    assertEquals(packages[7].id, p18)
  }

  @Test
  fun testUpdateStatuses() {
    val dataSource = AnalyticsSQLiteDataSource(helper, options)
    // @formatter:off
    val p1: Long = insertSyncPackage(helper, null, "TYPE_1", AnalyticEntry.STATE_NEW, "", "", "")
    val p2: Long = insertSyncPackage(helper, "PROCESS_1", "TYPE_1", AnalyticEntry.STATE_DELIVERY_FAILED, "", "", "")
    val p3: Long = insertSyncPackage(helper, "PROCESS_1", "TYPE_1", AnalyticEntry.STATE_DELIVERY_IN_PROGRESS, "", "", "")
    val p4: Long = insertSyncPackage(helper, "PROCESS_1", "TYPE_1", AnalyticEntry.STATE_DELIVERY_PENDING, "", "", "")
    // @formatter:on

    val pkg1 = AnalyticEntry(id = p1)
    val pkg2 = AnalyticEntry(id = p2)
    val pkg3 = AnalyticEntry(id = p3)
    val pkg4 = AnalyticEntry(id = p4)

    val packagesToUpdate = listOf(pkg1, pkg2, pkg3, pkg4)
    dataSource.updateStatuses(packagesToUpdate, AnalyticEntry.STATE_NEW)

    val packages = getAllSyncPackages(helper)
    Assert.assertNotNull(packages)
    assertTrue(packages.size == 4)
    packages.forEach {
      assertEquals(it.state, AnalyticEntry.STATE_NEW)
    }
  }

  @Test
  fun testResetPackages() {
    val dataSource = AnalyticsSQLiteDataSource(helper, options)
    // @formatter:off
    val p1: Long = insertSyncPackage(helper, null, "TYPE_1", AnalyticEntry.STATE_NEW, "", "", "")
    val p2: Long = insertSyncPackage(helper, "PROCESS_1", "TYPE_1", AnalyticEntry.STATE_DELIVERY_FAILED, "", "", "")
    val p3: Long = insertSyncPackage(helper, "PROCESS_1", "TYPE_1", AnalyticEntry.STATE_DELIVERY_IN_PROGRESS, "", "", "")
    val p4: Long = insertSyncPackage(helper, "PROCESS_1", "TYPE_1", AnalyticEntry.STATE_DELIVERY_PENDING, "", "", "")
    // @formatter:on

    dataSource.resetPackages()

    val packages = getAllSyncPackages(helper)
    Assert.assertNotNull(packages)
    assertTrue(packages.size == 4)
    packages.forEach {
      assertEquals(it.state, AnalyticEntry.STATE_NEW)
      assertTrue(it.processId == null)
    }
  }
}
