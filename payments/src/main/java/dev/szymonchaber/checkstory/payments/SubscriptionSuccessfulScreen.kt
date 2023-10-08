package dev.szymonchaber.checkstory.payments

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.szymonchaber.checkstory.design.R

@Preview
@Composable
internal fun SubscriptionSuccessfulView() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 48.dp)
    ) {
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .clip(RoundedCornerShape(12.dp))
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFFD6CFFC),
                            Color(0xFFFDF9D8)
                        ),
                    )
                )
                .padding(30.dp)
        ) {
            Text(
                color = MaterialTheme.colors.onSecondary,
                fontWeight = FontWeight.Medium,
                text = stringResource(id = R.string.subscription_successful_thanks_for_support)
            )
        }
    }
}
