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
import androidx.compose.ui.res.stringResource
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
    val (header, footer) = when (subscriptionPlan.planDuration) {
        PlanDuration.MONTHLY -> R.string.subscription_plan_monthly to null
        PlanDuration.QUARTERLY -> R.string.subscription_plan_quarterly to R.string.save_5_percent
        PlanDuration.YEARLY -> R.string.subscription_plan_yearly to R.string.save_20_percent
    }
    Card(
        modifier = Modifier
            .weight(1f),
//        backgroundColor = Color.LightGray,
        border = if (isSelected) BorderStroke(3.dp, MaterialTheme.colors.primary) else BorderStroke(
            3.dp,
            Color.LightGray
        ),
        onClick = {
            onClick(subscriptionPlan)
        }
    ) {
        Column(
            Modifier
                .padding(top = 12.dp, bottom = 12.dp)
        ) {
            Text(
                modifier = Modifier
                    .align(alignment = Alignment.CenterHorizontally)
                    .padding(horizontal = 16.dp),
                text = stringResource(id = header),
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Text(
                modifier = Modifier
                    .padding(top = 16.dp, bottom = 8.dp)
                    .padding(horizontal = 4.dp)
                    .align(alignment = Alignment.CenterHorizontally),
                text = subscriptionPlan.price,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            val dividerColor = if (subscriptionPlan.planDuration == PlanDuration.MONTHLY) {
                Color.Transparent
            } else {
                Color.LightGray
            }
            Box(
                modifier = Modifier
                    .background(dividerColor)
                    .fillMaxWidth()
                    .height(1.dp)
                    .padding(top = 8.dp)
            )
            Text(
                modifier = Modifier
                    .padding(top = 8.dp)
                    .padding(horizontal = 4.dp)
                    .align(alignment = Alignment.CenterHorizontally),
                text = footer?.let { stringResource(id = it) } ?: "",
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
        }
    }
}
