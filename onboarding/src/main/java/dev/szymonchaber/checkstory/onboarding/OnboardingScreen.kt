package dev.szymonchaber.checkstory.onboarding

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import dev.szymonchaber.checkstory.common.trackScreenName
import dev.szymonchaber.checkstory.navigation.Routes
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class)
@Destination("onboarding_screen", start = true)
@Composable
fun OnboardingScreen(
    navigator: DestinationsNavigator
) {
    trackScreenName("onboarding_screen")

    val pagerState = rememberPagerState()
    val scope = rememberCoroutineScope()
    BackHandler {
        if (pagerState.currentPage > 0) {
            scope.launch {
                pagerState.animateScrollToPage(pagerState.currentPage - 1)
            }
        }
    }
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(text = stringResource(R.string.checkstory))
                },
                elevation = 12.dp
            )
        },
        content = {
            Box(
                Modifier.padding(it)
            ) {
                OnboardingView(navigator, pagerState)
            }
        }
    )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun OnboardingView(navigator: DestinationsNavigator, pagerState: PagerState) {
    val scope = rememberCoroutineScope()
    HorizontalPager(pageCount = 2, state = pagerState, beyondBoundsPageCount = 1) { page ->
        when (page) {
            0 -> ReusableChecklistsPage {
                scope.launch {
                    pagerState.animateScrollToPage(1)
                }
            }
            1 -> KeepTrackOfThePastPage {
                navigator.navigate(Routes.aboutScreen()) {
                    popUpTo(Routes.homeScreen())
                }
            }
        }
    }
}

@Composable
private fun ReusableChecklistsPage(onButtonClick: () -> Unit) {
    OnboardingPage(
        title = "We’re all about\nreusable checklists",
        description = "Every checklist is made from a template.\n" +
                "This lets you start working with a single click.\n" +
                "You can work on multiple checklists at the same time.",
        buttonText = "Next",
        onButtonClick = onButtonClick,
        image = R.drawable.onboarding_1
    )
}

@Composable
private fun KeepTrackOfThePastPage(onButtonClick: () -> Unit) {
    OnboardingPage(
        title = "Keep track of the past",
        description = "When you’re done with a checklist,\nyou can keep it to track your past accomplishments & notes." +
                "\n" +
                "Editing a template affects only future checklists - we don’t rewrite history.",
        buttonText = "Let's begin",
        onButtonClick = onButtonClick,
        image = R.drawable.onboarding_2
    )
}

@Composable
private fun OnboardingPage(
    title: String,
    description: String,
    buttonText: String,
    onButtonClick: () -> Unit,
    image: Int
) {
    Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.SpaceBetween) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFD6CFFC))
            ) {
                Image(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .clip(RoundedCornerShape(8.dp)),
                    painter = painterResource(id = image),
                    contentDescription = "",
                    contentScale = ContentScale.FillWidth,
                )
            }
            Text(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp)
                    .padding(horizontal = 16.dp),
                style = MaterialTheme.typography.h5.copy(fontWeight = FontWeight.Medium, textAlign = TextAlign.Center),
                text = title
            )
            Text(
                modifier = Modifier
                    .padding(top = 16.dp)
                    .padding(horizontal = 16.dp),
                style = MaterialTheme.typography.body1.copy(textAlign = TextAlign.Center, lineHeight = 22.sp),
                text = description
            )
        }
        Button(
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(horizontal = 16.dp)
                .padding(bottom = 16.dp)
                .fillMaxWidth()
                .weight(1f, false),
            onClick = onButtonClick,
        ) {
            Text(text = buttonText)
        }
    }
}
