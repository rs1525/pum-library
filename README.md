# PUM Library - README

## ¿Qué es PUM?

**PUM** (Pack Universal Manager) es una biblioteca Android moderna para crear aplicaciones de paquetes de widgets (KWGT) y wallpapers (KLWP) de Kustom. Proporciona una interfaz de usuario completa y lista para usar con Jetpack Compose y Material 3.

## Características

✅ **Interfaz Moderna**: Tema oscuro con Material Design 3
✅ **Configuración Flexible**: Activa o desactiva secciones según tu necesidad
✅ **Navegación por Tabs**: Widgets, Wallpapers, y Wallpaper Cloud
✅ **Integración con Kustom**: Aplica widgets y wallpapers directamente a KWGT/KLWP
✅ **Fácil de Usar**: Solo necesitas configurar y agregar tus archivos

## Requisitos

- **minSdk**: 29 (Android 10)
- **compileSdk**: 36
- **Jetpack Compose**: Incluido
- **Kotlin**: 2.0.21+

## Instalación

### Opción 1: Como módulo local

1. Copia la carpeta `pum` a tu proyecto
2. En `settings.gradle.kts` agrega:
```kotlin
include(":pum")
```

3. En el `build.gradle.kts` de tu app agrega:
```kotlin
dependencies {
    implementation(project(":pum"))
}
```

### Opción 2: JitPack (próximamente)

```kotlin
dependencies {
    implementation("com.github.tuusuario:pum:1.0.0")
}
```

## Uso Básico

### 1. Configurar tu MainActivity

```kotlin
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val pumConfig = PumConfig(
            appName = "Mi Pack de Widgets",
            appSubtitle = "Widgets increíbles",
            appIcon = R.drawable.app_icon, // Tu icono
            packageName = packageName,
            showWidgets = true,
            showWallpapers = true,
            showWallpaperCloud = false
        )
        
        setContent {
            PumScreen(config = pumConfig)
        }
    }
}
```

### 2. Agregar archivos KWGT/KLWP

Coloca tus archivos en las carpetas correspondientes:
- **Widgets**: `app/src/main/assets/widgets/`
- **Wallpapers**: `app/src/main/assets/wallpapers/`

Ejemplo:
```
app/src/main/assets/
  ├── widgets/
  │   ├── widget_001.kwgt
  │   ├── widget_002.kwgt
  │   └── widget_003.kwgt
  └── wallpapers/
      ├── wallpaper_001.klwp
      └── wallpaper_002.klwp
```

### 3. Configurar FileProvider

En tu `AndroidManifest.xml`:

```xml
<provider
    android:name="androidx.core.content.FileProvider"
    android:authorities="${applicationId}.fileprovider"
    android:exported="false"
    android:grantUriPermissions="true">
    <meta-data
        android:name="android.support.FILE_PROVIDER_PATHS"
        android:resource="@xml/fileprovider" />
</provider>
```

Crea `res/xml/fileprovider.xml`:

```xml
<?xml version="1.0" encoding="utf-8"?>
<paths>
    <files-path name="kustom_files" path="kustom/" />
</paths>
```

## Configuración Avanzada

### More Apps (Carrusel en pantalla Acerca de)

Muestra un carrusel horizontal de tarjetas con tus otras apps en la pantalla **Acerca de**. Cada tarjeta incluye: imagen promocional, icono, nombre, descripción y botón INSTALL.

En tu `AppConfig.kt` define la lista de apps:

```kotlin
import com.akustom15.pum.config.MoreApp

val MORE_APPS = listOf(
    MoreApp(
        name = "Nombre de tu App",
        description = "Descripción corta de la app",
        iconUrl = "https://raw.githubusercontent.com/tu-usuario/tu-repo/main/icono.png",
        screenshotUrls = listOf(
            "https://raw.githubusercontent.com/tu-usuario/tu-repo/main/promo1.png",
            "https://raw.githubusercontent.com/tu-usuario/tu-repo/main/promo2.png"
        ),
        playStoreUrl = "https://play.google.com/store/apps/details?id=com.tu.paquete"
    )
)
```

Y pásalo a `PumConfig`:

```kotlin
val pumConfig = PumConfig(
    // ... otros parámetros ...
    moreApps = MORE_APPS
)
```

**Notas importantes:**
- Las URLs de imágenes deben ser **directas** (GitHub raw, Imgur, etc.). No usar Google Drive.
- `iconUrl` → Icono pequeño (se muestra junto al nombre, redondeado)
- `screenshotUrls` → Imágenes promocionales grandes (se muestran en la parte superior de la tarjeta, scroll horizontal)
- Si `moreApps` está vacía, la sección no se muestra
- Puedes agregar múltiples screenshots por app para un efecto de carrusel dentro de cada tarjeta

