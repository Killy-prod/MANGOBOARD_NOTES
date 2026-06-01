package com.prodkilly.mangoboard_notes

import com.prodkilly.mangoboard_notes.ui.theme.UltraRed
import com.prodkilly.mangoboard_notes.ui.theme.yellow10
import com.prodkilly.mangoboard_notes.DBAuxiliar
import com.prodkilly.mangoboard_notes.Proveedor
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp


// --- ACTIVIDAD DE PROVEEDORES ---
// Esta es la pantalla secundaria que se abre desde el menú lateral.
// Se encarga de mostrar el directorio de contactos y registrar nuevos proveedores de mango.
class ProveedoresActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                // Surface vuelve a ser nuestro lienzo base ocupando toda la pantalla
                Surface(modifier = Modifier.fillMaxSize()) {
                    PantallaProveedores()
                }
            }
        }
    }
}

// --- INTERFAZ DE PROVEEDORES ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaProveedores() {
    // Instanciamos nuestro ayudante de base de datos de Firebase
    val dbAuxiliar = DBAuxiliar()

    // Estado para almacenar la lista de proveedores que traemos de la nube
    var listaProveedores by remember { mutableStateOf(listOf<Proveedor>()) }

    // --- ESTADOS DEL FORMULARIO DE REGISTRO ---
    // Variables temporales para capturar lo que el usuario escribe sobre el nuevo proveedor
    var nombre by remember { mutableStateOf("") }
    var telefono by remember { mutableStateOf("") }
    var region by remember { mutableStateOf("") }

    // --- ESCUCHA EN TIEMPO REAL ---
    // LaunchedEffect(Unit) se activa en cuanto se dibuja esta pantalla por primera vez.
    // Llama al "radar" de Firebase para que nos envíe la lista de proveedores y se actualice sola si hay cambios.
    LaunchedEffect(Unit) {
        dbAuxiliar.escucharProveedores { proveedores ->
            listaProveedores = proveedores
        }
    }

    // Contenedor vertical principal con un margen de 16dp en los bordes
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {

        // Título de la pantalla
        Text(
            text = "Directorio de Proveedores",
            style = MaterialTheme.typography.headlineMedium,
            color = UltraRed
        )

        Spacer(modifier = Modifier.height(16.dp))

        // --- FORMULARIO PARA AGREGAR NUEVOS PROVEEDORES ---
        // Usamos una tarjeta (Card) con un tono gris/variante para separarla visualmente de la lista
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Nuevo Proveedor", style = MaterialTheme.typography.titleMedium)

                // Campo 1: Nombre
                OutlinedTextField(
                    value = nombre,
                    onValueChange = { nombre = it },
                    label = { Text("Nombre del proveedor") },
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                )

                // Campo 2: Teléfono
                OutlinedTextField(
                    value = telefono,
                    onValueChange = { telefono = it },
                    label = { Text("Teléfono") },
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                )

                // Campo 3: Región o Ciudad de donde viene el mango
                OutlinedTextField(
                    value = region,
                    onValueChange = { region = it },
                    label = { Text("Región / Ciudad") },
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                )

                // Botón para procesar el registro
                Button(
                    onClick = {
                        // VALIDACIÓN: Solo guarda si el Nombre y la Región NO están vacíos
                        if (nombre.isNotBlank() && region.isNotBlank()) {
                            // Creamos el objeto molde 'Proveedor' con los datos capturados
                            val nuevoProv = Proveedor(
                                nombre = nombre,
                                telefono = telefono,
                                region = region
                            )
                            // Lo enviamos a Firebase usando el DBAuxiliar
                            dbAuxiliar.agregarProveedor(nuevoProv)

                            // LIMPIEZA: Reseteamos los campos de texto para que queden vacíos de nuevo
                            nombre = ""
                            telefono = ""
                            region = ""
                        }
                    },
                    modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = yellow10) // Color amarillo de tu tema
                ) {
                    Text("Guardar Proveedor")
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // --- LISTA DE PROVEEDORES GUARDADOS ---
        Text("Proveedores Registrados", style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(8.dp))

        // LazyColumn es un contenedor inteligente (como el RecyclerView antiguo).
        // Solo renderiza en pantalla los elementos que el usuario está viendo en ese momento,
        // lo cual ahorra memoria y evita que la app se trabe si tienes cientos de proveedores.
        LazyColumn(modifier = Modifier.fillMaxSize()) {

            // items() recorre de forma optimizada la lista de proveedores que tenemos en el estado
            items(listaProveedores) { prov ->

                // Tarjeta individual para mostrar los datos de cada proveedor
                Card(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        // Nombre en texto destacado
                        Text(text = prov.nombre, style = MaterialTheme.typography.titleMedium)

                        // Fila de datos secundarios (Teléfono y Región) en color gris
                        Text(text = "Tel: ${prov.telefono} | Región: ${prov.region}", color = Color.Gray)
                    }
                }
            }
        }
    }
}