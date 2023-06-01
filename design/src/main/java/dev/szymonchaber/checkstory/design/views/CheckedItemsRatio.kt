package dev.szymonchaber.checkstory.design.views

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import dev.szymonchaber.checkstory.design.R
import dev.szymonchaber.checkstory.domain.model.checklist.fill.Checklist
import dev.szymonchaber.checkstory.domain.model.checklist.fill.Task.Companion.checkedCount

@Composable
fun CheckedItemsRatio(
    checklist: Checklist,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
    ) {
        val flattenedItems = checklist.flattenedItems
        val checkedCount = flattenedItems.checkedCount()
        val allCount = flattenedItems.count()
        val ratio = "$checkedCount/$allCount"
        Text(
            modifier = Modifier.align(Alignment.CenterVertically),
            style = MaterialTheme.typography.caption,
            text = ratio
        )
        Spacer(modifier = Modifier.size(4.dp))
        Icon(
            modifier = Modifier.size(16.dp),
            painter = painterResource(R.drawable.checkbox_marked),
            contentDescription = null
        )
    }
}
