package dev.szymonchaber.checkstory.data.repository

import com.google.common.truth.Truth.assertThat
import dev.szymonchaber.checkstory.data.database.model.command.CommandEntity
import dev.szymonchaber.checkstory.data.database.model.command.TemplateCommandEntity
import dev.szymonchaber.checkstory.domain.model.TemplateCommand
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import org.junit.Test
import java.util.*

internal class CommandMapperTest {

    @Test
    fun shouldDecodeCorrectly(): Unit {
        // given
        val jsonData =
            "{\"type\":\"createTemplate\",\"templateId\":\"33409752-5214-4c1d-ac31-3f25f2a31016\",\"timestamp\":\"2023-06-05T20:17:33.954Z\",\"commandId\":\"27e6b60e-2ee6-47a6-ac22-224c2935af78\",\"existingData\":{\"id\":\"33409752-5214-4c1d-ac31-3f25f2a31016\",\"title\":\"This firebase token might simply be correct XD\",\"description\":\"Description\",\"items\":[{\"id\":\"3cb214f7-6a3f-404c-9fb5-f38e9090cb9c\",\"templateId\":\"33409752-5214-4c1d-ac31-3f25f2a31016\",\"title\":\"Task 2\",\"children\":[{\"id\":\"f58fde40-512c-40a9-a27f-3e9e199158b3\",\"templateId\":\"33409752-5214-4c1d-ac31-3f25f2a31016\",\"parentTaskId\":\"3cb214f7-6a3f-404c-9fb5-f38e9090cb9c\",\"title\":\"Task 3 - deleted later\",\"children\":[],\"sortPosition\":1}],\"sortPosition\":0},{\"id\":\"41f9653f-e669-418c-ba95-25de34cb6aea\",\"templateId\":\"33409752-5214-4c1d-ac31-3f25f2a31016\",\"title\":\"Task 1\",\"children\":[],\"sortPosition\":1}],\"createdAt\":\"2023-06-05T20:17:33.954Z\",\"reminders\":[{\"type\":\"exact\",\"id\":\"d2ca1ae0-2f4b-4cc2-99ff-47178987854b\",\"forTemplate\":\"33409752-5214-4c1d-ac31-3f25f2a31016\",\"startDateTime\":\"2023-06-06T03:14:00.036\"},{\"type\":\"recurring\",\"id\":\"edc59d21-f02f-4020-b0a7-62db3526d33c\",\"forTemplate\":\"33409752-5214-4c1d-ac31-3f25f2a31016\",\"startDateTime\":\"2023-06-06T03:14:00.159\",\"interval\":{\"type\":\"daily\"}},{\"type\":\"recurring\",\"id\":\"f7f8e9a9-ee1e-4ee2-b409-47ef3098435b\",\"forTemplate\":\"33409752-5214-4c1d-ac31-3f25f2a31016\",\"startDateTime\":\"2023-06-06T03:14:00.468\",\"interval\":{\"type\":\"weekly\",\"dayOfWeek\":\"TUESDAY\"}},{\"type\":\"recurring\",\"id\":\"47a91bce-14a2-4684-b1fb-b2abc91766cf\",\"forTemplate\":\"33409752-5214-4c1d-ac31-3f25f2a31016\",\"startDateTime\":\"2023-06-06T03:14:00.611\",\"interval\":{\"type\":\"monthly\",\"dayOfMonth\":1}},{\"type\":\"recurring\",\"id\":\"ae1b765f-83af-4371-b97b-df61357642d3\",\"forTemplate\":\"33409752-5214-4c1d-ac31-3f25f2a31016\",\"startDateTime\":\"2023-06-06T03:15:00.243\",\"interval\":{\"type\":\"yearly\",\"dayOfYear\":1}}]}}"

        // when
        val toDomainCommand = CommandMapper().toDomainCommand(
            CommandEntity(
                id = UUID.fromString("27e6b60e-2ee6-47a6-ac22-224c2935af78"),
                type = "templateCommand",
                jsonData = jsonData
            )
        )

        // then
        assertThat((toDomainCommand as TemplateCommand.CreateNewTemplate).existingData).isNotNull()
    }

