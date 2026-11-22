package cu.banca.glosario

import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/* ------------------------------------------------------------------ */
/* Piezas comunes                                                      */
/* ------------------------------------------------------------------ */

@Composable
private fun Cabecera(
    titulo: String,
    subtitulo: String?,
    alVolver: (() -> Unit)?,
    acciones: (@Composable () -> Unit)? = null,
) {
    val c = LocalColoresApp.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Brush.horizontalGradient(listOf(c.cabecera, c.cabeceraFin)))
            .padding(horizontal = 4.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (alVolver != null) {
            IconButton(onClick = alVolver) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Volver",
                    tint = c.sobreCabecera,
                )
            }
        } else {
            Spacer(Modifier.width(16.dp))
        }
        Column(modifier = Modifier.weight(1f).padding(end = if (acciones != null) 8.dp else 16.dp)) {
            Text(
                text = titulo,
                style = MaterialTheme.typography.titleMedium,
                color = c.sobreCabecera,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            if (subtitulo != null) {
                Text(
                    text = subtitulo,
                    fontSize = 13.sp,
                    color = c.sobreCabeceraSuave,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
        if (acciones != null) {
            Row(
                modifier = Modifier.padding(end = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                acciones()
            }
        }
    }
}

/* Íconos dibujados con trazos (Material "volume_up" y "download"): evitan depender del paquete extendido de íconos. */
private fun iconoDeTrazo(trazo: String): ImageVector =
    ImageVector.Builder(
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f,
    ).addPath(
        pathData = PathParser().parsePathString(trazo).toNodes(),
        fill = SolidColor(Color.Black),
    ).build()

private val iconoAudio: ImageVector by lazy {
    iconoDeTrazo(
        "M3,9v6h4l5,5V4L7,9H3zM16.5,12c0,-1.77 -1.02,-3.29 -2.5,-4.03v8.05c1.48,-0.73 2.5,-2.25 2.5,-4.02z" +
            "M14,3.23v2.06c2.89,0.86 5,3.54 5,6.71s-2.11,5.85 -5,6.71v2.06c4.01,-0.91 7,-4.49 7,-8.77s-2.99,-7.86 -7,-8.77z",
    )
}

private val iconoDetener: ImageVector by lazy {
    iconoDeTrazo("M6,6h12v12H6z")
}

private val iconoDescarga: ImageVector by lazy {
    iconoDeTrazo("M5,20h14v-2H5v2zM19,9h-4V3H9v6H5l7,7 7,-7z")
}

/** Botón cuadrado pequeño de la cabecera (aumentar / disminuir letra). */
@Composable
private fun BotonCabecera(descripcion: String, activo: Boolean, alPulsar: () -> Unit, contenido: @Composable () -> Unit) {
    val c = LocalColoresApp.current
    Box(
        modifier = Modifier
            .size(38.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(c.sobreCabecera.copy(alpha = if (activo) 0.18f else 0.08f))
            .clickable(enabled = activo, onClick = alPulsar)
            .semantics { contentDescription = descripcion },
        contentAlignment = Alignment.Center,
    ) {
        contenido()
    }
}

/** Abre los ajustes de voz del teléfono (o los ajustes generales si ese acceso no existe). */
private fun abrirAjustesDeVoz(contexto: android.content.Context) {
    try {
        contexto.startActivity(Intent("com.android.settings.TTS_SETTINGS"))
    } catch (e: Exception) {
        try {
            contexto.startActivity(Intent(android.provider.Settings.ACTION_SETTINGS))
        } catch (e2: Exception) {
            // Sin acceso a ajustes: no hace nada.
        }
    }
}

/** Botón de audio bajo los botones de tamaño de letra: leer / detener el concepto. */
@Composable
private fun FilaAudio(lector: LectorVoz, concepto: Concepto) {
    val c = LocalColoresApp.current
    val contexto = LocalContext.current
    var avisoSinVoz by remember { mutableStateOf(false) }

    // Al salir del concepto, la lectura se detiene.
    DisposableEffect(concepto.numero) {
        onDispose { lector.detener() }
    }

    val iniciando = lector.estado == EstadoVoz.INICIANDO
    val leyendo = lector.leyendo
    Row(
        modifier = Modifier.fillMaxWidth().padding(end = 12.dp, top = 8.dp),
        horizontalArrangement = Arrangement.End,
    ) {
        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(if (iniciando) MaterialTheme.colorScheme.surfaceVariant else c.aplicacionAcento)
                .clickable(enabled = !iniciando) {
                    if (leyendo) {
                        lector.detener()
                    } else if (lector.estado == EstadoVoz.SIN_VOZ) {
                        avisoSinVoz = true
                    } else {
                        lector.leer(textoParaLeer(concepto))
                    }
                }
                .semantics {
                    contentDescription = if (leyendo) "Detener la lectura" else "Escuchar este concepto"
                },
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = if (leyendo) iconoDetener else iconoAudio,
                contentDescription = null,
                modifier = Modifier.size(22.dp),
                tint = if (iniciando) MaterialTheme.colorScheme.outline.copy(alpha = 0.6f) else Color.White,
            )
        }
    }

    if (avisoSinVoz) {
        AlertDialog(
            onDismissRequest = { avisoSinVoz = false },
            confirmButton = {
                TextButton(onClick = {
                    avisoSinVoz = false
                    abrirAjustesDeVoz(contexto)
                }) { Text("Abrir ajustes") }
            },
            dismissButton = {
                TextButton(onClick = { avisoSinVoz = false }) { Text("Cerrar") }
            },
            title = { Text("Voz en español no disponible") },
            text = {
                Text(
                    "Este teléfono no tiene instalada una voz en español para leer el texto. " +
                        "Puedes instalarla en Ajustes > Idioma > Salida de texto a voz (requiere conexión solo para descargarla una vez).",
                )
            },
        )
    }
}

/** Abre un enlace en el navegador del teléfono (no requiere permisos; el archivo sí necesita conexión). */
private fun abrirEnlace(contexto: android.content.Context, url: String) {
    try {
        contexto.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
    } catch (e: Exception) {
        // Sin navegador disponible: no hace nada.
    }
}

/** Logo de assets/logo.png sobre un recuadro blanco. Si el archivo no existe, no muestra nada. */
@Composable
private fun LogoBandec(ancho: Dp, modifier: Modifier = Modifier) {
    val contexto = LocalContext.current
    val imagen = remember {
        try {
            contexto.assets.open("logo.png").use { BitmapFactory.decodeStream(it) }?.asImageBitmap()
        } catch (e: Exception) {
            null
        }
    }
    if (imagen != null) {
        val forma = RoundedCornerShape(20.dp)
        Box(
            modifier = modifier
                .width(ancho)
                .shadow(6.dp, forma)
                .clip(forma)
                .background(Color.White)
                .padding(horizontal = 18.dp, vertical = 14.dp),
        ) {
            Image(
                bitmap = imagen,
                contentDescription = "Bandec, Banco de Crédito y Comercio",
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(imagen.width.toFloat() / imagen.height.toFloat()),
                contentScale = ContentScale.Fit,
            )
        }
    }
}

/** Imagen de assets (p. ej. logos/biofin.png). Si el archivo no existe, no muestra nada. */
@Composable
private fun LogoAsset(archivo: String, descripcion: String, modifier: Modifier = Modifier) {
    val contexto = LocalContext.current
    val imagen = remember(archivo) {
        try {
            contexto.assets.open(archivo).use { BitmapFactory.decodeStream(it) }?.asImageBitmap()
        } catch (e: Exception) {
            null
        }
    }
    if (imagen != null) {
        Image(
            bitmap = imagen,
            contentDescription = descripcion,
            modifier = modifier,
            contentScale = ContentScale.Fit,
        )
    }
}

@Composable
private fun BotonPrincipal(texto: String, alPulsar: () -> Unit) {
    Button(
        onClick = alPulsar,
        modifier = Modifier.fillMaxWidth().widthIn(max = 320.dp).height(54.dp),
        shape = RoundedCornerShape(16.dp),
    ) {
        Text(text = texto, style = MaterialTheme.typography.titleMedium)
    }
}

@Composable
private fun BotonSecundario(texto: String, alPulsar: () -> Unit) {
    val c = LocalColoresApp.current
    OutlinedButton(
        onClick = alPulsar,
        modifier = Modifier.fillMaxWidth().widthIn(max = 320.dp).height(54.dp),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary),
        colors = ButtonDefaults.outlinedButtonColors(containerColor = c.tarjeta),
    ) {
        Text(text = texto, style = MaterialTheme.typography.titleMedium)
    }
}

/** Contenedor de pantalla completa con el fondo de patrones, contenido centrado y con scroll si hace falta. */
@Composable
private fun PantallaCentrada(contenido: @Composable () -> Unit) {
    Box(modifier = Modifier.fillMaxSize()) {
        FondoNatural()
        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val alto = maxHeight
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .heightIn(min = alto)
                    .padding(horizontal = 28.dp, vertical = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                contenido()
            }
        }
    }
}

