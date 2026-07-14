package com.utp.finalproject

import com.utp.finalproject.data.local.entity.PetEntity
import com.utp.finalproject.domain.PetNotificationEvaluator
import com.utp.finalproject.domain.WellbeingAlertLevel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PetNotificationEvaluatorTest {
    private val now = 100_000_000L

    @Test
    fun `detecta nivel de advertencia`() {
        val decision = PetNotificationEvaluator.evaluate(pet(energy = 30), now)
        assertEquals(WellbeingAlertLevel.WARNING, decision.level)
        assertTrue(decision.shouldNotify)
    }

    @Test
    fun `detecta nivel critico`() {
        val decision = PetNotificationEvaluator.evaluate(pet(health = 15), now)
        assertEquals(WellbeingAlertLevel.CRITICAL, decision.level)
    }

    @Test
    fun `respeta cooldown de advertencia`() {
        val pet = pet(energy = 25).copy(
            lastDecayNotificationAt = now - 1_000,
            lastNotificationLevel = WellbeingAlertLevel.WARNING.name
        )
        assertFalse(PetNotificationEvaluator.evaluate(pet, now).shouldNotify)
    }

    @Test
    fun `permite critica al empeorar durante cooldown`() {
        val pet = pet(health = 10).copy(
            lastDecayNotificationAt = now - 1_000,
            lastNotificationLevel = WellbeingAlertLevel.WARNING.name
        )
        assertTrue(PetNotificationEvaluator.evaluate(pet, now).shouldNotify)
    }

    private fun pet(
        health: Int = 100,
        hunger: Int = 100,
        energy: Int = 100,
        happiness: Int = 100
    ) = PetEntity(
        name = "Luna",
        type = PetEntity.TYPE_DOG,
        health = health,
        hunger = hunger,
        energy = energy,
        happiness = happiness,
        lastDecayNotificationAt = 0
    )
}
