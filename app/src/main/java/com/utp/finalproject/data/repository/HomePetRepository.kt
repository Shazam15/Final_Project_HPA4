package com.utp.finalproject.data.repository

import android.content.Context
import com.utp.finalproject.data.local.database.HomePetDatabase
import com.utp.finalproject.data.local.entity.ActivityHistoryEntity
import com.utp.finalproject.data.local.entity.PetEntity
import com.utp.finalproject.data.local.entity.RewardEntity
import com.utp.finalproject.data.local.entity.TaskEntity
import com.utp.finalproject.data.preferences.HomePetPreferences
import com.utp.finalproject.domain.PetRules
import com.utp.finalproject.domain.PetDecayCalculator
import com.utp.finalproject.domain.PetDecayResult
import com.utp.finalproject.domain.WellbeingAlertLevel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.withContext

data class DashboardData(
    val pet: PetEntity?,
    val tasks: List<TaskEntity>,
    val rewards: List<RewardEntity>
)

data class ActivityStats(
    val completedTasks: Int,
    val pendingTasks: Int,
    val overdueTasks: Int,
    val completionRate: Int,
    val currentStreak: Int,
    val totalXp: Int
)

class HomePetRepository(context: Context) {
    private val database = HomePetDatabase.getInstance(context)
    private val taskDao = database.taskDao()
    private val petDao = database.petDao()
    private val rewardDao = database.rewardDao()
    private val historyDao = database.historyDao()
    private val preferences = HomePetPreferences(context)

    val petFlow: Flow<PetEntity?> = petDao.observePet()
    val tasksFlow: Flow<List<TaskEntity>> = taskDao.observeTasks()
    val rewardsFlow: Flow<List<RewardEntity>> = rewardDao.observeRewards()
    val historyFlow: Flow<List<ActivityHistoryEntity>> = historyDao.observeHistory()
    val dashboardFlow: Flow<DashboardData> = combine(petFlow, tasksFlow, rewardsFlow) { pet, tasks, rewards ->
        DashboardData(pet, tasks, rewards)
    }

    suspend fun prepareInitialData() = withContext(Dispatchers.IO) {
        seedRewards()
        applyPendingDecay()
        recordAppOpened()
        updateOverdueTasks()
    }

    fun isOnboardingCompleted(): Boolean = preferences.isOnboardingCompleted()

    suspend fun createPet(name: String, type: String) = withContext(Dispatchers.IO) {
        petDao.insert(PetEntity(name = name, type = type))
        preferences.setOnboardingCompleted(true)
        seedRewards()
        historyDao.insert(
            ActivityHistoryEntity(
                title = "Mascota creada",
                detail = "$name se unió a HomePet como $type.",
                type = "Mascota"
            )
        )
    }

    suspend fun saveTask(task: TaskEntity): Long = withContext(Dispatchers.IO) {
        val savedId = if (task.id == 0L) {
            taskDao.insert(task)
        } else {
            taskDao.update(task)
            task.id
        }
        recordInteraction()
        savedId
    }

    suspend fun getTask(id: Long): TaskEntity? = withContext(Dispatchers.IO) {
        taskDao.getTask(id)
    }

    suspend fun deleteTask(task: TaskEntity) = withContext(Dispatchers.IO) {
        taskDao.delete(task)
        historyDao.insert(
            ActivityHistoryEntity(
                title = "Tarea eliminada",
                detail = task.title,
                type = "Tarea"
            )
        )
    }

