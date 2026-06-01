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
import com.prodkilly.mangoboard_notes.ui.theme.lightred20
import com.prodkilly.mangoboard_notes.ui.theme.yellow10
import java.text.SimpleDateFormat
import java.util.*

class FormActivity : ComponentActivity() {

    // Conectamos la vista con el ViewModel (que maneja la base de datos)
    private val viewModel: ViewModel_board by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Recibimos los datos que nos manda la pantalla anterior (PizarraScreen)
        // Si es una nota nueva, estos valores llegarán vacíos o nulos.
        // Si estamos editando una nota, llegarán con la información guardada.
        val notaId = intent.getStringExtra("nota_id")
        val provActual = intent.getStringExtra("proveedor") ?: ""
        val tonsActuales = intent.getDoubleExtra("toneladas", 0.0)
        val descActual = intent.getStringExtra("descripcion") ?: ""
        val fechaActual = intent.getLongExtra("fecha_compra", System.currentTimeMillis())

        setContent {
            MaterialTheme {
                // El contenedor principal de la pantalla
                Surface(modifier = Modifier.fillMaxSize()) {
                    // Llamamos a la función que dibuja el formulario y le pasamos los datos recibidos
                    FormScreen(
                        notaId = notaId,
                        provActual = provActual,
                        // Convertimos el Double a String para el campo de texto, o lo dejamos vacío si es 0.0
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

    // --- ESTADOS DEL FORMULARIO ---
    // Estas variables "recuerdan" lo que el usuario escribe o selecciona.
    // Si su valor cambia, la pantalla se actualiza automáticamente.
    var listaProveedores by remember { mutableStateOf(listOf<Proveedor>()) }
    var proveedor by remember { mutableStateOf(provActual) }
    var toneladas by remember { mutableStateOf(tonsActuales) }
    var descripcion by remember { mutableStateOf(descActual) }
    var fechaLong by remember { mutableStateOf(fechaActual) }

    // Controla si el menú desplegable de proveedores está abierto o cerrado
    var menuExpandido by remember { mutableStateOf(false) }

    // Lista de colores disponibles para las notas adhesivas
    val listaColores = listOf("#FFF9C4", "#FFCCBC", "#C8E6C9", "#BBDEFB", "#E1BEE7")
    // Por defecto, selecciona el primer color (amarillo)
    var colorSeleccionado by remember { mutableStateOf(listaColores[0]) }

    // Herramientas para formatear y manejar la fecha/hora
    val formatoFecha = remember { SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()) }
    val calendario = Calendar.getInstance()

    // --- CARGA DE DATOS ---
    // LaunchedEffect se ejecuta una sola vez al abrir esta pantalla.
    // Aquí le pedimos a la base de datos la lista de proveedores registrados.
    LaunchedEffect(Unit) {
        dbAuxiliar.escucharProveedores { listaProveedores = it }
    }

    // --- DIÁLOGOS DE FECHA Y HORA (Nativos de Android) ---
    // 1. Selector de Hora
    val timePickerDialog = TimePickerDialog(contexto, { _, h, m ->
        calendario.timeInMillis = fechaLong
        calendario.set(Calendar.HOUR_OF_DAY, h); calendario.set(Calendar.MINUTE, m)
        fechaLong = calendario.timeInMillis // Guardamos el tiempo exacto en milisegundos
    }, calendario.get(Calendar.HOUR_OF_DAY), calendario.get(Calendar.MINUTE), true)

    // 2. Selector de Fecha (Día, Mes, Año)
    val datePickerDialog = DatePickerDialog(contexto, { _, y, m, d ->
        calendario.set(y, m, d)
        fechaLong = calendario.timeInMillis
        // Cuando el usuario elige el día, automáticamente le mostramos el selector de hora
        timePickerDialog.show()
    }, calendario.get(Calendar.YEAR), calendario.get(Calendar.MONTH), calendario.get(Calendar.DAY_OF_MONTH))

    // --- DISEÑO DE LA INTERFAZ ---
    // Columna principal que permite hacer scroll si la pantalla es muy pequeña
    Column(modifier = Modifier.padding(24.dp).fillMaxSize().verticalScroll(rememberScrollState())) {

        // Título dinámico: Cambia dependiendo de si estamos creando o editando
        Text(text = if (notaId == null) "Añadir Nota" else "Editar Nota", style = MaterialTheme.typography.headlineSmall)
        Spacer(modifier = Modifier.height(16.dp))

        // --- 1. SELECTOR DE PROVEEDOR ---
        Text("Proveedor", style = MaterialTheme.typography.bodySmall)
        Box(modifier = Modifier.fillMaxWidth()) {
            // El "botón" que muestra el proveedor seleccionado y abre el menú al tocarlo
            OutlinedCard(onClick = { menuExpandido = true }, modifier = Modifier.fillMaxWidth()) {
                Text(text = proveedor.ifBlank { "Selecciona un proveedor" }, modifier = Modifier.padding(16.dp))
            }
            // El menú desplegable con la lista de proveedores
            DropdownMenu(expanded = menuExpandido, onDismissRequest = { menuExpandido = false }) {
                listaProveedores.forEach { prov ->
                    DropdownMenuItem(text = { Text(prov.nombre) }, onClick = {
                        proveedor = prov.nombre // Guarda el proveedor elegido
                        menuExpandido = false   // Cierra el menú
                    })
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // --- 2. CAMPOS DE TEXTO ---
        // Campo para las toneladas
        OutlinedTextField(
            value = toneladas,
            onValueChange = { toneladas = it },
            label = { Text("Toneladas") },
            // ¡AQUÍ ESTÁ LA SOLUCIÓN! Esto obliga a Android a mostrar el teclado numérico
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Campo para la descripción/comentarios
        OutlinedTextField(
            value = descripcion,
            onValueChange = { descripcion = it },
            label = { Text("Descripción") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        // --- 3. SELECTOR DE COLOR ---
        Text("Color de nota", style = MaterialTheme.typography.bodySmall)
        // Dibuja una fila con círculos de colores
        Row(modifier = Modifier.padding(vertical = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listaColores.forEach { hexColor ->
                Box(modifier = Modifier
                    .size(40.dp)
                    // Convierte el código Hexadecimal ("#FFF...") a un Color de Android
                    .background(Color(android.graphics.Color.parseColor(hexColor)), CircleShape)
                    .clickable { colorSeleccionado = hexColor } // Al tocarlo, lo selecciona
                    // Dibuja un borde negro si este color es el que está seleccionado
                    .border(if (colorSeleccionado == hexColor) 2.dp else 0.dp, Color.Black, CircleShape)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // --- 4. SELECTOR DE FECHA Y HORA ---
        // Un botón con forma de tarjeta que muestra la fecha formateada
        OutlinedCard(onClick = { datePickerDialog.show() }, modifier = Modifier.fillMaxWidth()) {
            Text(text = "Fecha: ${formatoFecha.format(Date(fechaLong))}", modifier = Modifier.padding(16.dp))
        }

        Spacer(modifier = Modifier.height(24.dp))

        // --- 5. BOTONES DE ACCIÓN ---
        // Botón principal para GUARDAR
        Button(
            onClick = {
                // Convierte el texto de toneladas a número. Si está vacío o es inválido, pone 0.0
                val tons = toneladas.toDoubleOrNull() ?: 0.0

                if (notaId == null) {
                    // Si notaId es nulo, significa que es una nota NUEVA. Crea un ID aleatorio.
                    viewModel.crearNuevaNota(NotaPizarra(id = UUID.randomUUID().toString(), nombreProveedor = proveedor, cantidadToneladas = tons, descripcion = descripcion, fechaCompra = fechaLong, colorHex = colorSeleccionado))
                } else {
                    // Si ya existe, simplemente actualiza los datos
                    viewModel.actualizarNota(notaId, proveedor, tons, descripcion, fechaLong, colorSeleccionado)
                }
                actividad.finish() // Cierra esta pantalla y regresa a la pizarra
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = yellow10)
        ) { Text("Guardar") }

        // Botón secundario para ELIMINAR (Solo aparece si estamos editando una nota existente)
        if (notaId != null) {
            OutlinedButton(
                onClick = {
                    viewModel.eliminarNota(notaId) // Borra la nota de la BD
                    actividad.finish()             // Cierra la pantalla
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = lightred20),
            ) {
                Text("Despegar Nota (Eliminar)")
            }
        }
    }
}