package com.prodkilly.mangoboard_notes

import android.R
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
import com.prodkilly.mangoboard_notes.ui.theme.MiColorFondo
import com.prodkilly.mangoboard_notes.ui.theme.UltraRed
import com.prodkilly.mangoboard_notes.ui.theme.lightred20
import com.prodkilly.mangoboard_notes.ui.theme.yellow10
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt

// --- CLASE PRINCIPAL ---
// Esta es la primera pantalla que se ejecuta al abrir la app.
// Hereda de FragmentActivity para permitir el uso del sensor biométrico (huella).
class MainActivity : FragmentActivity() {

    // Conecta la vista con el ViewModel (donde vive la lógica de base de datos)
    private val viewModel: ViewModel_board by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // setContent es el puente de Compose. Todo lo que esté adentro es la interfaz gráfica.
        setContent {
            MaterialTheme {
                // Surface es el "lienzo" principal de toda la pantalla
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MiColorFondo // Usa el color personalizado que definiste
                ) {
                    // Estado para saber si el usuario ya puso su huella correctamente
                    var estaAutenticado by remember { mutableStateOf(false) }

                    // Si no está autenticado, mostramos la pantalla de bloqueo
                    if (!estaAutenticado) {
                        PantallaAutenticacion(onAutenticado = { estaAutenticado = true })
                    } else {
                        // --- INTERFAZ PRINCIPAL DE LA APP (Una vez desbloqueada) ---

                        // Estado para controlar si el menú lateral está abierto o cerrado
                        val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
                        // Scope para poder lanzar animaciones asíncronas (como abrir/cerrar el menú)
                        val scope = rememberCoroutineScope()
                        val contexto = LocalContext.current

                        // Componente que crea el menú lateral deslizable
                        ModalNavigationDrawer(
                            drawerState = drawerState,
                            drawerContent = {
                                // Lo que va ADENTRO del menú lateral
                                ModalDrawerSheet {
                                    Text("Menú MangoBoard",
                                        modifier = Modifier.padding(16.dp),
                                        color = UltraRed
                                    )
                                    HorizontalDivider() // Línea separadora

                                    // Botón del menú para ir a la lista de proveedores
                                    NavigationDrawerItem(
                                        label = { Text("Gestionar Proveedores") },
                                        selected = false,
                                        onClick = {
                                            val intent = Intent(contexto, ProveedoresActivity::class.java)
                                            contexto.startActivity(intent)
                                            // Cierra el menú automáticamente después de hacer clic
                                            scope.launch { drawerState.close() }
                                        }
                                    )
                                }
                            }
                        ) {
                            // Scaffold es la estructura básica de una pantalla Material Design (TopBar, Botón Flotante, etc.)
                            Scaffold(
                                topBar = {
                                    @OptIn(ExperimentalMaterial3Api::class)
                                    // La barra superior donde dice "MangoBoard"
                                    TopAppBar(
                                        title = { Text("MangoBoard", color = UltraRed) },
                                        navigationIcon = {
                                            // Botón de las 3 rayitas (hamburguesa) para abrir el menú lateral
                                            IconButton(onClick = { scope.launch { drawerState.open() } }) {
                                                Icon(Icons.Default.Menu, contentDescription = "Menú")
                                            }
                                        }
                                    )
                                }
                            ) { paddingValues ->
                                // Aquí llamamos a la pantalla que dibuja el corcho y las notas,
                                // pasándole los márgenes (paddingValues) para que no se encime con la barra superior
                                PizarraScreen(viewModel = viewModel, modifier = Modifier.padding(paddingValues))
                            }
                        }
                    }
                }
            }
        }
    }
}

