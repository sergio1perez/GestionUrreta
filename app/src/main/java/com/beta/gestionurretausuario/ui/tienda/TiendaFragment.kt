package com.beta.gestionurretausuario.ui.tienda

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import com.beta.gestionurretausuario.databinding.FragmentTiendaBinding
import com.google.firebase.firestore.FirebaseFirestore

class TiendaFragment : Fragment() {

    private var _binding: FragmentTiendaBinding? = null
    private val binding get() = _binding!!

    private lateinit var db: FirebaseFirestore
    private var selectedCategory = "todo"

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTiendaBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        db = FirebaseFirestore.getInstance()

        setupRecyclerView()
        setupCategoryChips()
        setupSearchView()
        loadProducts()
    }

    private fun setupRecyclerView() {
        binding.rvProductos.layoutManager = GridLayoutManager(requireContext(), 2)
    }

    private fun setupCategoryChips() {
        binding.chipTodo.setOnClickListener {
            selectedCategory = "todo"
            updateChipSelection()
            loadProducts()
        }

        binding.chipDoboks.setOnClickListener {
            selectedCategory = "doboks"
            updateChipSelection()
            loadProducts()
        }

        binding.chipCinturones.setOnClickListener {
            selectedCategory = "cinturones"
            updateChipSelection()
            loadProducts()
        }

        binding.chipProtecciones.setOnClickListener {
            selectedCategory = "protecciones"
            updateChipSelection()
            loadProducts()
        }
    }

    private fun updateChipSelection() {
        binding.chipTodo.isChecked = selectedCategory == "todo"
        binding.chipDoboks.isChecked = selectedCategory == "doboks"
        binding.chipCinturones.isChecked = selectedCategory == "cinturones"
        binding.chipProtecciones.isChecked = selectedCategory == "protecciones"
    }

    private fun setupSearchView() {
        binding.searchView.setOnQueryTextListener(object : androidx.appcompat.widget.SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                query?.let { searchProducts(it) }
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                if (newText.isNullOrEmpty()) {
                    loadProducts()
                }
                return true
            }
        })
    }

    private fun loadProducts() {
        binding.progressBar.visibility = View.VISIBLE

        var query = db.collection("productos")
            .whereEqualTo("activo", true)

        if (selectedCategory != "todo") {
            query = query.whereEqualTo("categoria", selectedCategory)
        }

        query.get()
            .addOnSuccessListener { documents ->
                binding.progressBar.visibility = View.GONE

                if (documents.isEmpty) {
                    binding.tvNoProductos.visibility = View.VISIBLE
                    binding.rvProductos.visibility = View.GONE
                } else {
                    binding.tvNoProductos.visibility = View.GONE
                    binding.rvProductos.visibility = View.VISIBLE
                    // TODO: Configurar adapter con los productos
                }
            }
            .addOnFailureListener {
                binding.progressBar.visibility = View.GONE
            }
    }

    private fun searchProducts(query: String) {
        binding.progressBar.visibility = View.VISIBLE

        // Búsqueda simple por nombre (para búsqueda avanzada usar Algolia o similar)
        db.collection("productos")
            .whereEqualTo("activo", true)
            .get()
            .addOnSuccessListener { documents ->
                binding.progressBar.visibility = View.GONE

                val filtered = documents.filter { doc ->
                    val nombre = doc.getString("nombre") ?: ""
                    nombre.contains(query, ignoreCase = true)
                }

                if (filtered.isEmpty()) {
                    binding.tvNoProductos.visibility = View.VISIBLE
                    binding.rvProductos.visibility = View.GONE
                } else {
                    binding.tvNoProductos.visibility = View.GONE
                    binding.rvProductos.visibility = View.VISIBLE
                    // TODO: Configurar adapter
                }
            }
            .addOnFailureListener {
                binding.progressBar.visibility = View.GONE
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}