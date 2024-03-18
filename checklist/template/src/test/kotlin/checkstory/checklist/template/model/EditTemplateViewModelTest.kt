package checkstory.checklist.template.model

import app.cash.turbine.turbineScope
import dev.szymonchaber.checkstory.checklist.template.model.EditTemplateEffect
import dev.szymonchaber.checkstory.checklist.template.model.EditTemplateEvent
import dev.szymonchaber.checkstory.checklist.template.model.EditTemplateViewModel
import dev.szymonchaber.checkstory.checklist.template.model.OnboardingTemplateFactory
import dev.szymonchaber.checkstory.common.Tracker
import dev.szymonchaber.checkstory.domain.model.User
import dev.szymonchaber.checkstory.domain.usecase.GetCurrentUserUseCase
import dev.szymonchaber.checkstory.domain.usecase.GetTemplateUseCase
import dev.szymonchaber.checkstory.domain.usecase.StoreCommandsUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestWatcher
import org.junit.runner.Description
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock

class EditTemplateViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    val getCurrentUserUseCase = mock<GetCurrentUserUseCase> {
        onBlocking { getCurrentUser() } doReturn User.Guest()
    }

    val viewModel by lazy {
        EditTemplateViewModel(
            mock<OnboardingTemplateFactory>(),
            mock<GetTemplateUseCase>(),
            getCurrentUserUseCase,
            mock<StoreCommandsUseCase>(),
            mock<Tracker>()
        )
    }

    @Test
    fun `given new template with no other changes, when close clicked, then should close the screen`() = runTest {
        turbineScope {
            // given
            val effectEvents = viewModel.effect.testIn(backgroundScope)
            viewModel.onEvent(EditTemplateEvent.CreateTemplate)

            // when
            viewModel.onEvent(EditTemplateEvent.BackClicked)

            // then
            assertEquals(EditTemplateEffect.CloseScreen, effectEvents.awaitItem())
        }
    }

    @Test
    fun `given new template with some changes, when close clicked, then show exit confirmation dialog`() = runTest {
        turbineScope {
            // given
            val effectEvents = viewModel.effect.testIn(backgroundScope)
            viewModel.onEvent(EditTemplateEvent.CreateTemplate)
            viewModel.onEvent(EditTemplateEvent.AddTaskClicked)

            // when
            viewModel.onEvent(EditTemplateEvent.BackClicked)

            // then
            assertEquals(EditTemplateEffect.ShowConfirmExitDialog, effectEvents.awaitItem())
        }
    }
}

class MainDispatcherRule(
    val testDispatcher: TestDispatcher = UnconfinedTestDispatcher(),
) : TestWatcher() {

    override fun starting(description: Description) {
        Dispatchers.setMain(testDispatcher)
        Dispatchers.setMain(testDispatcher)
    }

    override fun finished(description: Description) {
        Dispatchers.resetMain()
    }
}