// --- PANTALLA DEL CORCHO (PIZARRA) ---
@Composable
fun PizarraScreen(viewModel: ViewModel_board, modifier: Modifier = Modifier) {
    val contexto = LocalContext.current

    // Obtenemos la lista de notas de la base de datos y la convertimos en un Estado.
    // Si la base de datos cambia, la interfaz se actualiza sola.
    val listaNotas by viewModel.notas.collectAsState()

    Scaffold(
        // Botón flotante (+) en la esquina inferior derecha para crear notas nuevas
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    val intent = Intent(contexto, FormActivity::class.java)
                    contexto.startActivity(intent)
                },
                containerColor = yellow10
            ) {
                Text("+", style = MaterialTheme.typography.headlineMedium, color = Color.White)
            }
        }
    ) { paddingValues ->
        // Columna principal de la pantalla
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            // Fila superior con el título y el botón de ordenar
            Row(
                modifier = Modifier.fillMaxWidth().offset(y = (-50).dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Pizarra Diaria", style = MaterialTheme.typography.headlineMedium, color = lightred20)

                // Botón para organizar las notas en una cuadrícula
                Button(onClick = {
                    // 1. Ordena las notas de la más reciente a la más antigua
                    val notasOrdenadas = listaNotas.sortedByDescending { it.fechaCompra }

                    // 2. Define las medidas para la cuadrícula
                    val totalColumnas = 4
                    val distanciaX = 550f // Distancia horizontal entre notas
                    val distanciaY = 300f // Distancia vertical entre notas

                    // 3. Recorre cada nota y calcula su nueva posición X, Y
                    notasOrdenadas.forEachIndexed { index, nota ->
                        val columna = index % totalColumnas
                        val fila = index / totalColumnas
                        // Guarda las nuevas coordenadas en la base de datos
                        viewModel.moverNota(nota.id, columna * distanciaX, fila * distanciaY)
                    }
                }) {
                    Text("Ordenar")
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Box es el contenedor ideal para la pizarra, porque permite que los elementos
            // se superpongan y tengan coordenadas libres (X, Y)
            Box(modifier = Modifier.fillMaxSize()) {

                // Recorre la lista de notas y dibuja una por una
                listaNotas.forEach { notaDeLaLista ->
                    // key() le dice a Compose "esta nota es única", mejorando el rendimiento al animar/mover
                    key(notaDeLaLista.id) {
                        NotaAdhesivaItem(
                            nota = notaDeLaLista,
                            viewModel = viewModel,
                            onClick = {
                                // Al hacer clic en una nota, abre el formulario y le manda los datos actuales para editar
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

// --- COMPONENTE VISUAL DE LA NOTA ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotaAdhesivaItem(nota: NotaPizarra, viewModel: ViewModel_board, onClick: () -> Unit) {

    // Convierte el número largo (milisegundos) en una fecha legible (ej: 01/06/2026 14:30)
    val formatoFecha = remember { SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()) }
    val fechaString = formatoFecha.format(Date(nota.fechaCompra))

    // Intenta leer el color guardado (ej: "#FFCCBC"). Si falla por error de formato, pone un amarillo por defecto
    val colorNota = try {
        Color(android.graphics.Color.parseColor(nota.colorHex))
    } catch (e: Exception) {
        Color(0xFFFFF9C4)
    }

    // Variables temporales para el movimiento del dedo en pantalla (drag and drop)
    var offsetX by remember(nota.id) { mutableStateOf(nota.posicionX) }
    var offsetY by remember(nota.id) { mutableStateOf(nota.posicionY) }
    var estaArrastrando by remember { mutableStateOf(false) }

    // Este efecto escucha si las coordenadas cambian desde afuera (ej. cuando presionas "Ordenar")
    // Si la nota NO se está arrastrando con el dedo, actualiza su posición visual
    LaunchedEffect(nota.posicionX, nota.posicionY) {
        if (!estaArrastrando) {
            offsetX = nota.posicionX
            offsetY = nota.posicionY
        }
    }

    // La tarjeta visual de la nota
    Card(
        onClick = onClick,
        colors = CardDefaults.cardColors(containerColor = colorNota), // Aplica el color
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp), // Le da la sombra
        modifier = Modifier
            // offset es lo que posiciona la nota en el mapa X, Y
            .offset { IntOffset(offsetX.roundToInt(), offsetY.roundToInt()) }
            .width(250.dp)
            .height(140.dp)
            // pointerInput detecta los gestos físicos del usuario en la pantalla
            .pointerInput(nota.id) {
                detectDragGestures(
                    onDragStart = { estaArrastrando = true }, // Cuando el usuario toca la nota
                    onDragEnd = {
                        // Cuando el usuario suelta la nota, guardamos las coordenadas finales en la base de datos
                        estaArrastrando = false
                        viewModel.moverNota(nota.id, offsetX, offsetY)
                    },
                    onDrag = { change, dragAmount ->
                        // Mientras el usuario mueve el dedo, actualiza las coordenadas temporales en tiempo real
                        change.consume()
                        offsetX += dragAmount.x
                        offsetY += dragAmount.y
                    }
                )
            }
    ) {
        // --- CONTENIDO TEXTUAL DE LA NOTA ---
        Column(modifier = Modifier.padding(12.dp).fillMaxSize(), verticalArrangement = Arrangement.SpaceBetween) {
            // Toneladas en texto grande
            Text(text = "${nota.cantidadToneladas} Tons", style = MaterialTheme.typography.titleLarge)

            // Si hay alguna descripción, se muestra en chiquito. Si está en blanco, este bloque se ignora.
            if (nota.descripcion.isNotBlank()) {
                Text(text = nota.descripcion, style = MaterialTheme.typography.bodySmall, color = Color.DarkGray, maxLines = 2)
            }

            // Proveedor y fecha en la parte inferior
            Column {
                Text(text = "Prov: ${nota.nombreProveedor}", style = MaterialTheme.typography.bodyMedium)
                Text(text = fechaString, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
            }
        }
    }
}