/* ------------------------------------------------------------------ */
/* 1. Portada                                                          */
/* ------------------------------------------------------------------ */

@Composable
fun PantallaPortada(intro: Intro, alEntrar: () -> Unit, alApariencia: () -> Unit) {
    PantallaCentrada {
        LogoBandec(ancho = 280.dp)
        Spacer(Modifier.height(32.dp))
        Text(
            text = intro.etiqueta,
            modifier = Modifier.fillMaxWidth(),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(12.dp))
        Text(
            text = intro.titulo,
            modifier = Modifier.fillMaxWidth(),
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.primary,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(12.dp))
        Text(
            text = intro.subtitulo,
            modifier = Modifier.fillMaxWidth(),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(40.dp))
        BotonPrincipal("Entrar", alEntrar)
        Spacer(Modifier.height(12.dp))
        BotonSecundario("Modo claro / oscuro", alApariencia)
    }
}

/* ------------------------------------------------------------------ */
/* 2. Menú principal                                                   */
/* ------------------------------------------------------------------ */

@Composable
fun PantallaMenu(
    alComoUsar: () -> Unit,
    alInformacion: () -> Unit,
    alGlosario: () -> Unit,
    alConclusiones: () -> Unit,
    alCreditos: () -> Unit,
    alSalir: () -> Unit,
) {
    PantallaCentrada {
        LogoBandec(ancho = 200.dp)
        Spacer(Modifier.height(28.dp))
        Text(
            text = "Menú principal",
            modifier = Modifier.fillMaxWidth(),
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.primary,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(24.dp))
        BotonPrincipal("Cómo usar esta App", alComoUsar)
        Spacer(Modifier.height(12.dp))
        BotonPrincipal("Información General", alInformacion)
        Spacer(Modifier.height(12.dp))
        BotonPrincipal("Glosario", alGlosario)
        Spacer(Modifier.height(12.dp))
        BotonPrincipal("Conclusiones", alConclusiones)
        Spacer(Modifier.height(12.dp))
        BotonPrincipal("Créditos", alCreditos)
        Spacer(Modifier.height(12.dp))
        BotonSecundario("Salir", alSalir)
    }
}

