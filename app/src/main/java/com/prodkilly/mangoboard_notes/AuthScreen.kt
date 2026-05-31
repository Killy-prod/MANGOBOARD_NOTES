package com.prodkilly.mangoboard_notes


import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun PantallaAutenticacion(onAutenticado: () -> Unit) {
    val contexto = LocalContext.current as FragmentActivity

    // Lanzamos la petición de huella en cuanto la pantalla se abre
    LaunchedEffect(Unit) {
        solicitarHuella(contexto, onAutenticado)
    }

    // Diseño de la pantalla (Logo y texto)
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Filled.Lock,
            contentDescription = "Logo",
            modifier = Modifier.size(100.dp),
            tint = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "MangoBoard",
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = "Protegido con Biometría",
            fontSize = 16.sp,
            color = Color.Gray
        )

        Spacer(modifier = Modifier.height(48.dp))

        // Botón de respaldo
        Button(onClick = { solicitarHuella(contexto, onAutenticado) }) {
            Text("Usar Huella Digital")
        }
    }
}

// Función privada que solo se usa en este archivo para el sensor
private fun solicitarHuella(actividad: FragmentActivity, onExito: () -> Unit) {
    val executor = ContextCompat.getMainExecutor(actividad)

    val biometricPrompt = BiometricPrompt(actividad, executor,
        object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                super.onAuthenticationSucceeded(result)
                onExito() // Si la huella es correcta, avisamos que ya entró
            }

            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                super.onAuthenticationError(errorCode, errString)
            }
        })

    val promptInfo = BiometricPrompt.PromptInfo.Builder()
        .setTitle("Acceso a MangoBoard")
        .setSubtitle("Coloca tu dedo en el sensor")
        .setNegativeButtonText("Cancelar")
        .build()

    biometricPrompt.authenticate(promptInfo)
}