    @Test
    fun shouldDecodeMoreCorrectly(): Unit {
        // given
        val jsonData =
            "{\"type\":\"createTemplate\",\"templateId\":\"33409752-5214-4c1d-ac31-3f25f2a31016\",\"timestamp\":\"2023-06-05T20:17:33.954Z\",\"commandId\":\"27e6b60e-2ee6-47a6-ac22-224c2935af78\",\"existingData\":{\"id\":\"33409752-5214-4c1d-ac31-3f25f2a31016\",\"title\":\"This firebase token might simply be correct XD\",\"description\":\"Description\",\"items\":[{\"id\":\"3cb214f7-6a3f-404c-9fb5-f38e9090cb9c\",\"templateId\":\"33409752-5214-4c1d-ac31-3f25f2a31016\",\"title\":\"Task 2\",\"children\":[{\"id\":\"f58fde40-512c-40a9-a27f-3e9e199158b3\",\"templateId\":\"33409752-5214-4c1d-ac31-3f25f2a31016\",\"parentTaskId\":\"3cb214f7-6a3f-404c-9fb5-f38e9090cb9c\",\"title\":\"Task 3 - deleted later\",\"children\":[],\"sortPosition\":1}],\"sortPosition\":0},{\"id\":\"41f9653f-e669-418c-ba95-25de34cb6aea\",\"templateId\":\"33409752-5214-4c1d-ac31-3f25f2a31016\",\"title\":\"Task 1\",\"children\":[],\"sortPosition\":1}],\"createdAt\":\"2023-06-05T20:17:33.954Z\",\"reminders\":[{\"type\":\"exact\",\"id\":\"d2ca1ae0-2f4b-4cc2-99ff-47178987854b\",\"forTemplate\":\"33409752-5214-4c1d-ac31-3f25f2a31016\",\"startDateTime\":\"2023-06-06T03:14:00.036\"},{\"type\":\"recurring\",\"id\":\"edc59d21-f02f-4020-b0a7-62db3526d33c\",\"forTemplate\":\"33409752-5214-4c1d-ac31-3f25f2a31016\",\"startDateTime\":\"2023-06-06T03:14:00.159\",\"interval\":{\"type\":\"daily\"}},{\"type\":\"recurring\",\"id\":\"f7f8e9a9-ee1e-4ee2-b409-47ef3098435b\",\"forTemplate\":\"33409752-5214-4c1d-ac31-3f25f2a31016\",\"startDateTime\":\"2023-06-06T03:14:00.468\",\"interval\":{\"type\":\"weekly\",\"dayOfWeek\":\"TUESDAY\"}},{\"type\":\"recurring\",\"id\":\"47a91bce-14a2-4684-b1fb-b2abc91766cf\",\"forTemplate\":\"33409752-5214-4c1d-ac31-3f25f2a31016\",\"startDateTime\":\"2023-06-06T03:14:00.611\",\"interval\":{\"type\":\"monthly\",\"dayOfMonth\":1}},{\"type\":\"recurring\",\"id\":\"ae1b765f-83af-4371-b97b-df61357642d3\",\"forTemplate\":\"33409752-5214-4c1d-ac31-3f25f2a31016\",\"startDateTime\":\"2023-06-06T03:15:00.243\",\"interval\":{\"type\":\"yearly\",\"dayOfYear\":1}}]}}"

        // when
        val toDomainCommand =
            Json {
                explicitNulls = false
            }.decodeFromString<TemplateCommandEntity>(jsonData).toDomainCommand()

        // then
        assertThat((toDomainCommand as TemplateCommand.CreateNewTemplate).existingData).isNotNull()
    }
}
