package com.beta.gestionurretausuario.ui.auth

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.beta.gestionurretausuario.MainActivity
import com.beta.gestionurretausuario.R
import com.beta.gestionurretausuario.databinding.ActivityLoginBinding
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var firestore: FirebaseFirestore

    companion object {
        private const val RC_SIGN_IN = 9001
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Inicializar Firebase
        auth = Firebase.auth
        firestore = FirebaseFirestore.getInstance()

        // Configurar Google Sign In
        configureGoogleSignIn()

        // Verificar si ya está logueado
        if (auth.currentUser != null) {
            navigateToMain()
            return
        }

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
        // Botón de login
        binding.btnLogin.setOnClickListener {
            loginWithEmail()
        }

        // Botón de Google
        binding.btnGoogle.setOnClickListener {
            signInWithGoogle()
        }

        // Link a registro
        binding.tvRegisterLink.setOnClickListener {
            navigateToRegister()
        }

        // Link de olvidé contraseña
        binding.tvForgotPassword.setOnClickListener {
            navigateToForgotPassword()
        }
    }

    private fun loginWithEmail() {
        val email = binding.etEmail.text.toString().trim()
        val password = binding.etPassword.text.toString()

        // Validaciones
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

        // Limpiar errores
        binding.tilEmail.error = null
        binding.tilPassword.error = null

        // Mostrar loading
        showLoading(true)

        // Login con Firebase
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                showLoading(false)
                if (task.isSuccessful) {
                    navigateToMain()
                } else {
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
                Toast.makeText(this, getString(R.string.error_auth_failed), Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                showLoading(false)
                if (task.isSuccessful) {
                    // Verificar si es un usuario nuevo
                    val isNewUser = task.result?.additionalUserInfo?.isNewUser ?: false
                    if (isNewUser) {
                        createUserInFirestore()
                    }
                    navigateToMain()
                } else {
                    handleAuthError(task.exception)
                }
            }
    }

    private fun createUserInFirestore() {
        val user = auth.currentUser ?: return

        val userData = hashMapOf(
            "uid" to user.uid,
            "nombre" to (user.displayName ?: ""),
            "email" to (user.email ?: ""),
            "fotoUrl" to (user.photoUrl?.toString() ?: ""),
            "cinturon" to "Blanco",
            "gup" to "10° Kup",
            "fechaRegistro" to com.google.firebase.Timestamp.now(),
            "activo" to true
        )

        firestore.collection("usuarios")
            .document(user.uid)
            .set(userData)
    }

    private fun handleAuthError(exception: Exception?) {
        val message = when {
            exception?.message?.contains("no user record") == true ->
                getString(R.string.error_user_not_found)
            exception?.message?.contains("password is invalid") == true ->
                getString(R.string.error_wrong_password)
            exception?.message?.contains("network") == true ->
                getString(R.string.error_network)
            else -> getString(R.string.error_auth_failed)
        }
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }

    private fun showLoading(show: Boolean) {
        binding.progressBar.visibility = if (show) View.VISIBLE else View.GONE
        binding.btnLogin.isEnabled = !show
        binding.btnGoogle.isEnabled = !show
    }

    private fun navigateToMain() {
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    private fun navigateToRegister() {
        startActivity(Intent(this, RegisterActivity::class.java))
    }

    private fun navigateToForgotPassword() {
        startActivity(Intent(this, ForgotPasswordActivity::class.java))
    }
}