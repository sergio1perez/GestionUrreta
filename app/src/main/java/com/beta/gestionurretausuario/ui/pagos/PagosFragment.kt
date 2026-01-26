package com.beta.gestionurretausuario.ui.pagos

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.beta.gestionurretausuario.R
import com.beta.gestionurretausuario.databinding.FragmentPagosBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class PagosFragment : Fragment() {

    private var _binding: FragmentPagosBinding? = null
    private val binding get() = _binding!!

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPagosBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        setupRecyclerView()
        loadCurrentPayment()
        loadPaymentHistory()
    }

    private fun setupRecyclerView() {
        binding.rvHistorial.layoutManager = LinearLayoutManager(requireContext())
    }

    private fun loadCurrentPayment() {
        val userId = auth.currentUser?.uid ?: return

        binding.progressBar.visibility = View.VISIBLE

        db.collection("usuarios").document(userId)
            .collection("pagos")
            .whereEqualTo("estado", "pendiente")
            .orderBy("fechaVencimiento", Query.Direction.ASCENDING)
            .limit(1)
            .get()
            .addOnSuccessListener { documents ->
                binding.progressBar.visibility = View.GONE

                if (!documents.isEmpty) {
                    val pago = documents.first()
                    binding.cardPagoPendiente.visibility = View.VISIBLE
                    binding.tvMes.text = pago.getString("mes") ?: ""
                    binding.tvMonto.text = getString(R.string.currency_format, pago.getDouble("monto") ?: 0.0)

                    // Configurar el botón de pagar
                    binding.btnPagar.setOnClickListener {
                        // Aquí iría la lógica de pago (integración con pasarela)
                    }
                } else {
                    binding.cardPagoPendiente.visibility = View.GONE
                    binding.tvNoPendientes.visibility = View.VISIBLE
                }
            }
            .addOnFailureListener {
                binding.progressBar.visibility = View.GONE
            }
    }

    private fun loadPaymentHistory() {
        val userId = auth.currentUser?.uid ?: return

        db.collection("usuarios").document(userId)
            .collection("pagos")
            .whereEqualTo("estado", "completado")
            .orderBy("fechaPago", Query.Direction.DESCENDING)
            .limit(10)
            .get()
            .addOnSuccessListener { documents ->
                // Aquí se configuraría el adapter con el historial de pagos
                if (documents.isEmpty) {
                    binding.tvNoHistorial.visibility = View.VISIBLE
                    binding.rvHistorial.visibility = View.GONE
                } else {
                    binding.tvNoHistorial.visibility = View.GONE
                    binding.rvHistorial.visibility = View.VISIBLE
                    // TODO: Configurar adapter
                }
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}