/* ------------------------------------------------------------------ */
/* 3. Pantallas de texto: Cómo usar, Información general, Créditos     */
/* ------------------------------------------------------------------ */

@Composable
fun PantallaTarjetas(titulo: String, tarjetas: List<Tarjeta>, alVolver: () -> Unit) {
    Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        Cabecera(titulo = titulo, subtitulo = null, alVolver = alVolver)
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            tarjetas.forEach { TarjetaTexto(it) }
        }
    }
}

@Composable
private fun TarjetaTexto(tarjeta: Tarjeta) {
    val c = LocalColoresApp.current
    val forma = RoundedCornerShape(16.dp)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(forma)
            .background(c.tarjeta)
            .border(1.dp, c.borde, forma)
            .padding(20.dp),
    ) {
        Box(
            modifier = Modifier
                .width(28.dp)
                .height(3.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(c.aplicacionAcento),
        )
        Spacer(Modifier.height(10.dp))
        Text(
            text = tarjeta.titulo,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary,
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = tarjeta.texto,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}

@Composable
fun PantallaCreditos(creditos: List<Credito>, alVolver: () -> Unit) {
    val c = LocalColoresApp.current
    val forma = RoundedCornerShape(16.dp)
    Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        Cabecera(titulo = "Créditos", subtitulo = "Colectivo de autores", alVolver = alVolver)
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(forma)
                    .background(c.tarjeta)
                    .border(1.dp, c.borde, forma)
                    .padding(horizontal = 20.dp, vertical = 8.dp),
            ) {
                creditos.forEachIndexed { indice, credito ->
                    if (indice > 0) {
                        Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(c.borde))
                    }
                    Column(modifier = Modifier.padding(vertical = 14.dp)) {
                        Text(
                            text = credito.nombre,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        Spacer(Modifier.height(2.dp))
                        Text(
                            text = credito.afiliacion,
                            fontSize = 13.sp,
                            lineHeight = 18.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }

            Spacer(Modifier.height(16.dp))
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(forma)
                    .background(Color.White)
                    .border(1.dp, c.borde, forma)
                    .padding(horizontal = 16.dp, vertical = 20.dp),
                verticalArrangement = Arrangement.spacedBy(22.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    LogoAsset("logos/bandec.png", "Bandec, Banco de Crédito y Comercio", Modifier.weight(1f).heightIn(max = 64.dp))
                    LogoAsset("logos/biofin.png", "BIOFIN, Finanzas para la Biodiversidad", Modifier.weight(1f).heightIn(max = 64.dp))
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    LogoAsset("logos/bcc.png", "Banco Central de Cuba", Modifier.weight(1f).heightIn(max = 64.dp))
                    LogoAsset("logos/icb.png", "Instituto Cubano de Biodiversidad", Modifier.weight(1f).heightIn(max = 64.dp))
                }
            }
            Spacer(Modifier.height(8.dp))
        }
    }
}

/* ------------------------------------------------------------------ */
/* 3b. Conclusiones y descargas                                        */
/* ------------------------------------------------------------------ */

@Composable
fun PantallaConclusiones(conclusiones: Conclusiones, alVolver: () -> Unit) {
    Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        Cabecera(titulo = conclusiones.titulo, subtitulo = conclusiones.subtitulo, alVolver = alVolver)
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            conclusiones.tarjetas.forEach { TarjetaTexto(it) }
            TarjetaDescargas(conclusiones)
        }
    }
}

