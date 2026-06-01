package com.prodkilly.mangoboard_notes

import com.google.firebase.firestore.DocumentId

// --- MODELO: PROVEEDOR ---
// Representa a una persona o empresa que surte los mangos.
// Es un molde de datos puro (data class).
data class Proveedor(
    // @DocumentId le dice a Firebase: "Toma el ID único del documento en la nube
    // y mételo automáticamente en esta variable 'id' al descargar los datos".
    @DocumentId
    val id: String = "",

    val nombre: String = "",       // Nombre del proveedor (ej: "Frutas Martínez")
    val telefono: String = "",     // Número de contacto del proveedor
    val region: String = "",       // Zona geográfica o ciudad de donde traen el mango

    // Guarda el momento exacto en el que registraste al proveedor.
    // System.currentTimeMillis() toma la fecha actual del teléfono en milisegundos.
    val fechaRegistro: Long = System.currentTimeMillis()
)

// --- MODELO: NOTA DE LA PIZARRA (TARJETA ADHESIVA) ---
// Representa cada uno de los papelitos (post-its) virtuales que arrastras en el corcho.
data class NotaPizarra(
    val id: String = "",              // ID único creado manualmente al usar UUID.randomUUID()
    val nombreProveedor: String = "",  // El nombre del proveedor asociado a esta nota de compra
    val cantidadToneladas: Double = 0.0, // Peso en toneladas de la carga de mango comprada
    val descripcion: String = "",      // Detalles adicionales, comentarios o notas extras
    val fechaCompra: Long = 0L,        // Fecha y hora del trato (guardada en milisegundos largos)

    // --- COORDENADAS DE POSICIÓN ---
    // Estas dos variables guardan la ubicación exacta de la tarjeta en la pantalla.
    // Permiten que, al arrastrar la nota, el movimiento se guarde en la nube y no regrese al centro.
    val posicionX: Float = 0f,         // Posición horizontal en la pizarra
    val posicionY: Float = 0f,         // Posición vertical en la pizarra

    val colorHex: String = "#FFF9C4"   // Color de fondo de la tarjeta en formato Web/Hexadecimal (Amarillo por defecto)
)