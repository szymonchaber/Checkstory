package dev.szymonchaber.checkstory.api.command.dto

import kotlinx.datetime.DayOfWeek
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal sealed interface ApiTemplateCommandInterval {

    @Serializable
    @SerialName("daily")
    object Daily : ApiTemplateCommandInterval

    @Serializable
    @SerialName("weekly")
    data class Weekly(val dayOfWeek: DayOfWeek) : ApiTemplateCommandInterval

    @Serializable
    @SerialName("monthly")
    data class Monthly(val dayOfMonth: Int) : ApiTemplateCommandInterval

    @Serializable
    @SerialName("yearly")
    data class Yearly(val dayOfYear: Int) : ApiTemplateCommandInterval
}
