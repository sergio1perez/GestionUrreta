package com.beta.gestionurretausuario.ui.clases

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.beta.gestionurretausuario.R
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.google.firebase.Timestamp
import java.text.SimpleDateFormat
import java.util.*

class ClasesAdapter(
    private val clasesPorPeriodo: Map<String, List<Map<String, Any>>>,
    private val onClaseClick: (Map<String, Any>) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val items = mutableListOf<Any>()

    init {
        // Aplanar el mapa en una lista con headers
        clasesPorPeriodo.forEach { (periodo, clases) ->
            items.add(periodo) // Header
            items.addAll(clases) // Clases
        }
    }

    companion object {
        private const val TYPE_HEADER = 0
        private const val TYPE_CLASE = 1
    }

    override fun getItemViewType(position: Int): Int {
        return if (items[position] is String) TYPE_HEADER else TYPE_CLASE
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == TYPE_HEADER) {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_clase_header, parent, false)
            HeaderViewHolder(view)
        } else {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_clase, parent, false)
            ClaseViewHolder(view)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is HeaderViewHolder -> holder.bind(items[position] as String)
            is ClaseViewHolder -> holder.bind(items[position] as Map<String, Any>, onClaseClick)
        }
    }

    override fun getItemCount() = items.size

    class HeaderViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val tvHeader: TextView = view.findViewById(R.id.tv_header)

        fun bind(periodo: String) {
            tvHeader.text = periodo
        }
    }

    class ClaseViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val cardClase: MaterialCardView = view.findViewById(R.id.card_clase)
        private val tvHora: TextView = view.findViewById(R.id.tv_hora)
        private val tvAmPm: TextView = view.findViewById(R.id.tv_am_pm)
        private val tvCategoria: TextView = view.findViewById(R.id.tv_categoria)
        private val tvEstado: TextView = view.findViewById(R.id.tv_estado)
        private val tvNombre: TextView = view.findViewById(R.id.tv_nombre)
        private val tvInstructor: TextView = view.findViewById(R.id.tv_instructor)
        private val btnAccion: MaterialButton = view.findViewById(R.id.btn_accion)

        fun bind(clase: Map<String, Any>, onClick: (Map<String, Any>) -> Unit) {
            val nombre = clase["nombre"] as? String ?: "Clase"
            val categoria = clase["categoria"] as? String ?: "GENERAL"
            val instructor = clase["instructor"] as? String ?: ""
            val sala = clase["sala"] as? String ?: ""
            val cuposDisponibles = (clase["cuposDisponibles"] as? Long)?.toInt() ?: 0
            val cuposTotales = (clase["cuposTotales"] as? Long)?.toInt() ?: 20

            // Formatear hora
            val timestamp = clase["fecha"] as? Timestamp
            timestamp?.let {
                val date = it.toDate()
                val hourFormat = SimpleDateFormat("hh:mm", Locale.getDefault())
                val amPmFormat = SimpleDateFormat("a", Locale.getDefault())

                tvHora.text = hourFormat.format(date)
                tvAmPm.text = amPmFormat.format(date).uppercase()
            }

            tvCategoria.text = categoria.uppercase()
            tvNombre.text = nombre
            tvInstructor.text = "$instructor • $sala"

            // Estado de cupos
            val context = itemView.context
            when {
                cuposDisponibles > 5 -> {
                    tvEstado.text = "● Cupos disponibles"
                    tvEstado.setTextColor(context.getColor(R.color.accent_green))
                }
                cuposDisponibles > 0 -> {
                    tvEstado.text = "● Pocos cupos"
                    tvEstado.setTextColor(context.getColor(R.color.warning))
                }
                else -> {
                    tvEstado.text = "● Completo"
                    tvEstado.setTextColor(context.getColor(R.color.error))
                }
            }

            // Botón de acción
            val inscrito = clase["inscrito"] as? Boolean ?: false
            if (inscrito) {
                btnAccion.text = "Check-in"
                btnAccion.setBackgroundColor(context.getColor(R.color.accent_green))
            } else if (cuposDisponibles > 0) {
                btnAccion.text = "Reservar"
                btnAccion.setBackgroundColor(context.getColor(R.color.card_dark))
            } else {
                btnAccion.text = "Lista de Espera"
                btnAccion.setBackgroundColor(context.getColor(R.color.card_dark))
            }

            cardClase.setOnClickListener { onClick(clase) }
            btnAccion.setOnClickListener { onClick(clase) }
        }
    }
}