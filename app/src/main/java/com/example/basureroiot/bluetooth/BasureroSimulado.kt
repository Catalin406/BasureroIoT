package com.example.basureroiot.bluetooth

import android.os.Handler
import android.os.Looper

// Basurero de mentira para probar la app en el emulador (el emulador no tiene Bluetooth).
// Responde a los mismos comandos que el Arduino real.
class BasureroSimulado(
    private val alCambiarEstado: (EstadoConexion) -> Unit,
    private val alRecibirLinea: (String) -> Unit
) : DispositivoBasurero {

    private val principal = Handler(Looper.getMainLooper())
    private var conectado = false
    private var usos = 0
    private var nivel = 25

    override fun conectar(direccion: String) {
        alCambiarEstado(EstadoConexion.Conectando)
        principal.postDelayed({
            conectado = true
            alCambiarEstado(EstadoConexion.Conectado)
            alRecibirLinea("ESTADO:LISTO")
        }, 800)
    }

    override fun enviar(texto: String) {
        if (!conectado) return
        principal.postDelayed({
            if (!texto.startsWith("TACHO26:")) {
                alRecibirLinea("ERROR:NO_AUTORIZADO")
            } else {
                when (texto.removePrefix("TACHO26:")) {
                    "A" -> {
                        alRecibirLinea("ESTADO:ABIERTO")
                        principal.postDelayed({ alRecibirLinea("ESTADO:CERRADO") }, 2000)
                    }
                    "R" -> {
                        usos = 0
                        alRecibirLinea("USOS:0")
                    }
                    "S" -> {
                        alRecibirLinea("USOS:" + usos)
                        alRecibirLinea("NIVEL:" + nivel)
                    }
                    else -> alRecibirLinea("ERROR:COMANDO_INVALIDO")
                }
            }
        }, 300)
    }

    override fun desconectar() {
        conectado = false
        alCambiarEstado(EstadoConexion.Desconectado)
    }
}
