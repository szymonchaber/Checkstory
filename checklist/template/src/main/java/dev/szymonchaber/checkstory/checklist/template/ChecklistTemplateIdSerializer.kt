package dev.szymonchaber.checkstory.checklist.template

import com.ramcosta.composedestinations.navargs.DestinationsNavTypeSerializer
import com.ramcosta.composedestinations.navargs.NavTypeSerializer
import dev.szymonchaber.checkstory.domain.model.checklist.template.ChecklistTemplateId

@NavTypeSerializer
internal class ChecklistTemplateIdSerializer : DestinationsNavTypeSerializer<ChecklistTemplateId> {

    override fun toRouteString(value: ChecklistTemplateId): String {
        return value.id.toString()
    }

    override fun fromRouteString(routeStr: String): ChecklistTemplateId {
        return ChecklistTemplateId(routeStr.toLong())
    }
}
