package com.prodkilly.mangoboard_notes

import android.app.Activity
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
// Importa tu tema (MangoBoard_APPTheme) si lo estás usando

class FormActivity : ComponentActivity() {

    // 1. Inyectamos nuestro ViewModel
    private val viewModel: ViewModel_board by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 2. Recibimos los datos del Intent (Si es null, significa que estamos creando una nueva nota)
        val notaId = intent.getStringExtra("nota_id")
        val provActual = intent.getStringExtra("proveedor") ?: ""
        val tonsActuales = intent.getDoubleExtra("toneladas", 0.0)

        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    FormScreen(
                        notaId = notaId,
                        provActual = provActual,
                        tonsActuales = if (tonsActuales > 0.0) tonsActuales.toString() else "",
                        viewModel = viewModel
                    )
                }
            }
        }
    }
}

@Composable
fun FormScreen(
    notaId: String?,
    provActual: String,
    tonsActuales: String,
    viewModel: ViewModel_board
) {
    // Forma más segura de obtener la Activity para poder cerrarla después
    val actividad = LocalContext.current as Activity

    var proveedor by remember { mutableStateOf(provActual) }
    var toneladas by remember { mutableStateOf(tonsActuales) }

    Column(modifier = Modifier.padding(24.dp).fillMaxWidth()) {
        Text(
            text = if (notaId == null) "Añadir Nota a Pizarra" else "Editar Nota",
            style = MaterialTheme.typography.headlineSmall
        )

        Spacer(modifier = Modifier.height(24.dp))

        OutlinedTextField(
            value = proveedor,
            onValueChange = { proveedor = it },
            label = { Text("Proveedor Agropecuario") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = toneladas,
            onValueChange = { toneladas = it },
            label = { Text("Toneladas de Mango") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Botón de Guardar / Actualizar
        Button(
            onClick = {
                if (proveedor.isBlank() || toneladas.isBlank()) return@Button
                val tons = toneladas.toDoubleOrNull() ?: 0.0

                if (notaId == null) {
                    // Creamos el objeto usando tu modelo NotaPizarra
                    val nuevaNota = NotaPizarra(
                        nombreProveedor = proveedor,
                        cantidadToneladas = tons,
                        fechaCompra = System.currentTimeMillis(), // Guarda la fecha y hora exactas
                        posicionX = 100f, // Coordenadas iniciales por defecto al crearla
                        posicionY = 100f
                    )
                    viewModel.crearNuevaNota(nuevaNota)
                } else {
                    // Actualizamos la nota existente
                    viewModel.actualizarNota(notaId, proveedor, tons)
                }

                // Cerramos la pantalla y regresamos a la pizarra
                actividad.finish()
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Clavar en Pizarra")
        }

        // Botón de Eliminar (Solo visible si estamos editando)
        if (notaId != null) {
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedButton(
                onClick = {
                    viewModel.eliminarNota(notaId)
                    actividad.finish()
                },
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Red),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Despegar Nota")
            }
        }
    }
}