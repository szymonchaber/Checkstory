package dev.szymonchaber.checkstory.checklist.template.model

import app.cash.turbine.turbineScope
import dev.szymonchaber.checkstory.common.Tracker
import dev.szymonchaber.checkstory.domain.model.User
import dev.szymonchaber.checkstory.domain.usecase.GetCurrentUserUseCase
import dev.szymonchaber.checkstory.domain.usecase.GetTemplateUseCase
import dev.szymonchaber.checkstory.domain.usecase.StoreCommandsUseCase
import dev.szymonchaber.checkstory.test.MainDispatcherRule
import kotlinx.coroutines.test.runTest
import org.junit.Assert
import org.junit.Rule
import org.junit.Test
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
            Assert.assertEquals(EditTemplateEffect.CloseScreen, effectEvents.awaitItem())
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
            Assert.assertEquals(EditTemplateEffect.ShowConfirmExitDialog, effectEvents.awaitItem())
        }
    }
}
