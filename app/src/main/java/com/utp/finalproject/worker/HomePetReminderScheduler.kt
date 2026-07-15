package com.utp.finalproject.worker

import android.content.Context
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

object HomePetReminderScheduler {
    private const val WORK_NAME = "homepet_wellbeing_check"

    fun scheduleWellbeingCheck(context: Context) {
        // WorkManager guarda esta solicitud y ejecuta el Worker aunque la Activity ya no exista.
        val request = PeriodicWorkRequestBuilder<HomePetReminderWorker>(12, TimeUnit.HOURS)
            .addTag(WORK_NAME)
            .build()

        // El nombre único evita crear varios temporizadores para el mismo mantenimiento.
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            WORK_NAME,
            ExistingPeriodicWorkPolicy.UPDATE,
            request
        )
    }
}
