package com.prodkilly.mangoboard_notes

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

// --- EL CEREBRO DE LA PANTALLA (VIEWMODEL) ---
// El ViewModel se encarga de mantener y proteger los datos de la pantalla.
// Si el usuario voltea el teléfono o la pantalla se recrea, los datos NO se pierden.
// Sirve como puente de comunicación entre la Interfaz (UI) y la Base de Datos (DBAuxiliar).
class ViewModel_board : ViewModel() {

    // Instanciamos nuestra clase auxiliar para poder mandar llamar sus funciones de Firebase
    private val dbAuxiliar = DBAuxiliar()

    // --- MANEJO DE ESTADOS CON STATEFLOW ---
    // _notas (con guion bajo) es PRIVADO. Solo este ViewModel puede modificar la lista dentro de aquí.
    // Usamos MutableStateFlow porque su contenido va a cambiar constantemente.
    private val _notas = MutableStateFlow<List<NotaPizarra>>(emptyList())

    // notas (sin guion bajo) es PÚBLICO y de solo lectura (StateFlow).
    // Las pantallas (como PizarraScreen) se "conectan" a esta variable para ver las notas en tiempo real,
    // pero no pueden modificar la lista directamente desde la interfaz por seguridad.
    val notas: StateFlow<List<NotaPizarra>> = _notas

    // init es el bloque constructor. Se ejecuta AUTOMÁTICAMENTE en cuanto el ViewModel cobra vida.
    init {
        // Activamos el "radar" en tiempo real de Firebase.
        // Cada vez que alguien agregue, mueva o borre una nota en la nube, Firebase nos regresará
        // la 'listaDeNotas' actualizada y nosotros se la asignamos a nuestro flujo (_notas.value).
        dbAuxiliar.escucharPizarra { listaDeNotas ->
            _notas.value = listaDeNotas
        }
    }

    // --- ACCIONES QUE LA INTERFAZ PUEDE DETONAR ---

    // 1. Crear una nota nueva desde el formulario
    fun crearNuevaNota(nota: NotaPizarra) {
        dbAuxiliar.agregarNota(nota)
    }

    // 2. Mover una nota en el corcho (Pizarra)
    // Recibe el ID de la nota que se movió y sus nuevas coordenadas en la pantalla
    fun moverNota(notaId: String, x: Float, y: Float) {
        dbAuxiliar.actualizarCoordenadas(notaId, x, y)
    }

    // 3. Editar el contenido completo de una nota desde el formulario
    fun actualizarNota(id: String, prov: String, tons: Double, desc: String, fecha: Long, color: String) {
        dbAuxiliar.actualizarNota(id, prov, tons, desc, fecha, color)
    }

    // 4. Borrar una nota permanentemente
    fun eliminarNota(notaId: String) {
        dbAuxiliar.eliminarNota(notaId)
    }
}