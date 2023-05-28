package dev.szymonchaber.checkstory.notifications

import androidx.work.WorkManager
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import dagger.hilt.android.AndroidEntryPoint
import dev.szymonchaber.checkstory.data.synchronization.SynchronizationWorker
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import timber.log.Timber

@AndroidEntryPoint
class CheckstoryMessagingService : FirebaseMessagingService() {

    private val job = SupervisorJob()
    private val scope = CoroutineScope(Dispatchers.IO + job)

    override fun onNewToken(token: String) = Unit // TODO handle when we get actual backend

    override fun onMessageReceived(message: RemoteMessage) {
        Timber.e("Got message! ${message.notification}")
        scope.launch {
            SynchronizationWorker.scheduleExpeditedUnlessOngoing(WorkManager.getInstance(application))
        }
    }

    override fun onDestroy() {
        job.cancel()
        super.onDestroy()
    }
}
