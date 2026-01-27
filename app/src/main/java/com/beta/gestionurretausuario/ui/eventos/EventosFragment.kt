package com.beta.gestionurretausuario.ui.eventos

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.beta.gestionurretausuario.R
import com.beta.gestionurretausuario.databinding.FragmentEventosBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class EventosFragment : Fragment() {

    private var _binding: FragmentEventosBinding? = null
    private val binding get() = _binding!!

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    private var selectedCategory = "todos"

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEventosBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        setupViews()
        setupCategoryChips()
        loadEventos()
    }

    private fun setupViews() {
        binding.rvEventos.layoutManager = LinearLayoutManager(requireContext())
    }

    private fun setupCategoryChips() {
        binding.chipTodos.setOnClickListener {
            selectedCategory = "todos"
            updateChipSelection()
            loadEventos()
        }

        binding.chipTorneos.setOnClickListener {
            selectedCategory = "torneos"
            updateChipSelection()
            loadEventos()
        }

        binding.chipSeminarios.setOnClickListener {
            selectedCategory = "seminarios"
            updateChipSelection()
            loadEventos()
        }

        binding.chipExamenes.setOnClickListener {
            selectedCategory = "examenes"
            updateChipSelection()
            loadEventos()
        }
    }

    private fun updateChipSelection() {
        binding.chipTodos.isChecked = selectedCategory == "todos"
        binding.chipTorneos.isChecked = selectedCategory == "torneos"
        binding.chipSeminarios.isChecked = selectedCategory == "seminarios"
        binding.chipExamenes.isChecked = selectedCategory == "examenes"
    }

    private fun loadEventos() {
        binding.progressBar.visibility = View.VISIBLE

        var query = db.collection("eventos")
            .whereGreaterThanOrEqualTo("fecha", com.google.firebase.Timestamp.now())
            .orderBy("fecha", Query.Direction.ASCENDING)

        if (selectedCategory != "todos") {
            query = query.whereEqualTo("tipo", selectedCategory)
        }

        query.get()
            .addOnSuccessListener { documents ->
                binding.progressBar.visibility = View.GONE

                if (documents.isEmpty) {
                    binding.tvNoEventos.visibility = View.VISIBLE
                    binding.rvEventos.visibility = View.GONE
                } else {
                    binding.tvNoEventos.visibility = View.GONE
                    binding.rvEventos.visibility = View.VISIBLE

                    val eventos = documents.map { doc ->
                        val data = doc.data.toMutableMap()
                        data["id"] = doc.id
                        data
                    }

                    val adapter = EventosAdapter(eventos) { evento ->
                        onEventoClick(evento)
                    }
                    binding.rvEventos.adapter = adapter
                }
            }
            .addOnFailureListener { e ->
                binding.progressBar.visibility = View.GONE
                binding.tvNoEventos.visibility = View.VISIBLE
                binding.rvEventos.visibility = View.GONE
            }
    }

    private fun onEventoClick(evento: Map<String, Any>) {
        val eventoId = evento["id"] as? String ?: return
        val userId = auth.currentUser?.uid ?: return

        // Verificar si ya está inscrito
        db.collection("eventos").document(eventoId)
            .collection("inscritos")
            .document(userId)
            .get()
            .addOnSuccessListener { doc ->
                if (doc.exists()) {
                    Toast.makeText(requireContext(), "Ya estás inscrito en este evento", Toast.LENGTH_SHORT).show()
                } else {
                    inscribirseEnEvento(eventoId, evento)
                }
            }
    }

    private fun inscribirseEnEvento(eventoId: String, evento: Map<String, Any>) {
        val userId = auth.currentUser?.uid ?: return
        val userName = auth.currentUser?.displayName ?: ""

        val inscripcion = hashMapOf(
            "userId" to userId,
            "nombre" to userName,
            "fechaInscripcion" to com.google.firebase.Timestamp.now(),
            "estado" to "pendiente"
        )

        db.collection("eventos").document(eventoId)
            .collection("inscritos")
            .document(userId)
            .set(inscripcion)
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "Inscripción realizada", Toast.LENGTH_SHORT).show()
                loadEventos()
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Error al inscribirse", Toast.LENGTH_SHORT).show()
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}