@Composable
private fun TarjetaDescargas(conclusiones: Conclusiones) {
    val c = LocalColoresApp.current
    val contexto = LocalContext.current
    val forma = RoundedCornerShape(16.dp)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(forma)
            .background(c.tarjeta)
            .border(1.dp, c.borde, forma)
            .padding(20.dp),
    ) {
        Box(
            modifier = Modifier
                .width(28.dp)
                .height(3.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(c.aplicacionAcento),
        )
        Spacer(Modifier.height(10.dp))
        Text(
            text = conclusiones.tituloDescargas,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary,
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = conclusiones.notaDescargas,
            fontSize = 13.sp,
            lineHeight = 18.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        conclusiones.descargas.forEach { descarga ->
            Spacer(Modifier.height(14.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = descarga.titulo,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        text = descarga.detalle,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Spacer(Modifier.width(12.dp))
                // El enlace va en el ícono: no se muestra la dirección.
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(c.aplicacionAcento)
                        .clickable { abrirEnlace(contexto, descarga.url) }
                        .semantics { contentDescription = "Descargar ${descarga.titulo} (PDF)" },
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = iconoDescarga,
                        contentDescription = null,
                        modifier = Modifier.size(26.dp),
                        tint = Color.White,
                    )
                }
            }
        }
    }
}

/* ------------------------------------------------------------------ */
/* 4. Glosario: lista agrupada por bloque, con búsqueda                */
/* ------------------------------------------------------------------ */

private fun plural(n: Int, singular: String, plural: String): String = if (n == 1) singular else plural

