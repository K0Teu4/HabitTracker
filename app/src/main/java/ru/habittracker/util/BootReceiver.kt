package ru.habittracker.util

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

/**
 * После перезагрузки можно пересоздавать напоминания.
 * Пока заглушка — логику WorkManager добавим позже.
 */
class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            // TODO: reschedule reminders via WorkManager
        }
    }
}
