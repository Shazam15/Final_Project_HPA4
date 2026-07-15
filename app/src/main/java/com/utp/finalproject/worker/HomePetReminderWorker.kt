package com.utp.finalproject.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.utp.finalproject.R
import com.utp.finalproject.data.repository.HomePetRepository
import com.utp.finalproject.domain.PetNotificationEvaluator
import com.utp.finalproject.domain.WellbeingAlertLevel
import com.utp.finalproject.notifications.HomePetNotificationManager

class HomePetReminderWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return runCatching {
            // Flujo en segundo plano: Worker -> Repository -> Room -> evaluación de dominio.
            val repository = HomePetRepository(applicationContext)
            val now = System.currentTimeMillis()
            val decayResult = repository.applyPendingDecay(now) ?: return Result.success()
            repository.updateOverdueTasks()

            if (repository.areNotificationsEnabled()) {
                val decision = PetNotificationEvaluator.evaluate(decayResult.pet, now)
                if (decision.shouldNotify) {
                    val title = if (decision.level == WellbeingAlertLevel.CRITICAL) {
                        applicationContext.getString(R.string.notification_critical_title)
                    } else {
                        applicationContext.getString(R.string.notification_warning_title)
                    }
                    val message = applicationContext.getString(
                        R.string.notification_wellbeing_message,
                        decayResult.pet.name,
                        decision.affectedStat
                    )
                    // Si la regla lo permite, entrega título/mensaje al sistema de notificaciones.
                    HomePetNotificationManager.showWellbeingAlert(
                        applicationContext,
                        title,
                        message,
                        decision.level
                    )
                    repository.markNotificationSent(decision.level, now)
                }
            }
            Result.success()
        }.getOrElse {
            // WorkManager reintentará más tarde errores temporales de base de datos o sistema.
            Result.retry()
        }
    }
}
