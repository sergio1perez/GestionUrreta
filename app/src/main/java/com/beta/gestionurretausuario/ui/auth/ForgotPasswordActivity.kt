package com.beta.gestionurretausuario.ui.auth

import android.os.Bundle
import android.util.Patterns
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.beta.gestionurretausuario.R
import com.beta.gestionurretausuario.databinding.ActivityForgotPasswordBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class ForgotPasswordActivity : AppCompatActivity() {

    private lateinit var binding: ActivityForgotPasswordBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityForgotPasswordBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = Firebase.auth

        setupClickListeners()
    }

    private fun setupClickListeners() {
        binding.btnSendLink.setOnClickListener {
            sendPasswordResetEmail()
        }

        binding.btnBack.setOnClickListener {
            finish()
        }

        binding.tvBackToLogin.setOnClickListener {
            finish()
        }
    }

    private fun sendPasswordResetEmail() {
        val email = binding.etEmail.text.toString().trim()

        if (email.isEmpty()) {
            binding.tilEmail.error = getString(R.string.error_email_required)
            return
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.tilEmail.error = getString(R.string.error_email_invalid)
            return
        }

        binding.tilEmail.error = null
        showLoading(true)

        auth.sendPasswordResetEmail(email)
            .addOnCompleteListener { task ->
                showLoading(false)
                if (task.isSuccessful) {
                    Toast.makeText(
                        this,
                        getString(R.string.success_password_reset),
                        Toast.LENGTH_LONG
                    ).show()
                    finish()
                } else {
                    Toast.makeText(
                        this,
                        getString(R.string.error_unknown),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
    }

    private fun showLoading(show: Boolean) {
        binding.progressBar.visibility = if (show) View.VISIBLE else View.GONE
        binding.btnSendLink.isEnabled = !show
    }
}