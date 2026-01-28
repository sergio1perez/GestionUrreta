package com.beta.gestionurretausuario.ui.auth

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.beta.gestionurretausuario.MainActivity
import com.beta.gestionurretausuario.R
import com.beta.gestionurretausuario.data.preferences.PreferencesManager
import com.beta.gestionurretausuario.databinding.ActivityRegisterBinding
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.auth.ktx.userProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var firestore: FirebaseFirestore
    private lateinit var preferencesManager: PreferencesManager

    companion object {
        private const val RC_SIGN_IN = 9001
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Inicializar Firebase
        auth = Firebase.auth
        firestore = FirebaseFirestore.getInstance()

        // Inicializar PreferencesManager
        preferencesManager = PreferencesManager.getInstance(this)

        // Configurar Google Sign In
        configureGoogleSignIn()

        setupClickListeners()
    }

    private fun configureGoogleSignIn() {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)
    }

    private fun setupClickListeners() {
        // Botón de registro
        binding.btnRegister.setOnClickListener {
            registerWithEmail()
        }

        // Botón de Google
        binding.btnGoogle.setOnClickListener {
            signInWithGoogle()
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
                                showLoading(false)
                                if (profileTask.isSuccessful) {
                                    saveUserToFirestore(it, name)
                                    saveSession(it, "email")
                                    navigateToMain()
                                }
                            }
                    }
                } else {
                    showLoading(false)
                    handleAuthError(task.exception)
                }
            }
    }

    private fun signInWithGoogle() {
        showLoading(true)
        val signInIntent = googleSignInClient.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)
                firebaseAuthWithGoogle(account.idToken!!)
            } catch (e: ApiException) {
                showLoading(false)
                Toast.makeText(this, getString(R.string.error_google_sign_in), Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                showLoading(false)
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    val isNewUser = task.result?.additionalUserInfo?.isNewUser ?: false

                    user?.let {
                        if (isNewUser) {
                            saveUserToFirestore(it, it.displayName ?: "")
                        }
                        saveSession(it, "google")
                    }
                    navigateToMain()
                } else {
                    handleAuthError(task.exception)
                }
            }
    }

    private fun saveUserToFirestore(user: FirebaseUser, name: String) {
        val userData = hashMapOf(
            "uid" to user.uid,
            "nombre" to name,
            "email" to (user.email ?: ""),
            "fotoUrl" to (user.photoUrl?.toString() ?: ""),
            "cinturon" to "Blanco",
            "gup" to "10° Kup",
            "fechaRegistro" to com.google.firebase.Timestamp.now(),
            "activo" to true,
            "tipoUsuario" to "alumno"
        )

        firestore.collection("usuarios")
            .document(user.uid)
            .set(userData)
    }

    private fun saveSession(user: FirebaseUser, provider: String) {
        preferencesManager.saveUserSession(
            userId = user.uid,
            email = user.email ?: "",
            name = user.displayName,
            photoUrl = user.photoUrl?.toString(),
            provider = provider,
            remember = true // En registro, siempre recordamos la sesión
        )
    }

    private fun handleAuthError(exception: Exception?) {
        val message = when {
            exception?.message?.contains("email address is already in use") == true ->
                getString(R.string.error_email_in_use)
            exception?.message?.contains("network") == true ->
                getString(R.string.error_network)
            else -> getString(R.string.error_auth_failed)
        }
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }

    private fun showLoading(show: Boolean) {
        binding.progressBar.visibility = if (show) View.VISIBLE else View.GONE
        binding.btnRegister.isEnabled = !show
        binding.btnGoogle.isEnabled = !show
    }

    private fun navigateToMain() {
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}