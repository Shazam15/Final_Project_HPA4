package com.utp.finalproject

import com.utp.finalproject.data.local.entity.TaskEntity
import com.utp.finalproject.data.preferences.HomePetPreferences
import com.utp.finalproject.data.preferences.SessionPreferencePolicy
import com.utp.finalproject.data.preferences.ThemePreferencePolicy
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Test

class PreferencesAndLocationTest {
    @Test
    fun `normaliza y conserva tema valido`() {
        assertEquals(HomePetPreferences.THEME_DARK, ThemePreferencePolicy.normalize(HomePetPreferences.THEME_DARK))
        assertEquals(HomePetPreferences.THEME_SYSTEM, ThemePreferencePolicy.normalize("desconocido"))
    }

    @Test
    fun `cierre de sesion elimina datos de autenticacion`() {
        val cleared = SessionPreferencePolicy.cleared()
        assertFalse(cleared.isLoggedIn)
        assertEquals("", cleared.userName)
        assertEquals("", cleared.email)
    }

    @Test
    fun `tarea acepta ubicacion`() {
        val task = task().copy(locationName = "Parque", latitude = 8.98, longitude = -79.52)
        assertEquals("Parque", task.locationName)
        assertEquals(8.98, task.latitude!!, 0.0)
    }

    @Test
    fun `tarea puede guardarse sin ubicacion`() {
        val task = task()
        assertNull(task.locationName)
        assertNull(task.latitude)
        assertNull(task.longitude)
    }

    private fun task() = TaskEntity(
        title = "Pasear",
        description = "",
        category = "Mascota",
        priority = TaskEntity.PRIORITY_MEDIUM,
        frequency = TaskEntity.FREQUENCY_DAILY,
        dueAtMillis = 1_000L
    )
}
