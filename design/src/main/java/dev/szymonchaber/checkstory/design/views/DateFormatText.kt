package dev.szymonchaber.checkstory.design.views

import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*

@Composable
fun DateFormatText(
    localDateTime: LocalDateTime,
    modifier: Modifier = Modifier
) {
    val format = remember {
        DateTimeFormatter.ofPattern("dd MMM, HH:mm", Locale.getDefault())
    }
    Text(
        modifier = modifier,
        text = localDateTime.format(format),
        style = MaterialTheme.typography.caption,
    )
}


@Preview(showBackground = true)
@Composable
fun DateFormatTextPreview() {
    DateFormatText(localDateTime = LocalDateTime.now())
}
