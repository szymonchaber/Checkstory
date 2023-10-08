package dev.szymonchaber.checkstory.payments.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import dev.szymonchaber.checkstory.design.R

@Composable
internal fun Features() {
    Column(Modifier.padding(top = 16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        FeatureLine(stringResource(R.string.ads_free_experience))
        FeatureLine(stringResource(R.string.unlimited_templates))
        FeatureLine(stringResource(R.string.unlimited_history))
        FeatureLine(stringResource(R.string.unlimited_reminders))
    }
}
