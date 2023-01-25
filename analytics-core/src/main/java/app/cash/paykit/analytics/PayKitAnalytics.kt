package app.cash.paykit.analytics

import android.content.Context
import app.cash.paykit.analytics.core.Deliverable
import app.cash.paykit.analytics.core.DeliveryHandler
import app.cash.paykit.analytics.core.DeliveryWorker
import app.cash.paykit.analytics.persistence.EntriesDataSource
import app.cash.paykit.analytics.persistence.sqlite.AnalyticsSQLiteDataSource
import app.cash.paykit.analytics.persistence.sqlite.AnalyticsSqLiteHelper
import java.util.Locale
import java.util.concurrent.ExecutionException
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.FutureTask
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean

class PayKitAnalytics constructor(
  private val context: Context,
  private val options: AnalyticsOptions = AnalyticsOptions(),
  private val sqLiteHelper: AnalyticsSqLiteHelper = AnalyticsSqLiteHelper(
    context = context,
    options = options,
  ),
  val entriesDataSource: EntriesDataSource = AnalyticsSQLiteDataSource(
    sqLiteHelper = sqLiteHelper,
    options = options,
  ),
  private val logger: AnalyticsLogger = AnalyticsLogger(options = options),
  vararg deliveryHandlers: DeliveryHandler,
) {
  private val TAG = "PayKitAnalytics"

  private var deliveryTasks = mutableListOf<FutureTask<Unit>>()

  private var deliveryHandlers = mutableListOf<DeliveryHandler>().apply {
    deliveryHandlers.map {
      add(it)
      registerDeliveryHandler(it)
    }
  }

  private var executor: ExecutorService? = null

  private var scheduler: ScheduledExecutorService? = null

  private var shouldShutdown = AtomicBoolean(false)

  init {
    entriesDataSource.resetEntries()
    ensureExecutorIsUpAndRunning()
    ensureSchedulerIsUpAndRunning()
    logger.i(TAG, "Initialization completed.")
  }

  /**
   * Ensures that scheduler service is up and running and starts it if it is not.
   */
  private fun ensureSchedulerIsUpAndRunning() {
    scheduler?.run {
      if (isShutdown or isTerminated) {
        logger.w(TAG, "Recreating scheduler service after previous one was found to be shutdown.")
        initializeScheduledExecutorService()
      }
    } ?: run {
      logger.d(TAG, "Creating scheduler service.")
      initializeScheduledExecutorService()
    }
  }

  /**
   * Ensures that executor service is up and running and starts it if it is not.
   */
  private fun ensureExecutorIsUpAndRunning() {
    executor?.run {
      if (isShutdown or isTerminated) {
        logger.w(TAG, "Recreating executor service after previous one was found to be shutdown.")
        executor = Executors.newSingleThreadExecutor()
      }
    } ?: run {
      logger.d(TAG, "Creating executor service.")
      executor = Executors.newSingleThreadExecutor()
    }
  }

  /**
   * Starts the scheduler that will initiate synchronization task in regular intervals. Interval is defined in Options
   * .mPeriod and synchronization start will be delayed for number of seconds defined in Options.mDelay.
   */
  private fun initializeScheduledExecutorService() {
    shouldShutdown.getAndSet(false)
    scheduler = Executors.newSingleThreadScheduledExecutor().also {
      logger.d(
        TAG,
        java.lang.String.format(
          Locale.US,
          "Initializing scheduled executor service | delay:%ds, interval:%ds",
          options.delay.inWholeSeconds,
          options.interval.inWholeSeconds,
        ),
      )
      it.scheduleAtFixedRate({
        if (shouldShutdown.compareAndSet(true, false)) {
          shutdown()
          return@scheduleAtFixedRate
        }
        startDelivery(false)
      }, options.delay.inWholeSeconds, options.interval.inWholeSeconds, TimeUnit.SECONDS)
    }
  }

  /**
   * Removes tasks that are canceled or done from the queue.
   */
  private fun cleanupTaskQueue() {
    val itr: MutableIterator<FutureTask<Unit>> = deliveryTasks.iterator()
    while (itr.hasNext()) {
      itr.next().run {
        if (isCancelled || isDone) {
          logger.d(
            TAG,
            """Removing task from queue: ${toString()} (canceled=$isCancelled, done=$isDone)""",
          )
          itr.remove()
        }
      }
    }
  }

  /**
   * Creates a task for package synchronization and schedules it for execution. Tasks created by
   * this method will run in sequence and will not overlap with each other.
   *
   * @param blocking If true the method will execute synchronously.
   */
  @Synchronized
  private fun startDelivery(blocking: Boolean) {
    logger.v(TAG, "startDelivery($blocking)")
    ensureExecutorIsUpAndRunning()
    cleanupTaskQueue()
    DeliveryTask(entriesDataSource, deliveryHandlers, logger).also {
      deliveryTasks.add(it)
      executor!!.execute(it)
      if (blocking) {
        try {
          it.get()
        } catch (e: InterruptedException) {
          logger.w(TAG, "Blocking Delivery task interrupted")
        } catch (e: ExecutionException) {
          logger.w(TAG, "Could not execute blocking delivery task")
        }
      }
    }
  }

  private class DeliveryTask(
    dataSource: EntriesDataSource,
    handlers: List<DeliveryHandler>,
    logger: AnalyticsLogger,
  ) : FutureTask<Unit>(DeliveryWorker(dataSource, handlers, logger))

  fun scheduleShutdown() {
    shouldShutdown.getAndSet(true)
    logger.i(TAG, "Scheduled shutdown.")
  }

  private fun shutdown() {
    executor?.run {
      shutdown()
      logger.i(TAG, "Executor service shutdown.")
    }
    scheduler?.run {
      shutdown()
      logger.i(TAG, "Scheduled executor service shutdown.")
    }
    if (deliveryTasks.isNotEmpty()) {
      deliveryTasks.clear()
      logger.i(TAG, "FutureTask list cleared.")
    }

    // TODO shutdown the database?
    logger.i(TAG, "Shutdown completed.")
  }

  @Synchronized
  fun registerDeliveryHandler(handler: DeliveryHandler) {
    val existingHandler: DeliveryHandler? = getDeliveryHandler(handler.deliverableType)
    if (existingHandler == null) {
      handler.setDependencies(entriesDataSource, logger)
      deliveryHandlers.add(handler)
      logger.i(
        TAG,
        "Registering %s as delivery handler for %s".format(
          Locale.US,
          handler.javaClass.simpleName,
          handler.deliverableType,
        ),
      )
    } else {
      logger.w(
        TAG,
        "Handler for %s deliverable is already registered: %s".format(
          Locale.US,
          handler.deliverableType,
          existingHandler.javaClass,
        ),
      )
    }
  }

  /**
   * Returns synchronization handler for given package type.
   *
   * @param deliverableType AnalyticEntry type
   * @return Synchronization handler
   */
  /*package*/
  fun getDeliveryHandler(deliverableType: String?): DeliveryHandler? {
    return deliveryHandlers.find {
      it.deliverableType.equals(
        deliverableType,
        ignoreCase = true,
      )
    }
  }

  @Synchronized
  fun scheduleForDelivery(
    type: String,
    content: String?,
    metaData: String?,
  ): ScheduleDeliverableTask {
    ensureSchedulerIsUpAndRunning()
    ensureExecutorIsUpAndRunning()
    val handler: DeliveryHandler? = getDeliveryHandler(type)
    return if (handler != null && handler.deliverableType.equals(type, ignoreCase = true)) {
      ScheduleDeliverableTask(type, content, metaData).also {
        executor!!.execute(it)
      }
    } else {
      val msg = "No registered handler for deliverable of type: $type"
      logger.e(TAG, msg)
      throw IllegalArgumentException(msg)
    }
  }

  @Synchronized
  fun scheduleForDelivery(deliverable: Deliverable): ScheduleDeliverableTask {
    return scheduleForDelivery(deliverable.type, deliverable.content, deliverable.metaData)
  }

  inner class ScheduleDeliverableTask(type: String?, content: String?, metaData: String?) :
    FutureTask<Long>({
      if (type != null && content != null) {
        val entryId: Long = entriesDataSource.insertEntry(type, content, metaData)
        if (entryId > 0) {
          logger.d(
            TAG,
            String.format("%s scheduled for delivery. id: %d", type, entryId),
          )
          entryId
        } else {
          logger.e(TAG, String.format("%s NOT scheduled for delivery!", type))
          null
        }
      } else {
        logger.e(
          TAG,
          "All deliverable must provide not null values for type and content.",
        )
        null
      }
    })

  /**
   * It will immediately try to send the deliverable.
   *
   * @param deliverable deliverable to send
   */
  @Synchronized
  fun dispatch(deliverable: Deliverable): ScheduleDeliverableTask {
    return dispatch(deliverable.type, deliverable.content, deliverable.metaData)
  }

  /**
   * It will immediately try to send the deliverable.
   *
   * @param type deliverable type
   * @param content deliverable content
   * @param metaData deliverable meta data
   * @return
   */
  @Synchronized
  fun dispatch(type: String, content: String?, metaData: String?): PayKitAnalytics.ScheduleDeliverableTask {
    val task = scheduleForDelivery(type, content, metaData)
    startDelivery(false)
    return task
  }
}
