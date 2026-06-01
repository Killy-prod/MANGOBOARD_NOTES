package com.prodkilly.mangoboard_notes


import androidx.biometric.BiometricPrompt
import androidx.compose.foundation.Image
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.prodkilly.mangoboard_notes.ui.theme.UltraRed

@Composable
fun PantallaAutenticacion(onAutenticado: () -> Unit) {
    // Obtenemos el contexto actual de la aplicación y lo forzamos a ser un FragmentActivity.
    // Esto es un requisito obligatorio de Android para poder mostrar la ventana de la huella digital.
    val contexto = LocalContext.current as FragmentActivity

    // LaunchedEffect(Unit) hace que el bloque de código que tiene adentro se ejecute
    // automáticamente UNA SOLA VEZ justo en el momento en que esta pantalla aparece.
    LaunchedEffect(Unit) {
        // Lanzamos la petición de huella inmediatamente al abrir la app.
        solicitarHuella(contexto, onAutenticado)
    }

    // --- DISEÑO VISUAL DE LA PANTALLA ---
    // Column apila los elementos visuales uno debajo del otro.
    Column(
        modifier = Modifier
            .fillMaxSize()      // Hace que la columna ocupe toda la pantalla
            .padding(24.dp),    // Agrega un margen alrededor de los bordes
        horizontalAlignment = Alignment.CenterHorizontally, // Centra los elementos de izquierda a derecha
        verticalArrangement = Arrangement.Center            // Centra los elementos de arriba a abajo
    ) {
        // Muestra el logo de tu aplicación
        Image(
            painter = painterResource(id = R.drawable.logo_mangoboard), // Busca el logo en la carpeta drawable
            contentDescription = "Logo de MangoBoard", // Descripción oculta para accesibilidad (lectores de pantalla)
            modifier = Modifier.size(120.dp) // Define el tamaño exacto de la imagen
        )

        // Espaciador transparente para separar el logo del texto
        Spacer(modifier = Modifier.height(24.dp))

        // Título principal de la app
        Text(
            text = "MangoBoard",
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = UltraRed // Usa el color personalizado de tu tema
        )
        // Subtítulo
        Text(
            text = "Protegido con Biometría",
            fontSize = 16.sp,
            color = Color.Gray
        )

        // Espaciador más grande antes de mostrar el botón
        Spacer(modifier = Modifier.height(48.dp))

        // Botón de respaldo (Fallback)
        // Sirve por si el usuario presiona "Cancelar" por error o toca fuera de la ventana
        // del sensor biométrico, permitiéndole volver a invocar la lectura de huella manualmente.
        Button(
            onClick = { solicitarHuella(contexto, onAutenticado) },
            colors = ButtonDefaults.buttonColors(containerColor = UltraRed)
        ) {
            Text("Usar Huella Digital")
        }
    }
}

// --- LÓGICA DEL SENSOR BIOMÉTRICO ---
// Función privada: Solo puede ser utilizada dentro de este archivo específico.
// Recibe la actividad actual y una función (onExito) que nos dirá cuándo el usuario haya entrado.
private fun solicitarHuella(actividad: FragmentActivity, onExito: () -> Unit) {
    // El ejecutor se encarga de procesar la lectura del sensor en el hilo principal de la aplicación.
    val executor = ContextCompat.getMainExecutor(actividad)

    // Creamos el lector biométrico y configuramos qué hacer según el resultado (éxito o error).
    val biometricPrompt = BiometricPrompt(actividad, executor,
        object : BiometricPrompt.AuthenticationCallback() {

            // Si la huella coincide correctamente con la del dueño del teléfono:
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                super.onAuthenticationSucceeded(result)
                onExito() // Ejecuta la acción que avisa que ya entró y cambia a la pantalla de la Pizarra
            }

            // Si ocurre un error (ej. canceló la ventana, demasiados intentos fallidos con huella incorrecta):
            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                super.onAuthenticationError(errorCode, errString)
                // Opcional: Aquí podrías agregar un mensaje de error visual (Toast) si lo necesitas más adelante.
            }
        })

    // Configuramos cómo se va a ver la ventanita del sistema de Android que le pide la huella al usuario.
    val promptInfo = BiometricPrompt.PromptInfo.Builder()
        .setTitle("Acceso a MangoBoard")             // El título de la ventanita
        .setSubtitle("Coloca tu dedo en el sensor")  // La instrucción para el usuario
        .setNegativeButtonText("Cancelar")           // El texto del botón para cerrar la ventanita
        .build()

    // Finalmente, invoca y muestra la ventana en pantalla usando la configuración que armamos arriba.
    biometricPrompt.authenticate(promptInfo)
}