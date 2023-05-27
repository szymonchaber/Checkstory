package dev.szymonchaber.checkstory.data.synchronization

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequest
import androidx.work.OutOfQuotaPolicy
import androidx.work.PeriodicWorkRequest
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
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

    companion object {

        private const val WORK_NAME_SYNCHRONIZATION = "synchronization"

        fun scheduleExpedited(workManager: WorkManager) {
            workManager.enqueueUniqueWork(
                WORK_NAME_SYNCHRONIZATION,
                ExistingWorkPolicy.KEEP,
                OneTimeWorkRequest.Builder(SynchronizationWorker::class.java)
                    .setConstraints(
                        Constraints.Builder()
                            .setRequiredNetworkType(NetworkType.CONNECTED)
                            .build()
                    )
                    .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
                    .build()
            )
        }

        fun scheduleRepeating(workManager: WorkManager) {
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
            )
        }
    }
}
