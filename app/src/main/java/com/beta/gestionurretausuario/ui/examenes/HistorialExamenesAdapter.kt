package com.beta.gestionurretausuario.ui.examenes

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.beta.gestionurretausuario.R
import com.google.firebase.Timestamp
import java.text.SimpleDateFormat
import java.util.*

class HistorialExamenesAdapter(
    private val historial: List<Map<String, Any>>
) : RecyclerView.Adapter<HistorialExamenesAdapter.HistorialViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistorialViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_examen_historial, parent, false)
        return HistorialViewHolder(view)
    }

    override fun onBindViewHolder(holder: HistorialViewHolder, position: Int) {
        holder.bind(historial[position])
    }

    override fun getItemCount(): Int = historial.size

    class HistorialViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvCinturon: TextView = itemView.findViewById(R.id.tv_cinturon)
        private val tvFecha: TextView = itemView.findViewById(R.id.tv_fecha)

        fun bind(examen: Map<String, Any>) {
            val cinturon = examen["cinturon"] as? String ?: ""
            val fecha = examen["fecha"] as? Timestamp
            val calificacion = examen["calificacion"] as? String ?: "A"

            tvCinturon.text = "Cinturón $cinturon"

            val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale("es", "ES"))
            val fechaStr = fecha?.toDate()?.let { dateFormat.format(it).uppercase() } ?: ""
            tvFecha.text = "$fechaStr • CALIFICACIÓN: $calificacion"
        }
    }
}