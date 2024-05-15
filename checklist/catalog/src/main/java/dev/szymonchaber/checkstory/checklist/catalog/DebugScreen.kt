package dev.szymonchaber.checkstory.checklist.catalog

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import com.ramcosta.composedestinations.annotation.Destination
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.szymonchaber.checkstory.common.LogStorage
import javax.inject.Inject

@Composable
@Destination<HomeGraph>(route = "debug_screen")
fun DebugScreen() {
    val debugViewModel = hiltViewModel<DebugViewModel>()
    val logLines by debugViewModel.logStorage.logState.collectAsState(initial = "")

    Column(
        Modifier
            .padding(16.dp)
            .verticalScroll(rememberScrollState())) {
        Text(text = logLines)
    }
}

@HiltViewModel
class DebugViewModel @Inject constructor(
    val logStorage: LogStorage
) : ViewModel()
