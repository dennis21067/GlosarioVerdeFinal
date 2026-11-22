package cu.banca.glosario

import android.app.Activity
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat

class MainActivity : ComponentActivity() {
    private var lectorVoz: LectorVoz? = null

    override fun onStop() {
        super.onStop()
        lectorVoz?.detener()
    }

    override fun onDestroy() {
        super.onDestroy()
        lectorVoz?.liberar()
        lectorVoz = null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val lector = LectorVoz(applicationContext)
        lectorVoz = lector
        val glosario = cargarGlosario(applicationContext)
        val ajustes = getSharedPreferences("ajustes", MODE_PRIVATE)
        setContent {
            var modo by rememberSaveable { mutableStateOf(ajustes.getInt("modo_tema", 0)) }
            var pasoTexto by rememberSaveable {
                mutableStateOf(ajustes.getInt("paso_texto", PASO_TEXTO_INICIAL).coerceIn(0, escalasTexto.lastIndex))
            }
            TemaGlosario(modo) {
                GlosarioApp(
                    glosario = glosario,
                    modo = modo,
                    alCambiarModo = {
                        modo = it
                        ajustes.edit().putInt("modo_tema", it).apply()
                    },
                    lector = lector,
                    pasoTexto = pasoTexto,
                    alCambiarPasoTexto = {
                        pasoTexto = it.coerceIn(0, escalasTexto.lastIndex)
                        ajustes.edit().putInt("paso_texto", pasoTexto).apply()
                    },
                    alSalir = { finish() },
                )
            }
        }
    }
}

private const val VISTA_PORTADA = 0
private const val VISTA_MENU = 1
private const val VISTA_COMO_USAR = 2
private const val VISTA_GENERAL = 3
private const val VISTA_LISTA = 4
private const val VISTA_DETALLE = 5
private const val VISTA_CREDITOS = 6
private const val VISTA_CONCLUSIONES = 7

/** Tamaños de letra de la pantalla de concepto (multiplicadores). El paso inicial es el tamaño normal (1.0). */
val escalasTexto = listOf(0.85f, 1f, 1.15f, 1.3f, 1.5f, 1.75f)
const val PASO_TEXTO_INICIAL = 1

private val opcionesModo = listOf("Según el sistema", "Claro", "Oscuro")

@Composable
fun GlosarioApp(
    glosario: Glosario,
    modo: Int,
    alCambiarModo: (Int) -> Unit,
    lector: LectorVoz,
    pasoTexto: Int,
    alCambiarPasoTexto: (Int) -> Unit,
    alSalir: () -> Unit,
) {
    var vista by rememberSaveable { mutableStateOf(VISTA_PORTADA) }
    var seleccionado by rememberSaveable { mutableStateOf(1) }
    var consulta by rememberSaveable { mutableStateOf("") }
    var verApariencia by rememberSaveable { mutableStateOf(false) }
    val estadoLista = rememberLazyListState()

    val c = LocalColoresApp.current
    val fondo = MaterialTheme.colorScheme.background
    val vistaAndroid = LocalView.current
    val conPatron = vista == VISTA_PORTADA || vista == VISTA_MENU

    // Barras del sistema: se funden con el fondo de patrones en portada y menú, y con la cabecera en el resto.
    SideEffect {
        val ventana = (vistaAndroid.context as Activity).window
        ventana.statusBarColor = (if (conPatron) c.fondoNatural else c.cabecera).toArgb()
        ventana.navigationBarColor = (if (conPatron) c.fondoNatural else fondo).toArgb()
        val control = WindowCompat.getInsetsController(ventana, vistaAndroid)
        control.isAppearanceLightStatusBars = conPatron && !c.oscuro
        control.isAppearanceLightNavigationBars = !c.oscuro
    }

    fun volverAlMenu() {
        consulta = ""
        vista = VISTA_MENU
    }

    // Atrás: detalle -> lista -> menú -> portada -> (el sistema cierra la app)
    BackHandler(enabled = vista != VISTA_PORTADA) {
        when (vista) {
            VISTA_DETALLE -> vista = VISTA_LISTA
            VISTA_MENU -> vista = VISTA_PORTADA
            else -> volverAlMenu()
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(fondo)) {
        Crossfade(targetState = vista, animationSpec = tween(180), label = "vista") { v ->
            when (v) {
                VISTA_PORTADA -> PantallaPortada(
                    intro = glosario.intro,
                    alEntrar = { vista = VISTA_MENU },
                    alApariencia = { verApariencia = true },
                )

                VISTA_MENU -> PantallaMenu(
                    alComoUsar = { vista = VISTA_COMO_USAR },
                    alInformacion = { vista = VISTA_GENERAL },
                    alGlosario = { vista = VISTA_LISTA },
                    alConclusiones = { vista = VISTA_CONCLUSIONES },
                    alCreditos = { vista = VISTA_CREDITOS },
                    alSalir = alSalir,
                )

                VISTA_COMO_USAR -> PantallaTarjetas(
                    titulo = "Cómo usar esta App",
                    tarjetas = listOf(glosario.intro.comoUsar),
                    alVolver = { volverAlMenu() },
                )

                VISTA_GENERAL -> PantallaTarjetas(
                    titulo = "Información General",
                    tarjetas = glosario.intro.general,
                    alVolver = { volverAlMenu() },
                )

                VISTA_CONCLUSIONES -> PantallaConclusiones(
                    conclusiones = glosario.conclusiones,
                    alVolver = { volverAlMenu() },
                )

                VISTA_CREDITOS -> PantallaCreditos(
                    creditos = glosario.intro.creditos,
                    alVolver = { volverAlMenu() },
                )

                VISTA_LISTA -> PantallaLista(
                    glosario = glosario,
                    consulta = consulta,
                    alCambiarConsulta = { consulta = it },
                    estadoLista = estadoLista,
                    alVolver = { volverAlMenu() },
                    alAbrir = { numero ->
                        seleccionado = numero
                        vista = VISTA_DETALLE
                    },
                )

                else -> {
                    val concepto = glosario.conceptos.firstOrNull { it.numero == seleccionado }
                        ?: glosario.conceptos.first()
                    PantallaDetalle(
                        concepto = concepto,
                        bloque = glosario.bloques.firstOrNull { it.id == concepto.bloque },
                        total = glosario.conceptos.size,
                        q = plano(consulta.trim()),
                        pasoTexto = pasoTexto,
                        alCambiarPasoTexto = alCambiarPasoTexto,
                        lector = lector,
                        alVolver = { vista = VISTA_LISTA },
                    )
                }
            }
        }
    }

    if (verApariencia) {
        AlertDialog(
            onDismissRequest = { verApariencia = false },
            confirmButton = {
                TextButton(onClick = { verApariencia = false }) { Text("Cerrar") }
            },
            title = { Text("Apariencia") },
            text = {
                Column {
                    opcionesModo.forEachIndexed { indice, etiqueta ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { alCambiarModo(indice) }
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            RadioButton(selected = modo == indice, onClick = { alCambiarModo(indice) })
                            Text(text = etiqueta)
                        }
                    }
                }
            },
        )
    }
}
