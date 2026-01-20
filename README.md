# PUM - Pack Universal Manager

Libreria Android para crear apps de widgets (KWGT) y wallpapers (KLWP).

## Instalacion

### 1. Agregar JitPack en settings.gradle.kts:

dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
        maven { url = uri("https://jitpack.io") }
    }
}

### 2. Agregar dependencia en app/build.gradle.kts:

dependencies {
    implementation("com.github.TU_USUARIO:pum-library:1.0.0")
}

## Uso

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
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
