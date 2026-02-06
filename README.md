# PUM - Pack Universal Manager

Librería Android para crear apps de widgets (KWGT) y wallpapers (KLWP).

## Instalación

### 1. Agregar JitPack en settings.gradle.kts:

```kotlin
dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
        maven { url = uri("https://jitpack.io") }
    }
}
```

### 2. Agregar dependencia en app/build.gradle.kts:

```kotlin
dependencies {
    implementation("com.github.rs1525:pum-library:TAG")
}
```

## Uso básico

```kotlin
class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        // Inicializar notificaciones push
        PumNotificationHelper.initialize(this)
        
        val config = PumConfig(
            appName = "Mi Pack",
            appSubtitle = "Widgets",
            appIcon = R.mipmap.ic_launcher,
            packageName = packageName,
            showWidgets = true,
            showWallpapers = false,
            showWallpaperCloud = true,
            cloudWallpapersUrl = "https://tu-url.com/wallpapers.json"
        )
        
        setContent { PumScreen(config = config) }
    }
}
```

## Configuración de Firebase Cloud Messaging (Notificaciones Push)

PUM incluye soporte integrado para notificaciones push vía Firebase Cloud Messaging (FCM).
Esto permite notificar a los usuarios cuando publicas una nueva versión en Play Store.

### Paso 1: Crear proyecto en Firebase Console

1. Ve a [Firebase Console](https://console.firebase.google.com)
2. Crea un nuevo proyecto (o usa uno existente)
3. Haz clic en **"Agregar app"** → selecciona **Android**
4. Ingresa el **applicationId** de tu app (ej: `com.akustom15.peachforkwgt`)
5. Descarga el archivo **`google-services.json`**
6. Colócalo en la carpeta **`app/`** de tu proyecto

### Paso 2: Agregar plugin google-services

En el **build.gradle.kts del proyecto raíz**:

```kotlin
plugins {
    // ... plugins existentes
    id("com.google.gms.google-services") version "4.4.2" apply false
}
```

En el **app/build.gradle.kts**:

```kotlin
plugins {
    // ... plugins existentes
    id("com.google.gms.google-services")
}
```

### Paso 3: Inicializar en MainActivity

Agrega esta línea en `onCreate()` **antes** de `setContent`:

```kotlin
import com.akustom15.pum.notifications.PumNotificationHelper

// En onCreate():
PumNotificationHelper.initialize(this)
```

### Paso 4: Enviar notificaciones desde Firebase Console

1. Ve a **Firebase Console** → **Messaging** → **New Campaign** → **Notifications**
2. Escribe el título y texto del mensaje
3. En **Target**, selecciona **Topic** → escribe: `app_updates`
4. Envía la notificación → llega a todos los usuarios con notificaciones activadas

### Notas

- Los usuarios pueden activar/desactivar notificaciones desde **Configuración** en la app
- En Android 13+ se pide automáticamente el permiso `POST_NOTIFICATIONS`
- La biblioteca se suscribe al topic `app_updates` automáticamente al inicializar
- No necesitas dependencias adicionales de Firebase en tu app, PUM las incluye
