package com.prodkilly.mangoboard_notes

import com.google.firebase.firestore.DocumentId

// Modelo para la colección de Proveedores
data class Proveedor(
    @DocumentId val id: String = "", // Firebase asignará automáticamente el ID del documento aquí
    val nombre: String = "",
    val telefono: String = "",
    val region: String = "",
    val fechaRegistro: Long = System.currentTimeMillis()
)

// Modelo para las Notas de la Pizarra (Etiquetas Adhesivas)
data class NotaPizarra(
    @DocumentId val id: String = "",
    val idProveedor: String = "",
    val nombreProveedor: String = "",
    val cantidadToneladas: Double = 0.0,
    val fechaCompra: Long = 0L,
    val posicionX: Float = 100f,
    val posicionY: Float = 100f,
    val colorNota: String = "#FFEB3B"
)