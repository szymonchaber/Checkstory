package dev.szymonchaber.checkstory.checklist.fill

import com.ramcosta.composedestinations.navargs.DestinationsNavTypeSerializer
import com.ramcosta.composedestinations.navargs.NavTypeSerializer
import dev.szymonchaber.checkstory.domain.model.checklist.fill.ChecklistId

@NavTypeSerializer
class ChecklistIdSerializer : DestinationsNavTypeSerializer<ChecklistId> {

    override fun toRouteString(value: ChecklistId): String {
        return value.id.toString()
    }

    override fun fromRouteString(routeStr: String): ChecklistId {
        return ChecklistId(routeStr.toLong())
    }
}
