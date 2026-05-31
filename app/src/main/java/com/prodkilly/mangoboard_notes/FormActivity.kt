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

class FormActivity : ComponentActivity() {

    // Inyectamos nuestro ViewModel
    private val viewModel: ViewModel_board by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Recibimos los datos del Intent (Si es null, significa que estamos creando una nueva nota)
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

@OptIn(ExperimentalMaterial3Api::class) // <- Súper importante para el menú desplegable
@Composable
fun FormScreen(
    notaId: String?,
    provActual: String,
    tonsActuales: String,
    viewModel: ViewModel_board
) {
    val actividad = LocalContext.current as Activity

    // --- VARIABLES DE BASE DE DATOS Y ESTADO ---
    // Si tu DBAuxiliar pide context, usa: val dbAuxiliar = remember { DBAuxiliar(actividad) }
    val dbAuxiliar = remember { DBAuxiliar() }
    var listaProveedores by remember { mutableStateOf(listOf<Proveedor>()) }

    // Control del formulario
    var proveedor by remember { mutableStateOf(provActual) }
    var toneladas by remember { mutableStateOf(tonsActuales) }
    var menuExpandido by remember { mutableStateOf(false) }

    // --- DESCARGAR PROVEEDORES AL ABRIR LA PANTALLA ---
    LaunchedEffect(Unit) {
        dbAuxiliar.escucharProveedores { proveedoresDB ->
            listaProveedores = proveedoresDB
        }
    }

    Column(modifier = Modifier.padding(24.dp).fillMaxWidth()) {
        Text(
            text = if (notaId == null) "Añadir Nota a Pizarra" else "Editar Nota",
            style = MaterialTheme.typography.headlineSmall
        )

        Spacer(modifier = Modifier.height(24.dp))

        // --- EL NUEVO MENÚ DESPLEGABLE DE PROVEEDORES ---
        ExposedDropdownMenuBox(
            expanded = menuExpandido,
            onExpandedChange = { menuExpandido = !menuExpandido },
            modifier = Modifier.fillMaxWidth()
        ) {
            // El campo de texto que el usuario ve
            OutlinedTextField(
                value = proveedor,
                onValueChange = {}, // Vacío porque es de solo lectura, se llena al seleccionar
                readOnly = true,
                label = { Text("Selecciona un Proveedor") },
                trailingIcon = {
                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = menuExpandido)
                },
                colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                modifier = Modifier
                    .menuAnchor()
                    .fillMaxWidth()
            )

            // La lista que se abre
            ExposedDropdownMenu(
                expanded = menuExpandido,
                onDismissRequest = { menuExpandido = false }
            ) {
                if (listaProveedores.isEmpty()) {
                    DropdownMenuItem(
                        text = { Text("Cargando proveedores...") },
                        onClick = { }
                    )
                } else {
                    listaProveedores.forEach { prov ->
                        DropdownMenuItem(
                            text = { Text(prov.nombre) },
                            onClick = {
                                proveedor = prov.nombre // Guardamos el nombre seleccionado
                                menuExpandido = false   // Cerramos el menú
                            }
                        )
                    }
                }
            }
        }
        // --- FIN DEL MENÚ DESPLEGABLE ---

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = toneladas,
            onValueChange = { toneladas = it },
            label = { Text("Toneladas de Mango") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(32.dp))

        // --- BOTÓN DE GUARDAR / ACTUALIZAR ---
        Button(
            onClick = {
                // Validación para no guardar datos vacíos
                if (proveedor.isBlank() || toneladas.isBlank()) return@Button
                val tons = toneladas.toDoubleOrNull() ?: 0.0

                if (notaId == null) {
                    // Creamos nueva nota
                    val nuevaNota = NotaPizarra(
                        nombreProveedor = proveedor,
                        cantidadToneladas = tons,
                        fechaCompra = System.currentTimeMillis(),
                        posicionX = 100f,
                        posicionY = 100f
                    )
                    viewModel.crearNuevaNota(nuevaNota)
                } else {
                    // Actualizamos la nota existente
                    viewModel.actualizarNota(notaId, proveedor, tons)
                }

                actividad.finish()
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Clavar en Pizarra")
        }

        // --- BOTÓN DE ELIMINAR (Solo en edición) ---
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