package com.prodkilly.mangoboard_notes

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore

class DBAuxiliar {
    private val db = FirebaseFirestore.getInstance()
    // Definimos una constante para no equivocarnos nunca más
    private val COLECCION = "notas_pizarra"

    fun agregarNota(nota: NotaPizarra) {
        db.collection(COLECCION).document(nota.id).set(nota) // Usamos .set(nota) con ID para control total
            .addOnSuccessListener { Log.d("FIREBASE_APP", "Nota guardada!") }
    }

    fun escucharPizarra(onNotasCambialas: (List<NotaPizarra>) -> Unit) {
        db.collection(COLECCION)
            .addSnapshotListener { snapshot, error ->
                if (error != null) return@addSnapshotListener
                if (snapshot != null) {
                    val notas = snapshot.toObjects(NotaPizarra::class.java)
                    onNotasCambialas(notas)
                }
            }
    }

    fun actualizarCoordenadas(notaId: String, x: Float, y: Float) {
        val actualizaciones = mapOf("posicionX" to x, "posicionY" to y)
        // AHORA SÍ: Usamos la constante COLECCION correcta
        db.collection(COLECCION).document(notaId).update(actualizaciones)
    }

    fun actualizarNota(
        notaId: String,
        nuevoProveedor: String,
        nuevasToneladas: Double,
        nuevaDesc: String,
        nuevaFecha: Long,
        nuevoColor: String // <--- Agregamos este parámetro
    ) {
        val actualizaciones = mapOf(
            "nombreProveedor" to nuevoProveedor,
            "cantidadToneladas" to nuevasToneladas,
            "descripcion" to nuevaDesc,
            "fechaCompra" to nuevaFecha,
            "colorHex" to nuevoColor // <--- Lo incluimos en Firebase
        )

        db.collection("notas_pizarra").document(notaId)
            .update(actualizaciones)
    }

    fun eliminarNota(notaId: String) {
        db.collection(COLECCION).document(notaId).delete()
    }



    fun agregarProveedor(proveedor: Proveedor) {
        db.collection("proveedores").add(proveedor)
            .addOnSuccessListener { Log.d("FIREBASE_APP", "Proveedor guardado") }
            .addOnFailureListener { Log.e("FIREBASE_APP", "Error: ${it.message}") }
    }

    fun escucharProveedores(onProveedoresCambio: (List<Proveedor>) -> Unit) {
        db.collection("proveedores")
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) return@addSnapshotListener
                val lista = snapshot.toObjects(Proveedor::class.java)
                onProveedoresCambio(lista)
            }
    }


}

