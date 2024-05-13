package dev.szymonchaber.checkstory.domain.usecase

import dev.szymonchaber.checkstory.domain.interactor.AuthInteractor
import dev.szymonchaber.checkstory.domain.interactor.UserPaymentInteractor
import dev.szymonchaber.checkstory.domain.model.Result
import dev.szymonchaber.checkstory.domain.model.Tier
import dev.szymonchaber.checkstory.domain.model.User
import dev.szymonchaber.checkstory.domain.model.payment.ActiveSubscription
import dev.szymonchaber.checkstory.domain.model.payment.PurchaseToken
import dev.szymonchaber.checkstory.domain.repository.PlayPaymentRepository
import dev.szymonchaber.checkstory.domain.repository.Synchronizer
import dev.szymonchaber.checkstory.domain.repository.UserRepository
import dev.szymonchaber.checkstory.test.MainDispatcherRule
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.stub
import org.mockito.kotlin.verify

class RegisterUseCaseTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val userPaymentInteractor = mock<UserPaymentInteractor> {
        onBlocking { assignPaymentToCurrentUser(any()) } doReturn Result.success(Unit)
    }

    private val playPaymentRepository = mock<PlayPaymentRepository>()

    private val authInteractor = mock<AuthInteractor> {
        onBlocking { register() } doReturn Result.success(
            User.LoggedIn(
                id = "id",
                email = "email@email.com",
                tier = Tier.FREE
            )
        )
        onBlocking { login() } doReturn Result.success(
            User.LoggedIn(
                id = "id",
                email = "email@email.com",
                tier = Tier.PAID
            )
        )
    }

    private val synchronizer = mock<Synchronizer> {
        onBlocking { hasUnsynchronizedCommands() } doReturn false
    }

    private val userRepository = mock<UserRepository>()

    val registerUseCase = RegisterUseCase(
        authInteractor = authInteractor,
        userRepository = userRepository,
        synchronizer = synchronizer,
        userPaymentInteractor = userPaymentInteractor,
        paymentRepository = playPaymentRepository,
        firebaseTokenProvider = mock(),
        pushFirebaseTokenUseCase = mock(),
    )

    @Test
    fun `given existing payment, when register called, then should register and assign the payment to new account`() =
        runTest {
            // given
            val existingPurchase = PurchaseToken("token")
            playPaymentRepository.stub {
                onBlocking { getActiveSubscription() } doReturn ActiveSubscription(existingPurchase)
            }

            // when
            registerUseCase.register()

            // then
            verify(userPaymentInteractor).assignPaymentToCurrentUser(existingPurchase)
        }

    @Test
    fun `given existing payment, when register called, then should login again after payment token upload & store that value`() =
        runTest {
            // given
            val existingPurchase = PurchaseToken("token")
            playPaymentRepository.stub {
                onBlocking { getActiveSubscription() } doReturn ActiveSubscription(existingPurchase)
            }

            // when
            registerUseCase.register()

            // then
            verify(authInteractor).login()
            verify(userRepository).storeCurrentUser(User.LoggedIn(id = "id", email = "email@email.com", Tier.PAID))
        }
}
