package com.smartethnet.rustun.ui.component

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable

@Composable
fun ErrorMessageDialog(error: String? = null, onDismiss: () -> Unit) {
    when {
        !error.isNullOrBlank() -> {
            AlertDialog(
                title = { Text(text = "错误") },
                text = { Text(error) },
                onDismissRequest = { onDismiss() },
                confirmButton = {
                    TextButton(
                        onClick = {
                            onDismiss()
                        }
                    ) {
                        Text("确定")
                    }
                },
                shape = MaterialTheme.shapes.small
            )
        }
    }
}