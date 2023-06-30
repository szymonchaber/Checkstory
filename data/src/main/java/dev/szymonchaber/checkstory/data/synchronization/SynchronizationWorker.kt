package dev.szymonchaber.checkstory.data.synchronization

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.hilt.work.HiltWorker
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.ForegroundInfo
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequest
import androidx.work.OutOfQuotaPolicy
import androidx.work.PeriodicWorkRequest
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import androidx.work.await
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import dev.szymonchaber.checkstory.design.R
import java.time.Duration

@HiltWorker
class SynchronizationWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val synchronizer: SynchronizerImpl
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            synchronizer.performSynchronization()
            Result.success()
        } catch (exception: Exception) {
            Result.retry()
        }
    }

    override suspend fun getForegroundInfo(): ForegroundInfo {
        return ForegroundInfo(SYNCHRONIZATION_NOTIFICATION_ID, createSynchronizationNotification())
    }

    private fun createSynchronizationNotification(): Notification {
        createNotificationChannel()
        return NotificationCompat.Builder(applicationContext, SYNCHRONIZATION_CHANNEL_ID)
            .setSmallIcon(R.drawable.checkbox_marked)
            .setContentTitle(applicationContext.getString(R.string.synchronization_ongoing_title))
            .build()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createChannel(
                applicationContext.getString(R.string.synchronization_channel_name),
                applicationContext.getString(R.string.synchronization_channel_description),
                SYNCHRONIZATION_CHANNEL_ID,
                NotificationManager.IMPORTANCE_LOW
            )
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createChannel(name: String, description: String, channelId: String, importance: Int) {
        val channel = NotificationChannel(
            channelId,
            name,
            importance
        ).apply {
            this.description = description
        }
        val notificationManager =
            applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }

    companion object {

        private const val SYNCHRONIZATION_CHANNEL_ID = "synchronization"
        private const val SYNCHRONIZATION_NOTIFICATION_ID = 1000

        private const val WORK_NAME_SYNCHRONIZATION = "synchronization"

        suspend fun forceScheduleExpedited(workManager: WorkManager) {
            scheduleExpeditedActual(workManager, ExistingWorkPolicy.REPLACE)
        }

        suspend fun scheduleExpeditedUnlessOngoing(workManager: WorkManager) {
            scheduleExpeditedActual(workManager, ExistingWorkPolicy.KEEP)
        }

        private suspend fun scheduleExpeditedActual(workManager: WorkManager, existingWorkPolicy: ExistingWorkPolicy) {
            workManager.enqueueUniqueWork(
                WORK_NAME_SYNCHRONIZATION,
                existingWorkPolicy,
                OneTimeWorkRequest.Builder(SynchronizationWorker::class.java)
                    .setConstraints(
                        Constraints.Builder()
                            .setRequiredNetworkType(NetworkType.CONNECTED)
                            .build()
                    )
                    .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
                    .build()
            ).await()
        }

        suspend fun scheduleRepeating(workManager: WorkManager) {
            workManager.enqueueUniquePeriodicWork(
                WORK_NAME_SYNCHRONIZATION,
                ExistingPeriodicWorkPolicy.KEEP,
                PeriodicWorkRequest.Builder(SynchronizationWorker::class.java, Duration.ofMinutes(15))
                    .setConstraints(
                        Constraints.Builder()
                            .setRequiredNetworkType(NetworkType.CONNECTED)
                            .build()
                    )
                    .build()
            ).await()
        }
    }
}
