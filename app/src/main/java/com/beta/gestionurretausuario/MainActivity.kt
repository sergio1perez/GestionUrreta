package com.beta.gestionurretausuario

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.fragment.app.Fragment
import com.beta.gestionurretausuario.data.preferences.PreferencesManager
import com.beta.gestionurretausuario.databinding.ActivityMainBinding
import com.beta.gestionurretausuario.ui.auth.LoginActivity
import com.beta.gestionurretausuario.ui.fragments.*
import com.bumptech.glide.Glide
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private lateinit var preferencesManager: PreferencesManager

    // Views del header del NavigationView
    private lateinit var ivUserAvatar: ImageView
    private lateinit var tvUserName: TextView
    private lateinit var tvBeltRank: TextView
    private lateinit var viewBeltColor: View

    // Drawer y NavigationView
    private val drawerLayout by lazy { binding.drawerLayout }
    private val navigationView by lazy { binding.navigationView }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Inicializar Firebase
        auth = Firebase.auth
        firestore = FirebaseFirestore.getInstance()

        // Inicializar PreferencesManager
        preferencesManager = PreferencesManager.getInstance(this)

        // Verificar autenticación
        if (auth.currentUser == null) {
            navigateToLogin()
            return
        }

        setupToolbar()
        setupNavigationDrawer()
        loadUserData()

        // Cargar fragment inicial
        if (savedInstanceState == null) {
            loadFragment(HomeFragment())
            navigationView.setCheckedItem(R.id.nav_inicio)
        }
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        // Abrir drawer al pulsar el icono del menú
        binding.toolbar.setNavigationOnClickListener {
            drawerLayout.openDrawer(GravityCompat.START)
        }
    }

    private fun setupNavigationDrawer() {
        // Obtener el header del NavigationView
        val headerView = navigationView.getHeaderView(0)
        ivUserAvatar = headerView.findViewById(R.id.iv_user_avatar)
        tvUserName = headerView.findViewById(R.id.tv_user_name)
        tvBeltRank = headerView.findViewById(R.id.tv_belt_rank)
        viewBeltColor = headerView.findViewById(R.id.view_belt_color)

        // Click en el header para ir al perfil
        headerView.setOnClickListener {
            navigateToProfile()
        }

        // Botón de logout en el header
        headerView.findViewById<ImageView>(R.id.iv_logout)?.setOnClickListener {
            showLogoutDialog()
        }

        // Configurar navegación del menú
        navigationView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_inicio -> {
                    loadFragment(HomeFragment())
                    updateToolbarTitle(getString(R.string.menu_inicio))
                }
                R.id.nav_clases -> {
                    loadFragment(ClasesFragment())
                    updateToolbarTitle(getString(R.string.menu_clases))
                }
                R.id.nav_eventos -> {
                    loadFragment(EventosFragment())
                    updateToolbarTitle(getString(R.string.menu_eventos))
                }
                R.id.nav_noticias -> {
                    loadFragment(NoticiasFragment())
                    updateToolbarTitle(getString(R.string.menu_noticias))
                }
                R.id.nav_examenes -> {
                    loadFragment(ExamenesFragment())
                    updateToolbarTitle(getString(R.string.menu_examenes))
                }
                R.id.nav_pagos -> {
                    loadFragment(PagosFragment())
                    updateToolbarTitle(getString(R.string.menu_pagos))
                }
                R.id.nav_tienda -> {
                    loadFragment(TiendaFragment())
                    updateToolbarTitle(getString(R.string.menu_tienda))
                }
            }
            drawerLayout.closeDrawer(GravityCompat.START)
            true
        }
    }

    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }

    private fun updateToolbarTitle(title: String) {
        binding.tvToolbarTitle.text = title
        binding.tvToolbarTitle.visibility = View.VISIBLE
    }

    private fun loadUserData() {
        val currentUser = auth.currentUser ?: return

        // Cargar datos básicos del usuario de Firebase Auth
        tvUserName.text = currentUser.displayName ?: "Usuario"

        // Cargar imagen de perfil
        currentUser.photoUrl?.let { uri ->
            Glide.with(this)
                .load(uri)
                .placeholder(R.drawable.ic_person)
                .error(R.drawable.ic_person)
                .circleCrop()
                .into(ivUserAvatar)
        }

        // Cargar datos adicionales de Firestore
        firestore.collection("usuarios")
            .document(currentUser.uid)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val nombre = document.getString("nombre") ?: currentUser.displayName ?: "Usuario"
                    val cinturon = document.getString("cinturon") ?: "Blanco"
                    val gup = document.getString("gup") ?: "10° Kup"
                    val fotoUrl = document.getString("fotoUrl")

                    tvUserName.text = nombre
                    tvBeltRank.text = "Cinturón $cinturon • $gup"

                    // Actualizar color del cinturón
                    updateBeltColor(cinturon)

                    // Cargar foto de Firestore si existe
                    fotoUrl?.let { url ->
                        Glide.with(this)
                            .load(url)
                            .placeholder(R.drawable.ic_person)
                            .error(R.drawable.ic_person)
                            .circleCrop()
                            .into(ivUserAvatar)
                    }
                }
            }
            .addOnFailureListener {
                // En caso de error, usar datos de Auth
                tvBeltRank.text = "Miembro"
            }
    }

    private fun updateBeltColor(cinturon: String) {
        val colorRes = when (cinturon.lowercase()) {
            "blanco" -> R.color.belt_white
            "amarillo" -> R.color.belt_yellow
            "verde" -> R.color.belt_green
            "azul" -> R.color.belt_blue
            "rojo" -> R.color.belt_red
            "negro" -> R.color.belt_black
            else -> R.color.belt_white
        }
        viewBeltColor.setBackgroundColor(getColor(colorRes))
    }

    private fun navigateToProfile() {
        drawerLayout.closeDrawer(GravityCompat.START)
        loadFragment(PerfilFragment())
        updateToolbarTitle(getString(R.string.menu_perfil))
        // Desmarcar items del menú
        navigationView.setCheckedItem(View.NO_ID)
    }

    private fun showLogoutDialog() {
        AlertDialog.Builder(this, R.style.AlertDialogTheme)
            .setTitle(R.string.logout_confirm_title)
            .setMessage(R.string.logout_confirm_message)
            .setPositiveButton(R.string.yes) { _, _ ->
                logout()
            }
            .setNegativeButton(R.string.no, null)
            .show()
    }

    private fun logout() {
        // Limpiar sesión de SharedPreferences
        preferencesManager.clearSession()

        // Cerrar sesión de Firebase
        auth.signOut()

        // Cerrar sesión de Google si estaba logueado con Google
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        val googleSignInClient = GoogleSignIn.getClient(this, gso)
        googleSignInClient.signOut()

        navigateToLogin()
    }

    private fun navigateToLogin() {
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }
}