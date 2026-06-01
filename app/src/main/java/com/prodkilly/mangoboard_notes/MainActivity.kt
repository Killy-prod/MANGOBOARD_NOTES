package com.prodkilly.mangoboard_notes

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.fragment.app.FragmentActivity
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt

class MainActivity : FragmentActivity() {
    private val viewModel: ViewModel_board by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                var estaAutenticado by remember { mutableStateOf(false) }

                if (!estaAutenticado) {
                    PantallaAutenticacion(onAutenticado = { estaAutenticado = true })
                } else {
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
    }
}

@Composable
fun PizarraScreen(viewModel: ViewModel_board, modifier: Modifier = Modifier) {
    val contexto = LocalContext.current
    val listaNotas by viewModel.notas.collectAsState()

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
    ) { paddingValues ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Pizarra Diaria", style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.primary)

                Button(onClick = {
                    val notasOrdenadas = listaNotas.sortedByDescending { it.fechaCompra }
                    val totalColumnas = 3
                    val distanciaX = 550f
                    val distanciaY = 350f

                    notasOrdenadas.forEachIndexed { index, nota ->
                        val columna = index % totalColumnas
                        val fila = index / totalColumnas
                        viewModel.moverNota(nota.id, columna * distanciaX, fila * distanciaY)
                    }
                }) {
                    Text("Ordenar")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // La Pizarra Libre (Box)
            Box(modifier = Modifier.fillMaxSize()) {
                // Usamos .forEach para iterar sobre la lista de notas
                listaNotas.forEach { notaDeLaLista ->
                    key(notaDeLaLista.id) {
                        NotaAdhesivaItem(
                            nota = notaDeLaLista,
                            viewModel = viewModel,
                            onClick = {
                                // AQUÍ ESTAMOS USANDO 'notaDeLaLista' que definimos arriba
                                val intent = Intent(contexto, FormActivity::class.java).apply {
                                    putExtra("nota_id", notaDeLaLista.id)
                                    putExtra("proveedor", notaDeLaLista.nombreProveedor)
                                    putExtra("toneladas", notaDeLaLista.cantidadToneladas)
                                    putExtra("descripcion", notaDeLaLista.descripcion)
                                    putExtra("fecha_compra", notaDeLaLista.fechaCompra)
                                }
                                contexto.startActivity(intent)
                            }
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotaAdhesivaItem(nota: NotaPizarra, viewModel: ViewModel_board, onClick: () -> Unit) {
    // 1. Definimos el formateador y la fecha aquí mismo
    val formatoFecha = remember { SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()) }
    val fechaString = formatoFecha.format(Date(nota.fechaCompra))

    // 2. Estados de posición
    var offsetX by remember(nota.id) { mutableStateOf(nota.posicionX) }
    var offsetY by remember(nota.id) { mutableStateOf(nota.posicionY) }
    var estaArrastrando by remember { mutableStateOf(false) }

    LaunchedEffect(nota.posicionX, nota.posicionY) {
        if (!estaArrastrando) {
            offsetX = nota.posicionX
            offsetY = nota.posicionY
        }
    }

    Card(
        onClick = onClick,
        modifier = Modifier
            .offset { IntOffset(offsetX.roundToInt(), offsetY.roundToInt()) }
            .width(250.dp)
            .height(140.dp)
            .pointerInput(nota.id) {
                detectDragGestures(
                    onDragStart = { estaArrastrando = true },
                    onDragEnd = {
                        estaArrastrando = false
                        viewModel.moverNota(nota.id, offsetX, offsetY)
                    },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        offsetX += dragAmount.x
                        offsetY += dragAmount.y
                    }
                )
            }
    ) {
        Column(modifier = Modifier.padding(12.dp).fillMaxSize(), verticalArrangement = Arrangement.SpaceBetween) {
            Text(text = "${nota.cantidadToneladas} Tons", style = MaterialTheme.typography.titleLarge)

            if (nota.descripcion.isNotBlank()) {
                Text(text = nota.descripcion, style = MaterialTheme.typography.bodySmall, color = Color.DarkGray, maxLines = 2)
            }

            Column {
                Text(text = "Prov: ${nota.nombreProveedor}", style = MaterialTheme.typography.bodyMedium)
                // ¡AQUÍ ESTAMOS USANDO LA VARIABLE QUE DEFINIMOS ARRIBA!
                Text(text = fechaString, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
            }
        }
    }
}