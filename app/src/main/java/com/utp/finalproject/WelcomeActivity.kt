package com.utp.finalproject

import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.utp.finalproject.data.local.entity.PetEntity
import com.utp.finalproject.data.repository.HomePetRepository
import com.utp.finalproject.databinding.ActivityWelcomeBinding
import com.utp.finalproject.viewmodel.OnboardingViewModel
import com.utp.finalproject.viewmodel.RepositoryViewModelFactory
import com.utp.finalproject.ui.PetArtwork
import kotlinx.coroutines.launch

class WelcomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityWelcomeBinding
    private lateinit var viewModel: OnboardingViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWelcomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // La Activity entrega eventos de la interfaz al ViewModel; la Factory le inyecta
        // el Repository para que la pantalla no acceda directamente a Room.
        viewModel = ViewModelProvider(
            this,
            RepositoryViewModelFactory(HomePetRepository(applicationContext))
        )[OnboardingViewModel::class.java]

        setupPetTypeSpinner()

        binding.startButton.setOnClickListener {
            savePet()
        }

        // Flujo de regreso: Repository -> ViewModel.uiState -> Activity -> navegación.
        lifecycleScope.launch {
            viewModel.uiState.collect { state ->
                if (state.shouldOpenHome) {
                    openHome()
                } else if (state.shouldOpenLogin) {
                    openLogin()
                }
            }
        }
    }

    private fun setupPetTypeSpinner() {
        val petTypes = listOf(PetEntity.TYPE_DOG, PetEntity.TYPE_CAT, PetEntity.TYPE_RABBIT)
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, petTypes)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.petTypeSpinner.adapter = adapter
        binding.petTypeSpinner.onItemSelectedListener = object : android.widget.AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: android.widget.AdapterView<*>?,
                view: android.view.View?,
                position: Int,
                id: Long
            ) {
                binding.petPreviewImage.setImageResource(
                    PetArtwork.pet(petTypes[position], PetEntity.MOOD_HAPPY)
                )
            }

            override fun onNothingSelected(parent: android.widget.AdapterView<*>?) = Unit
        }
    }

    private fun savePet() {
        val petName = binding.petNameInput.text.toString().trim()
        if (petName.isBlank()) {
            Toast.makeText(this, R.string.task_required_fields, Toast.LENGTH_SHORT).show()
            return
        }
        // Envía nombre y tipo al OnboardingViewModel, que los pasa al Repository y a Room.
        viewModel.savePet(petName, binding.petTypeSpinner.selectedItem.toString())
    }

    private fun openHome() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }

    private fun openLogin() {
        startActivity(Intent(this, LoginActivity::class.java))
        finish()
    }
}
