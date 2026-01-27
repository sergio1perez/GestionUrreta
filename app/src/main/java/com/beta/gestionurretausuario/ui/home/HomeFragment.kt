package com.beta.gestionurretausuario.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.beta.gestionurretausuario.R
import com.beta.gestionurretausuario.databinding.FragmentHomeBinding
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        loadUserData()
        loadNextClass()
        loadWeeklyActivity()
    }

    private fun loadUserData() {
        val currentUser = auth.currentUser ?: return

        // Cargar nombre
        val firstName = currentUser.displayName?.split(" ")?.firstOrNull() ?: "Usuario"
        binding.tvGreeting.text = getString(R.string.home_greeting)
        binding.tvUserName.text = firstName

        // Cargar avatar
        currentUser.photoUrl?.let { uri ->
            Glide.with(this)
                .load(uri)
                .placeholder(R.drawable.ic_person)
                .error(R.drawable.ic_person)
                .circleCrop()
                .into(binding.ivUserAvatar)
        }

        // Cargar datos de Firestore
        db.collection("usuarios").document(currentUser.uid)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val nombre = document.getString("nombre") ?: currentUser.displayName ?: "Usuario"
                    val cinturon = document.getString("cinturon") ?: "Blanco"
                    val gup = document.getString("gup") ?: "10° Kup"
                    val fotoUrl = document.getString("fotoUrl")

                    binding.tvUserName.text = nombre.split(" ").firstOrNull() ?: nombre
                    binding.tvCinturon.text = "CINTURÓN ${cinturon.uppercase()}"
                    binding.tvGup.text = gup

                    // Color del cinturón
                    updateBeltColor(cinturon)

                    // Foto de Firestore
                    if (!fotoUrl.isNullOrEmpty()) {
                        Glide.with(this)
                            .load(fotoUrl)
                            .placeholder(R.drawable.ic_person)
                            .error(R.drawable.ic_person)
                            .circleCrop()
                            .into(binding.ivUserAvatar)
                    }

                    // Calcular progreso (ejemplo: basado en asistencia)
                    val asistencia = document.getLong("asistenciaActual")?.toInt() ?: 0
                    val asistenciaRequerida = document.getLong("asistenciaRequerida")?.toInt() ?: 30
                    val progreso = if (asistenciaRequerida > 0) (asistencia * 100) / asistenciaRequerida else 0
                    binding.tvProgress.text = "$progreso%"
                    binding.progressCinturon.progress = progreso
                }
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

        val nextBelt = when (cinturon.lowercase()) {
            "blanco" -> "AMARILLO"
            "amarillo" -> "VERDE"
            "verde" -> "AZUL"
            "azul" -> "ROJO"
            "rojo" -> "NEGRO"
            else -> "SIGUIENTE"
        }

        binding.tvNextBelt.text = "PROGRESO AL $nextBelt"
        binding.progressCinturon.setIndicatorColor(requireContext().getColor(colorRes))
    }

    private fun loadNextClass() {
        val userId = auth.currentUser?.uid ?: return

        // Buscar próxima clase
        val today = Calendar.getInstance()
        val dayOfWeek = today.get(Calendar.DAY_OF_WEEK)

        db.collection("clases")
            .whereGreaterThanOrEqualTo("fecha", com.google.firebase.Timestamp.now())
            .orderBy("fecha")
            .limit(1)
            .get()
            .addOnSuccessListener { documents ->
                if (!documents.isEmpty) {
                    val clase = documents.first()
                    binding.tvNextClassName.text = clase.getString("nombre") ?: "Clase"

                    val timestamp = clase.getTimestamp("fecha")
                    timestamp?.let {
                        val date = it.toDate()
                        val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
                        val dateFormat = SimpleDateFormat("EEEE", Locale("es", "ES"))

                        val isToday = isSameDay(date, today.time)
                        val dayText = if (isToday) "Hoy" else dateFormat.format(date).replaceFirstChar { c -> c.uppercase() }

                        binding.tvNextClassTime.text = "$dayText • ${timeFormat.format(date)} - ${clase.getString("sala") ?: "Sala A"}"
                    }

                    binding.cardNextClass.visibility = View.VISIBLE
                } else {
                    binding.cardNextClass.visibility = View.GONE
                }
            }
            .addOnFailureListener {
                // Mostrar datos de ejemplo si no hay clases
                binding.tvNextClassName.text = "Sparring & Combate"
                binding.tvNextClassTime.text = "Hoy • 18:00 - Sala A"
                binding.cardNextClass.visibility = View.VISIBLE
            }
    }

    private fun isSameDay(date1: Date, date2: Date): Boolean {
        val cal1 = Calendar.getInstance().apply { time = date1 }
        val cal2 = Calendar.getInstance().apply { time = date2 }
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
    }

    private fun loadWeeklyActivity() {
        val userId = auth.currentUser?.uid ?: return

        db.collection("usuarios").document(userId)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val racha = document.getLong("racha")?.toInt() ?: 0
                    binding.tvStreak.text = "Racha: $racha días"

                    // Cargar días de asistencia de esta semana
                    val diasAsistidos = document.get("diasAsistidosSemana") as? List<*>
                    updateWeekDays(diasAsistidos?.map { it.toString().toIntOrNull() ?: 0 } ?: emptyList())
                }
            }
    }

    private fun updateWeekDays(diasAsistidos: List<Int>) {
        val dayViews = listOf(
            binding.dayL, binding.dayM, binding.dayX,
            binding.dayJ, binding.dayV, binding.dayS, binding.dayD
        )

        val today = Calendar.getInstance().get(Calendar.DAY_OF_WEEK)

        dayViews.forEachIndexed { index, textView ->
            val dayNumber = if (index == 6) 1 else index + 2 // Ajustar para que L=2, D=1

            when {
                diasAsistidos.contains(dayNumber) -> {
                    textView.setBackgroundResource(R.drawable.bg_day_completed)
                    textView.setTextColor(requireContext().getColor(R.color.white))
                }
                dayNumber == today -> {
                    textView.setBackgroundResource(R.drawable.bg_day_today)
                    textView.setTextColor(requireContext().getColor(R.color.primary))
                }
                else -> {
                    textView.background = null
                    textView.setTextColor(requireContext().getColor(R.color.text_secondary))
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}