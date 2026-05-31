package com.prodkilly.mangoboard_notes

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore

class DBAuxiliar {
    private val db = FirebaseFirestore.getInstance()

    // 1. Función para agregar una nueva nota de compra
    fun agregarNota(nota: NotaPizarra) {
        db.collection("notas_pizarra").add(nota)
            .addOnSuccessListener {
                Log.d("FIREBASE_APP", "¡Nota guardada con éxito!")
            }
            .addOnFailureListener { error ->
                Log.e("FIREBASE_APP", "Error al guardar: ${error.message}")
            }
    }

    // 2. Función para escuchar la pizarra EN TIEMPO REAL
    fun escucharPizarra(onNotasCambialas: (List<NotaPizarra>) -> Unit) {
        db.collection("notas_pizarra")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("FIREBASE_APP", "Error al escuchar: ${error.message}")
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    val notas = snapshot.toObjects(NotaPizarra::class.java)
                    Log.d("FIREBASE_APP", "Se leyeron ${notas.size} notas de la nube")
                    onNotasCambialas(notas)
                }
            }
    }

    // 3. Función para actualizar coordenadas (drag & drop)
    fun actualizarCoordenadas(notaId: String, x: Float, y: Float) {
        db.collection("notas_pizarra").document(notaId)
            .update(
                mapOf(
                    "posicionX" to x,
                    "posicionY" to y
                )
            )
    }

    // 4. Función para editar los datos de una nota
    fun actualizarDatosNota(notaId: String, proveedor: String, toneladas: Double) {
        db.collection("notas_pizarra").document(notaId)
            .update(
                mapOf(
                    "nombreProveedor" to proveedor,
                    "cantidadToneladas" to toneladas
                )
            )
    }

    // 5. Función para eliminar una nota
    fun eliminarNota(notaId: String) {
        db.collection("notas_pizarra").document(notaId).delete()
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