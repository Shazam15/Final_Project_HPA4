package com.utp.finalproject

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.utp.finalproject.data.UserPreferencesRepository
import kotlinx.coroutines.launch

class WelcomeActivity : AppCompatActivity() {

    private lateinit var preferencesRepository: UserPreferencesRepository
    private lateinit var welcomeText: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_welcome)

        preferencesRepository = UserPreferencesRepository(applicationContext)
        welcomeText = findViewById(R.id.welcomeText)

        findViewById<Button>(R.id.startButton).setOnClickListener {
            lifecycleScope.launch {
                val savedUserName = preferencesRepository.getUserName()
                val intent = Intent(this@WelcomeActivity, LoginActivity::class.java).apply {
                    putExtra(LoginActivity.EXTRA_SAVED_USER_NAME, savedUserName)
                }
                startActivity(intent)
            }
        }

        loadSavedPreferences()
    }

    private fun loadSavedPreferences() {
        lifecycleScope.launch {
            val savedUserName = preferencesRepository.getUserName()
            welcomeText.text = if (savedUserName.isBlank()) {
                getString(R.string.welcome_message)
            } else {
                getString(R.string.welcome_back_message, savedUserName)
            }
        }
    }
}
