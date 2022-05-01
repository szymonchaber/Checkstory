package dev.szymonchaber.checkstory.notifications

import com.google.firebase.messaging.FirebaseMessagingService

class CheckstoryMessagingService : FirebaseMessagingService() {

    override fun onNewToken(token: String) = Unit // TODO handle when we get actual backend
}
