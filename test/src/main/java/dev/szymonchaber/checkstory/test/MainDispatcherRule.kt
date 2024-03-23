package dev.szymonchaber.checkstory.test

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain

class MainDispatcherRule(
    val testDispatcher: kotlinx.coroutines.test.TestDispatcher = kotlinx.coroutines.test.UnconfinedTestDispatcher(),
) : org.junit.rules.TestWatcher() {

    override fun starting(description: org.junit.runner.Description) {
        Dispatchers.setMain(testDispatcher)
        Dispatchers.setMain(testDispatcher)
    }

    override fun finished(description: org.junit.runner.Description) {
        Dispatchers.resetMain()
    }
}
