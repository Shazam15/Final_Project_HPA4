package com.utp.finalproject

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.utp.finalproject.data.UserPreferencesRepository
import com.utp.finalproject.ui.LoginViewModel
import com.utp.finalproject.ui.LoginViewModelFactory
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {

    private val helpPhoneNumber = "+50762538997"
    private lateinit var viewModel: LoginViewModel
    private lateinit var nameInput: EditText
    private lateinit var emailInput: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val preferencesRepository = UserPreferencesRepository(applicationContext)
        viewModel = ViewModelProvider(
            this,
            LoginViewModelFactory(preferencesRepository)
        )[LoginViewModel::class.java]

        nameInput = findViewById(R.id.nameInput)
        emailInput = findViewById(R.id.emailInput)

        intent.getStringExtra(EXTRA_SAVED_USER_NAME)
            ?.takeIf { it.isNotBlank() }
            ?.let { nameInput.setText(it) }

        lifecycleScope.launch {
            viewModel.uiState.collect { state ->
                if (nameInput.text.isBlank() && state.savedUserName.isNotBlank()) {
                    nameInput.setText(state.savedUserName)
                }
            }
        }

        findViewById<Button>(R.id.loginButton).setOnClickListener {
            login()
        }

        findViewById<Button>(R.id.helpButton).setOnClickListener {
            requestHelpCall()
        }
    }

    private fun login() {
        val userName = nameInput.text.toString().trim()
        val email = emailInput.text.toString().trim()

        if (userName.isBlank() || email.isBlank()) {
            Toast.makeText(this, R.string.login_required_fields, Toast.LENGTH_SHORT).show()
            return
        }

        viewModel.saveUserName(userName)

        val intent = Intent(this, MainActivity::class.java).apply {
            putExtra(MainActivity.EXTRA_USER_NAME, userName)
            putExtra(MainActivity.EXTRA_USER_EMAIL, email)
        }
        startActivity(intent)
    }

    private fun requestHelpCall() {
        val permission = Manifest.permission.CALL_PHONE

        if (ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED) {
            callHelpNumber()
            return
        }

        ActivityCompat.requestPermissions(
            this,
            arrayOf(permission),
            HELP_CALL_PERMISSION_REQUEST
        )
    }

    private fun callHelpNumber() {
        val callIntent = Intent(Intent.ACTION_CALL).apply {
            data = Uri.parse("tel:$helpPhoneNumber")
        }

        startActivity(callIntent)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode != HELP_CALL_PERMISSION_REQUEST) {
            return
        }

        if (grantResults.firstOrNull() == PackageManager.PERMISSION_GRANTED) {
            callHelpNumber()
        } else {
            Toast.makeText(this, R.string.call_permission_denied, Toast.LENGTH_SHORT).show()
        }
    }

    companion object {
        const val EXTRA_SAVED_USER_NAME = "com.utp.finalproject.extra.SAVED_USER_NAME"
        private const val HELP_CALL_PERMISSION_REQUEST = 1001
    }
}
