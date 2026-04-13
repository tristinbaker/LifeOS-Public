package com.lifeos.modules.lifeos_notes.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import com.lifeos.modules.lifeos_notes.data.local.ChecklistItem
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

fun parseChecklist(json: String): List<ChecklistItem> {
    return try {
        val items = mutableListOf<ChecklistItem>()
        val array = JSONArray(json)
        for (i in 0 until array.length()) {
            val obj = array.getJSONObject(i)
            items.add(ChecklistItem(
                id = obj.getString("id"),
                text = obj.getString("text"),
                isChecked = obj.getBoolean("isChecked")
            ))
        }
        items
    } catch (e: Exception) {
        emptyList()
    }
}

fun serializeChecklist(items: List<ChecklistItem>): String {
    val array = JSONArray()
    items.forEach { item ->
        val obj = JSONObject().apply {
            put("id", item.id)
            put("text", item.text)
            put("isChecked", item.isChecked)
        }
        array.put(obj)
    }
    return array.toString()
}

@Composable
fun ChecklistEditor(
    checklistJson: String,
    onChecklistChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var items by remember(checklistJson) { mutableStateOf(parseChecklist(checklistJson)) }
    var newItemText by remember { mutableStateOf("") }

    LaunchedEffect(checklistJson) {
        items = parseChecklist(checklistJson)
    }

    Column(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(vertical = 8.dp)
        ) {
            itemsIndexed(items) { index, item ->
                ChecklistRow(
                    item = item,
                    isLastItem = index == items.size - 1,
                    onToggle = {
                        items = items.toMutableList().apply {
                            this[index] = item.copy(isChecked = !item.isChecked)
                        }
                        onChecklistChange(serializeChecklist(items))
                    },
                    onTextChange = { newText ->
                        items = items.toMutableList().apply {
                            this[index] = item.copy(text = newText)
                        }
                        onChecklistChange(serializeChecklist(items))
                    },
                    onDelete = {
                        items = items.toMutableList().apply {
                            removeAt(index)
                        }
                        onChecklistChange(serializeChecklist(items))
                    },
                    onDone = {
                        items = items + ChecklistItem(
                            id = UUID.randomUUID().toString(),
                            text = "",
                            isChecked = false
                        )
                        onChecklistChange(serializeChecklist(items))
                    }
                )
            }

            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    BasicTextField(
                        value = newItemText,
                        onValueChange = { newItemText = it },
                        modifier = Modifier
                            .weight(1f)
                            .padding(vertical = 8.dp),
                        textStyle = MaterialTheme.typography.bodyLarge.copy(
                            color = MaterialTheme.colorScheme.onSurface
                        ),
                        cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                        decorationBox = { innerTextField ->
                            Box {
                                innerTextField()
                                if (newItemText.isEmpty()) {
                                    Text(
                                        "Add item...",
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        },
                        singleLine = false,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                        keyboardActions = androidx.compose.foundation.text.KeyboardActions(
                            onDone = {
                                if (newItemText.isNotEmpty()) {
                                    items = items + ChecklistItem(
                                        id = UUID.randomUUID().toString(),
                                        text = newItemText,
                                        isChecked = false
                                    )
                                    onChecklistChange(serializeChecklist(items))
                                    newItemText = ""
                                }
                            }
                        )
                    )
                }
            }
        }
    }
}

@Composable
private fun ChecklistRow(
    item: ChecklistItem,
    isLastItem: Boolean,
    onToggle: () -> Unit,
    onTextChange: (String) -> Unit,
    onDelete: () -> Unit,
    onDone: () -> Unit
) {
    var textValue by remember(item.id) { mutableStateOf(TextFieldValue(item.text)) }

    LaunchedEffect(item.text) {
        textValue = TextFieldValue(item.text)
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(
                if (item.isChecked)
                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                else
                    MaterialTheme.colorScheme.surfaceVariant
            )
            .clickable { onToggle() }
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = item.isChecked,
            onCheckedChange = { onToggle() }
        )

        Spacer(modifier = Modifier.width(8.dp))

        Box(modifier = Modifier.weight(1f)) {
            BasicTextField(
                value = textValue,
                onValueChange = { 
                    textValue = it
                    onTextChange(it.text)
                },
                modifier = Modifier.fillMaxWidth(),
                textStyle = MaterialTheme.typography.bodyLarge.copy(
                    color = if (item.isChecked)
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    else
                        MaterialTheme.colorScheme.onSurface
                ),
                cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                decorationBox = { innerTextField ->
                    Box {
                        innerTextField()
                        if (textValue.text.isEmpty()) {
                            Text(
                                "Type here...",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                singleLine = false,
                keyboardOptions = KeyboardOptions(imeAction = if (isLastItem) ImeAction.Done else ImeAction.Next),
                keyboardActions = androidx.compose.foundation.text.KeyboardActions(
                    onNext = { onDone() },
                    onDone = { onDone() }
                )
            )
        }

        IconButton(onClick = onDelete) {
            Icon(
                Icons.Default.Close,
                "Delete",
                tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f),
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
