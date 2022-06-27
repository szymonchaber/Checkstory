package dev.szymonchaber.checkstory.design.views

import androidx.compose.material.AlertDialog
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.res.stringResource
import dev.szymonchaber.checkstory.checklist.fill.R

@Composable
fun ConfirmExitWithoutSavingDialog(openDialog: MutableState<Boolean>, onConfirmClicked: () -> Unit) {
    AlertDialog(
        onDismissRequest = {
            openDialog.value = false
        },
        title = {
            Text(stringResource(id = R.string.confirm_exit_without_saving_dialog_title))
        },
        text = {
            Text(stringResource(id = R.string.confirm_exit_without_saving_dialog_body))
        },
        confirmButton = {
            TextButton(
                onClick = onConfirmClicked
            ) {
                Text(stringResource(id = R.string.confirm_exit_without_saving_dialog_confirm))
            }
        },
        dismissButton = {
            TextButton(
                onClick = {
                    openDialog.value = false
                }) {
                Text(stringResource(id = R.string.confirm_exit_without_saving_dialog_cancel))
            }
        }
    )
}
