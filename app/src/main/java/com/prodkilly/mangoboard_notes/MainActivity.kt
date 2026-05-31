package com.prodkilly.mangoboard_notes

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels // ¡Importante para inyectar el ViewModel!
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.ui.Alignment
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.fragment.app.FragmentActivity
import kotlin.math.roundToInt
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import kotlinx.coroutines.launch
import androidx.compose.material.icons.filled.Menu


// RECUERDA: Debe ser FragmentActivity para que la huella funcione
class MainActivity : FragmentActivity() {

    private val viewModel: ViewModel_board by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize()) {

                    // Controlador de estado
                    var estaAutenticado by remember { mutableStateOf(false) }

                    // El "Policía de tráfico"
                    if (estaAutenticado) {
                        // Llama a tu archivo de Pizarra
                        PizarraScreen(viewModel = viewModel)
                    } else {
                        // Llama a tu nuevo archivo de Autenticación
                        PantallaAutenticacion(
                            onAutenticado = { estaAutenticado = true }
                        )
                    }

                }
            }

            val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
            val scope = rememberCoroutineScope()
            val contexto = LocalContext.current

            ModalNavigationDrawer(
                drawerState = drawerState,
                drawerContent = {
                    ModalDrawerSheet {
                        Text("Menú MangoBoard", modifier = Modifier.padding(16.dp))
                        HorizontalDivider()
                        NavigationDrawerItem(
                            label = { Text("Gestionar Proveedores") },
                            selected = false,
                            onClick = {
                                // Aquí abrimos la nueva pantalla que creaste
                                val intent = Intent(contexto, ProveedoresActivity::class.java)
                                contexto.startActivity(intent)
                                scope.launch { drawerState.close() }
                            }
                        )
                    }
                }
            ) {
                Scaffold(
                    topBar = {
                        @OptIn(ExperimentalMaterial3Api::class)
                        TopAppBar(
                            title = { Text("MangoBoard") },
                            navigationIcon = {
                                IconButton(onClick = { scope.launch { drawerState.open() } }) {
                                    Icon(Icons.Default.Menu, contentDescription = "Menú")
                                }
                            }
                        )
                    }
                ) { paddingValues ->
                    PizarraScreen(viewModel = viewModel, modifier = Modifier.padding(paddingValues))
                }
            }
        }
    }
}
@Composable
fun PizarraScreen(viewModel: ViewModel_board, modifier: Modifier = Modifier) {
    val contexto = LocalContext.current
    val listaNotas by viewModel.notas.collectAsState()

    // Usamos Scaffold para poder agregar el FAB (Botón flotante)
    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    val intent = Intent(contexto, FormActivity::class.java)
                    contexto.startActivity(intent)
                },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Text("+", style = MaterialTheme.typography.headlineMedium, color = Color.White)
            }
        }
    ) { paddingValues -> // El Scaffold nos da un padding necesario

        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues) // Aplicamos el padding del Scaffold
                .padding(16.dp)
        ) {
            // Cabecera con Botón de Ordenar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Pizarra Diaria",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.primary
                )

                Button(onClick = {
                    listaNotas.forEachIndexed { index, nota ->
                        val columna = index % 2
                        val fila = index / 2
                        val nuevoX = if (columna == 0) 0f else 500f
                        val nuevoY = fila * 450f
                        viewModel.moverNota(nota.id, nuevoX, nuevoY)
                    }
                }) {
                    Text("Ordenar")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // La Pizarra Libre (Box)
            Box(modifier = Modifier.fillMaxSize()) {
                listaNotas.forEach { nota ->
                    NotaAdhesivaItem(
                        nota = nota,
                        viewModel = viewModel,
                        onClick = {
                            val intent = Intent(contexto, FormActivity::class.java)
                            intent.putExtra("nota_id", nota.id)
                            contexto.startActivity(intent)
                        }
                    )
                }
            }
        }
    }
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotaAdhesivaItem(nota: NotaPizarra, viewModel: ViewModel_board, onClick: () -> Unit) {
    val formatoFecha = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
    val fechaString = formatoFecha.format(Date(nota.fechaCompra))

    // Convertimos los píxeles (drag) a DP (pantalla) para que el movimiento sea suave
    val densidad = LocalDensity.current

    // Variables de estado temporal mientras el usuario arrastra el dedo
    var offsetX by remember(nota.posicionX) { mutableStateOf(nota.posicionX) }
    var offsetY by remember(nota.posicionY) { mutableStateOf(nota.posicionY) }

    Card(
        onClick = onClick,
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF9C4)),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        modifier = Modifier
            // 1. Posicionamos la tarjeta según sus coordenadas
            .offset { IntOffset(offsetX.roundToInt(), offsetY.roundToInt()) }
            // 2. Le damos un tamaño fijo tipo Post-it para que no se deformen
            .width(160.dp)
            .height(140.dp)
            // 3. Activamos la detección de arrastre
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragEnd = {
                        // Cuando el usuario suelta la nota, guardamos en Firebase
                        viewModel.moverNota(nota.id, offsetX, offsetY)
                    }
                ) { change, dragAmount ->
                    change.consume()
                    // Actualizamos la posición visual mientras mueve el dedo
                    offsetX += dragAmount.x
                    offsetY += dragAmount.y
                }
            }
    ) {
        Column(
            modifier = Modifier.padding(12.dp).fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "${nota.cantidadToneladas} Tons",
                style = MaterialTheme.typography.titleLarge
            )
            Column {
                Text(
                    text = "Prov: ${nota.nombreProveedor}",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = fechaString,
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.Gray
                )
            }
        }
    }
}