### Buscar Actualizaciones (Botón en Configuración)

Agrega un botón "Buscar actualizaciones" en la pantalla de **Configuración** que verifica si hay una nueva versión disponible.

1. Crea un archivo JSON de versión en un hosting (GitHub raw recomendado):

```json
{
    "version_code": 2,
    "version_name": "1.1.0",
    "update_url": "https://play.google.com/store/apps/details?id=com.tu.paquete",
    "changelog": "- Nueva función X\n- Corrección de bugs"
}
```

2. En tu `AppConfig.kt`:

```kotlin
const val UPDATE_JSON_URL = "https://raw.githubusercontent.com/tu-usuario/tu-repo/main/version.json"
```

3. Pásalo a `PumConfig`:

```kotlin
val pumConfig = PumConfig(
    // ... otros parámetros ...
    updateJsonUrl = UPDATE_JSON_URL
)
```

**Notas importantes:**
- Si `updateJsonUrl` está vacío, el botón no se muestra
- El `version_code` del JSON se compara con el `versionCode` actual de la app
- Si hay actualización, se muestra un diálogo con el changelog y un botón para ir a Play Store
- Si la app está al día, se muestra un Toast confirmando

---

### Mostrar solo Widgets

```kotlin
val pumConfig = PumConfig(
    appName = "Widget Pack",
    packageName = packageName,
    showWidgets = true,
    showWallpapers = false,
    showWallpaperCloud = false
)
```

### Mostrar solo Wallpapers

```kotlin
val pumConfig = PumConfig(
    appName = "Wallpaper Pack",
    packageName = packageName,
    showWidgets = false,
    showWallpapers = true,
    showWallpaperCloud = false
)
```

### Habilitar Wallpapers en la Nube

```kotlin
val pumConfig = PumConfig(
    appName = "Complete Pack",
    packageName = packageName,
    showWidgets = true,
    showWallpapers = true,
    showWallpaperCloud = true // Activa la tab de wallpapers cloud
)
```

**Nota**: La funcionalidad de wallpapers cloud requiere implementación adicional (Firebase, API personalizada, etc.)

## Estructura del Proyecto

```
pum/
├── config/
│   ├── PumConfig.kt          # Configuración de la biblioteca
│   └── MoreApp.kt            # Modelo para sección "More Apps"
├── model/
│   ├── WidgetItem.kt         # Modelo de widget
│   ├── WallpaperItem.kt      # Modelo de wallpaper
│   └── CloudWallpaperItem.kt # Modelo de wallpaper cloud
├── utils/
│   ├── AssetsReader.kt       # Lee archivos de assets
│   └── KustomIntegration.kt  # Integración con Kustom
└── ui/
    ├── theme/                # Tema Material 3
    ├── components/           # Componentes reutilizables
    ├── screens/              # Pantallas (grids)
    └── PumScreen.kt          # Pantalla principal
```

## Cómo Funciona

1. **Al iniciar** la app, PUM escanea automáticamente las carpetas `assets/widgets/` y `assets/wallpapers/`
2. **Genera una lista** de items encontrados
3. **Muestra los items** en un grid con previews
4. **Al tocar "Aplicar"**, copia el archivo al cache y lo envía a KWGT/KLWP mediante un Intent
5. **KWGT/KLWP abre** el archivo para que el usuario lo aplique

## Publicación de la Biblioteca

### En JitPack

1. Sube tu proyecto a GitHub
2. Crea un release (tag)
3. Agrega JitPack a tu proyecto:

```kotlin
repositories {
    maven { url = uri("https://jitpack.io") }
}
```

### En Maven Local

```bash
./gradlew :pum:publishToMavenLocal
```

## Personalización

Puedes personalizar los colores y el tema editando:
- `pum/src/main/java/com/akustom15/pum/ui/theme/Color.kt`
- `pum/src/main/java/com/akustom15/pum/ui/theme/Theme.kt`

## Requisitos de Kustom

Los usuarios necesitan tener instalado:
- **KWGT** para widgets → [Play Store](https://play.google.com/store/apps/details?id=org.kustom.widget)
- **KLWP** para wallpapers → [Play Store](https://play.google.com/store/apps/details?id=org.kustom.wallpaper)

## Ejemplo Completo

Revisa el módulo `app` para ver un ejemplo funcional de cómo usar la biblioteca.

## Soporte

- **Android 10+** (API 29+)
- **Jetpack Compose**
- **Material 3**
- **Kotlin 2.0+**

## Licencia

MIT License - Usa libremente en tus proyectos

---

**¡Listo para crear tu pack de widgets o wallpapers!** 🎨
