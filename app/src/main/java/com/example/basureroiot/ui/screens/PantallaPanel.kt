package com.example.basureroiot.ui.screens

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.example.basureroiot.bluetooth.ConexionBasurero
import com.example.basureroiot.bluetooth.DispositivoEmparejado
import com.example.basureroiot.bluetooth.EstadoConexion
import com.example.basureroiot.bluetooth.listarEmparejados

// ¿Tiene la app el permiso de Bluetooth? (en Android 11 o menos no hace falta pedirlo)
fun tienePermisoBluetooth(contexto: Context): Boolean {
    return Build.VERSION.SDK_INT < Build.VERSION_CODES.S ||
            ContextCompat.checkSelfPermission(contexto, Manifest.permission.BLUETOOTH_CONNECT) ==
            PackageManager.PERMISSION_GRANTED
}

// Resumen de hoy: estado de la conexión, usos, nivel y alerta de lleno.
@Composable
fun PantallaPanel(onVerHistorial: () -> Unit, onCerrarSesion: () -> Unit) {
    val c = ConexionBasurero
    var mostrarConexion by rememberSaveable { mutableStateOf(false) }
    var vaciadoMarcado by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(text = "Resumen de hoy", style = MaterialTheme.typography.headlineMedium)

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = c.textoEstado())
            OutlinedButton(onClick = { mostrarConexion = true }) {
                Text("Conectar")
            }
        }

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(text = "Usos desde el último vaciado", style = MaterialTheme.typography.labelLarge)
                Text(text = c.usos.toString(), style = MaterialTheme.typography.displayMedium)
            }
        }

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                if (c.nivel < 0) {
                    Text(text = "Nivel actual: sin dato", style = MaterialTheme.typography.labelLarge)
                } else {
                    Text(text = "Nivel actual: " + c.nivel + " %", style = MaterialTheme.typography.labelLarge)
                    Spacer(modifier = Modifier.height(8.dp))
                    LinearProgressIndicator(
                        progress = { c.nivel / 100f },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        if (c.alerta) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(text = "⚠ Basurero casi lleno", style = MaterialTheme.typography.titleMedium)
                    Text(text = "Se recomienda vaciarlo", style = MaterialTheme.typography.bodyMedium)
                }
            }
        }

        Button(
            onClick = {
                c.marcarVaciado()
                vaciadoMarcado = true
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Marcar vaciado")
        }
        if (vaciadoMarcado) {
            Text("Vaciado registrado")
        }

        OutlinedButton(onClick = onVerHistorial, modifier = Modifier.fillMaxWidth()) {
            Text("Ver historial")
        }

        TextButton(onClick = {
            c.desconectar()
            onCerrarSesion()
        }) {
            Text("Cerrar sesión")
        }
    }

    if (mostrarConexion) {
        DialogoConexion(onCerrar = { mostrarConexion = false })
    }
}

// Ventana para elegir el HC-05 (o el simulador). Se cierra sola cuando conecta.
@Composable
private fun DialogoConexion(onCerrar: () -> Unit) {
    val contexto = LocalContext.current
    var permiso by remember { mutableStateOf(tienePermisoBluetooth(contexto)) }
    val pedirPermiso = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { concedido -> permiso = concedido }
    var dispositivos by remember { mutableStateOf(emptyList<DispositivoEmparejado>()) }
    val estado = ConexionBasurero.estado

    LaunchedEffect(permiso) {
        if (permiso) dispositivos = listarEmparejados()
    }
    LaunchedEffect(estado) {
        if (estado is EstadoConexion.Conectado) onCerrar()
    }

    AlertDialog(
        onDismissRequest = onCerrar,
        title = { Text("Conectar al basurero") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Estado: " + ConexionBasurero.textoEstado())
                if (!permiso) {
                    Text("Falta el permiso de Bluetooth.")
                    Button(onClick = { pedirPermiso.launch(Manifest.permission.BLUETOOTH_CONNECT) }) {
                        Text("Dar permiso")
                    }
                } else {
                    if (dispositivos.isEmpty()) {
                        Text("No hay dispositivos emparejados. Empareja el HC-05 en Ajustes > Bluetooth.")
                    }
                    dispositivos.forEach { d ->
                        OutlinedButton(
                            onClick = { ConexionBasurero.conectar(d.direccion, false) },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(d.nombre)
                        }
                    }
                }
                OutlinedButton(
                    onClick = { ConexionBasurero.conectar("", true) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Usar simulador (sin Arduino)")
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onCerrar) { Text("Cerrar") }
        }
    )
}
