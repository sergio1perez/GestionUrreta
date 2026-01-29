package com.beta.gestionurretausuario.ui.auth

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.beta.gestionurretausuario.MainActivity
import com.beta.gestionurretausuario.R
import com.beta.gestionurretausuario.data.preferences.PreferencesManager
import com.beta.gestionurretausuario.databinding.ActivityLoginBinding
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.*
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var firestore: FirebaseFirestore
    private lateinit var preferencesManager: PreferencesManager

    // Variable para guardar las credenciales de Google temporalmente
    private var pendingGoogleCredential: AuthCredential? = null
    private var pendingGoogleEmail: String? = null

    companion object {
        private const val RC_SIGN_IN = 9001
        private const val TAG = "LoginActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Inicializar Firebase
        auth = Firebase.auth
        firestore = FirebaseFirestore.getInstance()

        // Inicializar PreferencesManager
        preferencesManager = PreferencesManager.getInstance(this)

        // Configurar Google Sign In
        configureGoogleSignIn()

        // Verificar si hay sesión guardada
        if (checkSavedSession()) {
            return
        }

        // Verificar si hay verificación pendiente
        if (checkPendingVerification()) {
            return
        }

        setupClickListeners()
        setupRememberCheckbox()
    }

    /**
     * Verifica si hay una sesión guardada y si se debe mantener
     */
    private fun checkSavedSession(): Boolean {
        if (preferencesManager.shouldKeepSession() && auth.currentUser != null) {
            // Verificar que el email esté verificado
            val user = auth.currentUser!!
            if (user.isEmailVerified || user.providerData.any { it.providerId == GoogleAuthProvider.PROVIDER_ID }) {
                navigateToMain()
                return true
            }
        }

        if (auth.currentUser != null && !preferencesManager.rememberSession) {
            auth.signOut()
        }

        return false
    }

    /**
     * Verifica si hay un email pendiente de verificación
     */
    private fun checkPendingVerification(): Boolean {
        val currentUser = auth.currentUser
        if (currentUser != null && !currentUser.isEmailVerified) {
            // Solo verificar si el proveedor es email/password
            val isEmailProvider = currentUser.providerData.any {
                it.providerId == EmailAuthProvider.PROVIDER_ID
            }
            if (isEmailProvider) {
                navigateToVerifyEmail()
                return true
            }
        }
        return false
    }

    private fun configureGoogleSignIn() {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)
    }

    private fun setupClickListeners() {
        binding.btnLogin.setOnClickListener {
            loginWithEmail()
        }

        binding.btnGoogle.setOnClickListener {
            signInWithGoogle()
        }

        binding.tvRegisterLink.setOnClickListener {
            navigateToRegister()
        }

        binding.tvForgotPassword.setOnClickListener {
            navigateToForgotPassword()
        }
    }

    private fun setupRememberCheckbox() {
        binding.cbRememberSession.isChecked = preferencesManager.rememberSession
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

        binding.tilEmail.error = null
        binding.tilPassword.error = null

        showLoading(true)

        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    if (user != null) {
                        // Verificar si el email está verificado
                        if (user.isEmailVerified) {
                            saveSessionIfRemember(user, "email")
                            showLoading(false)
                            navigateToMain()
                        } else {
                            showLoading(false)
                            // Email no verificado, enviar a pantalla de verificación
                            preferencesManager.pendingVerificationEmail = user.email
                            navigateToVerifyEmail()
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
        // Primero cerrar cualquier sesión anterior de Google para permitir elegir cuenta
        googleSignInClient.signOut().addOnCompleteListener {
            val signInIntent = googleSignInClient.signInIntent
            startActivityForResult(signInIntent, RC_SIGN_IN)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)
                handleGoogleSignIn(account)
            } catch (e: ApiException) {
                showLoading(false)
                Log.e(TAG, "Google sign in failed: ${e.statusCode}", e)
                Toast.makeText(this, getString(R.string.error_google_sign_in), Toast.LENGTH_SHORT).show()
            }
        }
    }

    /**
     * Maneja el inicio de sesión con Google.
     *
     * NUEVO ENFOQUE: En lugar de usar fetchSignInMethodsForEmail (deprecado),
     * intentamos el login directamente y capturamos la excepción de colisión.
     */
    private fun handleGoogleSignIn(account: GoogleSignInAccount) {
        val credential = GoogleAuthProvider.getCredential(account.idToken, null)
        val googleEmail = account.email

        // Guardar las credenciales para posible vinculación posterior
        pendingGoogleCredential = credential
        pendingGoogleEmail = googleEmail

        Log.d(TAG, "Attempting Google sign in for email: $googleEmail")

        // Intentar directamente el login con Google
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Login exitoso
                    showLoading(false)
                    val user = auth.currentUser
                    val isNewUser = task.result?.additionalUserInfo?.isNewUser ?: false

                    Log.d(TAG, "Google sign in successful. Is new user: $isNewUser")

                    user?.let {
                        if (isNewUser) {
                            saveUserToFirestore(it)
                        } else {
                            // Usuario existente - actualizar proveedores en Firestore
                            updateUserProviders(it.uid)
                        }
                        saveSessionIfRemember(it, "google")
                    }
                    navigateToMain()
                } else {
                    // Manejar errores
                    handleGoogleSignInError(task.exception, googleEmail, credential)
                }
            }
    }

    /**
     * Maneja los errores de inicio de sesión con Google.
     * Si hay colisión de cuentas, muestra el diálogo para vincular.
     */
    private fun handleGoogleSignInError(exception: Exception?, email: String?, credential: AuthCredential) {
        showLoading(false)

        Log.e(TAG, "Google sign in error: ${exception?.message}", exception)

        when (exception) {
            is FirebaseAuthUserCollisionException -> {
                // Ya existe una cuenta con este email pero con otro proveedor
                Log.d(TAG, "Account collision detected for email: $email")

                when (exception.errorCode) {
                    "ERROR_ACCOUNT_EXISTS_WITH_DIFFERENT_CREDENTIAL",
                    "ERROR_EMAIL_ALREADY_IN_USE" -> {
                        // Existe cuenta email/password - mostrar diálogo para vincular
                        if (email != null) {
                            showLinkAccountDialog(email, credential)
                        } else {
                            Toast.makeText(
                                this,
                                getString(R.string.error_account_exists),
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }
                    else -> {
                        Toast.makeText(
                            this,
                            getString(R.string.error_account_already_linked),
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            }
            else -> {
                // Otro tipo de error
                handleAuthError(exception)
            }
        }

        // Limpiar el intento de Google
        googleSignInClient.signOut()
    }

    /**
     * Muestra un diálogo para vincular la cuenta de Google con la existente de email/password
     */
    private fun showLinkAccountDialog(email: String, googleCredential: AuthCredential) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_link_account, null)
        val etPassword = dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.et_dialog_password)
        val tvEmail = dialogView.findViewById<android.widget.TextView>(R.id.tv_dialog_email)

        tvEmail.text = email

        val dialog = androidx.appcompat.app.AlertDialog.Builder(this, R.style.AlertDialogTheme)
            .setTitle(getString(R.string.link_account_title))
            .setMessage(getString(R.string.link_account_message))
            .setView(dialogView)
            .setPositiveButton(getString(R.string.btn_link)) { _, _ ->
                val password = etPassword.text.toString()
                if (password.isNotEmpty()) {
                    performAccountLinking(email, password, googleCredential)
                } else {
                    Toast.makeText(this, getString(R.string.error_password_required), Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton(getString(R.string.btn_cancel)) { dialog, _ ->
                dialog.dismiss()
                googleSignInClient.signOut() // Limpiar el intento de Google
            }
            .create()

        dialog.show()
    }

    /**
     * Realiza la vinculación de cuentas.
     * 1. Primero autentica con email/password
     * 2. Luego vincula la cuenta de Google
     */
    private fun performAccountLinking(email: String, password: String, googleCredential: AuthCredential) {
        showLoading(true)

        // Primero autenticar con email/password
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { signInTask ->
                if (signInTask.isSuccessful) {
                    val user = auth.currentUser
                    if (user != null) {
                        // Verificar que el email esté verificado antes de vincular
                        if (!user.isEmailVerified) {
                            showLoading(false)
                            Toast.makeText(
                                this,
                                getString(R.string.error_email_not_verified),
                                Toast.LENGTH_LONG
                            ).show()
                            preferencesManager.pendingVerificationEmail = email
                            navigateToVerifyEmail()
                            return@addOnCompleteListener
                        }

                        // Ahora vincular la cuenta de Google
                        user.linkWithCredential(googleCredential)
                            .addOnCompleteListener { linkTask ->
                                showLoading(false)
                                if (linkTask.isSuccessful) {
                                    Log.d(TAG, "Account linking successful")
                                    // Actualizar Firestore con el nuevo proveedor
                                    updateUserProviders(user.uid)
                                    Toast.makeText(
                                        this,
                                        getString(R.string.account_linked_success),
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    saveSessionIfRemember(user, "google")
                                    navigateToMain()
                                } else {
                                    handleLinkError(linkTask.exception)
                                }
                            }
                    }
                } else {
                    showLoading(false)
                    Log.e(TAG, "Email/password sign in failed for linking", signInTask.exception)
                    handleAuthError(signInTask.exception)
                }
            }
    }

    /**
     * Actualiza los proveedores del usuario en Firestore
     */
    private fun updateUserProviders(uid: String) {
        val user = auth.currentUser ?: return
        val providers = user.providerData.map { it.providerId }.filter { it != "firebase" }

        Log.d(TAG, "Updating providers for user $uid: $providers")

        firestore.collection("usuarios")
            .document(uid)
            .update(
                mapOf(
                    "providers" to providers,
                    "fotoUrl" to (user.photoUrl?.toString() ?: "")
                )
            )
            .addOnFailureListener { e ->
                Log.e(TAG, "Failed to update providers", e)
            }
    }

    private fun handleLinkError(exception: Exception?) {
        Log.e(TAG, "Link error: ${exception?.message}", exception)

        val message = when (exception) {
            is FirebaseAuthUserCollisionException -> getString(R.string.error_account_already_linked)
            else -> getString(R.string.error_linking_account)
        }
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
        googleSignInClient.signOut()
    }

    private fun saveSessionIfRemember(user: FirebaseUser, provider: String) {
        val shouldRemember = binding.cbRememberSession.isChecked

        preferencesManager.saveUserSession(
            userId = user.uid,
            email = user.email ?: "",
            name = user.displayName,
            photoUrl = user.photoUrl?.toString(),
            provider = provider,
            remember = shouldRemember
        )
    }

    private fun saveUserToFirestore(user: FirebaseUser) {
        val providers = user.providerData.map { it.providerId }.filter { it != "firebase" }

        val userData = hashMapOf(
            "uid" to user.uid,
            "nombre" to (user.displayName ?: ""),
            "email" to (user.email ?: ""),
            "fotoUrl" to (user.photoUrl?.toString() ?: ""),
            "cinturon" to "Blanco",
            "gup" to "10° Kup",
            "fechaRegistro" to com.google.firebase.Timestamp.now(),
            "activo" to true,
            "tipoUsuario" to "alumno",
            "emailVerificado" to true, // Google ya verifica el email
            "providers" to providers
        )

        Log.d(TAG, "Saving new user to Firestore: ${user.uid}")

        firestore.collection("usuarios")
            .document(user.uid)
            .set(userData)
            .addOnFailureListener { e ->
                Log.e(TAG, "Failed to save user to Firestore", e)
            }
    }

    private fun handleAuthError(exception: Exception?) {
        val message = when {
            exception?.message?.contains("no user record") == true ||
                    exception?.message?.contains("invalid") == true ||
                    exception?.message?.contains("INVALID_LOGIN_CREDENTIALS") == true ->
                getString(R.string.error_invalid_credentials)
            exception?.message?.contains("network") == true ->
                getString(R.string.error_network)
            exception?.message?.contains("blocked") == true ||
                    exception?.message?.contains("TOO_MANY_ATTEMPTS") == true ->
                getString(R.string.error_too_many_attempts)
            else -> getString(R.string.error_login_failed)
        }
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
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

    private fun navigateToVerifyEmail() {
        val intent = Intent(this, VerifyEmailActivity::class.java)
        startActivity(intent)
    }

    private fun navigateToForgotPassword() {
        startActivity(Intent(this, ForgotPasswordActivity::class.java))
    }
}