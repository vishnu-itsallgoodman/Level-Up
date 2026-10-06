package com.levelup.app.utils

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.*
import com.levelup.app.data.database.LevelUpDatabase
import com.levelup.app.widget.updateAllWidgets
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.util.concurrent.TimeUnit

/**
 * Periodic worker that:
 *  1. Updates the home-screen widget with fresh data.
 *  2. (Stub) Sends a reminder notification if configured.
 *
 * Scheduled to run every 30 minutes so the widget stays reasonably fresh.
 */
@HiltWorker
class WidgetRefreshWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val database: LevelUpDatabase
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        updateAllWidgets(applicationContext, database)
        return Result.success()
    }

    companion object {
        private const val WORK_NAME = "widget_refresh"

        fun schedule(context: Context) {
            val request = PeriodicWorkRequestBuilder<WidgetRefreshWorker>(30, TimeUnit.MINUTES)
                .setConstraints(Constraints.Builder().build())
                .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                request
            )
        }
    }
}