@Composable
fun PantallaLista(
    glosario: Glosario,
    consulta: String,
    alCambiarConsulta: (String) -> Unit,
    estadoLista: LazyListState,
    alVolver: () -> Unit,
    alAbrir: (Int) -> Unit,
) {
    val c = LocalColoresApp.current
    val foco = LocalFocusManager.current

    // Texto visible de cada concepto (nombre + definición + aplicación), sin tildes ni mayúsculas.
    val textos = remember(glosario) {
        glosario.conceptos.associate {
            it.numero to plano(it.nombre + "\n" + sinMarcas(it.definicion) + "\n" + sinMarcas(it.aplicacion))
        }
    }
    val q = plano(consulta.trim())
    val conteos = remember(glosario, q) {
        glosario.conceptos.associate { it.numero to contar(textos[it.numero] ?: "", q) }
    }
    val grupos = remember(glosario, q) {
        glosario.bloques
            .map { b ->
                b to glosario.conceptos.filter { k ->
                    k.bloque == b.id && (q.isEmpty() || (conteos[k.numero] ?: 0) > 0)
                }
            }
            .filter { it.second.isNotEmpty() }
    }
    val totalApariciones = conteos.values.sum()
    val conceptosConAparicion = conteos.values.count { it > 0 }

    val botonBorrar: (@Composable () -> Unit)? = if (consulta.isNotEmpty()) {
        {
            IconButton(onClick = { alCambiarConsulta("") }) {
                Icon(Icons.Filled.Close, contentDescription = "Borrar búsqueda")
            }
        }
    } else {
        null
    }

    Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        Cabecera(
            titulo = "Glosario",
            subtitulo = "${glosario.conceptos.size} conceptos en ${glosario.bloques.size} bloques",
            alVolver = alVolver,
        )

        OutlinedTextField(
            value = consulta,
            onValueChange = alCambiarConsulta,
            modifier = Modifier.fillMaxWidth().padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 4.dp),
            singleLine = true,
            shape = RoundedCornerShape(28.dp),
            placeholder = { Text("Buscar en el glosario") },
            leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
            trailingIcon = botonBorrar,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(onSearch = { foco.clearFocus() }),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = c.tarjeta,
                unfocusedContainerColor = c.tarjeta,
            ),
        )

        if (q.isNotEmpty()) {
            Text(
                text = if (totalApariciones == 0) {
                    "Sin resultados"
                } else {
                    "$totalApariciones ${plural(totalApariciones, "aparición", "apariciones")} " +
                        "en $conceptosConAparicion ${plural(conceptosConAparicion, "concepto", "conceptos")}"
                },
                modifier = Modifier.padding(horizontal = 28.dp).padding(bottom = 4.dp),
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        } else {
            Spacer(Modifier.height(8.dp))
        }

        LazyColumn(
            state = estadoLista,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 0.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            grupos.forEach { (bloque, lista) ->
                item(key = "bloque-${bloque.id}") { EncabezadoBloque(bloque) }
                items(lista, key = { it.numero }) { concepto ->
                    FilaConcepto(concepto, q, conteos[concepto.numero] ?: 0) {
                        foco.clearFocus()
                        alAbrir(concepto.numero)
                    }
                }
            }
        }
    }
}

@Composable
private fun EncabezadoBloque(bloque: Bloque) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(top = 16.dp, bottom = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(CircleShape)
                .background(ColoresBloque.de(bloque.id)),
        )
        Spacer(Modifier.width(10.dp))
        Column {
            Text(
                text = "BLOQUE ${romano(bloque.id)}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = bloque.titulo,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground,
            )
        }
    }
}

