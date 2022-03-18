package dev.szymonchaber.checkstory

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import dagger.hilt.android.AndroidEntryPoint
import dev.szymonchaber.checkstory.checklist.fill.FillChecklistScreen
import dev.szymonchaber.checkstory.checklist.fill.FillChecklistView
import dev.szymonchaber.checkstory.domain.model.checklist.fill.checklist
import dev.szymonchaber.checkstory.design.theme.CheckstoryTheme

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CheckstoryTheme {
                // A surface container using the 'background' color from the theme
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colors.background) {
                    FillChecklistScreen()
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    CheckstoryTheme {
        FillChecklistView(checklist)
    }
}
