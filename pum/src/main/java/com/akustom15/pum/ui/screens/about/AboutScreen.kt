package com.akustom15.pum.ui.screens.about

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.annotation.DrawableRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Apps
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.draw.clip
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
import androidx.compose.ui.text.style.TextOverflow
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
import com.akustom15.pum.config.MoreApp
import com.akustom15.pum.config.MoreAppsLoader
import com.akustom15.pum.config.SocialMediaConfig
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.coroutines.launch
import androidx.compose.runtime.rememberCoroutineScope
import com.akustom15.pum.ui.theme.PumTheme

/**
 * Pantalla de información "Acerca de"
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutScreen(
    appIcon: Int?,
    developerLogoUrl: String = "",
    developerName: String = "AKustom15",
    moreAppsUrl: String = "",
    moreApps: List<MoreApp> = emptyList(),
    moreAppsJsonUrl: String = "",
    privacyPolicyUrl: String = "",
    @DrawableRes xIcon: Int,
    @DrawableRes instagramIcon: Int,
    @DrawableRes youtubeIcon: Int,
    @DrawableRes facebookIcon: Int,
    @DrawableRes telegramIcon: Int,
    onNavigateBack: () -> Unit,
    viewModel: AboutViewModel = viewModel()
) {
    // Load More Apps from JSON URL if provided, otherwise use hardcoded list
    var remoteMoreApps by remember { mutableStateOf<List<MoreApp>?>(null) }
    val coroutineScope = rememberCoroutineScope()
    LaunchedEffect(moreAppsJsonUrl) {
        if (moreAppsJsonUrl.isNotBlank()) {
            coroutineScope.launch {
                val loaded = MoreAppsLoader.loadFromUrl(moreAppsJsonUrl)
                if (loaded.isNotEmpty()) {
                    remoteMoreApps = loaded
                }
            }
        }
    }
    val effectiveMoreApps = remoteMoreApps ?: moreApps
    
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
                                    model = coil.request.ImageRequest.Builder(context)
                                        .data(developerLogoUrl)
                                        .crossfade(true)
                                        .diskCachePolicy(coil.request.CachePolicy.ENABLED)
                                        .memoryCachePolicy(coil.request.CachePolicy.ENABLED)
                                        .build(),
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
                            
                            // Título del desarrollador
                            LimitedFontScaleText(
                                text = developerName,
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
                            
                            // Botón More Apps (solo si hay URL)
                            if (moreAppsUrl.isNotEmpty()) {
                                Button(
                                    onClick = {
                                        try {
                                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(moreAppsUrl))
                                            context.startActivity(intent)
                                        } catch (e: Exception) {
                                            Toast.makeText(context, context.getString(R.string.about_error_opening_link), Toast.LENGTH_SHORT).show()
                                        }
                                    },
                                    modifier = Modifier.padding(vertical = 16.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.primary
                                    )
                                ) {
                                    Text(
                                        text = stringResource(R.string.about_more_apps),
                                        color = MaterialTheme.colorScheme.onPrimary
                                    )
                                }
                            }
                            
                            // Privacy Policy link
                            if (privacyPolicyUrl.isNotEmpty()) {
                                TextButton(
                                    onClick = {
                                        try {
                                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(privacyPolicyUrl))
                                            context.startActivity(intent)
                                        } catch (e: Exception) {
                                            Toast.makeText(context, context.getString(R.string.about_error_opening_link), Toast.LENGTH_SHORT).show()
                                        }
                                    },
                                    modifier = Modifier.padding(vertical = 4.dp)
                                ) {
                                    Text(
                                        text = stringResource(R.string.about_privacy_policy),
                                        color = MaterialTheme.colorScheme.primary,
                                        fontSize = 14.sp
                                    )
                                }
                            }
                            
                            // More Apps section
                            if (effectiveMoreApps.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(16.dp))
                                
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 0.dp),
                                    shape = RoundedCornerShape(16.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                                    )
                                ) {
                                    Column(
                                        modifier = Modifier.padding(vertical = 16.dp)
                                    ) {
                                        // Section header
                                        Row(
                                            modifier = Modifier.padding(horizontal = 16.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Apps,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(28.dp)
                                            )
                                            Spacer(modifier = Modifier.width(12.dp))
                                            Column {
                                                Text(
                                                    text = stringResource(R.string.about_more_apps_title),
                                                    fontSize = 18.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = MaterialTheme.colorScheme.onSurface
                                                )
                                                Text(
                                                    text = stringResource(R.string.about_more_apps_desc),
                                                    fontSize = 13.sp,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                        }
                                        
                                        Spacer(modifier = Modifier.height(12.dp))
                                        
                                        // Horizontal scrollable app cards
                                        LazyRow(
                                            contentPadding = PaddingValues(horizontal = 16.dp),
                                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                                        ) {
                                            items(effectiveMoreApps) { app ->
                                                MoreAppCard(
                                                    app = app,
                                                    onClick = {
                                                        if (app.playStoreUrl.isNotEmpty()) {
                                                            try {
                                                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(app.playStoreUrl))
                                                                context.startActivity(intent)
                                                            } catch (e: Exception) {
                                                                Toast.makeText(context, context.getString(R.string.about_error_opening_link), Toast.LENGTH_SHORT).show()
                                                            }
                                                        }
                                                    }
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                            
                            Spacer(modifier = Modifier.height(24.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MoreAppCard(
    app: MoreApp,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .width(340.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHighest
        )
    ) {
        Column {
            // Large promotional screenshots area
            if (app.screenshotUrls.isNotEmpty()) {
                if (app.screenshotUrls.size == 1) {
                    coil.compose.AsyncImage(
                        model = coil.request.ImageRequest.Builder(androidx.compose.ui.platform.LocalContext.current)
                            .data(app.screenshotUrls.first())
                            .crossfade(true)
                            .diskCachePolicy(coil.request.CachePolicy.ENABLED)
                            .memoryCachePolicy(coil.request.CachePolicy.ENABLED)
                            .build(),
                        contentDescription = app.name,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(280.dp)
                            .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    LazyRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(280.dp)
                            .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)),
                        contentPadding = PaddingValues(4.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        items(app.screenshotUrls) { screenshotUrl ->
                            coil.compose.AsyncImage(
                                model = coil.request.ImageRequest.Builder(androidx.compose.ui.platform.LocalContext.current)
                                    .data(screenshotUrl)
                                    .crossfade(true)
                                    .diskCachePolicy(coil.request.CachePolicy.ENABLED)
                                    .memoryCachePolicy(coil.request.CachePolicy.ENABLED)
                                    .build(),
                                contentDescription = app.name,
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .width(160.dp)
                                    .clip(RoundedCornerShape(8.dp)),
                                contentScale = ContentScale.Crop
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            
            // App icon + name + "App" label
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // App icon (small, rounded)
                if (app.iconUrl.isNotEmpty()) {
                    coil.compose.AsyncImage(
                        model = coil.request.ImageRequest.Builder(androidx.compose.ui.platform.LocalContext.current)
                            .data(app.iconUrl)
                            .crossfade(true)
                            .diskCachePolicy(coil.request.CachePolicy.ENABLED)
                            .memoryCachePolicy(coil.request.CachePolicy.ENABLED)
                            .build(),
                        contentDescription = app.name,
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(10.dp)),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Apps,
                            contentDescription = app.name,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.size(26.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
                
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = app.name,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "App",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Description
            if (app.description.isNotEmpty()) {
                Text(
                    text = app.description,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 18.sp,
                    modifier = Modifier.padding(horizontal = 14.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Install button
            Button(
                onClick = onClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp)
                    .padding(horizontal = 14.dp),
                shape = RoundedCornerShape(24.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Text(
                    text = stringResource(R.string.about_install),
                    color = MaterialTheme.colorScheme.onPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }
            
            Spacer(modifier = Modifier.height(14.dp))
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
