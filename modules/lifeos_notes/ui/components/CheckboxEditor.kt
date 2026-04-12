package com.lifeos.modules.lifeos_notes.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun CheckboxEditor(
    content: String,
    onContentChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var text by remember { mutableStateOf(content) }

    OutlinedTextField(
        value = text,
        onValueChange = {
            text = it
            onContentChange(it)
        },
        modifier = modifier.fillMaxSize(),
        placeholder = { Text("Type your note here...") }
    )
}
