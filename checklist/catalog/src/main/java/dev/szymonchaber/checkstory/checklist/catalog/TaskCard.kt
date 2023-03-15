package dev.szymonchaber.checkstory.checklist.catalog

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun TaskCard(task: Task) {
    Card(
        elevation = 10.dp,
        backgroundColor = Color.White,
        modifier = Modifier.padding(8.dp)
    ) {
        Column {
            val childTasks = remember {
                mutableStateListOf<Task>()
            }
            val siblingTasks = remember {
                mutableStateListOf<Task>()
            }
            Box(Modifier.height(IntrinsicSize.Min)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .padding(10.dp)
                        .fillMaxWidth()
                ) {
                    Draggable(modifier = Modifier, dataToDrop = task) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(16.dp))
                                .background(task.color)
                                .size(30.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(20.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = task.name,
                            fontSize = 22.sp,
                            color = Color.DarkGray
                        )
                    }
                }
                Receptacles(
                    task = task,
                    { childTask ->
                        siblingTasks.add(childTask)
                    }
                ) { childTask ->
                    childTasks.add(childTask.copy(name = "Child ${childTask.name}"))
                }
            }
            childTasks.forEach { task ->
                Row {
                    Text(
                        modifier = Modifier.padding(start = 32.dp),
                        text = "* " + task.name,
                        fontSize = 22.sp,
                        color = Color.Blue
                    )
                }
            }
            siblingTasks.forEach { task ->
                Row {
                    Text(
                        text = "* " + task.name,
                        fontSize = 22.sp,
                        color = Color.DarkGray
                    )
                }
            }
        }
    }
}

@Composable
private fun Receptacles(
    task: Task,
    onSiblingTaskDropped: (Task) -> Unit,
    onChildTaskDropped: (Task) -> Unit
) {
    Row(Modifier.fillMaxSize()) {
        DropTarget<Task>(
            modifier = Modifier
                .fillMaxHeight()
                .weight(0.2f),
            key = task
        ) { isInBound, siblingTask ->
            siblingTask?.let {
                if (isInBound) {
                    onSiblingTaskDropped(it)
                }
            }
        }
        DropTarget<Task>(
            modifier = Modifier
                .fillMaxHeight()
                .weight(1f),
            key = task
        ) { isInBound, childTask ->
            childTask?.let {
                if (isInBound) {
                    onChildTaskDropped(childTask)
                }
            }
        }
    }
}
