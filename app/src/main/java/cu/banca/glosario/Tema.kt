package cu.banca.glosario

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/*
 * PALETA — todo el color de la app está en este archivo.
 * Granate institucional (tomado del logo de Bandec) + verdes naturales (banca verde).
 */

@Immutable
data class ColoresApp(
    val oscuro: Boolean,
    val cabecera: Color,
    val cabeceraFin: Color,
    val sobreCabecera: Color,
    val sobreCabeceraSuave: Color,
    val tarjeta: Color,
    val borde: Color,
    val aplicacionFondo: Color,
    val aplicacionTexto: Color,
    val aplicacionEtiqueta: Color,
    val aplicacionAcento: Color,
    val resaltado: Color,
    val sobreResaltado: Color,
    // Fondo de patrones de la portada y el menú
    val fondoNatural: Color,
    val hojas: List<Color>,
    val nervio: Color,
)

val LocalColoresApp = staticCompositionLocalOf<ColoresApp> { error("TemaGlosario no está presente") }

private val ColoresClaro = ColoresApp(
    oscuro = false,
    cabecera = Color(0xFF88182A),
    cabeceraFin = Color(0xFF5E1020),
    sobreCabecera = Color(0xFFFFFFFF),
    sobreCabeceraSuave = Color(0xFFF2D6DC),
    tarjeta = Color(0xFFFFFFFF),
    borde = Color(0xFFE0E5DF),
    aplicacionFondo = Color(0xFFE4F3EA),
    aplicacionTexto = Color(0xFF15281E),
    aplicacionEtiqueta = Color(0xFF14663C),
    aplicacionAcento = Color(0xFF2E9A5F),
    resaltado = Color(0xFFFFE082),
    sobreResaltado = Color(0xFF1A1A1A),
    fondoNatural = Color(0xFFE9F3E4),
    hojas = listOf(Color(0xFFDDEBD3), Color(0xFFD0E5C4), Color(0xFFC4DEB7), Color(0xFFD7E8CC)),
    nervio = Color(0xFFE6F1DF),
)

private val ColoresOscuro = ColoresApp(
    oscuro = true,
    cabecera = Color(0xFF4A1220),
    cabeceraFin = Color(0xFF330B15),
    sobreCabecera = Color(0xFFFFFFFF),
    sobreCabeceraSuave = Color(0xFFE8C3CB),
    tarjeta = Color(0xFF18231D),
    borde = Color(0xFF2B3B32),
    aplicacionFondo = Color(0xFF14291E),
    aplicacionTexto = Color(0xFFDCEEE3),
    aplicacionEtiqueta = Color(0xFF6FD4A0),
    aplicacionAcento = Color(0xFF4CC38A),
    resaltado = Color(0xFF8A6D00),
    sobreResaltado = Color(0xFFFFFFFF),
    fondoNatural = Color(0xFF0F1D15),
    hojas = listOf(Color(0xFF15291C), Color(0xFF1A3322), Color(0xFF1F3D29), Color(0xFF173021)),
    nervio = Color(0xFF244A33),
)

private val EsquemaClaro = lightColorScheme(
    primary = Color(0xFF88182A),
    onPrimary = Color(0xFFFFFFFF),
    background = Color(0xFFF5F7F3),
    onBackground = Color(0xFF18221C),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF18221C),
    surfaceVariant = Color(0xFFE6ECE4),
    onSurfaceVariant = Color(0xFF4F5C53),
    outline = Color(0xFF8A988E),
    outlineVariant = Color(0xFFE0E5DF),
)

private val EsquemaOscuro = darkColorScheme(
    primary = Color(0xFFF2A9B6),
    onPrimary = Color(0xFF4A0E1A),
    background = Color(0xFF0D1611),
    onBackground = Color(0xFFE8EFE9),
    surface = Color(0xFF18231D),
    onSurface = Color(0xFFE8EFE9),
    surfaceVariant = Color(0xFF212F27),
    onSurfaceVariant = Color(0xFFABBAB0),
    outline = Color(0xFF65786B),
    outlineVariant = Color(0xFF2B3B32),
)

/** Un color por bloque temático (solo decorativo: nunca se usa para texto). */
object ColoresBloque {
    private val lista = listOf(
        Color(0xFF2E8B57), // I   Fundamentos
        Color(0xFFE07B39), // II  Clima y desastres
        Color(0xFF1E88A8), // III Biodiversidad
        Color(0xFF8E5BA8), // IV  Riesgos
        Color(0xFFC9A227), // V   Finanzas verdes
    )

    fun de(bloque: Int): Color = lista[(bloque - 1).coerceIn(0, lista.lastIndex)]
}

private val Tipografia = Typography(
    headlineMedium = TextStyle(
        fontFamily = FontFamily.Serif, fontWeight = FontWeight.Bold,
        fontSize = 28.sp, lineHeight = 35.sp,
    ),
    titleLarge = TextStyle(
        fontFamily = FontFamily.Serif, fontWeight = FontWeight.Bold,
        fontSize = 25.sp, lineHeight = 32.sp,
    ),
    titleMedium = TextStyle(
        fontWeight = FontWeight.SemiBold, fontSize = 17.sp, lineHeight = 23.sp,
    ),
    bodyLarge = TextStyle(fontSize = 16.sp, lineHeight = 26.sp),
    bodyMedium = TextStyle(fontSize = 15.sp, lineHeight = 23.sp),
    labelSmall = TextStyle(
        fontWeight = FontWeight.SemiBold, fontSize = 11.sp, lineHeight = 15.sp,
        letterSpacing = 1.2.sp,
    ),
)

/** Modos de apariencia: 0 = según el sistema, 1 = claro, 2 = oscuro. */
@Composable
fun TemaGlosario(modo: Int, content: @Composable () -> Unit) {
    val oscuro = when (modo) {
        1 -> false
        2 -> true
        else -> isSystemInDarkTheme()
    }
    val colores = if (oscuro) ColoresOscuro else ColoresClaro
    val esquema = if (oscuro) EsquemaOscuro else EsquemaClaro

    CompositionLocalProvider(LocalColoresApp provides colores) {
        MaterialTheme(colorScheme = esquema, typography = Tipografia, content = content)
    }
}
