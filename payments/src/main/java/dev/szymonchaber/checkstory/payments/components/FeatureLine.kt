package dev.szymonchaber.checkstory.payments.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.szymonchaber.checkstory.design.R

@Composable
internal fun FeatureLine(text: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
    ) {
        Image(
            modifier = Modifier.align(Alignment.CenterVertically),
            painter = painterResource(id = R.drawable.ic_check),
            contentDescription = null
        )
        Text(
            modifier = Modifier
                .padding(start = 16.dp)
                .fillMaxWidth()
                .align(Alignment.CenterVertically),
            text = text,
        )
    }
}

@Preview(showBackground = true)
@Composable
internal fun FeatureLinePreview() {
    FeatureLine("Unlimited checklists per template - start filling a checklist with a single click")
}
