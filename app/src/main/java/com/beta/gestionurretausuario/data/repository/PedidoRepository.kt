package com.beta.gestionurretausuario.data.repository

import com.beta.gestionurretausuario.data.local.dao.CarritoDao
import com.beta.gestionurretausuario.data.local.dao.PedidoDao
import com.beta.gestionurretausuario.data.local.entity.ItemPedidoEntity
import com.beta.gestionurretausuario.data.local.entity.PedidoEntity
import com.beta.gestionurretausuario.data.remote.model.PedidoFirestore
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.*

class PedidoRepository(
    private val pedidoDao: PedidoDao,
    private val carritoDao: CarritoDao,
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {

    companion object {
        private const val COLLECTION_PEDIDOS = "pedidos"
    }

    // Operaciones locales
    suspend fun insertLocal(pedido: PedidoEntity, items: List<ItemPedidoEntity>) {
        pedidoDao.insertPedidoConItems(pedido, items)
    }

    suspend fun getByIdLocal(id: String): PedidoEntity? = pedidoDao.getById(id)
    fun getByIdLocalFlow(id: String): Flow<PedidoEntity?> = pedidoDao.getByIdFlow(id)
    fun getByUsuario(usuarioId: String): Flow<List<PedidoEntity>> = pedidoDao.getByUsuario(usuarioId)
    fun getByUsuarioYEstado(usuarioId: String, estado: String) = pedidoDao.getByUsuarioYEstado(usuarioId, estado)
    fun getItemsByPedido(pedidoId: String): Flow<List<ItemPedidoEntity>> = pedidoDao.getItemsByPedido(pedidoId)
    suspend fun getItemsByPedidoSync(pedidoId: String): List<ItemPedidoEntity> = pedidoDao.getItemsByPedidoSync(pedidoId)

    // Operaciones remotas
    suspend fun syncFromRemote(usuarioId: String): Result<List<PedidoEntity>> {
        return try {
            val documents = firestore.collection(COLLECTION_PEDIDOS)
                .whereEqualTo("usuarioId", usuarioId)
                .get()
                .await()

            documents.forEach { doc ->
                val pedidoFirestore = doc.toObject(PedidoFirestore::class.java)
                val pedido = pedidoFirestore.toEntity()
                val items = pedidoFirestore.toItemEntities()
                pedidoDao.insertPedidoConItems(pedido, items)
            }

            val pedidos = documents.mapNotNull { doc ->
                doc.toObject(PedidoFirestore::class.java).toEntity()
            }

            Result.success(pedidos)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Crea un pedido desde el carrito actual
     */
    suspend fun crearPedidoDesdeCarrito(
        usuarioId: String,
        direccionEnvio: String,
        ciudadEnvio: String,
        codigoPostalEnvio: String,
        metodoPago: String,
        gastosEnvio: Double = 0.0,
        descuento: Double = 0.0
    ): Result<PedidoEntity> {
        return try {
            // Obtener items del carrito
            val carritoItems = carritoDao.getAll().first()

            if (carritoItems.isEmpty()) {
                return Result.failure(Exception("El carrito está vacío"))
            }

            // Generar ID y número de pedido
            val pedidoId = UUID.randomUUID().toString()
            val numeroPedido = generarNumeroPedido()
            val fechaPedido = System.currentTimeMillis()

            // Calcular totales
            val subtotal = carritoItems.sumOf { it.precio * it.cantidad }
            val total = subtotal - descuento + gastosEnvio

            // Crear entidad del pedido
            val pedido = PedidoEntity(
                id = pedidoId,
                usuarioId = usuarioId,
                numeroPedido = numeroPedido,
                fechaPedido = fechaPedido,
                subtotal = subtotal,
                descuento = descuento,
                gastosEnvio = gastosEnvio,
                total = total,
                estado = "pendiente",
                direccionEnvio = direccionEnvio,
                ciudadEnvio = ciudadEnvio,
                codigoPostalEnvio = codigoPostalEnvio,
                metodoPago = metodoPago,
                pagado = false
            )

            // Crear items del pedido
            val items = carritoItems.map { carrito ->
                ItemPedidoEntity(
                    pedidoId = pedidoId,
                    productoId = carrito.productoId,
                    nombreProducto = carrito.nombreProducto,
                    cantidad = carrito.cantidad,
                    precioUnitario = carrito.precio,
                    subtotal = carrito.precio * carrito.cantidad,
                    talla = carrito.talla,
                    color = carrito.color
                )
            }

            // Guardar en local
            pedidoDao.insertPedidoConItems(pedido, items)

            // Guardar en Firestore
            val pedidoFirestore = PedidoFirestore.fromEntity(pedido, items)
            firestore.collection(COLLECTION_PEDIDOS)
                .document(pedidoId)
                .set(pedidoFirestore)
                .await()

            // Vaciar carrito
            carritoDao.vaciar()

            Result.success(pedido)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun generarNumeroPedido(): String {
        val sdf = SimpleDateFormat("yyyyMMdd", Locale.getDefault())
        val fecha = sdf.format(Date())
        val random = (1000..9999).random()
        return "PED-$fecha-$random"
    }

    suspend fun actualizarEstado(pedidoId: String, nuevoEstado: String): Result<Unit> {
        return try {
            // Actualizar local
            val pedido = pedidoDao.getById(pedidoId)
            pedido?.let {
                val pedidoActualizado = it.copy(estado = nuevoEstado)
                pedidoDao.update(pedidoActualizado)
            }

            // Actualizar remoto
            firestore.collection(COLLECTION_PEDIDOS)
                .document(pedidoId)
                .update("estado", nuevoEstado)
                .await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}