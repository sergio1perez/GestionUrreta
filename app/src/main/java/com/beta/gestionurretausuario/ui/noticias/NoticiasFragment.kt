package com.beta.gestionurretausuario.ui.noticias

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.beta.gestionurretausuario.databinding.FragmentNoticiasBinding
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class NoticiasFragment : Fragment() {

    private var _binding: FragmentNoticiasBinding? = null
    private val binding get() = _binding!!

    private lateinit var db: FirebaseFirestore

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentNoticiasBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        db = FirebaseFirestore.getInstance()

        setupViews()
        loadNoticias()
    }

    private fun setupViews() {
        binding.rvNoticias.layoutManager = LinearLayoutManager(requireContext())

        // Pull to refresh
        binding.swipeRefresh.setOnRefreshListener {
            loadNoticias()
        }
        binding.swipeRefresh.setColorSchemeResources(
            com.beta.gestionurretausuario.R.color.primary
        )
    }

    private fun loadNoticias() {
        binding.progressBar.visibility = View.VISIBLE

        db.collection("noticias")
            .orderBy("fecha", Query.Direction.DESCENDING)
            .limit(20)
            .get()
            .addOnSuccessListener { documents ->
                binding.progressBar.visibility = View.GONE
                binding.swipeRefresh.isRefreshing = false

                if (documents.isEmpty) {
                    binding.layoutEmpty.visibility = View.VISIBLE  // CAMBIO
                    binding.rvNoticias.visibility = View.GONE
                } else {
                    binding.layoutEmpty.visibility = View.GONE  // CAMBIO
                    binding.rvNoticias.visibility = View.VISIBLE

                    val noticias = documents.map { doc ->
                        val data = doc.data.toMutableMap()
                        data["id"] = doc.id
                        data
                    }

                    val adapter = NoticiasAdapter(noticias) { noticia ->
                        onNoticiaClick(noticia)
                    }
                    binding.rvNoticias.adapter = adapter
                }
            }
            .addOnFailureListener { e ->
                binding.progressBar.visibility = View.GONE
                binding.swipeRefresh.isRefreshing = false
                binding.layoutEmpty.visibility = View.VISIBLE  // CAMBIO
                binding.rvNoticias.visibility = View.GONE
            }
    }

    private fun onNoticiaClick(noticia: Map<String, Any>) {
        // Abrir detalle de la noticia
        // TODO: Implementar navegación a detalle
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}