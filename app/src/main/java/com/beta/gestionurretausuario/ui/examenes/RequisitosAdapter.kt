package com.beta.gestionurretausuario.ui.examenes

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.beta.gestionurretausuario.R

class RequisitosAdapter(
    private val requisitos: List<Map<String, Any>>
) : RecyclerView.Adapter<RequisitosAdapter.RequisitoViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RequisitoViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_requisito, parent, false)
        return RequisitoViewHolder(view)
    }

    override fun onBindViewHolder(holder: RequisitoViewHolder, position: Int) {
        holder.bind(requisitos[position])
    }

    override fun getItemCount(): Int = requisitos.size

    class RequisitoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val ivEstado: ImageView = itemView.findViewById(R.id.iv_estado)
        private val tvTitulo: TextView = itemView.findViewById(R.id.tv_titulo)
        private val tvDescripcion: TextView = itemView.findViewById(R.id.tv_descripcion)
        private val ivAction: ImageView = itemView.findViewById(R.id.iv_action)

        fun bind(requisito: Map<String, Any>) {
            val titulo = requisito["titulo"] as? String ?: ""
            val descripcion = requisito["descripcion"] as? String ?: ""
            val completado = requisito["completado"] as? Boolean ?: false
            val enProgreso = requisito["enProgreso"] as? Boolean ?: false
            val bloqueado = requisito["bloqueado"] as? Boolean ?: false

            tvTitulo.text = titulo
            tvDescripcion.text = descripcion

            when {
                completado -> {
                    ivEstado.setImageResource(R.drawable.ic_check_circle)
                    ivEstado.setColorFilter(itemView.context.getColor(R.color.accent_green))
                    ivEstado.setBackgroundResource(R.drawable.bg_icon_circle_green)
                    ivAction.setImageResource(R.drawable.ic_chevron_right)
                    ivAction.setColorFilter(itemView.context.getColor(R.color.text_hint))
                }
                enProgreso -> {
                    ivEstado.setImageResource(R.drawable.ic_arrow_forward)
                    ivEstado.setColorFilter(itemView.context.getColor(R.color.warning))
                    ivEstado.setBackgroundResource(R.drawable.bg_icon_circle_yellow)
                    ivAction.setImageResource(R.drawable.ic_chevron_right)
                    ivAction.setColorFilter(itemView.context.getColor(R.color.text_hint))
                }
                bloqueado -> {
                    ivEstado.setImageResource(R.drawable.ic_lock)
                    ivEstado.setColorFilter(itemView.context.getColor(R.color.text_hint))
                    ivEstado.setBackgroundResource(R.drawable.bg_icon_circle_gray)
                    ivAction.setImageResource(R.drawable.ic_lock)
                    ivAction.setColorFilter(itemView.context.getColor(R.color.text_hint))
                    itemView.alpha = 0.6f
                }
                else -> {
                    ivEstado.setImageResource(R.drawable.ic_check_circle)
                    ivEstado.setColorFilter(itemView.context.getColor(R.color.text_hint))
                    ivEstado.setBackgroundResource(R.drawable.bg_icon_circle_gray)
                    ivAction.setImageResource(R.drawable.ic_chevron_right)
                    ivAction.setColorFilter(itemView.context.getColor(R.color.text_hint))
                }
            }
        }
    }
}