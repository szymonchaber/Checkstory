package dev.szymonchaber.checkstory.design.views

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import dev.szymonchaber.checkstory.design.ActiveUser
import dev.szymonchaber.checkstory.design.BuildConfig

@SuppressLint("MissingPermission")
@Composable
fun AdvertView(modifier: Modifier = Modifier) {
    Box(
        modifier = Modifier
            .height(50.dp)
    ) {
        val isInEditMode = LocalInspectionMode.current
        if (isInEditMode) {
            Text(
                modifier = modifier
                    .fillMaxWidth()
                    .background(Color.Red)
                    .padding(horizontal = 2.dp, vertical = 6.dp),
                textAlign = TextAlign.Center,
                color = Color.White,
                text = "Advert Here",
            )
        } else {
            var isLoaded by remember {
                mutableStateOf(false)
            }
            if (!isLoaded) {
                LoadingViewNoPadding()
            }
            AndroidView(
                modifier = modifier.fillMaxWidth(),
                factory = { context ->
                    AdView(context).apply {
                        setAdSize(AdSize.BANNER)
                        adUnitId = BuildConfig.BANNER_AD_UNIT_ID
                        loadAd(AdRequest.Builder().build())
                        adListener = object : AdListener() {

                            override fun onAdLoaded() {
                                isLoaded = true
                            }
                        }
                    }
                }
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AdvertPreview() {
    AdvertView()
}

@Composable
fun AdvertScaffold(
    topBar: @Composable () -> Unit,
    content: @Composable (PaddingValues) -> Unit,
    floatingActionButton: @Composable () -> Unit = {},
    contentAboveBottomBar: @Composable () -> Unit = {}
) {
    val activeUser = ActiveUser.current
    Scaffold(
        topBar = topBar,
        content = {
            Box(
                Modifier.padding(it)
            ) {
                content(it)
            }
        },
        floatingActionButton = floatingActionButton,
        bottomBar = {
            Column {
                contentAboveBottomBar()
                if (activeUser.isPaidUser.not()) {
                    AdvertView()
                }
            }
        }
    )
}
