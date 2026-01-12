package com.smartethnet.rustun.ui.component

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.smartethnet.rustun.ui.theme.RustunTheme

@Composable
fun LoadingDialog(loading: Boolean = false, text: String = "Loading......") {
    if (loading) {
        Dialog(onDismissRequest = {}) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentSize(Alignment.Center)
                    .height(150.dp)
                    .padding(vertical = 16.dp),
                shape = MaterialTheme.shapes.small
            ) {
                Text(
                    text = text,
                    modifier = Modifier
                        .fillMaxSize()
                        .wrapContentSize(Alignment.Center)
                )
            }
        }
    }
}

@Preview
@Composable
fun LoadingDialogPreview() {
    RustunTheme {
        LoadingDialog(true, "加载中......")
    }
}