package dev.szymonchaber.checkstory.checklist.template

import androidx.compose.material.AlertDialog
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.res.stringResource

@Composable
fun ConfirmDeleteTemplateDialog(openDialog: MutableState<Boolean>, onConfirmClicked: () -> Unit) {
    AlertDialog(
        onDismissRequest = {
            openDialog.value = false
        },
        title = {
            Text(stringResource(id = R.string.delete_template_dialog_title))
        },
        text = {
            Text(stringResource(id = R.string.delete_template_dialog_body))
        },
        confirmButton = {
            TextButton(
                onClick = onConfirmClicked
            ) {
                Text(stringResource(id = R.string.delete_template_dialog_confirm))
            }
        },
        dismissButton = {
            TextButton(
                onClick = {
                    openDialog.value = false
                }) {
                Text(stringResource(id = R.string.delete_template_dialog_cancel))
            }
        }
    )
}
