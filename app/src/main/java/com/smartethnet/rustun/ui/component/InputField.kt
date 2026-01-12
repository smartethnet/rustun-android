package com.smartethnet.rustun.ui.component

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview

@Composable
fun InputField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    password: Boolean = false,
    label: String? = null,
    placeholder: String? = null,
    error: String? = null,
) {
    val isError = when {
        !error.isNullOrBlank() -> true
        else -> false
    }
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.extraSmall),
        enabled = enabled,
        readOnly = false,
        label = { Text(text = label ?: "") },
        placeholder = {
            Text(
                text = placeholder ?: "",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary
            )
        },
        supportingText = { Text(text = error ?: "") },
        singleLine = true,
        shape = MaterialTheme.shapes.small,
        isError = isError,
        visualTransformation = if (password) PasswordVisualTransformation() else VisualTransformation.None,
        keyboardOptions = if (password) KeyboardOptions(keyboardType = KeyboardType.Password) else KeyboardOptions.Default
    )
}

@Composable
@Preview
fun InputFieldPreview() {
    InputField(value = "", label = "用户名", placeholder = "请输入值", onValueChange = {})
}