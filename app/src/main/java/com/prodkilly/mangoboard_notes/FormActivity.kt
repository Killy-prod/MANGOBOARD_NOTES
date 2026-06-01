package com.prodkilly.mangoboard_notes

import android.app.Activity
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import java.text.SimpleDateFormat
import java.util.*

class FormActivity : ComponentActivity() {

    private val viewModel: ViewModel_board by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val notaId = intent.getStringExtra("nota_id")
        val provActual = intent.getStringExtra("proveedor") ?: ""
        val tonsActuales = intent.getDoubleExtra("toneladas", 0.0)
        // Recibimos los nuevos datos (si es edición)
        val descActual = intent.getStringExtra("descripcion") ?: ""
        val fechaActual = intent.getLongExtra("fecha_compra", System.currentTimeMillis())

        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    FormScreen(
                        notaId = notaId,
                        provActual = provActual,
                        tonsActuales = if (tonsActuales > 0.0) tonsActuales.toString() else "",
                        descActual = descActual,
                        fechaActual = fechaActual,
                        viewModel = viewModel
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FormScreen(
    notaId: String?,
    provActual: String,
    tonsActuales: String,
    descActual: String,
    fechaActual: Long,
    viewModel: ViewModel_board
) {
    val actividad = LocalContext.current as Activity
    val contexto = LocalContext.current

    val dbAuxiliar = remember { DBAuxiliar() }
    var listaProveedores by remember { mutableStateOf(listOf<Proveedor>()) }

    var proveedor by remember { mutableStateOf(provActual) }
    var toneladas by remember { mutableStateOf(tonsActuales) }
    var descripcion by remember { mutableStateOf(descActual) }
    var fechaLong by remember { mutableStateOf(fechaActual) }

    var menuExpandido by remember { mutableStateOf(false) }

    // Formateador para mostrar la fecha de forma bonita en el botón selector
    val formatoFecha = remember { SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()) }
    val calendario = remember { Calendar.getInstance() }

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

        Text(
            text = "Proveedor Agropecuario",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)
        )

        // --- CONTENEDOR SELECCIONADOR ---
        Box(modifier = Modifier.fillMaxWidth()) {
            OutlinedCard(
                onClick = { menuExpandido = true },
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.outlinedCardColors(containerColor = Color.Transparent)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (proveedor.isBlank()) "Selecciona un proveedor de la lista" else proveedor,
                        style = MaterialTheme.typography.bodyLarge,
                        color = if (proveedor.isBlank()) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface
                    )
                    Icon(
                        imageVector = Icons.Default.ArrowDropDown,
                        contentDescription = "Desplegar lista",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            DropdownMenu(
                expanded = menuExpandido,
                onDismissRequest = { menuExpandido = false },
                modifier = Modifier.fillMaxWidth(0.85f)
            ) {
                if (listaProveedores.isEmpty()) {
                    DropdownMenuItem(
                        text = { Text("No hay proveedores registrados") },
                        onClick = { menuExpandido = false }
                    )
                } else {
                    listaProveedores.forEach { prov ->
                        DropdownMenuItem(
                            text = { Text(prov.nombre) },
                            onClick = {
                                proveedor = prov.nombre
                                menuExpandido = false
                            }
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Campo de Toneladas
        OutlinedTextField(
            value = toneladas,
            onValueChange = { toneladas = it },
            label = { Text("Toneladas de Mango") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        // NUEVO: Campo de Descripción General / Comentarios
        OutlinedTextField(
            value = descripcion,
            onValueChange = { descripcion = it },
            label = { Text("Descripción / Comentarios extras") },
            modifier = Modifier.fillMaxWidth(),
            maxLines = 3
        )

        Spacer(modifier = Modifier.height(16.dp))

        // NUEVO: Selector de Fecha y Hora Integrado
        Text(
            text = "Fecha y Hora del Registro",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)
        )

        // Configuración de los diálogos nativos
        val timePickerDialog = TimePickerDialog(
            contexto,
            { _, hour, minute ->
                calendario.set(Calendar.HOUR_OF_DAY, hour)
                calendario.set(Calendar.MINUTE, minute)
                fechaLong = calendario.timeInMillis // Guardamos los cambios finales
            },
            calendario.get(Calendar.HOUR_OF_DAY),
            calendario.get(Calendar.MINUTE),
            true
        )

        val datePickerDialog = DatePickerDialog(
            contexto,
            { _, year, month, day ->
                calendario.set(Calendar.YEAR, year)
                calendario.set(Calendar.MONTH, month)
                calendario.set(Calendar.DAY_OF_MONTH, day)
                // Al confirmar el día, salta inmediatamente el de la hora
                timePickerDialog.show()
            },
            calendario.get(Calendar.YEAR),
            calendario.get(Calendar.MONTH),
            calendario.get(Calendar.DAY_OF_MONTH)
        )

        OutlinedCard(
            onClick = {
                calendario.timeInMillis = fechaLong
                datePickerDialog.show()
            },
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.outlinedCardColors(containerColor = Color.Transparent)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = formatoFecha.format(Date(fechaLong)),
                    style = MaterialTheme.typography.bodyLarge
                )
                Text(
                    text = "Cambiar",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Guardado de datos actualizado
        Button(
            onClick = {
                if (proveedor.isBlank() || toneladas.isBlank()) return@Button
                val tons = toneladas.toDoubleOrNull() ?: 0.0

                if (notaId == null) {
                    // Caso: Crear nota nueva
                    val nuevaNota = NotaPizarra(
                        nombreProveedor = proveedor,
                        cantidadToneladas = tons,
                        descripcion = descripcion, // <-- Ya lo tenías
                        fechaCompra = fechaLong,
                        posicionX = 100f,
                        posicionY = 100f
                    )
                    viewModel.crearNuevaNota(nuevaNota)
                } else {
                    // Caso: EDITAR NOTA EXISTENTE
                    // ¡AQUÍ ESTÁ EL POSIBLE ERROR! Asegúrate de enviar los 5 parámetros:
                    viewModel.actualizarNota(
                        notaId,
                        proveedor,
                        tons,
                        descripcion, // <-- ¿Le estás pasando esto?
                        fechaLong    // <-- ¿Le estás pasando esto?
                    )
                }

                actividad.finish()
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Clavar en Pizarra")
        }

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