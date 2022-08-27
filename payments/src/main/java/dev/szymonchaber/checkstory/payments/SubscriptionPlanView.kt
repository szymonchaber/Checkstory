package dev.szymonchaber.checkstory.payments

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Card
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun RowScope.SubscriptionPlanView(
    subscriptionPlan: SubscriptionPlan,
    isSelected: Boolean,
    onClick: (SubscriptionPlan) -> Unit
) {

    val header = when (subscriptionPlan.planDuration) {
        PlanDuration.MONTHLY -> "1\nmonth"
        PlanDuration.QUARTERLY -> "3\nmonths"
        PlanDuration.YEARLY -> "12\nmonths"
    }
    Card(
        modifier = Modifier
            .weight(1f),
        backgroundColor = Color.LightGray,
        border = if (isSelected) BorderStroke(3.dp, MaterialTheme.colors.primary) else null,
        onClick = {
            onClick(subscriptionPlan)
        }
    ) {
        Column(
            Modifier
                .padding(horizontal = 16.dp)
                .padding(top = 12.dp, bottom = 16.dp)
        ) {
            Text(
                modifier = Modifier.align(alignment = Alignment.CenterHorizontally),
                text = header,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Text(
                modifier = Modifier
                    .padding(top = 16.dp, bottom = 8.dp)
                    .align(alignment = Alignment.CenterHorizontally),
                text = subscriptionPlan.price,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Box(
                modifier =
                Modifier
                    .background(Color.Gray)
                    .fillMaxWidth()
                    .height(1.dp)
                    .padding(top = 8.dp)
            )
            Text(
                modifier = Modifier
                    .padding(top = 8.dp)
                    .align(alignment = Alignment.CenterHorizontally),
                text = subscriptionPlan.pricePerMonth,
//                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
        }
    }
}