package com.example.financetracker.broadcast

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

object RecordBroadcast {
    const val ACTION_RECORD_CHANGED = "com.example.financetracker.RECORD_CHANGED"
    const val EXTRA_MESSAGE = "message"

    fun send(context: Context, message: String) {
        val intent = Intent(ACTION_RECORD_CHANGED).apply {
            setPackage(context.packageName)
            putExtra(EXTRA_MESSAGE, message)
        }
        context.sendBroadcast(intent)
    }
}

class RecordChangeReceiver(private val onMessage: (String) -> Unit) : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == RecordBroadcast.ACTION_RECORD_CHANGED) {
            onMessage(intent.getStringExtra(RecordBroadcast.EXTRA_MESSAGE).orEmpty())
        }
    }
}
