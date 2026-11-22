package cu.banca.glosario

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.translate

/** Hoja grande: posición (fracción de la pantalla), largo (fracción del lado mayor), giro y tono. */
private class Hoja(val x: Float, val y: Float, val largo: Float, val giro: Float, val tono: Int)

private val hojas = listOf(
    Hoja(0.00f, 0.05f, 0.44f, 28f, 0),
    Hoja(1.00f, 0.02f, 0.40f, 152f, 1),
    Hoja(0.04f, 0.40f, 0.36f, -22f, 2),
    Hoja(1.02f, 0.36f, 0.46f, 205f, 0),
    Hoja(0.00f, 0.74f, 0.50f, -52f, 1),
    Hoja(0.58f, 1.02f, 0.46f, -122f, 3),
    Hoja(0.96f, 0.80f, 0.38f, 238f, 2),
    Hoja(0.38f, 0.13f, 0.20f, 68f, 3),
    Hoja(0.62f, 0.58f, 0.24f, -158f, 3),
)

// Hoja de largo 1 apuntando hacia +x, con la base en el origen.
private val formaHoja = Path().apply {
    moveTo(0f, 0f)
    cubicTo(0.25f, -0.30f, 0.75f, -0.30f, 1f, 0f)
    cubicTo(0.75f, 0.30f, 0.25f, 0.30f, 0f, 0f)
    close()
}

private val nervio = Path().apply {
    moveTo(0.02f, 0f)
    lineTo(0.90f, 0f)
}

/** Fondo de patrones grandes de hojas en verdes naturales claros (oscuros en modo oscuro). */
@Composable
fun FondoNatural(modifier: Modifier = Modifier) {
    val c = LocalColoresApp.current
    Canvas(modifier = modifier.fillMaxSize()) {
        drawRect(color = c.fondoNatural)
        val lado = size.maxDimension
        hojas.forEach { h ->
            translate(left = size.width * h.x, top = size.height * h.y) {
                rotate(degrees = h.giro, pivot = Offset.Zero) {
                    scale(scale = lado * h.largo, pivot = Offset.Zero) {
                        drawPath(path = formaHoja, color = c.hojas[h.tono % c.hojas.size])
                        drawPath(path = nervio, color = c.nervio, style = Stroke(width = 0.010f))
                    }
                }
            }
        }
    }
}
