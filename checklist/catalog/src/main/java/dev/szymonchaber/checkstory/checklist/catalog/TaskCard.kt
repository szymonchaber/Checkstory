package dev.szymonchaber.checkstory.checklist.catalog

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
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
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(10.dp)
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
            Row {
                val childTasks = remember {
                    mutableListOf<Task>()
                }
                val siblingTasks = remember {
                    mutableListOf<Task>()
                }
                DropTarget<Task>(
                    modifier = Modifier
                        .defaultMinSize(minHeight = 30.dp)
                        .weight(0.2f)
                        .background(Color.Gray),
                    key = task
                ) { isInBound, childTask ->
                    childTask?.let {
                        if (isInBound) {
                            childTasks.add(childTask)
                        }
                    }
                    Column {
                        childTasks.forEach { task ->
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
                DropTarget<Task>(
                    modifier = Modifier
                        .defaultMinSize(minHeight = 30.dp)
                        .weight(1f)
                        .background(Color.DarkGray),
                    key = task
                ) { isInBound, childTask ->
                    childTask?.let {
                        if (isInBound) {
                            siblingTasks.add(childTask)
                        }
                    }
                    Column {
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
        }
    }
}
