package com.lifeos.modules.lifeos_notes.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckBox
import androidx.compose.material.icons.filled.CheckBoxOutlineBlank
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp

private val checkboxRegex = Regex("^(\\s*)-\\s\\[([ xX])\\]\\s(.*)$", RegexOption.MULTILINE)

data class ContentLine(val text: String, val isCheckbox: Boolean, val isChecked: Boolean, val indent: Int)

private fun parseContent(content: String): List<ContentLine> {
    val lines = mutableListOf<ContentLine>()
    checkboxRegex.findAll(content).forEach { match ->
        val indent = match.groupValues[1].length
        val checked = match.groupValues[2].lowercase() == "x"
        val text = match.groupValues[3]
        lines.add(ContentLine(text, true, checked, indent))
    }
    return lines
}

private fun updateCheckbox(content: String, index: Int, newChecked: Boolean): String {
    val matches = checkboxRegex.findAll(content).toList()
    if (index < 0 || index >= matches.size) return content
    
    val match = matches[index]
    val newMark = if (newChecked) "x" else " "
    val newLine = "${match.groupValues[1]}- [$newMark] ${match.groupValues[3]}"
    
    return content.substring(0, match.range.first) + newLine + content.substring(match.range.last + 1)
}

@Composable
fun CheckboxEditor(
    content: String,
    onContentChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var textFieldValue by remember { mutableStateOf(TextFieldValue(content)) }
    var showRawText by remember { mutableStateOf(false) }
    val checkboxes = remember(content) { parseContent(content) }
    val hasCheckboxes = checkboxes.isNotEmpty()

    LaunchedEffect(content) {
        if (textFieldValue.text != content) {
            textFieldValue = TextFieldValue(content)
        }
    }

    Column(modifier = modifier.fillMaxSize()) {
        if (hasCheckboxes) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton({ showRawText = !showRawText }) {
                    Text(if (showRawText) "Show Checkboxes" else "Show Raw Text")
                }
            }
        }

        if (showRawText || !hasCheckboxes) {
            Box(modifier = Modifier.weight(1f)) {
                BasicTextField(
                    value = textFieldValue,
                    onValueChange = { newValue ->
                        textFieldValue = newValue
                        onContentChange(newValue.text)
                    },
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    textStyle = MaterialTheme.typography.bodyLarge.copy(
                        color = MaterialTheme.colorScheme.onSurface
                    ),
                    cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                    decorationBox = { innerTextField ->
                        if (textFieldValue.text.isEmpty()) {
                            Text(
                                "Type your note here...\n\nTip: Use - [ ] or - [x] for checkboxes",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        innerTextField()
                    }
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 16.dp)
            ) {
                itemsIndexed(checkboxes) { index, line ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                val newContent = updateCheckbox(content, index, !line.isChecked)
                                textFieldValue = TextFieldValue(newContent)
                                onContentChange(newContent)
                            }
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Spacer(modifier = Modifier.width((line.indent * 16).dp))
                        Icon(
                            imageVector = if (line.isChecked) Icons.Filled.CheckBox else Icons.Filled.CheckBoxOutlineBlank,
                            contentDescription = null,
                            tint = if (line.isChecked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = line.text,
                            style = MaterialTheme.typography.bodyLarge,
                            color = if (line.isChecked) 
                                MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                            else 
                                MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }
    }
}
