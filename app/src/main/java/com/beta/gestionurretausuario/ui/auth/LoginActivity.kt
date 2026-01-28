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
        val signInIntent = googleSignInClient.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
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
                Toast.makeText(this, getString(R.string.error_google_sign_in), Toast.LENGTH_SHORT).show()
            }
        }
    }

    /**
     * Maneja el inicio de sesión con Google, incluyendo la vinculación de cuentas
     */
    private fun handleGoogleSignIn(account: GoogleSignInAccount) {
        val credential = GoogleAuthProvider.getCredential(account.idToken, null)
        val googleEmail = account.email

        // Primero verificar si existe una cuenta con este email
        if (googleEmail != null) {
            auth.fetchSignInMethodsForEmail(googleEmail)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val signInMethods = task.result?.signInMethods ?: emptyList()

                        when {
                            // Si ya existe cuenta con email/password, vincular
                            signInMethods.contains(EmailAuthProvider.EMAIL_PASSWORD_SIGN_IN_METHOD) &&
                                    !signInMethods.contains(GoogleAuthProvider.GOOGLE_SIGN_IN_METHOD) -> {
                                // Existe cuenta email/password pero no Google - Intentar vincular
                                linkGoogleToExistingAccount(credential, googleEmail)
                            }
                            else -> {
                                // No existe cuenta o ya tiene Google - Login normal
                                firebaseAuthWithGoogle(credential)
                            }
                        }
                    } else {
                        // Error al verificar, intentar login normal
                        firebaseAuthWithGoogle(credential)
                    }
                }
        } else {
            firebaseAuthWithGoogle(credential)
        }
    }

    /**
     * Vincula la cuenta de Google a una cuenta existente de email/password
     */
    private fun linkGoogleToExistingAccount(credential: AuthCredential, email: String) {
        // Informar al usuario que necesita autenticarse primero
        showLoading(false)

        // Mostrar diálogo pidiendo la contraseña
        showLinkAccountDialog(email, credential)
    }

    /**
     * Muestra un diálogo para vincular la cuenta de Google con la existente
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
     * Realiza la vinculación de cuentas
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
                    handleAuthError(signInTask.exception)
                }
            }
    }

    /**
     * Login normal con Google (sin vinculación)
     */
    private fun firebaseAuthWithGoogle(credential: AuthCredential) {
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                showLoading(false)
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    val isNewUser = task.result?.additionalUserInfo?.isNewUser ?: false

                    user?.let {
                        if (isNewUser) {
                            saveUserToFirestore(it)
                        }
                        saveSessionIfRemember(it, "google")
                    }
                    navigateToMain()
                } else {
                    handleAuthError(task.exception)
                }
            }
    }

    /**
     * Actualiza los proveedores del usuario en Firestore
     */
    private fun updateUserProviders(uid: String) {
        val user = auth.currentUser ?: return
        val providers = user.providerData.map { it.providerId }.filter { it != "firebase" }

        firestore.collection("usuarios")
            .document(uid)
            .update(
                mapOf(
                    "providers" to providers,
                    "fotoUrl" to (user.photoUrl?.toString() ?: "")
                )
            )
    }

    private fun handleLinkError(exception: Exception?) {
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

        firestore.collection("usuarios")
            .document(user.uid)
            .set(userData)
    }

    private fun handleAuthError(exception: Exception?) {
        val message = when {
            exception?.message?.contains("no user record") == true ||
                    exception?.message?.contains("invalid") == true ->
                getString(R.string.error_invalid_credentials)
            exception?.message?.contains("network") == true ->
                getString(R.string.error_network)
            exception?.message?.contains("blocked") == true ->
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