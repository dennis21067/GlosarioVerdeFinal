# Glosario Banca Verde (Android 7+, sin conexión)

App de consulta con los 41 conceptos del glosario ambiental y de biodiversidad para la banca cubana.
Sin permisos, sin internet, sin servidor: todo el contenido está en `app/src/main/assets/glosario.json`.

## Compilar el APK

**Con Android Studio (lo más simple)**
1. Instalar Android Studio (versión reciente) y abrir esta carpeta (`File > Open`).
2. Aceptar cuando proponga instalar el SDK de Android 14 (API 34) y JDK 17. La primera sincronización de Gradle necesita internet.
3. `Build > Build Bundle(s) / APK(s) > Build APK(s)`.
4. El archivo queda en `app/build/outputs/apk/debug/app-debug.apk`.

**Por línea de comandos** (con JDK 17 y el SDK instalados): `./gradlew assembleDebug`

## APK firmado para distribuir (recomendado)

Un APK "debug" se instala sin problema, pero está firmado con una llave distinta en cada computadora: si más adelante
publican una versión nueva compilada en otra PC, Android no permitirá actualizar encima de la anterior.
Para evitarlo, crear una llave una sola vez y guardarla en lugar seguro:

    keytool -genkey -v -keystore glosario.jks -alias glosario -keyalg RSA -keysize 2048 -validity 10000

Crear `keystore.properties` en la raíz del proyecto (no compartirlo):

    storeFile=glosario.jks
    storePassword=SU_CLAVE
    keyAlias=glosario
    keyPassword=SU_CLAVE

Luego `./gradlew assembleRelease` → `app/build/outputs/apk/release/app-release.apk`.

## Instalar en los teléfonos

Copiar el APK por USB o mensajería y abrirlo. Android pedirá permitir "instalar apps desconocidas" para la app desde la que se abre el archivo (Archivos, WhatsApp, etc.).

## Cambiar contenido o aspecto

- **Textos del glosario / introducción / créditos:** editar `app/src/main/assets/glosario.json` (UTF-8). `*cursiva*` y `**negrita**` se interpretan. Cada concepto: `numero`, `bloque` (1-5), `nombre`, `definicion`, `aplicacion`. Subir `versionCode` y `versionName` en `app/build.gradle.kts` al publicar una versión nueva.
- **Conclusiones y descargas:** también están en `glosario.json` (bloque `conclusiones`). Los enlaces de los dos PDF están en `conclusiones.descargas.items[].url`; en la app van ocultos detrás del ícono de descarga y se abren en el navegador (requieren internet).
- **Logos de los créditos:** `app/src/main/assets/logos/` (`bandec.png`, `biofin.png`, `bcc.png`, `icb.png`). Se pueden reemplazar por versiones de mayor resolución con el mismo nombre.
- **Tamaño de letra:** los botones A− / A+ de la pantalla de concepto usan los pasos definidos en `escalasTexto` (`MainActivity.kt`); la elección queda guardada. 
- **Lectura en voz alta:** el botón de audio de la pantalla de concepto usa el motor de voz del propio teléfono (sin internet ni permisos); el código está en `Voz.kt`. Si el teléfono no tiene una voz en español, la app lo avisa y abre los ajustes de voz. Las siglas que se leen mal se corrigen en el diccionario `pronunciacion` de `Voz.kt`.
- **Logo de la portada y el menú:** reemplazar `app/src/main/assets/logo.png` (se muestra sobre un recuadro blanco, así que sirve un PNG con fondo blanco o transparente). Si se borra el archivo, simplemente no se muestra.
- **Modo claro/oscuro:** botón de la portada; la elección queda guardada en el teléfono.
- **Colores:** todo en `app/src/main/java/cu/banca/glosario/Tema.kt` (valores hexadecimales, modo claro y oscuro; granate tomado del logo de Bandec y verdes naturales para el fondo de hojas, que se dibuja en `Fondo.kt`).
- **Ícono de la app (hoja verde con "BV"):** `mipmap-*/ic_launcher_foreground.png` (capa frontal del ícono adaptable), `mipmap-*/ic_launcher.png` (Android 7) y el color de fondo `icono_fondo` en `res/values/colors.xml` y `res/values-night/colors.xml`.

## Sin Android Studio: compilar gratis en GitHub

1. Crear cuenta en github.com y un repositorio **privado** nuevo.
2. Descomprimir el zip y subir el *contenido* de la carpeta (`Add file > Upload files`, arrastrando archivos y carpetas). Si la carpeta oculta `.github` no se sube, crear el archivo con `Add file > Create new file`, nombre `.github/workflows/build-apk.yml`, y pegar su contenido.
3. Pestaña **Actions** > "Compilar APK" > **Run workflow**. Tarda unos 5-10 minutos.
4. Al terminar, abrir la ejecución y descargar el artefacto `glosario-apk` (un zip con `app-debug.apk`).
