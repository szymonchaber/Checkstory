package dev.szymonchaber.checkstory.api.template.model

import kotlinx.datetime.DayOfWeek
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal sealed interface ApiInterval {

    @Serializable
    @SerialName("daily")
    object Daily : ApiInterval

    @Serializable
    @SerialName("weekly")
    data class Weekly(val dayOfWeek: DayOfWeek) : ApiInterval

    @Serializable
    @SerialName("monthly")
    data class Monthly(val dayOfMonth: Int) : ApiInterval

    @Serializable
    @SerialName("yearly")
    data class Yearly(val dayOfYear: Int) : ApiInterval
}
