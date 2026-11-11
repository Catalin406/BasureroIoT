package com.example.basureroiot.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.basureroiot.bluetooth.ConexionBasurero
import com.example.basureroiot.data.DbHelper
import com.example.basureroiot.data.Registro
import com.example.basureroiot.data.Sesion

// Historial: lee de la base de datos SQLite los usos por día y los últimos registros.
@Composable
fun PantallaHistorial(onVolver: () -> Unit) {
    val contexto = LocalContext.current
    val db = remember { DbHelper(contexto) }
    var registros by remember { mutableStateOf(emptyList<Registro>()) }
    var usosPorDia by remember { mutableStateOf(emptyList<Pair<String, Int>>()) }

    // Se leen los datos al abrir la pantalla y cada vez que llega un mensaje nuevo.
    LaunchedEffect(ConexionBasurero.registros.size) {
        val usuario = Sesion.usuario
        if (usuario != null) {
            registros = db.ultimosRegistros(usuario.id, 50)
            usosPorDia = db.usosPorDia(usuario.id)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        Text(text = "Historial", style = MaterialTheme.typography.headlineMedium)
        Text(text = "Datos guardados en SQLite", style = MaterialTheme.typography.bodyMedium)

        Spacer(modifier = Modifier.height(12.dp))

        Text(text = "Usos por día", style = MaterialTheme.typography.titleMedium)
        if (usosPorDia.isEmpty()) {
            Text("Todavía no hay usos registrados")
        }
        usosPorDia.forEach { (dia, cantidad) ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = dia)
                Text(text = cantidad.toString())
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(text = "Registros", style = MaterialTheme.typography.titleMedium)
        // LazyColumn: lista que se desplaza y solo dibuja lo que se ve en pantalla.
        LazyColumn(modifier = Modifier.weight(1f)) {
            items(registros) { r ->
                Text(
                    text = r.fechaHora + "  " + r.descripcion(),
                    modifier = Modifier.padding(vertical = 4.dp)
                )
                HorizontalDivider()
            }
        }

        Button(onClick = onVolver, modifier = Modifier.fillMaxWidth()) {
            Text("Volver al resumen")
        }
    }
}