    suspend fun completeTask(task: TaskEntity) = withContext(Dispatchers.IO) {
        val now = System.currentTimeMillis()
        val reward = PetRules.calculateReward(task, now)
        val status = if (reward.completedOnTime) {
            TaskEntity.STATUS_COMPLETED
        } else {
            TaskEntity.STATUS_COMPLETED_LATE
        }

        taskDao.update(
            task.copy(
                status = status,
                completedAtMillis = now,
                completedOnTime = reward.completedOnTime,
                xpReward = reward.xp
            )
        )

        val pet = petDao.getPet()
        if (pet != null) {
            val overdueTasks = taskDao.countByStatus(TaskEntity.STATUS_OVERDUE)
            val updatedPet = PetRules.updatePetAfterTask(pet, reward, overdueTasks)
            petDao.update(updatedPet)
            unlockRewardsForLevel(updatedPet.level)
        }

        historyDao.insert(
            ActivityHistoryEntity(
                title = "Tarea completada",
                detail = "${task.title} dio ${reward.xp} XP y ${reward.coins} monedas.",
                type = "Progreso",
                xpDelta = reward.xp,
                coinDelta = reward.coins
            )
        )
    }

    suspend fun updateOverdueTasks() = withContext(Dispatchers.IO) {
        val now = System.currentTimeMillis()
        val expiredTasks = taskDao.getExpiredTasks(
            listOf(TaskEntity.STATUS_PENDING),
            now
        )
        expiredTasks.forEach { task ->
            taskDao.update(task.copy(status = TaskEntity.STATUS_OVERDUE))
            historyDao.insert(
                ActivityHistoryEntity(
                    title = "Tarea vencida",
                    detail = task.title,
                    type = "Alerta"
                )
            )
        }

        val pet = petDao.getPet()
        if (pet != null) {
            val pending = taskDao.countByStatus(TaskEntity.STATUS_PENDING)
            val overdue = taskDao.countByStatus(TaskEntity.STATUS_OVERDUE)
            petDao.update(PetRules.updatePetByTaskPressure(pet, pending, overdue))
        }
    }

    suspend fun buyOrEquipReward(reward: RewardEntity): String = withContext(Dispatchers.IO) {
        val pet = petDao.getPet() ?: return@withContext "Configura tu mascota primero."
        val latestReward = rewardDao.getReward(reward.id) ?: reward

        if (!latestReward.isUnlocked) {
            if (pet.level < latestReward.requiredLevel) {
                return@withContext "Necesitas nivel ${latestReward.requiredLevel}."
            }
            if (pet.coins < latestReward.cost) {
                return@withContext "Necesitas ${latestReward.cost} monedas."
            }
            rewardDao.unequipType(latestReward.type)
            rewardDao.update(latestReward.copy(isUnlocked = true, isEquipped = true))
            petDao.update(
                applyRewardAppearance(
                    pet = pet.copy(coins = pet.coins - latestReward.cost),
                    reward = latestReward
                )
            )
            historyDao.insert(
                ActivityHistoryEntity(
                    title = "Recompensa desbloqueada",
                    detail = latestReward.name,
                    type = "Recompensa",
                    coinDelta = -latestReward.cost
                )
            )
            recordInteraction()
            return@withContext "${latestReward.name} comprado y equipado."
        }

        rewardDao.unequipType(latestReward.type)
        rewardDao.update(latestReward.copy(isEquipped = true))
        petDao.update(applyRewardAppearance(pet, latestReward))
        recordInteraction()
        "${latestReward.name} equipado."
    }

    private fun applyRewardAppearance(pet: PetEntity, reward: RewardEntity): PetEntity {
        return when (reward.type) {
            "Accesorio" -> pet.copy(equippedAccessory = reward.name)
            "Sombrero" -> pet.copy(equippedHat = reward.name)
            "Ropa" -> pet.copy(equippedClothing = reward.name)
            "Color" -> pet.copy(equippedColor = reward.name)
            "Fondo" -> pet.copy(equippedBackground = reward.name)
            else -> pet
        }
    }

    suspend fun applyPendingDecay(now: Long = System.currentTimeMillis()): PetDecayResult? =
        withContext(Dispatchers.IO) {
            val pet = petDao.getPet() ?: return@withContext null
            val result = PetDecayCalculator.calculate(pet, now)
            if (result.changed) {
                petDao.update(result.pet)
            }
            result
        }

