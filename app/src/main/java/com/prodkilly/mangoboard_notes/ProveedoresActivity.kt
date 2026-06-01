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

class ProveedoresActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    PantallaProveedores()
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaProveedores() {
    val dbAuxiliar = DBAuxiliar()
    var listaProveedores by remember { mutableStateOf(listOf<Proveedor>()) }

    // Variables para el formulario
    var nombre by remember { mutableStateOf("") }
    var telefono by remember { mutableStateOf("") }
    var region by remember { mutableStateOf("") }

    // Escuchar proveedores en tiempo real
    LaunchedEffect(Unit) {
        dbAuxiliar.escucharProveedores { proveedores ->
            listaProveedores = proveedores
        }
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text(
            text = "Directorio de Proveedores",
            style = MaterialTheme.typography.headlineMedium,
            color = UltraRed
        )

        Spacer(modifier = Modifier.height(16.dp))

        // --- FORMULARIO PARA AGREGAR ---
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Nuevo Proveedor", style = MaterialTheme.typography.titleMedium)

                OutlinedTextField(
                    value = nombre,
                    onValueChange = { nombre = it },
                    label = { Text("Nombre del proveedor") },
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                )
                OutlinedTextField(
                    value = telefono,
                    onValueChange = { telefono = it },
                    label = { Text("Teléfono") },
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                )
                OutlinedTextField(
                    value = region,
                    onValueChange = { region = it },
                    label = { Text("Región / Ciudad") },
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                )

                Button(
                    onClick = {
                        if (nombre.isNotBlank() && region.isNotBlank()) {
                            val nuevoProv = Proveedor(
                                nombre = nombre,
                                telefono = telefono,
                                region = region
                            )
                            dbAuxiliar.agregarProveedor(nuevoProv)
                            // Limpiar campos
                            nombre = ""
                            telefono = ""
                            region = ""
                        }
                    },
                    modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = yellow10)
                ) {
                    Text("Guardar Proveedor")
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // --- LISTA DE PROVEEDORES GUARDADOS ---
        Text("Proveedores Registrados", style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(8.dp))

        LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(listaProveedores) { prov ->
                Card(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(text = prov.nombre, style = MaterialTheme.typography.titleMedium)
                        Text(text = "Tel: ${prov.telefono} | Región: ${prov.region}", color = Color.Gray)
                    }
                }
            }
        }
    }
}