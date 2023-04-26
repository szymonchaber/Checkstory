package dev.szymonchaber.checkstory.data.api.event

import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import dev.szymonchaber.checkstory.data.api.event.mapper.toCommandDto
import dev.szymonchaber.checkstory.domain.model.DomainCommand
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import javax.inject.Inject

internal class CommandsApi @Inject constructor(private val httpClient: HttpClient) {

    suspend fun pushCommands(editTemplateDomainCommands: List<DomainCommand>) {
        val currentUser = Firebase.auth.currentUser!!
        val token = currentUser.getIdToken(false).result!!.token
        val eventDtos = editTemplateDomainCommands.map {
            it.toCommandDto(currentUser)
        }
            .shuffled() // TODO delete when it's confirmed to be working
        return httpClient.post("http://10.0.2.2:8080/commands") {
            header("Authorization", "Bearer $token")
            setBody(eventDtos)
        }.body()
    }
}
