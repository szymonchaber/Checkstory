package dev.szymonchaber.checkstory.checklist.template

import com.ramcosta.composedestinations.navargs.DestinationsNavTypeSerializer
import com.ramcosta.composedestinations.navargs.NavTypeSerializer
import dev.szymonchaber.checkstory.domain.model.checklist.template.TemplateId
import java.util.*

@NavTypeSerializer
internal class TemplateIdSerializer : DestinationsNavTypeSerializer<TemplateId> {

    override fun toRouteString(value: TemplateId): String {
        return value.id.toString()
    }

    override fun fromRouteString(routeStr: String): TemplateId {
        return TemplateId(UUID.fromString(routeStr))
    }
}
