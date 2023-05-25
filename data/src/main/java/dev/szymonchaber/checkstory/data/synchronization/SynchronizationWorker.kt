package dev.szymonchaber.checkstory.data.synchronization

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

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

        fun schedule(workManager: WorkManager) {
            workManager
                .enqueue(
                    OneTimeWorkRequest.Builder(SynchronizationWorker::class.java)
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
