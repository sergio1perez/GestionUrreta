package com.beta.gestionurretausuario.data.remote.model

import com.google.firebase.firestore.DocumentId

data class PedidoFirestore(
    @DocumentId
    val id: String = "",
    val usuarioId: String = "",
    val numeroPedido: String = "",

    val fechaPedido: Long = 0,
    val fechaEnvio: Long? = null,
    val fechaEntrega: Long? = null,

    val subtotal: Double = 0.0,
    val descuento: Double = 0.0,
    val gastosEnvio: Double = 0.0,
    val total: Double = 0.0,

    val estado: String = "pendiente",

    val direccionEnvio: String? = null,
    val ciudadEnvio: String? = null,
    val codigoPostalEnvio: String? = null,

    val metodoPago: String? = null,
    val pagado: Boolean = false,

    val observaciones: String? = null,

    // En Firestore los items van embebidos
    val items: List<ItemPedidoFirestore> = emptyList()
) {
    constructor() : this(id = "")

    fun toEntity(): com.beta.gestionurretausuario.data.local.entity.PedidoEntity {
        return com.beta.gestionurretausuario.data.local.entity.PedidoEntity(
            id = id,
            usuarioId = usuarioId,
            numeroPedido = numeroPedido,
            fechaPedido = fechaPedido,
            fechaEnvio = fechaEnvio,
            fechaEntrega = fechaEntrega,
            subtotal = subtotal,
            descuento = descuento,
            gastosEnvio = gastosEnvio,
            total = total,
            estado = estado,
            direccionEnvio = direccionEnvio,
            ciudadEnvio = ciudadEnvio,
            codigoPostalEnvio = codigoPostalEnvio,
            metodoPago = metodoPago,
            pagado = pagado,
            observaciones = observaciones
        )
    }

    fun toItemEntities(): List<com.beta.gestionurretausuario.data.local.entity.ItemPedidoEntity> {
        return items.map { item ->
            com.beta.gestionurretausuario.data.local.entity.ItemPedidoEntity(
                pedidoId = id,
                productoId = item.productoId,
                nombreProducto = item.nombreProducto,
                cantidad = item.cantidad,
                precioUnitario = item.precioUnitario,
                subtotal = item.subtotal,
                talla = item.talla ?: "",
                color = item.color ?: ""
            )
        }
    }

    companion object {
        fun fromEntity(
            entity: com.beta.gestionurretausuario.data.local.entity.PedidoEntity,
            items: List<com.beta.gestionurretausuario.data.local.entity.ItemPedidoEntity>
        ): PedidoFirestore {
            return PedidoFirestore(
                id = entity.id,
                usuarioId = entity.usuarioId,
                numeroPedido = entity.numeroPedido,
                fechaPedido = entity.fechaPedido,
                fechaEnvio = entity.fechaEnvio,
                fechaEntrega = entity.fechaEntrega,
                subtotal = entity.subtotal,
                descuento = entity.descuento,
                gastosEnvio = entity.gastosEnvio,
                total = entity.total,
                estado = entity.estado,
                direccionEnvio = entity.direccionEnvio,
                ciudadEnvio = entity.ciudadEnvio,
                codigoPostalEnvio = entity.codigoPostalEnvio,
                metodoPago = entity.metodoPago,
                pagado = entity.pagado,
                observaciones = entity.observaciones,
                items = items.map { ItemPedidoFirestore.fromEntity(it) }
            )
        }
    }
}

data class ItemPedidoFirestore(
    val productoId: String = "",
    val nombreProducto: String = "",
    val cantidad: Int = 0,
    val precioUnitario: Double = 0.0,
    val subtotal: Double = 0.0,
    val talla: String? = null,
    val color: String? = null
) {
    constructor() : this(productoId = "")

    companion object {
        fun fromEntity(entity: com.beta.gestionurretausuario.data.local.entity.ItemPedidoEntity): ItemPedidoFirestore {
            return ItemPedidoFirestore(
                productoId = entity.productoId,
                nombreProducto = entity.nombreProducto,
                cantidad = entity.cantidad,
                precioUnitario = entity.precioUnitario,
                subtotal = entity.subtotal,
                talla = entity.talla.ifEmpty { null },
                color = entity.color.ifEmpty { null }
            )
        }
    }
}