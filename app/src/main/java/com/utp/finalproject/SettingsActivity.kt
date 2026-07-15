package com.utp.finalproject

import android.Manifest
import android.content.Intent
import android.os.Bundle
import android.os.Build
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.utp.finalproject.data.local.entity.PetEntity
import com.utp.finalproject.data.preferences.HomePetPreferences
import com.utp.finalproject.data.repository.HomePetRepository
import com.utp.finalproject.databinding.ActivitySettingsBinding
import com.utp.finalproject.viewmodel.RepositoryViewModelFactory
import com.utp.finalproject.viewmodel.SettingsViewModel
import com.utp.finalproject.ui.ThemeManager
import kotlinx.coroutines.launch

class SettingsActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySettingsBinding
    private lateinit var viewModel: SettingsViewModel
    private val petTypes = listOf(PetEntity.TYPE_DOG, PetEntity.TYPE_CAT, PetEntity.TYPE_RABBIT)
    private val themeModes = listOf(
        HomePetPreferences.THEME_SYSTEM,
        HomePetPreferences.THEME_LIGHT,
        HomePetPreferences.THEME_DARK
    )
    // Android devuelve aquí el resultado del permiso solicitado desde el checkbox.
    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (!granted) {
            binding.notificationsCheck.isChecked = false
            viewModel.setNotificationsEnabled(false)
            Toast.makeText(this, R.string.notification_permission_denied, Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel = ViewModelProvider(
            this,
            RepositoryViewModelFactory(HomePetRepository(applicationContext))
        )[SettingsViewModel::class.java]

        setupSpinner()
        setupThemeSpinner()
        // ViewModel lee estas preferencias desde Repository -> SharedPreferences al abrir la pantalla.
        binding.notificationsCheck.isChecked = viewModel.notificationsEnabled
        binding.reminderHourInput.setText(viewModel.reminderHour.toString())

        binding.saveSettingsButton.setOnClickListener { saveSettings() }
        binding.shareButton.setOnClickListener { shareProgress() }
        binding.resetButton.setOnClickListener { confirmReset() }
        binding.logoutButton.setOnClickListener { confirmLogout() }
        binding.notificationsCheck.setOnCheckedChangeListener { _, enabled ->
            if (enabled && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }

        // El registro de mascota fluye desde Room hasta la UI mediante petFlow/StateFlow.
        lifecycleScope.launch {
            viewModel.pet.collect { pet ->
                if (pet != null && binding.petNameInput.text.isBlank()) {
                    binding.petNameInput.setText(pet.name)
                    binding.petTypeSpinner.setSelection(petTypes.indexOf(pet.type).coerceAtLeast(0))
                }
            }
        }
    }

    private fun setupThemeSpinner() {
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, themeModes)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.themeSpinner.adapter = adapter
        binding.themeSpinner.setSelection(themeModes.indexOf(viewModel.themeMode).coerceAtLeast(0))
    }

    private fun setupSpinner() {
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, petTypes)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.petTypeSpinner.adapter = adapter
    }

    private fun saveSettings() {
        val name = binding.petNameInput.text.toString().trim()
        val hour = binding.reminderHourInput.text.toString().toIntOrNull() ?: 18
        if (name.isBlank()) {
            Toast.makeText(this, R.string.task_required_fields, Toast.LENGTH_SHORT).show()
            return
        }
        // Mascota se guarda en Room; notificaciones, hora y tema se guardan en SharedPreferences.
        viewModel.savePet(name, binding.petTypeSpinner.selectedItem.toString())
        viewModel.setNotificationsEnabled(binding.notificationsCheck.isChecked)
        viewModel.setReminderHour(hour)
        val selectedTheme = binding.themeSpinner.selectedItem.toString()
        viewModel.setThemeMode(selectedTheme)
        ThemeManager.apply(selectedTheme)
        Toast.makeText(this, R.string.save_settings, Toast.LENGTH_SHORT).show()
    }

    private fun confirmLogout() {
        AlertDialog.Builder(this)
            .setTitle(R.string.logout)
            .setMessage(R.string.logout_confirm)
            .setPositiveButton(R.string.logout) { _, _ ->
                viewModel.logout()
                startActivity(Intent(this, LoginActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                })
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    private fun shareProgress() {
        val text = "Estoy cuidando mi hogar y a mi mascota en HomePet."
        // ACTION_SEND entrega el texto a cualquier aplicación compatible elegida por el usuario.
        startActivity(Intent.createChooser(Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, text)
        }, getString(R.string.share_progress)))
    }

    private fun confirmReset() {
        AlertDialog.Builder(this)
            .setTitle(R.string.reset_progress)
            .setMessage(R.string.reset_confirm)
            .setPositiveButton(R.string.yes_delete) { _, _ ->
                viewModel.resetProgress()
                startActivity(Intent(this, WelcomeActivity::class.java))
                finishAffinity()
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }
}
