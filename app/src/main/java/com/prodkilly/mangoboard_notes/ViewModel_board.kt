package com.prodkilly.mangoboard_notes

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class ViewModel_board : ViewModel() {
    // Instanciamos tu clase auxiliar de la Base de Datos
    private val dbAuxiliar = DBAuxiliar()

    // Estado de la lista de notas para la interfaz
    private val _notas = MutableStateFlow<List<NotaPizarra>>(emptyList())
    val notas: StateFlow<List<NotaPizarra>> = _notas

    init {
        // CORRECCIÓN 1: Cambiado 'escucharNotas' por 'escucharPizarra'
        dbAuxiliar.escucharPizarra { listaDeNotas ->
            _notas.value = listaDeNotas
        }
    }

    // CORRECCIÓN 2: Cambiado 'guardarNota' por 'agregarNota'
    fun crearNuevaNota(nota: NotaPizarra) {
        dbAuxiliar.agregarNota(nota)
    }


    // LLAMAR A LA BASE DE DATOS PARA ACTUALIZAR LAS COORDENADAS DE LA PIZARRA

    fun moverNota(notaId: String, x: Float, y: Float) {
        dbAuxiliar.actualizarCoordenadas(notaId, x, y)
    }

    fun actualizarNota(notaId: String, proveedor: String, toneladas: Double) {
        dbAuxiliar.actualizarDatosNota(notaId, proveedor, toneladas)
    }

    fun eliminarNota(notaId: String) {
        dbAuxiliar.eliminarNota(notaId)
    }
}