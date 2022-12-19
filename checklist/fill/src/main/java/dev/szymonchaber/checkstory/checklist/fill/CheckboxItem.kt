package dev.szymonchaber.checkstory.checklist.fill

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.Checkbox
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import dev.szymonchaber.checkstory.design.views.LinkifyText
import dev.szymonchaber.checkstory.domain.model.checklist.fill.Checkbox

@Composable
fun CheckboxItem(
    modifier: Modifier = Modifier,
    checkbox: Checkbox,
    onCheckedChange: (Boolean) -> Unit,
    icon: @Composable RowScope.() -> Unit = {}
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row {
            Checkbox(
                modifier = Modifier.align(Alignment.CenterVertically),
                checked = checkbox.isChecked,
                onCheckedChange = onCheckedChange
            )
            LinkifyText(
                modifier = Modifier.align(Alignment.CenterVertically),
                text = checkbox.title
            )
        }
        Row(
            modifier = Modifier
                .weight(1f, false)
        ) {
            icon()
        }
    }
}
