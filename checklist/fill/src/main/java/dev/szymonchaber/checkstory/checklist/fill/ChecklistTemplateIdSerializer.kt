package dev.szymonchaber.checkstory.checklist.fill

import com.ramcosta.composedestinations.navargs.DestinationsNavTypeSerializer
import com.ramcosta.composedestinations.navargs.NavTypeSerializer
import dev.szymonchaber.checkstory.domain.model.checklist.template.ChecklistTemplateId

@NavTypeSerializer
class ChecklistTemplateIdSerializer : DestinationsNavTypeSerializer<ChecklistTemplateId> {

    override fun toRouteString(value: ChecklistTemplateId): String {
        return value.id.toString()
    }

    override fun fromRouteString(routeStr: String): ChecklistTemplateId {
        return ChecklistTemplateId(routeStr.toLong())
    }
}
