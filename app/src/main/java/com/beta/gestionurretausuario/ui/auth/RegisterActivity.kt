package com.beta.gestionurretausuario.ui.auth

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.beta.gestionurretausuario.R
import com.beta.gestionurretausuario.data.preferences.PreferencesManager
import com.beta.gestionurretausuario.databinding.ActivityRegisterBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.auth.ktx.userProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private lateinit var preferencesManager: PreferencesManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Inicializar Firebase
        auth = Firebase.auth
        firestore = FirebaseFirestore.getInstance()

        // Inicializar PreferencesManager
        preferencesManager = PreferencesManager.getInstance(this)

        setupClickListeners()
    }

    private fun setupClickListeners() {
        // Botón de registro
        binding.btnRegister.setOnClickListener {
            registerWithEmail()
        }

        // Link a login
        binding.tvLoginLink.setOnClickListener {
            finish()
        }

        // Botón de volver
        binding.btnBack.setOnClickListener {
            finish()
        }
    }

    private fun registerWithEmail() {
        val name = binding.etName.text.toString().trim()
        val email = binding.etEmail.text.toString().trim()
        val password = binding.etPassword.text.toString()
        val confirmPassword = binding.etConfirmPassword.text.toString()

        // Validaciones
        if (name.isEmpty()) {
            binding.tilName.error = getString(R.string.error_name_required)
            return
        }

        if (email.isEmpty()) {
            binding.tilEmail.error = getString(R.string.error_email_required)
            return
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.tilEmail.error = getString(R.string.error_email_invalid)
            return
        }

        if (password.isEmpty()) {
            binding.tilPassword.error = getString(R.string.error_password_required)
            return
        }

        if (password.length < 6) {
            binding.tilPassword.error = getString(R.string.error_password_short)
            return
        }

        if (password != confirmPassword) {
            binding.tilConfirmPassword.error = getString(R.string.error_password_mismatch)
            return
        }

        // Limpiar errores
        binding.tilName.error = null
        binding.tilEmail.error = null
        binding.tilPassword.error = null
        binding.tilConfirmPassword.error = null

        // Mostrar loading
        showLoading(true)

        // Crear usuario con Firebase
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    user?.let {
                        // Actualizar perfil con el nombre
                        val profileUpdates = userProfileChangeRequest {
                            displayName = name
                        }
                        it.updateProfile(profileUpdates)
                            .addOnCompleteListener { profileTask ->
                                if (profileTask.isSuccessful) {
                                    // Enviar email de verificación
                                    sendVerificationEmail(it, name)
                                } else {
                                    showLoading(false)
                                    Toast.makeText(this, getString(R.string.error_profile_update), Toast.LENGTH_SHORT).show()
                                }
                            }
                    }
                } else {
                    showLoading(false)
                    handleAuthError(task.exception)
                }
            }
    }

    /**
     * Envía el email de verificación y navega a la pantalla de verificación
     */
    private fun sendVerificationEmail(user: FirebaseUser, name: String) {
        user.sendEmailVerification()
            .addOnCompleteListener { task ->
                showLoading(false)
                if (task.isSuccessful) {
                    // Guardar datos en Firestore (pero con estado no verificado)
                    saveUserToFirestore(user, name, emailVerified = false)

                    // Guardar email temporalmente para la pantalla de verificación
                    preferencesManager.pendingVerificationEmail = user.email
                    preferencesManager.pendingVerificationName = name

                    // Navegar a pantalla de verificación
                    navigateToVerifyEmail()
                } else {
                    Toast.makeText(
                        this,
                        getString(R.string.error_sending_verification),
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
    }

    private fun saveUserToFirestore(user: FirebaseUser, name: String, emailVerified: Boolean) {
        val userData = hashMapOf(
            "uid" to user.uid,
            "nombre" to name,
            "email" to (user.email ?: ""),
            "fotoUrl" to (user.photoUrl?.toString() ?: ""),
            "cinturon" to "Blanco",
            "gup" to "10° Kup",
            "fechaRegistro" to com.google.firebase.Timestamp.now(),
            "activo" to true,
            "tipoUsuario" to "alumno",
            "emailVerificado" to emailVerified
        )

        firestore.collection("usuarios")
            .document(user.uid)
            .set(userData)
    }

    private fun navigateToVerifyEmail() {
        val intent = Intent(this, VerifyEmailActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    private fun handleAuthError(exception: Exception?) {
        val message = when {
            exception?.message?.contains("email address is already in use") == true ->
                getString(R.string.error_email_in_use)
            exception?.message?.contains("network") == true ->
                getString(R.string.error_network)
            else -> getString(R.string.error_register_failed)
        }
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun showLoading(show: Boolean) {
        binding.progressBar.visibility = if (show) View.VISIBLE else View.GONE
        binding.btnRegister.isEnabled = !show
    }
}