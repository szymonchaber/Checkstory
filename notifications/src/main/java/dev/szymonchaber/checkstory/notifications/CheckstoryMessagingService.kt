package dev.szymonchaber.checkstory.notifications

import androidx.work.WorkManager
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import dagger.hilt.android.AndroidEntryPoint
import dev.szymonchaber.checkstory.data.synchronization.SynchronizationWorker
import dev.szymonchaber.checkstory.domain.usecase.LoginUseCase
import dev.szymonchaber.checkstory.domain.usecase.PushFirebaseMessagingTokenUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint
class CheckstoryMessagingService : FirebaseMessagingService() {

    private val job = SupervisorJob()
    private val scope = CoroutineScope(Dispatchers.IO + job)

    @Inject
    lateinit var loginUseCase: LoginUseCase

    @Inject
    lateinit var firebaseTokenUseCase: PushFirebaseMessagingTokenUseCase

    override fun onNewToken(token: String) {
        scope.launch {
            firebaseTokenUseCase.pushFirebaseMessagingToken(token)
        }
    }

    override fun onMessageReceived(message: RemoteMessage) {
        Timber.e("Got message! ${message.notification}")
        scope.launch {
            SynchronizationWorker.scheduleExpeditedUnlessOngoing(WorkManager.getInstance(application))
            loginUseCase.login()
        }
    }

    override fun onDestroy() {
        job.cancel()
        super.onDestroy()
    }
}
