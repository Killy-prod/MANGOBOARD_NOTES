package com.prodkilly.mangoboard_notes

import android.app.Activity
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
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

    // Estados
    var listaProveedores by remember { mutableStateOf(listOf<Proveedor>()) }
    var proveedor by remember { mutableStateOf(provActual) }
    var toneladas by remember { mutableStateOf(tonsActuales) }
    var descripcion by remember { mutableStateOf(descActual) }
    var fechaLong by remember { mutableStateOf(fechaActual) }
    var menuExpandido by remember { mutableStateOf(false) }

    val listaColores = listOf("#FFF9C4", "#FFCCBC", "#C8E6C9", "#BBDEFB", "#E1BEE7")
    var colorSeleccionado by remember { mutableStateOf(listaColores[0]) }

    val formatoFecha = remember { SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()) }
    val calendario = Calendar.getInstance()

    // Carga de datos
    LaunchedEffect(Unit) {
        dbAuxiliar.escucharProveedores { listaProveedores = it }
    }

    // DIÁLOGOS
    val timePickerDialog = TimePickerDialog(contexto, { _, h, m ->
        calendario.timeInMillis = fechaLong
        calendario.set(Calendar.HOUR_OF_DAY, h); calendario.set(Calendar.MINUTE, m)
        fechaLong = calendario.timeInMillis
    }, calendario.get(Calendar.HOUR_OF_DAY), calendario.get(Calendar.MINUTE), true)

    val datePickerDialog = DatePickerDialog(contexto, { _, y, m, d ->
        calendario.set(y, m, d)
        fechaLong = calendario.timeInMillis
        timePickerDialog.show()
    }, calendario.get(Calendar.YEAR), calendario.get(Calendar.MONTH), calendario.get(Calendar.DAY_OF_MONTH))

    // UI PRINCIPAL
    Column(modifier = Modifier.padding(24.dp).fillMaxSize().verticalScroll(rememberScrollState())) {
        Text(text = if (notaId == null) "Añadir Nota" else "Editar Nota", style = MaterialTheme.typography.headlineSmall)
        Spacer(modifier = Modifier.height(16.dp))

        // 1. Selector de Proveedor
        Text("Proveedor", style = MaterialTheme.typography.bodySmall)
        Box(modifier = Modifier.fillMaxWidth()) {
            OutlinedCard(onClick = { menuExpandido = true }, modifier = Modifier.fillMaxWidth()) {
                Text(text = proveedor.ifBlank { "Selecciona un proveedor" }, modifier = Modifier.padding(16.dp))
            }
            DropdownMenu(expanded = menuExpandido, onDismissRequest = { menuExpandido = false }) {
                listaProveedores.forEach { prov ->
                    DropdownMenuItem(text = { Text(prov.nombre) }, onClick = { proveedor = prov.nombre; menuExpandido = false })
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 2. Campos de Texto
        OutlinedTextField(value = toneladas, onValueChange = { toneladas = it }, label = { Text("Toneladas") }, modifier = Modifier.fillMaxWidth())
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(value = descripcion, onValueChange = { descripcion = it }, label = { Text("Descripción") }, modifier = Modifier.fillMaxWidth())

        Spacer(modifier = Modifier.height(16.dp))

        // 3. Selector de Color
        Text("Color de nota", style = MaterialTheme.typography.bodySmall)
        Row(modifier = Modifier.padding(vertical = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listaColores.forEach { hexColor ->
                Box(modifier = Modifier.size(40.dp).background(Color(android.graphics.Color.parseColor(hexColor)), CircleShape)
                    .clickable { colorSeleccionado = hexColor }
                    .border(if (colorSeleccionado == hexColor) 2.dp else 0.dp, Color.Black, CircleShape))
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 4. Selector Fecha
        OutlinedCard(onClick = { datePickerDialog.show() }, modifier = Modifier.fillMaxWidth()) {
            Text(text = "Fecha: ${formatoFecha.format(Date(fechaLong))}", modifier = Modifier.padding(16.dp))
        }

        Spacer(modifier = Modifier.height(24.dp))

        // 5. Botones
        Button(onClick = {
            val tons = toneladas.toDoubleOrNull() ?: 0.0
            if (notaId == null) {
                viewModel.crearNuevaNota(NotaPizarra(id = UUID.randomUUID().toString(), nombreProveedor = proveedor, cantidadToneladas = tons, descripcion = descripcion, fechaCompra = fechaLong, colorHex = colorSeleccionado))
            } else {
                viewModel.actualizarNota(notaId, proveedor, tons, descripcion, fechaLong, colorSeleccionado)
            }
            actividad.finish()
        }, modifier = Modifier.fillMaxWidth()) { Text("Guardar") }

        if (notaId != null) {
            OutlinedButton(onClick = { viewModel.eliminarNota(notaId); actividad.finish() }, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Red)) {
                Text("Despegar Nota (Eliminar)")
            }
        }
    }
}