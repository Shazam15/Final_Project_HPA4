package com.utp.finalproject

import com.utp.finalproject.data.local.entity.PetEntity
import com.utp.finalproject.domain.PetDecayCalculator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PetDecayCalculatorTest {
    private val start = 1_000_000L

    @Test
    fun `degrada estadisticas despues de seis horas`() {
        val pet = pet(lastStatsUpdateAt = start)
        val result = PetDecayCalculator.calculate(pet, start + PetDecayCalculator.INTERVAL_MILLIS)

        assertEquals(92, result.pet.hunger)
        assertEquals(96, result.pet.happiness)
        assertEquals(97, result.pet.energy)
        assertEquals(100, result.pet.health)
    }

    @Test
    fun `degrada salud despues de veinticuatro horas con necesidades bajas`() {
        val pet = pet(hunger = 25, happiness = 25, lastStatsUpdateAt = start)
        val result = PetDecayCalculator.calculate(pet, start + 4 * PetDecayCalculator.INTERVAL_MILLIS)

        assertEquals(0, result.pet.hunger)
        assertEquals(9, result.pet.happiness)
        assertEquals(95, result.pet.health)
    }

    @Test
    fun `limita valores entre cero y cien`() {
        val pet = pet(health = 2, hunger = 3, energy = 2, happiness = 1, lastStatsUpdateAt = start)
        val result = PetDecayCalculator.calculate(pet, start + 40 * PetDecayCalculator.INTERVAL_MILLIS)

        assertTrue(listOf(result.pet.health, result.pet.hunger, result.pet.energy, result.pet.happiness)
            .all { it in 0..100 })
    }

    @Test
    fun `no aplica dos veces el mismo intervalo`() {
        val first = PetDecayCalculator.calculate(
            pet(lastStatsUpdateAt = start),
            start + PetDecayCalculator.INTERVAL_MILLIS
        )
        val second = PetDecayCalculator.calculate(
            first.pet,
            start + PetDecayCalculator.INTERVAL_MILLIS
        )

        assertFalse(second.changed)
        assertEquals(first.pet, second.pet)
    }

    @Test
    fun `cambia estado emocional cuando el bienestar cae`() {
        val result = PetDecayCalculator.calculate(
            pet(health = 20, happiness = 20, lastStatsUpdateAt = start),
            start + PetDecayCalculator.INTERVAL_MILLIS
        )

        assertEquals(PetEntity.MOOD_DANGER, result.pet.mood)
    }

    private fun pet(
        health: Int = 100,
        hunger: Int = 100,
        energy: Int = 100,
        happiness: Int = 100,
        lastStatsUpdateAt: Long
    ) = PetEntity(
        name = "Luna",
        type = PetEntity.TYPE_DOG,
        health = health,
        hunger = hunger,
        energy = energy,
        happiness = happiness,
        lastStatsUpdateAt = lastStatsUpdateAt
    )
}
