package dev.szymonchaber.checkstory.data.api.payment

import dev.szymonchaber.checkstory.data.di.ConfiguredHttpClient
import dev.szymonchaber.checkstory.domain.interactor.UserPaymentInteractor
import dev.szymonchaber.checkstory.domain.model.Result
import dev.szymonchaber.checkstory.domain.model.payment.PurchaseToken
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import kotlinx.serialization.Serializable
import javax.inject.Inject

internal class UserPaymentInteractorImpl @Inject constructor(private val client: ConfiguredHttpClient) :
    UserPaymentInteractor {

    override suspend fun assignPaymentToCurrentUser(purchaseToken: PurchaseToken): Result<Exception, Unit> {
        return try {
            client.post("/payment/google-play-token") {
                setBody(AssignPurchaseTokenPayload(purchaseToken.token))
            }
            Result.success(Unit)
        } catch (exception: Exception) {
            Result.error(exception)
        }
    }
}

@Serializable
internal data class AssignPurchaseTokenPayload(val token: String)
