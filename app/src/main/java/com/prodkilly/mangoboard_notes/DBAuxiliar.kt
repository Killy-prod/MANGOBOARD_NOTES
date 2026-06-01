package com.prodkilly.mangoboard_notes

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore

// --- CLASE AUXILIAR DE BASE DE DATOS (FIREBASE) ---
// Esta clase centraliza todas las consultas a Firebase Firestore.
// Desde aquí se guardan, actualizan, eliminan y leen los datos en la nube en tiempo real.
class DBAuxiliar {

    // Inicializa la instancia de Firebase Firestore para poder usar la base de datos
    private val db = FirebaseFirestore.getInstance()

    // Definimos una constante para el nombre de la colección de notas.
    // Usar una constante evita errores de dedo al escribir el nombre en diferentes funciones.
    private val COLECCION = "notas_pizarra"

    // --- 1. AGREGAR NOTA NUEVA ---
    fun agregarNota(nota: NotaPizarra) {
        // Entramos a la colección, creamos un documento usando el ID único de la nota
        // y usamos .set(nota) para guardar o sobrescribir todo el objeto directamente.
        db.collection(COLECCION).document(nota.id).set(nota)
            .addOnSuccessListener {
                // Esto se ejecuta solo si Firebase guardó la nota correctamente
                Log.d("FIREBASE_APP", "Nota guardada con éxito en la nube!")
            }
    }

    // --- 2. ESCUCHAR CAMBIOS EN LA PIZARRA (TIEMPO REAL) ---
    // Esta función se queda "escuchando" la base de datos. Si otro usuario cambia algo,
    // tu app se entera e inmediatamente actualiza la pantalla sin necesidad de reiniciar.
    fun escucharPizarra(onNotasCambialas: (List<NotaPizarra>) -> Unit) {
        db.collection(COLECCION)
            // addSnapshotListener activa el "radar" de tiempo real en esa colección
            .addSnapshotListener { snapshot, error ->
                // Si ocurre un error de conexión o permisos, detenemos la función aquí
                if (error != null) return@addSnapshotListener

                // Si nos llega información (snapshot) de Firebase:
                if (snapshot != null) {
                    // .toObjects convierte automáticamente los documentos de Firebase
                    // en una lista de objetos tipo 'NotaPizarra' legibles para Kotlin
                    val notas = snapshot.toObjects(NotaPizarra::class.java)

                    // Mandamos la lista de regreso a la pantalla para que se dibuje
                    onNotasCambialas(notas)
                }
            }
    }

    // --- 3. ACTUALIZAR COORDENADAS (PARA EL ARRASTRE DE NOTAS) ---
    // Modifica únicamente la posición X e Y de una nota cuando la mueves con el dedo.
    fun actualizarCoordenadas(notaId: String, x: Float, y: Float) {
        // Creamos un "Mapa" que le dice a Firebase exactamente qué campos modificar
        // clave ("posicionX") -> valor (coordenada x)
        val actualizaciones = mapOf("posicionX" to x, "posicionY" to y)

        // Buscamos el documento por su ID y usamos .update() para cambiar SOLO esos dos campos
        db.collection(COLECCION).document(notaId).update(actualizaciones)
    }

    // --- 4. ACTUALIZAR DATOS DE LA NOTA (FORMULARIO DE EDICIÓN) ---
    // Modifica los datos principales de la nota cuando la editas en FormActivity.
    fun actualizarNota(
        notaId: String,
        nuevoProveedor: String,
        nuevasToneladas: Double,
        nuevaDesc: String,
        nuevaFecha: Long,
        nuevoColor: String
    ) {
        // Creamos un mapa con todos los campos modificados en el formulario
        val actualizaciones = mapOf(
            "nombreProveedor" to nuevoProveedor,
            "cantidadToneladas" to nuevasToneladas,
            "descripcion" to nuevaDesc,
            "fechaCompra" to nuevaFecha,
            "colorHex" to nuevoColor
        )

        // Buscamos el documento de la nota y reemplazamos sus campos antiguos por los nuevos
        db.collection(COLECCION).document(notaId)
            .update(actualizaciones)
    }

    // --- 5. ELIMINAR NOTA ---
    // Borra por completo el documento de la nota de la base de datos en la nube.
    fun eliminarNota(notaId: String) {
        db.collection(COLECCION).document(notaId).delete()
    }

    // --- 6. AGREGAR PROVEEDOR NUEVO ---
    fun agregarProveedor(proveedor: Proveedor) {
        // A diferencia de las notas, aquí usamos .add(). Esto hace que Firebase
        // genere automáticamente un ID aleatorio para el documento del proveedor.
        db.collection("proveedores").add(proveedor)
            .addOnSuccessListener { Log.d("FIREBASE_APP", "Proveedor guardado") }
            .addOnFailureListener { Log.e("FIREBASE_APP", "Error al guardar proveedor: ${it.message}") }
    }

    // --- 7. ESCUCHAR PROVEEDORES (TIEMPO REAL) ---
    // Se queda escuchando la lista de proveedores para que aparezcan en el formulario.
    fun escucharProveedores(onProveedoresCambio: (List<Proveedor>) -> Unit) {
        db.collection("proveedores")
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) return@addSnapshotListener

                // Convierte los documentos en objetos de la clase 'Proveedor'
                val lista = snapshot.toObjects(Proveedor::class.java)

                // Enviamos la lista de regreso al Formulario
                onProveedoresCambio(lista)
            }
    }
}
