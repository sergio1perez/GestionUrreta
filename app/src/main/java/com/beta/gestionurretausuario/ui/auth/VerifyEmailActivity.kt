package com.beta.gestionurretausuario.ui.auth

import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.beta.gestionurretausuario.MainActivity
import com.beta.gestionurretausuario.R
import com.beta.gestionurretausuario.data.preferences.PreferencesManager
import com.beta.gestionurretausuario.databinding.ActivityVerifyEmailBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase

class VerifyEmailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityVerifyEmailBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private lateinit var preferencesManager: PreferencesManager
    
    private var resendTimer: CountDownTimer? = null
    private var canResend = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityVerifyEmailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = Firebase.auth
        firestore = FirebaseFirestore.getInstance()
        preferencesManager = PreferencesManager.getInstance(this)

        setupUI()
        setupClickListeners()
    }

    private fun setupUI() {
        // Mostrar el email al que se envió la verificación
        val email = preferencesManager.pendingVerificationEmail ?: auth.currentUser?.email ?: ""
        binding.tvEmailSent.text = getString(R.string.verification_email_sent_to, email)
    }

    private fun setupClickListeners() {
        // Botón verificar (refrescar estado)
        binding.btnCheckVerification.setOnClickListener {
            checkEmailVerification()
        }

        // Botón reenviar email
        binding.btnResendEmail.setOnClickListener {
            if (canResend) {
                resendVerificationEmail()
            }
        }

        // Botón cambiar email (volver al registro)
        binding.btnChangeEmail.setOnClickListener {
            // Cerrar sesión y volver al login
            auth.signOut()
            preferencesManager.clearPendingVerification()
            navigateToLogin()
        }
    }

    private fun checkEmailVerification() {
        showLoading(true)
        
        val user = auth.currentUser
        if (user == null) {
            showLoading(false)
            navigateToLogin()
            return
        }

        // Recargar el usuario para obtener el estado actualizado
        user.reload()
            .addOnCompleteListener { task ->
                showLoading(false)
                if (task.isSuccessful) {
                    if (user.isEmailVerified) {
                        // Email verificado - actualizar Firestore y proceder
                        onEmailVerified(user.uid)
                    } else {
                        Toast.makeText(
                            this,
                            getString(R.string.email_not_verified_yet),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                } else {
                    Toast.makeText(
                        this,
                        getString(R.string.error_checking_verification),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
    }

    private fun onEmailVerified(uid: String) {
        // Actualizar estado en Firestore
        firestore.collection("usuarios")
            .document(uid)
            .update("emailVerificado", true)
            .addOnCompleteListener {
                // Guardar sesión
                auth.currentUser?.let { user ->
                    preferencesManager.saveUserSession(
                        userId = user.uid,
                        email = user.email ?: "",
                        name = user.displayName ?: preferencesManager.pendingVerificationName,
                        photoUrl = user.photoUrl?.toString(),
                        provider = "email",
                        remember = true
                    )
                }
                
                // Limpiar datos temporales
                preferencesManager.clearPendingVerification()
                
                // Navegar a MainActivity
                navigateToMain()
            }
    }

    private fun resendVerificationEmail() {
        val user = auth.currentUser
        if (user == null) {
            navigateToLogin()
            return
        }

        showLoading(true)
        
        user.sendEmailVerification()
            .addOnCompleteListener { task ->
                showLoading(false)
                if (task.isSuccessful) {
                    Toast.makeText(
                        this,
                        getString(R.string.verification_email_resent),
                        Toast.LENGTH_SHORT
                    ).show()
                    startResendCooldown()
                } else {
                    Toast.makeText(
                        this,
                        getString(R.string.error_sending_verification),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
    }

    /**
     * Inicia un countdown de 60 segundos para poder reenviar el email
     */
    private fun startResendCooldown() {
        canResend = false
        binding.btnResendEmail.isEnabled = false
        
        resendTimer = object : CountDownTimer(60000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val seconds = millisUntilFinished / 1000
                binding.btnResendEmail.text = getString(R.string.resend_in_seconds, seconds)
            }

            override fun onFinish() {
                canResend = true
                binding.btnResendEmail.isEnabled = true
                binding.btnResendEmail.text = getString(R.string.resend_email)
            }
        }.start()
    }

    private fun navigateToMain() {
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    private fun navigateToLogin() {
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    private fun showLoading(show: Boolean) {
        binding.progressBar.visibility = if (show) View.VISIBLE else View.GONE
        binding.btnCheckVerification.isEnabled = !show
    }

    override fun onDestroy() {
        super.onDestroy()
        resendTimer?.cancel()
    }
}