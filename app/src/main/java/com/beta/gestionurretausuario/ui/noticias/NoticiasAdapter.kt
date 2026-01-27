package com.beta.gestionurretausuario.ui.noticias

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.beta.gestionurretausuario.R
import com.bumptech.glide.Glide
import com.google.firebase.Timestamp
import java.util.concurrent.TimeUnit

class NoticiasAdapter(
    private val noticias: List<Map<String, Any>>,
    private val onNoticiaClick: (Map<String, Any>) -> Unit
) : RecyclerView.Adapter<NoticiasAdapter.NoticiaViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NoticiaViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_noticia, parent, false)
        return NoticiaViewHolder(view)
    }

    override fun onBindViewHolder(holder: NoticiaViewHolder, position: Int) {
        holder.bind(noticias[position], onNoticiaClick)
    }

    override fun getItemCount(): Int = noticias.size

    class NoticiaViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val ivImagen: ImageView = itemView.findViewById(R.id.iv_imagen)
        private val tvCategoria: TextView = itemView.findViewById(R.id.tv_categoria)
        private val tvTiempo: TextView = itemView.findViewById(R.id.tv_tiempo)
        private val tvTitulo: TextView = itemView.findViewById(R.id.tv_titulo)
        private val tvDescripcion: TextView = itemView.findViewById(R.id.tv_descripcion)
        private val tvLikes: TextView = itemView.findViewById(R.id.tv_likes)
        private val tvLeerMas: TextView = itemView.findViewById(R.id.tv_leer_mas)

        fun bind(noticia: Map<String, Any>, onNoticiaClick: (Map<String, Any>) -> Unit) {
            val titulo = noticia["titulo"] as? String ?: ""
            val descripcion = noticia["descripcion"] as? String ?: ""
            val categoria = noticia["categoria"] as? String ?: ""
            val imagenUrl = noticia["imagenUrl"] as? String
            val fecha = noticia["fecha"] as? Timestamp
            val likes = (noticia["likes"] as? Long)?.toInt() ?: 0
            val importante = noticia["importante"] as? Boolean ?: false

            tvTitulo.text = titulo
            tvDescripcion.text = descripcion
            tvCategoria.text = categoria.uppercase()
            tvLikes.text = likes.toString()

            // Calcular tiempo transcurrido
            fecha?.let {
                tvTiempo.text = getTimeAgo(it)
            }

            // Cargar imagen
            if (!imagenUrl.isNullOrEmpty()) {
                Glide.with(itemView.context)
                    .load(imagenUrl)
                    .placeholder(R.drawable.bg_surface_rounded)
                    .error(R.drawable.bg_surface_rounded)
                    .centerCrop()
                    .into(ivImagen)
                ivImagen.visibility = View.VISIBLE
            } else {
                ivImagen.visibility = View.GONE
            }

            // Estilo para noticias importantes
            if (importante) {
                tvCategoria.setBackgroundResource(R.drawable.bg_badge_primary)
                tvCategoria.setTextColor(itemView.context.getColor(R.color.white))
            } else {
                tvCategoria.setBackgroundResource(R.drawable.bg_badge_primary_outline)
                tvCategoria.setTextColor(itemView.context.getColor(R.color.primary))
            }

            // Click listeners
            itemView.setOnClickListener { onNoticiaClick(noticia) }
            tvLeerMas.setOnClickListener { onNoticiaClick(noticia) }
        }

        private fun getTimeAgo(timestamp: Timestamp): String {
            val now = System.currentTimeMillis()
            val diff = now - timestamp.toDate().time

            val hours = TimeUnit.MILLISECONDS.toHours(diff)
            val days = TimeUnit.MILLISECONDS.toDays(diff)

            return when {
                hours < 1 -> "Hace unos minutos"
                hours < 24 -> "HACE $hours HORAS"
                days < 7 -> "HACE $days DÍAS"
                else -> {
                    val weeks = days / 7
                    "HACE $weeks SEMANAS"
                }
            }
        }
    }
}