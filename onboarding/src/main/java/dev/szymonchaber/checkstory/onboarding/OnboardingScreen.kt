package dev.szymonchaber.checkstory.onboarding

import androidx.activity.compose.BackHandler
import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
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
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.HorizontalPagerIndicator
import com.google.accompanist.pager.PagerState
import com.google.accompanist.pager.rememberPagerState
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import dev.szymonchaber.checkstory.common.trackScreenName
import dev.szymonchaber.checkstory.navigation.Routes
import kotlinx.coroutines.launch

@OptIn(ExperimentalPagerApi::class)
@Destination("onboarding_screen", start = true)
@Composable
fun OnboardingScreen(
    navigator: DestinationsNavigator
) {
    trackScreenName("onboarding_screen")
    hiltViewModel<OnboardingViewModel>()

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
                    Text(text = stringResource(R.string.welcome))
                },
                elevation = 12.dp
            )
        },
        content = {
            Box(
                Modifier.padding(it)
            ) {
                OnboardingView(pagerState)
            }
        },
        bottomBar = {
            val buttonText = when (pagerState.currentPage) {
                0 -> "Next"
                1 -> "Let's begin"
                else -> error("Page ${pagerState.currentPage} does not exist")
            }
            Column(
                Modifier
                    .fillMaxWidth()
            ) {
                HorizontalPagerIndicator(
                    pagerState = pagerState,
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .padding(16.dp),
                )
                Button(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.CenterHorizontally)
                        .padding(horizontal = 16.dp)
                        .padding(bottom = 16.dp),
                    onClick = {
                        when (pagerState.currentPage) {
                            0 -> {
                                scope.launch {
                                    pagerState.animateScrollToPage(1)
                                }
                            }
                            1 -> {
                                navigator.navigate(Routes.onboardingChecklistTemplateScreen()) {
                                    popUpTo(Routes.homeScreen())
                                }
                            }
                        }
                    },
                ) {
                    Text(text = buttonText)
                }
            }
        }
    )
}

@OptIn(ExperimentalPagerApi::class)
@Composable
fun OnboardingView(pagerState: PagerState) {
    HorizontalPager(count = 2, state = pagerState) { page ->
        when (page) {
            0 -> ReusableChecklistsPage()
            1 -> KeepTrackOfThePastPage()
        }
    }
}

@Composable
private fun ReusableChecklistsPage() {
    OnboardingPage(
        imageRes = R.drawable.onboarding_1,
        title = "We’re all about\nreusable checklists",
        description = "Every checklist is made from a template.\n" +
                "This lets you start working with\na single click.\n" +
                "You can also work on multiple checklists at the same time."
    )
}

@Composable
private fun KeepTrackOfThePastPage() {
    OnboardingPage(
        imageRes = R.drawable.onboarding_2,
        title = "Keep track of the past",
        description = "When you’re done with a checklist,\nyou can keep it to track your past accomplishments & notes." +
                "\n" +
                "Editing a template affects only future checklists - we don’t rewrite history."
    )
}

@Composable
private fun OnboardingPage(
    @DrawableRes imageRes: Int,
    title: String,
    description: String
) {
    Column(Modifier.fillMaxSize()) {
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
                painter = painterResource(id = imageRes),
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
                .padding(top = 8.dp)
                .padding(horizontal = 16.dp),
            style = MaterialTheme.typography.body1.copy(
                textAlign = TextAlign.Center,
                fontSize = 18.sp,
                lineHeight = 25.sp
            ),
            text = description
        )
    }
}
