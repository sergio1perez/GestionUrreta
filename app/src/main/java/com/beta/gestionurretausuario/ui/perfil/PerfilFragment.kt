package com.beta.gestionurretausuario.ui.perfil

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import com.beta.gestionurretausuario.R
import com.beta.gestionurretausuario.databinding.FragmentPerfilBinding
import com.beta.gestionurretausuario.ui.auth.LoginActivity
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage

class PerfilFragment : Fragment() {

    private var _binding: FragmentPerfilBinding? = null
    private val binding get() = _binding!!

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var storage: FirebaseStorage

    private var selectedImageUri: Uri? = null

    private val pickImageLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                selectedImageUri = uri
                binding.ivAvatar.setImageURI(uri)
                uploadProfileImage(uri)
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPerfilBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
        storage = FirebaseStorage.getInstance()

        binding.tabDatos.isSelected = true

        setupClickListeners()
        setupTabs()
        loadUserProfile()
    }

    private fun setupClickListeners() {
        binding.ivAvatar.setOnClickListener {
            openImagePicker()
        }

        binding.btnCambiarFoto.setOnClickListener {
            openImagePicker()
        }

        binding.tvEditarDatos.setOnClickListener {
            // TODO: Abrir diálogo o pantalla de edición de datos
        }

        binding.btnCerrarSesion.setOnClickListener {
            cerrarSesion()
        }
    }

    private fun setupTabs() {
        binding.tabDatos.setOnClickListener {
            selectTab("datos")
        }

        binding.tabGrados.setOnClickListener {
            selectTab("grados")
        }

        binding.tabTienda.setOnClickListener {
            selectTab("tienda")
        }
    }

    private fun selectTab(tab: String) {
        // Resetear todos los tabs
        binding.tabDatos.isSelected = false
        binding.tabGrados.isSelected = false
        binding.tabTienda.isSelected = false

        // Ocultar todos los contenidos
        binding.layoutDatos.visibility = View.GONE
        binding.layoutGrados.visibility = View.GONE
        binding.layoutPedidos.visibility = View.GONE

        // Seleccionar tab y mostrar contenido
        when (tab) {
            "datos" -> {
                binding.tabDatos.isSelected = true
                binding.layoutDatos.visibility = View.VISIBLE
            }
            "grados" -> {
                binding.tabGrados.isSelected = true
                binding.layoutGrados.visibility = View.VISIBLE
            }
            "tienda" -> {
                binding.tabTienda.isSelected = true
                binding.layoutPedidos.visibility = View.VISIBLE
            }
        }
    }

    private fun openImagePicker() {
        val intent = Intent(Intent.ACTION_PICK).apply {
            type = "image/*"
        }
        pickImageLauncher.launch(intent)
    }

    private fun uploadProfileImage(uri: Uri) {
        val userId = auth.currentUser?.uid ?: return

        binding.progressBar.visibility = View.VISIBLE

        val ref = storage.reference.child("usuarios/$userId/perfil.jpg")

        ref.putFile(uri)
            .addOnSuccessListener {
                ref.downloadUrl.addOnSuccessListener { downloadUri ->
                    updateProfilePhotoUrl(downloadUri.toString())
                }
            }
            .addOnFailureListener {
                binding.progressBar.visibility = View.GONE
                Toast.makeText(requireContext(), R.string.error_upload_image, Toast.LENGTH_SHORT).show()
            }
    }

    private fun updateProfilePhotoUrl(url: String) {
        val userId = auth.currentUser?.uid ?: return

        db.collection("usuarios").document(userId)
            .update("fotoUrl", url)
            .addOnSuccessListener {
                binding.progressBar.visibility = View.GONE
                Toast.makeText(requireContext(), R.string.success_profile_updated, Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                binding.progressBar.visibility = View.GONE
            }
    }

    private fun loadUserProfile() {
        val userId = auth.currentUser?.uid ?: return

        binding.progressBar.visibility = View.VISIBLE

        db.collection("usuarios").document(userId)
            .get()
            .addOnSuccessListener { document ->
                binding.progressBar.visibility = View.GONE

                if (document.exists()) {
                    // Datos básicos
                    val nombre = document.getString("nombre") ?: ""
                    val cinturon = document.getString("cinturon") ?: "Blanco"
                    val gup = document.getString("gup") ?: "10° Kup"
                    val fotoUrl = document.getString("fotoUrl")
                    val activo = document.getBoolean("activo") ?: false

                    // Mostrar datos básicos
                    binding.tvNombre.text = nombre
                    binding.tvCinturon.text = cinturon.uppercase()
                    binding.tvGup.text = gup
                    binding.tvEstado.text = if (activo) getString(R.string.perfil_miembro_activo) else getString(R.string.perfil_miembro_inactivo)

                    // Cargar foto
                    if (!fotoUrl.isNullOrEmpty()) {
                        Glide.with(this)
                            .load(fotoUrl)
                            .placeholder(R.drawable.ic_person)
                            .error(R.drawable.ic_person)
                            .circleCrop()
                            .into(binding.ivAvatar)
                    }

                    // Color del cinturón
                    val colorCinturon = getCinturonColor(cinturon)
                    binding.viewCinturonColor.setBackgroundColor(colorCinturon)
                    binding.progressCinturon.setIndicatorColor(colorCinturon)

                    // Datos personales
                    binding.tvFechaNac.text = document.getString("fechaNacimiento") ?: "-"
                    binding.tvLicencia.text = document.getString("licencia") ?: "-"
                    binding.tvFisico.text = document.getString("fisico") ?: "-"
                    binding.tvTelefono.text = document.getString("telefono") ?: "-"

                    // Contacto de emergencia
                    binding.tvEmergencia.text = document.getString("contactoEmergencia") ?: "-"
                    binding.tvTelefonoEmergencia.text = document.getString("telefonoEmergencia") ?: ""

                    // Cargar historial de grados
                    loadGradosHistory(userId)

                    // Cargar pedidos
                    loadPedidos(userId)
                }
            }
            .addOnFailureListener {
                binding.progressBar.visibility = View.GONE
            }
    }

    private fun getCinturonColor(cinturon: String): Int {
        return when (cinturon.lowercase()) {
            "blanco" -> requireContext().getColor(R.color.belt_white)
            "amarillo" -> requireContext().getColor(R.color.belt_yellow)
            "verde" -> requireContext().getColor(R.color.belt_green)
            "azul" -> requireContext().getColor(R.color.belt_blue)
            "rojo" -> requireContext().getColor(R.color.belt_red)
            "negro" -> requireContext().getColor(R.color.belt_black)
            else -> requireContext().getColor(R.color.belt_white)
        }
    }

    private fun loadGradosHistory(userId: String) {
        db.collection("usuarios").document(userId)
            .collection("grados")
            .orderBy("fecha", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { documents ->
                if (documents.isEmpty) {
                    binding.tvNoGrados.visibility = View.VISIBLE
                    binding.rvGrados.visibility = View.GONE
                } else {
                    binding.tvNoGrados.visibility = View.GONE
                    binding.rvGrados.visibility = View.VISIBLE
                    // TODO: Configurar adapter
                }
            }
    }

    private fun loadPedidos(userId: String) {
        db.collection("usuarios").document(userId)
            .collection("pedidos")
            .orderBy("fecha", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .limit(5)
            .get()
            .addOnSuccessListener { documents ->
                if (documents.isEmpty) {
                    binding.tvNoPedidos.visibility = View.VISIBLE
                    binding.rvPedidos.visibility = View.GONE
                } else {
                    binding.tvNoPedidos.visibility = View.GONE
                    binding.rvPedidos.visibility = View.VISIBLE
                    // TODO: Configurar adapter
                }
            }
    }

    private fun cerrarSesion() {
        auth.signOut()

        // Navegar a LoginActivity
        val intent = Intent(requireContext(), LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        requireActivity().finish()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}