@Composable
private fun FilaConcepto(concepto: Concepto, q: String, conteo: Int, alPulsar: () -> Unit) {
    val c = LocalColoresApp.current
    val forma = RoundedCornerShape(14.dp)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
            .clip(forma)
            .background(c.tarjeta)
            .border(1.dp, c.borde, forma)
            .clickable(onClick = alPulsar),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .width(5.dp)
                .fillMaxHeight()
                .background(ColoresBloque.de(concepto.bloque)),
        )
        Text(
            text = concepto.numero.toString(),
            modifier = Modifier.width(44.dp),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        Column(modifier = Modifier.weight(1f).padding(vertical = 14.dp)) {
            TextoRico(
                texto = concepto.nombre,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                resaltado = q,
            )
            if (q.isNotEmpty()) {
                Spacer(Modifier.height(2.dp))
                Text(
                    text = "$conteo ${plural(conteo, "aparición", "apariciones")}",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            modifier = Modifier.padding(horizontal = 8.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

/* ------------------------------------------------------------------ */
/* 5. Detalle de un concepto                                           */
/* ------------------------------------------------------------------ */

@Composable
fun PantallaDetalle(
    concepto: Concepto,
    bloque: Bloque?,
    total: Int,
    q: String,
    pasoTexto: Int,
    alCambiarPasoTexto: (Int) -> Unit,
    lector: LectorVoz,
    alVolver: () -> Unit,
) {
    val c = LocalColoresApp.current
    val escala = escalasTexto[pasoTexto.coerceIn(0, escalasTexto.lastIndex)]
    val apariciones = remember(concepto, q) {
        contar(
            plano(concepto.nombre + "\n" + sinMarcas(concepto.definicion) + "\n" + sinMarcas(concepto.aplicacion)),
            q,
        )
    }
    Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        Cabecera(
            titulo = "Concepto ${concepto.numero} de $total",
            subtitulo = null,
            alVolver = alVolver,
            acciones = {
                BotonCabecera(
                    descripcion = "Disminuir tamaño de letra",
                    activo = pasoTexto > 0,
                    alPulsar = { alCambiarPasoTexto(pasoTexto - 1) },
                ) {
                    Text(
                        text = "A−",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = c.sobreCabecera.copy(alpha = if (pasoTexto > 0) 1f else 0.4f),
                    )
                }
                BotonCabecera(
                    descripcion = "Aumentar tamaño de letra",
                    activo = pasoTexto < escalasTexto.lastIndex,
                    alPulsar = { alCambiarPasoTexto(pasoTexto + 1) },
                ) {
                    Text(
                        text = "A+",
                        fontSize = 19.sp,
                        fontWeight = FontWeight.Bold,
                        color = c.sobreCabecera.copy(alpha = if (pasoTexto < escalasTexto.lastIndex) 1f else 0.4f),
                    )
                }
            },
        )

        // Botón de lectura en voz alta (motor de voz del teléfono).
        FilaAudio(lector = lector, concepto = concepto)

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 20.dp),
        ) {
            if (q.isNotEmpty()) {
                Text(
                    text = "Búsqueda resaltada · $apariciones ${plural(apariciones, "aparición", "apariciones")} en este concepto",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.height(12.dp))
            }
            if (bloque != null) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(ColoresBloque.de(bloque.id)),
                    )
                    Spacer(Modifier.width(10.dp))
                    Text(
                        text = "BLOQUE ${romano(bloque.id)} · ${bloque.titulo.uppercase()}",
                        modifier = Modifier.weight(1f),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Spacer(Modifier.height(14.dp))
            }

            TextoRico(
                texto = concepto.nombre,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onBackground,
                resaltado = q,
                escala = escala,
            )

            Spacer(Modifier.height(22.dp))
            Text(
                text = "DEFINICIÓN",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary,
            )
            Spacer(Modifier.height(8.dp))
            TextoRico(
                texto = concepto.definicion,
                color = MaterialTheme.colorScheme.onBackground,
                resaltado = q,
                escala = escala,
            )

            Spacer(Modifier.height(24.dp))
            val forma = RoundedCornerShape(16.dp)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(IntrinsicSize.Min)
                    .clip(forma)
                    .background(c.aplicacionFondo),
            ) {
                Box(
                    modifier = Modifier
                        .width(5.dp)
                        .fillMaxHeight()
                        .background(c.aplicacionAcento),
                )
                Column(modifier = Modifier.weight(1f).padding(18.dp)) {
                    Text(
                        text = "APLICACIÓN EN EL ANÁLISIS DE RIESGOS",
                        style = MaterialTheme.typography.labelSmall,
                        color = c.aplicacionEtiqueta,
                    )
                    Spacer(Modifier.height(8.dp))
                    TextoRico(
                        texto = concepto.aplicacion,
                        color = c.aplicacionTexto,
                        resaltado = q,
                        escala = escala,
                    )
                }
            }
            Spacer(Modifier.height(8.dp))
        }
    }
}
