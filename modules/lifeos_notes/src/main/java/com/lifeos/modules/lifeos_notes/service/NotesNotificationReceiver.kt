package com.lifeos.modules.lifeos_notes.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class NotesNotificationReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val noteId = intent.getLongExtra("noteId", -1)
        val title = intent.getStringExtra("title") ?: "Note Reminder"
        val content = intent.getStringExtra("content") ?: ""

        if (noteId != -1L) {
            NotesNotificationHelper.showNotification(context, noteId, title, content)
        }
    }
}
