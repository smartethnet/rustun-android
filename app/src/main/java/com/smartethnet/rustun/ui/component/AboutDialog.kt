package com.smartethnet.rustun.ui.component

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import com.smartethnet.rustun.R
import com.smartethnet.rustun.ui.theme.RustunTheme

@Composable
fun AboutUs(onDismissRequest: () -> Unit = {}) {
    AlertDialog(
        title = {
            Text(text = stringResource(R.string.app_name))
        },
        icon = {
            Icon(Icons.Default.Info, contentDescription = null)
        },
        onDismissRequest = { onDismissRequest() },
        dismissButton = {
            TextButton(
                onClick = {
                    onDismissRequest()
                }
            ) {
                Text("关闭")
            }
        },
        confirmButton = {
        },
        text = {
            Text("    比扬云网络连接终端是比杨云组网功能的客户端部分。\n    开启后，当前设备将和您的站点组网，可直接通过局域网IP等方式访问站点所能访问的所有内容")
        }
    )
}

@Composable
@PreviewLightDark
fun AboutUsPreview() {
    RustunTheme {
        AboutUs()
    }
}