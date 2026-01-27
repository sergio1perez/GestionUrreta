package com.beta.gestionurretausuario.ui.examenes

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.beta.gestionurretausuario.R
import com.beta.gestionurretausuario.databinding.FragmentExamenesBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class ExamenesFragment : Fragment() {

    private var _binding: FragmentExamenesBinding? = null
    private val binding get() = _binding!!

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentExamenesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        setupViews()
        loadUserProgress()
        loadRequisitos()
        loadHistorial()
    }

    private fun setupViews() {
        binding.rvRequisitos.layoutManager = LinearLayoutManager(requireContext())
        binding.rvHistorial.layoutManager = LinearLayoutManager(requireContext())

        binding.btnSolicitarExamen.setOnClickListener {
            solicitarExamen()
        }
    }

    private fun loadUserProgress() {
        val userId = auth.currentUser?.uid ?: return

        binding.progressBar.visibility = View.VISIBLE

        db.collection("usuarios").document(userId)
            .get()
            .addOnSuccessListener { document ->
                binding.progressBar.visibility = View.GONE

                if (document.exists()) {
                    val cinturon = document.getString("cinturon") ?: "Blanco"
                    val gup = document.getString("gup") ?: "10° Kup"
                    val asistenciaActual = document.getLong("asistenciaActual")?.toInt() ?: 0
                    val asistenciaRequerida = document.getLong("asistenciaRequerida")?.toInt() ?: 30
                    val progreso = document.getLong("progresoExamen")?.toInt() ?: 0

                    // Mostrar próximo rango
                    val proximoCinturon = getNextBelt(cinturon)
                    val proximoGup = getNextGup(gup)

                    binding.tvProximoCinturon.text = "Cinturón $proximoCinturon"
                    binding.tvProximoGup.text = "$proximoGup • Requisitos del Dojang"
                    binding.tvGupBadge.text = gup

                    // Mostrar progreso circular
                    binding.progressCircular.progress = progreso
                    binding.tvProgresoPercent.text = "$progreso%"

                    // Mostrar asistencia
                    binding.tvAsistencia.text = "$asistenciaActual / $asistenciaRequerida"
                    binding.progressAsistencia.max = asistenciaRequerida
                    binding.progressAsistencia.progress = asistenciaActual

                    // Actualizar color del progreso según cinturón
                    updateProgressColor(proximoCinturon)

                    // Verificar si puede solicitar examen
                    val puedeExaminar = asistenciaActual >= asistenciaRequerida
                    binding.btnSolicitarExamen.isEnabled = puedeExaminar
                    binding.tvRequisitosLocked.visibility = if (puedeExaminar) View.GONE else View.VISIBLE
                }
            }
            .addOnFailureListener {
                binding.progressBar.visibility = View.GONE
            }
    }

    private fun getNextBelt(currentBelt: String): String {
        return when (currentBelt.lowercase()) {
            "blanco" -> "Amarillo"
            "amarillo" -> "Verde"
            "verde" -> "Azul"
            "azul" -> "Rojo"
            "rojo" -> "Negro"
            "negro" -> "Dan Superior"
            else -> "Amarillo"
        }
    }

    private fun getNextGup(currentGup: String): String {
        val gupNumber = currentGup.filter { it.isDigit() }.toIntOrNull() ?: 10
        return if (gupNumber > 1) {
            "${gupNumber - 1}° Kup"
        } else {
            "1° Dan"
        }
    }

    private fun updateProgressColor(cinturon: String) {
        val colorRes = when (cinturon.lowercase()) {
            "amarillo" -> R.color.belt_yellow
            "verde" -> R.color.belt_green
            "azul" -> R.color.belt_blue
            "rojo" -> R.color.belt_red
            "negro" -> R.color.belt_black
            else -> R.color.belt_yellow
        }
        binding.progressCircular.setIndicatorColor(requireContext().getColor(colorRes))
        binding.progressAsistencia.setIndicatorColor(requireContext().getColor(colorRes))
    }

    private fun loadRequisitos() {
        val userId = auth.currentUser?.uid ?: return

        db.collection("usuarios").document(userId)
            .collection("requisitos")
            .orderBy("orden")
            .get()
            .addOnSuccessListener { documents ->
                if (documents.isEmpty) {
                    // Mostrar requisitos por defecto
                    showDefaultRequisitos()
                } else {
                    val requisitos = documents.map { doc ->
                        val data = doc.data.toMutableMap()
                        data["id"] = doc.id
                        data
                    }
                    val adapter = RequisitosAdapter(requisitos)
                    binding.rvRequisitos.adapter = adapter
                }
            }
    }

    private fun showDefaultRequisitos() {
        val defaultRequisitos = listOf(
            mapOf(
                "titulo" to "Poomsae: Taegeuk 3",
                "descripcion" to "Dominio técnico completo",
                "completado" to true
            ),
            mapOf(
                "titulo" to "Patadas (Kicks)",
                "descripcion" to "Dwi Chagi en proceso",
                "completado" to false,
                "enProgreso" to true
            ),
            mapOf(
                "titulo" to "Teoría: Filosofía",
                "descripcion" to "Pendiente de revisión",
                "completado" to false,
                "bloqueado" to true
            )
        )
        val adapter = RequisitosAdapter(defaultRequisitos)
        binding.rvRequisitos.adapter = adapter
    }

    private fun loadHistorial() {
        val userId = auth.currentUser?.uid ?: return

        db.collection("usuarios").document(userId)
            .collection("grados")
            .orderBy("fecha", Query.Direction.DESCENDING)
            .limit(5)
            .get()
            .addOnSuccessListener { documents ->
                if (documents.isEmpty) {
                    binding.tvNoHistorial.visibility = View.VISIBLE
                    binding.rvHistorial.visibility = View.GONE
                } else {
                    binding.tvNoHistorial.visibility = View.GONE
                    binding.rvHistorial.visibility = View.VISIBLE

                    val historial = documents.map { doc ->
                        val data = doc.data.toMutableMap()
                        data["id"] = doc.id
                        data
                    }
                    val adapter = HistorialExamenesAdapter(historial)
                    binding.rvHistorial.adapter = adapter
                }
            }
    }

    private fun solicitarExamen() {
        val userId = auth.currentUser?.uid ?: return

        binding.btnSolicitarExamen.isEnabled = false

        val solicitud = hashMapOf(
            "userId" to userId,
            "fechaSolicitud" to com.google.firebase.Timestamp.now(),
            "estado" to "pendiente"
        )

        db.collection("solicitudesExamen")
            .add(solicitud)
            .addOnSuccessListener {
                Toast.makeText(
                    requireContext(),
                    "Solicitud de examen enviada correctamente",
                    Toast.LENGTH_LONG
                ).show()
                binding.btnSolicitarExamen.text = "SOLICITUD ENVIADA"
            }
            .addOnFailureListener {
                binding.btnSolicitarExamen.isEnabled = true
                Toast.makeText(
                    requireContext(),
                    "Error al enviar la solicitud",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}