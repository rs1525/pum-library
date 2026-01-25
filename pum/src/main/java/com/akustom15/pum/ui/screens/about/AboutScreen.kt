package com.akustom15.pum.ui.screens.about

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.annotation.DrawableRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.ui.res.painterResource
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.CompositionLocalProvider
import android.content.res.Configuration
import android.os.Build
import java.util.*
import androidx.activity.compose.BackHandler
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.core.graphics.drawable.toBitmap
import androidx.compose.ui.res.stringResource
import com.akustom15.pum.R
import com.akustom15.pum.config.SocialMediaConfig
import com.akustom15.pum.ui.theme.PumTheme

/**
 * Pantalla de información "Acerca de"
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutScreen(
    appIcon: Int?,
    developerLogoUrl: String = "",
    @DrawableRes xIcon: Int,
    @DrawableRes instagramIcon: Int,
    @DrawableRes youtubeIcon: Int,
    @DrawableRes facebookIcon: Int,
    @DrawableRes telegramIcon: Int,
    onNavigateBack: () -> Unit,
    viewModel: AboutViewModel = viewModel()
) {
    // Observar cambios en el idioma actual
    val currentLanguage = viewModel.currentLanguage.collectAsState().value
    
    // Forzar actualización cuando cambia la configuración del dispositivo
    val configuration = LocalConfiguration.current
    LaunchedEffect(configuration) {
        viewModel.updateLanguage()
    }
    
    // Manejar el botón atrás
    BackHandler {
        onNavigateBack()
    }
    
    // Use key to force recomposition when language changes
    key(currentLanguage) {
        val context = LocalContext.current
        
        // Crear un contexto localizado manualmente
        val localizedContext = remember(currentLanguage) {
            createLocalizedContext(context, currentLanguage)
        }
        
        // Proporcionar el contexto localizado usando CompositionLocalProvider
        CompositionLocalProvider(LocalContext provides localizedContext) {
            PumTheme {
                Scaffold(
                    topBar = {
                        TopAppBar(
                            title = { Text(stringResource(R.string.about_title), color = MaterialTheme.colorScheme.onSurface) },
                            navigationIcon = {
                                IconButton(onClick = onNavigateBack) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                        contentDescription = stringResource(R.string.about_back),
                                        tint = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            },
                            colors = TopAppBarDefaults.topAppBarColors(
                                containerColor = MaterialTheme.colorScheme.background,
                                titleContentColor = MaterialTheme.colorScheme.onSurface,
                                navigationIconContentColor = MaterialTheme.colorScheme.onSurface
                            )
                        )
                    }
                ) { paddingValues ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(MaterialTheme.colorScheme.background)
                            .padding(paddingValues)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .verticalScroll(rememberScrollState())
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            // Logo del desarrollador desde URL o icono de la app como fallback
                            if (developerLogoUrl.isNotEmpty()) {
                                coil.compose.AsyncImage(
                                    model = developerLogoUrl,
                                    contentDescription = "Developer Logo",
                                    modifier = Modifier
                                        .size(100.dp)
                                        .padding(8.dp),
                                    contentScale = ContentScale.Fit
                                )
                            } else {
                                appIcon?.let { iconResId ->
                                    val drawable = remember(iconResId) {
                                        context.getDrawable(iconResId)
                                    }
                                    drawable?.let { d ->
                                        val bitmap = remember(d) {
                                            d.toBitmap(width = 300, height = 300)
                                        }
                                        Image(
                                            bitmap = bitmap.asImageBitmap(),
                                            contentDescription = "Logo",
                                            modifier = Modifier
                                                .size(100.dp)
                                                .padding(8.dp),
                                            contentScale = ContentScale.Fit
                                        )
                                    }
                                }
                            }
                            
                            // Título AKustom15
                            LimitedFontScaleText(
                                text = "AKustom15",
                                baseSizeSp = 24f,
                                color = MaterialTheme.colorScheme.onSurface,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(top = 8.dp)
                            )
                            
                            // Descripción del desarrollador
                            LimitedFontScaleText(
                                text = stringResource(R.string.about_developer_desc),
                                baseSizeSp = 15f,
                                color = MaterialTheme.colorScheme.onSurface,
                                fontWeight = FontWeight.Normal,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(top = 16.dp, bottom = 16.dp)
                            )
                            
                            // Iconos de redes sociales
                            val socialMediaLinks = remember {
                                SocialMediaConfig.getSocialMediaLinks(
                                    xIcon = xIcon,
                                    instagramIcon = instagramIcon,
                                    youtubeIcon = youtubeIcon,
                                    facebookIcon = facebookIcon,
                                    telegramIcon = telegramIcon
                                )
                            }
                            
                            // Usar el contexto original, no el localizado
                            val originalContext = context
                            
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                socialMediaLinks.forEach { socialMedia ->
                                    Box(
                                        modifier = Modifier
                                            .size(44.dp)
                                            .clickable {
                                                try {
                                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(socialMedia.url))
                                                    originalContext.startActivity(intent)
                                                } catch (e: Exception) {
                                                    Toast.makeText(originalContext, context.getString(R.string.about_error_opening_link), Toast.LENGTH_SHORT).show()
                                                    e.printStackTrace()
                                                }
                                            },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            painter = painterResource(id = socialMedia.iconRes),
                                            contentDescription = socialMedia.name,
                                            tint = MaterialTheme.colorScheme.onSurface,
                                            modifier = Modifier.size(28.dp)
                                        )
                                    }
                                }
                            }
                            
                            HorizontalDivider(
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f),
                                modifier = Modifier.padding(vertical = 16.dp)
                            )
                            
                            // Sección PUM
                            LimitedFontScaleText(
                                text = stringResource(R.string.about_pum_title),
                                baseSizeSp = 22f,
                                color = MaterialTheme.colorScheme.onSurface,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center
                            )
                            
                            LimitedFontScaleText(
                                text = stringResource(R.string.about_pum_desc),
                                baseSizeSp = 15f,
                                color = MaterialTheme.colorScheme.onSurface,
                                fontWeight = FontWeight.Normal,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(top = 8.dp, bottom = 16.dp)
                            )
                            
                            Spacer(modifier = Modifier.height(24.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun LimitedFontScaleText(
    text: String,
    baseSizeSp: Float,
    color: androidx.compose.ui.graphics.Color,
    fontWeight: FontWeight,
    textAlign: TextAlign = TextAlign.Start,
    modifier: Modifier = Modifier
) {
    val density = LocalDensity.current
    val fontScale = density.fontScale.coerceAtMost(1.0f)
    
    Text(
        text = text,
        fontSize = (baseSizeSp * fontScale).sp,
        color = color,
        fontWeight = fontWeight,
        textAlign = textAlign,
        modifier = modifier
    )
}

// Función privada para crear un contexto localizado
private fun createLocalizedContext(context: android.content.Context, locale: Locale): android.content.Context {
    val configuration = Configuration(context.resources.configuration)
    
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
        configuration.setLocale(locale)
        configuration.setLayoutDirection(locale)
    } else {
        @Suppress("DEPRECATION")
        configuration.locale = locale
        configuration.setLayoutDirection(locale)
    }
    
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
        context.createConfigurationContext(configuration)
    } else {
        @Suppress("DEPRECATION")
        val resources = context.resources
        resources.updateConfiguration(configuration, resources.displayMetrics)
        context
    }
}
