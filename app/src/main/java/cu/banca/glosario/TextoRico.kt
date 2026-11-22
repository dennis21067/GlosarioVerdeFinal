package cu.banca.glosario

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle

/** Interpreta **negrita** y *cursiva* (nombres científicos, términos en inglés). */
fun aAnotado(s: String): AnnotatedString = buildAnnotatedString {
    var i = 0
    while (i < s.length) {
        if (s.startsWith("**", i)) {
            val fin = s.indexOf("**", i + 2)
            if (fin > 0) {
                withStyle(SpanStyle(fontWeight = FontWeight.SemiBold)) { append(s.substring(i + 2, fin)) }
                i = fin + 2
                continue
            }
        } else if (s[i] == '*') {
            val fin = s.indexOf('*', i + 1)
            if (fin > 0) {
                withStyle(SpanStyle(fontStyle = FontStyle.Italic)) { append(s.substring(i + 1, fin)) }
                i = fin + 1
                continue
            }
        }
        append(s[i])
        i++
    }
}

/** Texto tal como se ve en pantalla (sin los marcadores * y **). */
fun sinMarcas(s: String): String = aAnotado(s).text

/** Marca todas las apariciones de [q] (ya normalizado con [plano]) en [base]. */
fun resaltar(base: AnnotatedString, q: String, fondo: Color, frente: Color): AnnotatedString {
    if (q.isEmpty()) return base
    val p = plano(base.text)
    return buildAnnotatedString {
        append(base)
        var i = p.indexOf(q)
        while (i >= 0) {
            addStyle(
                SpanStyle(background = fondo, color = frente, fontWeight = FontWeight.Bold),
                i,
                i + q.length,
            )
            i = p.indexOf(q, i + q.length)
        }
    }
}

@Composable
fun TextoRico(
    texto: String,
    modifier: Modifier = Modifier,
    style: TextStyle = MaterialTheme.typography.bodyLarge,
    color: Color = Color.Unspecified,
    resaltado: String = "",
    escala: Float = 1f,
) {
    val c = LocalColoresApp.current
    val anotado = remember(texto, resaltado, c) {
        resaltar(aAnotado(texto), resaltado, c.resaltado, c.sobreResaltado)
    }
    val estilo = if (escala == 1f) {
        style
    } else {
        style.copy(fontSize = style.fontSize * escala, lineHeight = style.lineHeight * escala)
    }
    Text(text = anotado, modifier = modifier, style = estilo, color = color)
}
