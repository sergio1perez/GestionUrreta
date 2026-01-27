package com.beta.gestionurretausuario.ui.clases

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.beta.gestionurretausuario.R
import com.beta.gestionurretausuario.databinding.FragmentClasesBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import java.text.SimpleDateFormat
import java.util.*

class ClasesFragment : Fragment() {

    private var _binding: FragmentClasesBinding? = null
    private val binding get() = _binding!!

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    private var selectedDate: Calendar = Calendar.getInstance()
    private var viewMode = "diario" // "diario" o "semanal"

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentClasesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        setupViews()
        setupDateSelector()
        loadClasses()
    }

    private fun setupViews() {
        // Setup RecyclerView
        binding.rvClases.layoutManager = LinearLayoutManager(requireContext())

        // Tab Diario/Semanal
        binding.tabDiario.setOnClickListener {
            viewMode = "diario"
            updateTabSelection()
            loadClasses()
        }

        binding.tabSemanal.setOnClickListener {
            viewMode = "semanal"
            updateTabSelection()
            loadClasses()
        }

        updateTabSelection()
    }

    private fun updateTabSelection() {
        binding.tabDiario.isSelected = viewMode == "diario"
        binding.tabSemanal.isSelected = viewMode == "semanal"
    }

    private fun setupDateSelector() {
        val dateFormat = SimpleDateFormat("dd", Locale.getDefault())
        val dayFormat = SimpleDateFormat("EEE", Locale("es", "ES"))

        // Configurar los 5 días visibles
        val days = listOf(
            binding.day1, binding.day2, binding.day3, binding.day4, binding.day5
        )
        val dayLabels = listOf(
            binding.dayLabel1, binding.dayLabel2, binding.dayLabel3, binding.dayLabel4, binding.dayLabel5
        )

        for (i in days.indices) {
            val cal = Calendar.getInstance()
            cal.add(Calendar.DAY_OF_YEAR, i)

            days[i].text = dateFormat.format(cal.time)

            val label = when (i) {
                0 -> "HOY"
                1 -> "MAÑ"
                else -> dayFormat.format(cal.time).uppercase().take(3)
            }
            dayLabels[i].text = label

            days[i].setOnClickListener {
                selectedDate = cal.clone() as Calendar
                updateDateSelection(i)
                loadClasses()
            }
        }

        // Seleccionar hoy por defecto
        updateDateSelection(0)
    }

    private fun updateDateSelection(selectedIndex: Int) {
        val days = listOf(
            binding.layoutDay1, binding.layoutDay2, binding.layoutDay3,
            binding.layoutDay4, binding.layoutDay5
        )

        days.forEachIndexed { index, layout ->
            if (index == selectedIndex) {
                layout.setBackgroundResource(R.drawable.bg_day_selected)
            } else {
                layout.background = null
            }
        }
    }

    private fun loadClasses() {
        binding.progressBar.visibility = View.VISIBLE

        // Obtener inicio y fin del día/semana seleccionado
        val startCal = selectedDate.clone() as Calendar
        startCal.set(Calendar.HOUR_OF_DAY, 0)
        startCal.set(Calendar.MINUTE, 0)
        startCal.set(Calendar.SECOND, 0)

        val endCal = selectedDate.clone() as Calendar
        if (viewMode == "semanal") {
            endCal.add(Calendar.DAY_OF_YEAR, 7)
        } else {
            endCal.add(Calendar.DAY_OF_YEAR, 1)
        }
        endCal.set(Calendar.HOUR_OF_DAY, 0)
        endCal.set(Calendar.MINUTE, 0)
        endCal.set(Calendar.SECOND, 0)

        db.collection("clases")
            .whereGreaterThanOrEqualTo("fecha", com.google.firebase.Timestamp(startCal.time))
            .whereLessThan("fecha", com.google.firebase.Timestamp(endCal.time))
            .orderBy("fecha", Query.Direction.ASCENDING)
            .get()
            .addOnSuccessListener { documents ->
                binding.progressBar.visibility = View.GONE

                if (documents.isEmpty) {
                    binding.layoutNoClases.visibility = View.VISIBLE
                    binding.rvClases.visibility = View.GONE
                } else {
                    binding.layoutNoClases.visibility = View.GONE
                    binding.rvClases.visibility = View.VISIBLE

                    // Agrupar por período del día
                    val clasesPorPeriodo = mutableMapOf<String, MutableList<Map<String, Any>>>()

                    documents.forEach { doc ->
                        val data = doc.data.toMutableMap()
                        data["id"] = doc.id

                        val timestamp = doc.getTimestamp("fecha")
                        val hora = timestamp?.toDate()?.let {
                            val cal = Calendar.getInstance()
                            cal.time = it
                            cal.get(Calendar.HOUR_OF_DAY)
                        } ?: 12

                        val periodo = when {
                            hora < 12 -> "MAÑANA"
                            hora < 18 -> "TARDE"
                            else -> "NOCHE"
                        }

                        clasesPorPeriodo.getOrPut(periodo) { mutableListOf() }.add(data)
                    }

                    // Configurar adapter
                    val adapter = ClasesAdapter(clasesPorPeriodo) { clase ->
                        onClaseClick(clase)
                    }
                    binding.rvClases.adapter = adapter
                }
            }
            .addOnFailureListener { e ->
                binding.progressBar.visibility = View.GONE
                binding.layoutNoClases.visibility = View.VISIBLE
                binding.rvClases.visibility = View.GONE
            }
    }

    private fun onClaseClick(clase: Map<String, Any>) {
        val claseId = clase["id"] as? String ?: return
        val userId = auth.currentUser?.uid ?: return

        // Verificar si ya está inscrito
        db.collection("clases").document(claseId)
            .collection("inscritos")
            .document(userId)
            .get()
            .addOnSuccessListener { doc ->
                if (doc.exists()) {
                    // Ya inscrito - hacer check-in
                    Toast.makeText(requireContext(), "Check-in realizado", Toast.LENGTH_SHORT).show()
                } else {
                    // Inscribirse
                    inscribirseEnClase(claseId)
                }
            }
    }

    private fun inscribirseEnClase(claseId: String) {
        val userId = auth.currentUser?.uid ?: return

        val inscripcion = hashMapOf(
            "userId" to userId,
            "fechaInscripcion" to com.google.firebase.Timestamp.now(),
            "checkIn" to false
        )

        db.collection("clases").document(claseId)
            .collection("inscritos")
            .document(userId)
            .set(inscripcion)
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "Reserva confirmada", Toast.LENGTH_SHORT).show()
                loadClasses()
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Error al reservar", Toast.LENGTH_SHORT).show()
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}