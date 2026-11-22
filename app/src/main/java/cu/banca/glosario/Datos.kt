package cu.banca.glosario

import android.content.Context
import org.json.JSONObject
import java.text.Normalizer

data class Tarjeta(val titulo: String, val texto: String)

data class Credito(val nombre: String, val afiliacion: String)

data class Intro(
    val etiqueta: String,
    val titulo: String,
    val subtitulo: String,
    val general: List<Tarjeta>,
    val comoUsar: Tarjeta,
    val creditos: List<Credito>,
)

data class Bloque(val id: Int, val titulo: String)

data class Concepto(
    val numero: Int,
    val bloque: Int,
    val nombre: String,
    val definicion: String,
    val aplicacion: String,
)

data class Descarga(val titulo: String, val detalle: String, val url: String)

data class Conclusiones(
    val titulo: String,
    val subtitulo: String,
    val tarjetas: List<Tarjeta>,
    val tituloDescargas: String,
    val notaDescargas: String,
    val descargas: List<Descarga>,
)

data class Glosario(
    val intro: Intro,
    val bloques: List<Bloque>,
    val conceptos: List<Concepto>,
    val conclusiones: Conclusiones,
)

/** Lee assets/glosario.json (todo el contenido de la app vive en ese archivo). */
fun cargarGlosario(context: Context): Glosario {
    val texto = context.assets.open("glosario.json").bufferedReader(Charsets.UTF_8).use { it.readText() }
    val raiz = JSONObject(texto)

    val i = raiz.getJSONObject("intro")
    val generalJson = i.getJSONArray("general")
    val general = List(generalJson.length()) { k ->
        val t = generalJson.getJSONObject(k)
        Tarjeta(t.getString("titulo"), t.getString("texto"))
    }
    val usoJson = i.getJSONObject("comoUsar")
    val creditosJson = i.getJSONArray("creditos")
    val creditos = List(creditosJson.length()) { k ->
        val t = creditosJson.getJSONObject(k)
        Credito(t.getString("nombre"), t.getString("afiliacion"))
    }
    val intro = Intro(
        etiqueta = i.getString("etiqueta"),
        titulo = i.getString("titulo"),
        subtitulo = i.getString("subtitulo"),
        general = general,
        comoUsar = Tarjeta(usoJson.getString("titulo"), usoJson.getString("texto")),
        creditos = creditos,
    )

    val bloquesJson = raiz.getJSONArray("bloques")
    val bloques = List(bloquesJson.length()) { k ->
        val b = bloquesJson.getJSONObject(k)
        Bloque(b.getInt("id"), b.getString("titulo"))
    }

    val conceptosJson = raiz.getJSONArray("conceptos")
    val conceptos = List(conceptosJson.length()) { k ->
        val c = conceptosJson.getJSONObject(k)
        Concepto(
            numero = c.getInt("numero"),
            bloque = c.getInt("bloque"),
            nombre = c.getString("nombre"),
            definicion = c.getString("definicion"),
            aplicacion = c.getString("aplicacion"),
        )
    }

    val k = raiz.getJSONObject("conclusiones")
    val tarjetasJson = k.getJSONArray("tarjetas")
    val tarjetasConclusiones = List(tarjetasJson.length()) { n ->
        val t = tarjetasJson.getJSONObject(n)
        Tarjeta(t.getString("titulo"), t.getString("texto"))
    }
    val d = k.getJSONObject("descargas")
    val itemsJson = d.getJSONArray("items")
    val descargas = List(itemsJson.length()) { n ->
        val t = itemsJson.getJSONObject(n)
        Descarga(t.getString("titulo"), t.getString("detalle"), t.getString("url"))
    }
    val conclusiones = Conclusiones(
        titulo = k.getString("titulo"),
        subtitulo = k.getString("subtitulo"),
        tarjetas = tarjetasConclusiones,
        tituloDescargas = d.getString("titulo"),
        notaDescargas = d.getString("nota"),
        descargas = descargas,
    )

    return Glosario(intro, bloques, conceptos, conclusiones)
}

/**
 * Minúsculas y sin tildes, carácter por carácter (la longitud NO cambia), para que
 * "ecosistemico" encuentre "ecosistémico" y los índices sirvan para resaltar el texto original.
 */
fun plano(s: String): String {
    val sb = StringBuilder(s.length)
    for (ch in s) {
        val descompuesto = Normalizer.normalize(ch.toString(), Normalizer.Form.NFD)
        sb.append((descompuesto.firstOrNull() ?: ch).lowercaseChar())
    }
    return sb.toString()
}

/** Número de apariciones (sin solaparse) de [q] en [textoPlano]; ambos ya pasados por [plano]. */
fun contar(textoPlano: String, q: String): Int {
    if (q.isEmpty()) return 0
    var n = 0
    var i = textoPlano.indexOf(q)
    while (i >= 0) {
        n++
        i = textoPlano.indexOf(q, i + q.length)
    }
    return n
}

private val romanos = listOf("I", "II", "III", "IV", "V", "VI", "VII", "VIII", "IX", "X")

fun romano(n: Int): String = romanos.getOrElse(n - 1) { n.toString() }
