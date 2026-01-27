package com.beta.gestionurretausuario.ui.eventos

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.beta.gestionurretausuario.R
import com.bumptech.glide.Glide
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.google.firebase.Timestamp
import java.text.SimpleDateFormat
import java.util.*

class EventosAdapter(
    private val eventos: List<Map<String, Any>>,
    private val onEventoClick: (Map<String, Any>) -> Unit
) : RecyclerView.Adapter<EventosAdapter.EventoViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventoViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_evento, parent, false)
        return EventoViewHolder(view)
    }

    override fun onBindViewHolder(holder: EventoViewHolder, position: Int) {
        holder.bind(eventos[position], onEventoClick)
    }

    override fun getItemCount() = eventos.size

    class EventoViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val cardEvento: MaterialCardView = view.findViewById(R.id.card_evento)
        private val ivImagen: ImageView = view.findViewById(R.id.iv_imagen)
        private val tvMes: TextView = view.findViewById(R.id.tv_mes)
        private val tvDia: TextView = view.findViewById(R.id.tv_dia)
        private val tvTipo: TextView = view.findViewById(R.id.tv_tipo)
        private val tvTitulo: TextView = view.findViewById(R.id.tv_titulo)
        private val tvUbicacion: TextView = view.findViewById(R.id.tv_ubicacion)
        private val btnRegistrarse: MaterialButton = view.findViewById(R.id.btn_registrarse)

        fun bind(evento: Map<String, Any>, onClick: (Map<String, Any>) -> Unit) {
            val titulo = evento["titulo"] as? String ?: "Evento"
            val tipo = evento["tipo"] as? String ?: "general"
            val ubicacion = evento["ubicacion"] as? String ?: ""
            val imagenUrl = evento["imagenUrl"] as? String
            val inscrito = evento["inscrito"] as? Boolean ?: false

            tvTitulo.text = titulo
            tvUbicacion.text = "📍 $ubicacion"

            // Tipo del evento
            val context = itemView.context
            val tipoText = when (tipo.lowercase()) {
                "torneos" -> "🏆 TORNEO NACIONAL"
                "seminarios" -> "📚 SEMINARIO TÉCNICO"
                "examenes" -> "📋 EXAMEN DE GRADO"
                else -> "📅 EVENTO"
            }
            tvTipo.text = tipoText

            // Color según tipo
            val tipoColor = when (tipo.lowercase()) {
                "torneos" -> R.color.primary
                "seminarios" -> R.color.info
                "examenes" -> R.color.warning
                else -> R.color.text_secondary
            }
            tvTipo.setTextColor(context.getColor(tipoColor))

            // Fecha
            val timestamp = evento["fecha"] as? Timestamp
            timestamp?.let {
                val date = it.toDate()
                val mesFormat = SimpleDateFormat("MMM", Locale("es", "ES"))
                val diaFormat = SimpleDateFormat("dd", Locale.getDefault())

                tvMes.text = mesFormat.format(date).uppercase()
                tvDia.text = diaFormat.format(date)
            }

            // Imagen
            if (!imagenUrl.isNullOrEmpty()) {
                Glide.with(context)
                    .load(imagenUrl)
                    .placeholder(R.color.surface_dark)
                    .error(R.color.surface_dark)
                    .centerCrop()
                    .into(ivImagen)
            } else {
                ivImagen.setImageResource(R.color.surface_dark)
            }

            // Botón
            if (inscrito) {
                btnRegistrarse.text = "REGISTRADO"
                btnRegistrarse.isEnabled = false
                btnRegistrarse.setBackgroundColor(context.getColor(R.color.accent_green))
            } else {
                btnRegistrarse.text = "REGISTRARSE"
                btnRegistrarse.isEnabled = true
                btnRegistrarse.setBackgroundColor(context.getColor(R.color.primary))
            }

            cardEvento.setOnClickListener { onClick(evento) }
            btnRegistrarse.setOnClickListener { onClick(evento) }
        }
    }
}