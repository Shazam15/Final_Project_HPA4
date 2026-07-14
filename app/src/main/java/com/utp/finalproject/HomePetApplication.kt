package com.utp.finalproject

import android.app.Application
import com.utp.finalproject.data.preferences.HomePetPreferences
import com.utp.finalproject.notifications.HomePetNotificationManager
import com.utp.finalproject.ui.ThemeManager
import com.utp.finalproject.worker.HomePetReminderScheduler

class HomePetApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        ThemeManager.apply(HomePetPreferences(this).getThemeMode())
        HomePetNotificationManager.createChannel(this)
        HomePetReminderScheduler.scheduleWellbeingCheck(this)
    }
}