    suspend fun recordAppOpened(now: Long = System.currentTimeMillis()) = withContext(Dispatchers.IO) {
        val pet = petDao.getPet() ?: return@withContext
        petDao.update(pet.copy(lastAppOpenedAt = now))
    }

    suspend fun recordInteraction(now: Long = System.currentTimeMillis()) = withContext(Dispatchers.IO) {
        val pet = petDao.getPet() ?: return@withContext
        petDao.update(pet.copy(lastInteractionAt = now, lastUpdatedAt = now))
    }

    suspend fun markNotificationSent(level: WellbeingAlertLevel, now: Long) =
        withContext(Dispatchers.IO) {
            val pet = petDao.getPet() ?: return@withContext
            petDao.update(
                pet.copy(
                    lastDecayNotificationAt = now,
                    lastNotificationLevel = level.name
                )
            )
        }

    suspend fun updatePetSettings(name: String, type: String) = withContext(Dispatchers.IO) {
        val pet = petDao.getPet() ?: return@withContext
        petDao.update(pet.copy(name = name, type = type))
    }

    suspend fun buildStats(): ActivityStats = withContext(Dispatchers.IO) {
        val completed = taskDao.countByStatus(TaskEntity.STATUS_COMPLETED) +
            taskDao.countByStatus(TaskEntity.STATUS_COMPLETED_LATE)
        val pending = taskDao.countByStatus(TaskEntity.STATUS_PENDING)
        val overdue = taskDao.countByStatus(TaskEntity.STATUS_OVERDUE)
        val total = completed + pending + overdue
        val pet = petDao.getPet()

        ActivityStats(
            completedTasks = completed,
            pendingTasks = pending,
            overdueTasks = overdue,
            completionRate = if (total == 0) 0 else completed * 100 / total,
            currentStreak = completed.coerceAtMost(7),
            totalXp = pet?.experience ?: 0
        )
    }

    suspend fun resetProgress() = withContext(Dispatchers.IO) {
        taskDao.clear()
        petDao.clear()
        rewardDao.clear()
        historyDao.clear()
        preferences.clear()
        seedRewards()
    }

    fun areNotificationsEnabled(): Boolean = preferences.areNotificationsEnabled()

    fun setNotificationsEnabled(enabled: Boolean) {
        preferences.setNotificationsEnabled(enabled)
    }

    fun getReminderHour(): Int = preferences.getReminderHour()

    fun setReminderHour(hour: Int) {
        preferences.setReminderHour(hour)
    }

    fun getThemeMode(): String = preferences.getThemeMode()

    fun setThemeMode(mode: String) = preferences.setThemeMode(mode)

    fun isLoggedIn(): Boolean = preferences.isLoggedIn()

    fun saveSession(userName: String, email: String) = preferences.saveSession(userName, email)

    fun getUserName(): String = preferences.getUserName()

    fun clearSession() = preferences.clearSession()

    private suspend fun seedRewards() {
        if (rewardDao.countRewards() > 0) {
            return
        }
        rewardDao.insertAll(
            listOf(
                RewardEntity(1, "Collar azul", "Accesorio", "Un collar tranquilo para empezar.", "collar_blue", 1, 10, true),
                RewardEntity(2, "Sombrero verde", "Sombrero", "Sombrero para días productivos.", "hat_green", 2, 25),
                RewardEntity(3, "Capa de héroe", "Ropa", "Para rescatar tareas vencidas.", "hero_cape", 3, 45),
                RewardEntity(4, "Color dorado", "Color", "Brillo especial para rachas buenas.", "gold_color", 4, 70),
                RewardEntity(5, "Fondo jardín", "Fondo", "Un rincón alegre para descansar.", "garden_bg", 5, 90)
            )
        )
    }

    private suspend fun unlockRewardsForLevel(level: Int) {
        rewardDao.getRewards()
            .filter { it.requiredLevel <= level && it.cost == 0 && !it.isUnlocked }
            .forEach { rewardDao.update(it.copy(isUnlocked = true)) }
    }
}
