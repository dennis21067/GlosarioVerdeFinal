package cu.banca.glosario

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import java.util.Locale

/** Estado del motor de voz del teléfono. */
enum class EstadoVoz { INICIANDO, LISTA, SIN_VOZ }

/**
 * Lectura en voz alta con el motor de voz (TextToSpeech) del propio teléfono.
 * No usa internet ni permisos: depende de que el equipo tenga una voz en español instalada.
 */
class LectorVoz(contexto: Context) {

    var estado by mutableStateOf(EstadoVoz.INICIANDO)
        private set

    var leyendo by mutableStateOf(false)
        private set

    private var motor: TextToSpeech? = null
    private var contador = 0
    private var ultimoId = ""

    init {
        motor = TextToSpeech(contexto.applicationContext) { resultado ->
            val m = motor
            if (resultado != TextToSpeech.SUCCESS || m == null) {
                estado = EstadoVoz.SIN_VOZ
                return@TextToSpeech
            }
            var disponible = false
            for (idioma in IDIOMAS) {
                if (m.setLanguage(idioma) >= TextToSpeech.LANG_AVAILABLE) {
                    disponible = true
                    break
                }
            }
            if (disponible) {
                m.setSpeechRate(TASA_DE_HABLA)
                m.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                    override fun onStart(utteranceId: String?) {}

                    override fun onDone(utteranceId: String?) {
                        if (utteranceId == ultimoId) leyendo = false
                    }

                    @Deprecated("Deprecated in Java")
                    override fun onError(utteranceId: String?) {
                        leyendo = false
                    }
                })
                estado = EstadoVoz.LISTA
            } else {
                estado = EstadoVoz.SIN_VOZ
            }
        }
    }

    /** Lee [texto] (corta cualquier lectura anterior). */
    fun leer(texto: String) {
        val m = motor ?: return
        if (estado != EstadoVoz.LISTA) return
        val trozos = partir(prepararParaVoz(texto), maximoPorTrozo())
        if (trozos.isEmpty()) return
        contador++
        val base = "lec$contador"
        ultimoId = "$base-${trozos.lastIndex}"
        leyendo = true
        trozos.forEachIndexed { i, trozo ->
            val modo = if (i == 0) TextToSpeech.QUEUE_FLUSH else TextToSpeech.QUEUE_ADD
            val r = m.speak(trozo, modo, null, "$base-$i")
            if (r == TextToSpeech.ERROR) {
                leyendo = false
                return
            }
        }
    }

    fun detener() {
        motor?.stop()
        leyendo = false
    }

    fun liberar() {
        motor?.stop()
        motor?.shutdown()
        motor = null
        leyendo = false
    }

    private fun maximoPorTrozo(): Int =
        minOf(TextToSpeech.getMaxSpeechInputLength(), 1500)

    private companion object {
        val IDIOMAS = listOf(Locale("es", "CU"), Locale("es", "MX"), Locale("es", "ES"), Locale("es"))
        const val TASA_DE_HABLA = 0.95f
    }
}

/** Texto que se narra para un concepto: número, nombre, definición y aplicación (sin marcas de formato). */
fun textoParaLeer(c: Concepto): String =
    "Concepto ${c.numero}. ${sinMarcas(c.nombre)}. ${sinMarcas(c.definicion)} " +
        "Aplicación en el análisis de riesgos. ${sinMarcas(c.aplicacion)}"

/** Cómo debe sonar cada sigla (el motor suele leerlas mal o deletrearlas al azar). */
private val pronunciacion = mapOf(
    "PNUD" to "P N U D",
    "BIOFIN" to "Biofín",
    "ODS" to "O D S",
    "OD" to "O D S",
    "SESP" to "S E S P",
    "SES" to "S E S",
    "MAHRH" to "M A H R H",
    "CITES" to "Cites",
    "ASG" to "A S G",
    "ESG" to "E S G",
    "UICN" to "U I C N",
    "GEI" to "G E I",
    "SNAP" to "S N A P",
    "EIA" to "E I A",
    "IPCC" to "I P C C",
    "ORSA" to "Orsa",
    "COP15" to "COP 15",
    "SARAS" to "Saras",
)

private val regexSiglas = Regex("\\b(" + pronunciacion.keys.joinToString("|") + ")\\b")

/** Ajustes para que la voz suene natural: siglas, cifras, porcentajes y guiones. */
fun prepararParaVoz(texto: String): String {
    var t = texto.replace(regexSiglas) { pronunciacion.getValue(it.value) }
    t = t.replace("30x30", "treinta por treinta")
    t = t.replace(Regex("(\\d)/(\\d)"), "\$1 de \$2")
    t = t.replace(Regex("(\\d)\\s?%"), "\$1 por ciento")
    t = t.replace(Regex("\\s*[—–]\\s*"), ", ")
    t = t.replace(Regex("\\s+-\\s*|\\s*-\\s+"), ", ")
    return t
}

/** Divide el texto por oraciones en trozos que no superen [max] caracteres. */
private fun partir(texto: String, max: Int): List<String> {
    val oraciones = texto.split(Regex("(?<=[.;:!?])\\s+")).map { it.trim() }.filter { it.isNotEmpty() }
    val salida = mutableListOf<String>()
    val actual = StringBuilder()
    for (oracion in oraciones) {
        var resto = oracion
        while (resto.length > max) {
            var corte = resto.lastIndexOf(' ', max)
            if (corte <= 0) corte = max
            if (actual.isNotEmpty()) {
                salida.add(actual.toString())
                actual.setLength(0)
            }
            salida.add(resto.substring(0, corte).trim())
            resto = resto.substring(corte).trim()
        }
        if (actual.isNotEmpty() && actual.length + resto.length + 1 > max) {
            salida.add(actual.toString())
            actual.setLength(0)
        }
        if (actual.isNotEmpty()) actual.append(' ')
        actual.append(resto)
    }
    if (actual.isNotEmpty()) salida.add(actual.toString())
    return salida
}
