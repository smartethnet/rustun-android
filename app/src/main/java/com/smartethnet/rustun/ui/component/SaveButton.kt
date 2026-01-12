package com.smartethnet.rustun.ui.component

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.smartethnet.rustun.ui.theme.RustunTheme

@Composable
fun SaveButton(
    onClick: () -> Unit, modifier: Modifier = Modifier,
    loading: Boolean = false
) {
    val text = when {
        loading -> "保存中......"
        else -> "保存"
    }
    Button(
        enabled = !loading,
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(48.dp),
        shape = MaterialTheme.shapes.small
    ) {
        Text(text = text, style = MaterialTheme.typography.bodyLarge)
    }
}

@Composable
@Preview
@PreviewLightDark
fun SaveButtonPreviewsWhenLoading() {
    RustunTheme {
        SaveButton(onClick = {}, loading = true)
    }
}

@Composable
@Preview
fun SaveButtonPreviews() {
    SaveButton(onClick = {}, loading = false)
}
