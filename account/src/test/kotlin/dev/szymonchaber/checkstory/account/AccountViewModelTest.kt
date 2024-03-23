package dev.szymonchaber.checkstory.account

import app.cash.turbine.turbineScope
import com.google.common.truth.Truth.assertThat
import dev.szymonchaber.checkstory.domain.model.User
import dev.szymonchaber.checkstory.domain.usecase.GetCurrentUserUseCase
import dev.szymonchaber.checkstory.test.MainDispatcherRule
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock

class AccountViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    val getCurrentUserUseCase = mock<GetCurrentUserUseCase> {
        onBlocking { getCurrentUser() } doReturn User.Guest()
    }

    val viewModel by lazy {
        AccountViewModel(
            tracker = mock(),
            loginUseCase = mock(),
            registerUseCase = mock(),
            getCurrentUserUseCase = getCurrentUserUseCase,
            logoutUseCase = mock(),
            deleteAccountUseCase = mock(),
        )
    }

    @Test
    fun `when upgrade clicked, then should navigate to purchase screen`() = runTest {
        turbineScope {
            // given
            val effectEvents = viewModel.effect.testIn(backgroundScope)

            // when
            viewModel.onEvent(AccountEvent.UpgradeClicked)

            // then
            assertThat(effectEvents.awaitItem()).isEqualTo(AccountEffect.NavigateToPurchaseScreen)
        }
    }
}
