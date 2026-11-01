package ics.rem

import android.graphics.ImageDecoder
import android.graphics.Bitmap
import android.content.Intent
import android.net.Uri
import android.os.Handler
import android.os.Looper
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.Image
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.PopupProperties
import dev.rem.api.Argb
import dev.rem.api.CustomColorDefinition
import dev.rem.api.Rem
import dev.rem.api.SchemeOptions
import dev.rem.api.ThemeMode
import dev.rem.api.ThemeStyle
import dev.rem.compose.toMaterial3ColorScheme
import java.util.concurrent.Executors
import java.util.concurrent.RejectedExecutionException
import java.util.concurrent.atomic.AtomicBoolean

private data class DecodedImage(
    val bitmap: Bitmap,
    val seed: Argb,
)

private val CUSTOM_COLOR_DEFINITIONS = listOf(
    CustomColorDefinition(
        id = "success",
        source = Argb.fromLong(0xFF2E7D32L),
        harmonizeWithSeed = true,
    ),
    CustomColorDefinition(
        id = "warning",
        source = Argb.fromLong(0xFFED6C02L),
        harmonizeWithSeed = true,
    ),
    CustomColorDefinition(
        id = "info",
        source = Argb.fromLong(0xFF0288D1L),
        harmonizeWithSeed = true,
    ),
)

@Composable
fun RemSampleApp() {
    val context = LocalContext.current
    val mainHandler = remember { Handler(Looper.getMainLooper()) }
    val imageExecutor = remember { Executors.newSingleThreadExecutor() }
    val screenIsActive = remember { AtomicBoolean(true) }
    DisposableEffect(imageExecutor) {
        screenIsActive.set(true)
        onDispose {
            screenIsActive.set(false)
            imageExecutor.shutdown()
        }
    }
    var themeModeName by rememberSaveable { mutableStateOf(ThemeMode.LIGHT.name) }
    val themeMode = ThemeMode.valueOf(themeModeName)
    var themeStyleName by rememberSaveable { mutableStateOf(ThemeStyle.TONAL_SPOT.name) }
    val themeStyle = ThemeStyle.valueOf(themeStyleName)
    var contrastLevel by rememberSaveable { mutableStateOf(0f) }
    var styleMenuExpanded by remember { mutableStateOf(false) }
    var paletteViewerOpen by remember { mutableStateOf(false) }
    var exportInProgress by remember { mutableStateOf(false) }
    var exportMessage by remember { mutableStateOf<String?>(null) }
    var activeSeedValue by rememberSaveable { mutableStateOf(0xFF6750A4L) }
    val activeSeed = Argb.fromLong(activeSeedValue)
    var imageDerivedSeedValue by rememberSaveable { mutableStateOf<Long?>(null) }
    val imageDerivedSeed = imageDerivedSeedValue?.let(Argb::fromLong)
    var selectedImageUriString by rememberSaveable { mutableStateOf<String?>(null) }
    var selectedImage by remember { mutableStateOf<Bitmap?>(null) }
    var seedInput by rememberSaveable { mutableStateOf("#6750A4") }
    val seedInputError = parseSeedColor(seedInput) == null
    var imageSeedMessage by remember { mutableStateOf<String?>(null) }
    var imageSeedLoading by remember { mutableStateOf(false) }
    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
    ) { uri: Uri? ->
        if (uri != null) {
            imageSeedLoading = true
            imageSeedMessage = "Extracting seed color…"
            val hasPersistedReadGrant = runCatching {
                context.contentResolver.takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION,
                )
            }.isSuccess
            imageExecutor.execute {
                val result = runCatching { decodeImageAndExtractSeed(context, uri) }
                mainHandler.post {
                    if (!screenIsActive.get()) {
                        result.getOrNull()?.bitmap?.recycle()
                        if (uri.toString() != selectedImageUriString) {
                            releaseUriPermission(context, uri, hasPersistedReadGrant)
                        }
                        return@post
                    }
                    imageSeedLoading = false
                    result.fold(
                        onSuccess = { decodedImage ->
                            val previousUri = selectedImageUriString?.let(Uri::parse)
                            if (previousUri != null && previousUri != uri) {
                                releaseUriPermission(context, previousUri, persisted = true)
                            }
                            selectedImageUriString = uri.toString()
                            selectedImage = decodedImage.bitmap
                            imageDerivedSeedValue = decodedImage.seed.toLong()
                            activeSeedValue = decodedImage.seed.toLong()
                            seedInput = decodedImage.seed.toHex()
                            imageSeedMessage = "Seed extracted from image."
                        },
                        onFailure = {
                            if (uri.toString() != selectedImageUriString) {
                                releaseUriPermission(context, uri, hasPersistedReadGrant)
                            }
                            imageSeedMessage = "Could not extract a seed from that image. Choose another image."
                        },
                    )
                }
            }
        }
    }

    LaunchedEffect(selectedImageUriString) {
        val savedUriString = selectedImageUriString ?: return@LaunchedEffect
        if (selectedImage != null) return@LaunchedEffect

        imageSeedLoading = true
        imageSeedMessage = "Restoring selected image…"
        val savedUri = Uri.parse(savedUriString)
        imageExecutor.execute {
            val result = runCatching { decodeImageAndExtractSeed(context, savedUri) }
            mainHandler.post {
                if (!screenIsActive.get()) {
                    result.getOrNull()?.bitmap?.recycle()
                    return@post
                }
                imageSeedLoading = false
                result.fold(
                    onSuccess = { decodedImage ->
                        selectedImage = decodedImage.bitmap
                        if (imageDerivedSeedValue == null) {
                            imageDerivedSeedValue = decodedImage.seed.toLong()
                        }
                        imageSeedMessage = "Selected image restored."
                    },
                    onFailure = {
                        imageSeedMessage = "Could not restore the selected image preview. Select the image again."
                    },
                )
            }
        }
    }

    val theme = remember(activeSeed, themeStyle, contrastLevel) {
        Rem.createTheme(
            seed = activeSeed,
            customColors = CUSTOM_COLOR_DEFINITIONS,
            options = SchemeOptions(
                style = themeStyle,
                contrastLevel = contrastLevel.toDouble(),
            ),
        )
    }
    val remScheme = if (themeMode == ThemeMode.DARK) theme.schemes.dark else theme.schemes.light

    val materialColorScheme = remember(remScheme) { remScheme.toMaterial3ColorScheme() }
    val paletteRoles = remember(materialColorScheme) { materialColorScheme.toPaletteRoles() }

    val pngExportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("image/png"),
    ) { uri: Uri? ->
        if (uri == null) {
            exportInProgress = false
            exportMessage = "PNG export canceled."
        } else {
            exportMessage = "Writing PNG…"
            startPaletteExport(
                context = context,
                destination = uri,
                format = PaletteExportFormat.PNG,
                roles = paletteRoles,
                customColors = theme.customColors,
                themeMode = themeMode,
                executor = imageExecutor,
                mainHandler = mainHandler,
                screenIsActive = screenIsActive,
            ) { result ->
                exportInProgress = false
                exportMessage = result.fold(
                    onSuccess = { "PNG palette saved." },
                    onFailure = { "Could not save PNG: ${it.message ?: "unknown error"}" },
                )
            }
        }
    }
    val txtExportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("text/plain"),
    ) { uri: Uri? ->
        if (uri == null) {
            exportInProgress = false
            exportMessage = "TXT export canceled."
        } else {
            exportMessage = "Writing TXT…"
            startPaletteExport(
                context = context,
                destination = uri,
                format = PaletteExportFormat.TXT,
                roles = paletteRoles,
                customColors = theme.customColors,
                themeMode = themeMode,
                executor = imageExecutor,
                mainHandler = mainHandler,
                screenIsActive = screenIsActive,
            ) { result ->
                exportInProgress = false
                exportMessage = result.fold(
                    onSuccess = { "TXT palette saved." },
                    onFailure = { "Could not save TXT: ${it.message ?: "unknown error"}" },
                )
            }
        }
    }

    MaterialTheme(colorScheme = materialColorScheme) {
        val focusManager = LocalFocusManager.current
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClickLabel = "Dismiss keyboard",
                ) {
                    focusManager.clearFocus()
                },
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 18.dp)
                    .padding(top = 22.dp, bottom = 28.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp),
            ) {
                RemHeader()
                SeedManagementSection(
                    seedInput = seedInput,
                    seedInputError = seedInputError,
                    selectedImage = selectedImage,
                    imageDerivedSeed = imageDerivedSeed,
                    activeSeed = activeSeed,
                    imageSeedLoading = imageSeedLoading,
                    imageSeedMessage = imageSeedMessage,
                    onSeedInputChange = { input ->
                        seedInput = input
                        val parsedSeed = parseSeedColor(input)
                        if (parsedSeed != null) activeSeedValue = parsedSeed.toLong()
                    },
                    onChooseImage = {
                        imagePicker.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly),
                        )
                    },
                )
                ThemeConfigurationSection(
                    themeStyle = themeStyle,
                    styleMenuExpanded = styleMenuExpanded,
                    themeMode = themeMode,
                    contrastLevel = contrastLevel,
                    onStyleMenuExpandedChange = { styleMenuExpanded = it },
                    onThemeStyleChange = { themeStyleName = it.name },
                    onThemeModeChange = { themeModeName = it.name },
                    onContrastChange = { contrastLevel = it.coerceIn(-1f, 1f) },
                )
                CompactPalettePreview(
                    roles = paletteRoles,
                    onOpenViewer = { paletteViewerOpen = true },
                )
                CustomSemanticColorsSection(
                    customColors = theme.customColors,
                    themeMode = themeMode,
                )
            }
        }
        if (paletteViewerOpen) {
            PaletteViewerDialog(
                roles = paletteRoles,
                customColors = theme.customColors,
                themeMode = themeMode,
                exportInProgress = exportInProgress,
                exportMessage = exportMessage,
                onExportPng = {
                    exportInProgress = true
                    exportMessage = null
                    runCatching { pngExportLauncher.launch("rem-palette.png") }
                        .onFailure { failure ->
                            exportInProgress = false
                            exportMessage = "Could not open PNG destination: ${failure.message ?: "unknown error"}"
                        }
                },
                onExportTxt = {
                    exportInProgress = true
                    exportMessage = null
                    runCatching { txtExportLauncher.launch("rem-palette.txt") }
                        .onFailure { failure ->
                            exportInProgress = false
                            exportMessage = "Could not open TXT destination: ${failure.message ?: "unknown error"}"
                        }
                },
                onDismiss = { paletteViewerOpen = false },
            )
        }
    }
}

@Composable
private fun RemHeader() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = "RemTheme Engine",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Normal,
        )
    }
}

@Composable
private fun SeedManagementSection(
    seedInput: String,
    seedInputError: Boolean,
    selectedImage: Bitmap?,
    imageDerivedSeed: Argb?,
    activeSeed: Argb,
    imageSeedLoading: Boolean,
    imageSeedMessage: String?,
    onSeedInputChange: (String) -> Unit,
    onChooseImage: () -> Unit,
) {
    SectionContent(title = "Seed color", subtitle = "Choose an image or enter a color value.") {
        Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                if (selectedImage == null) {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(112.dp),
                        color = MaterialTheme.colorScheme.surfaceContainerHigh,
                        shape = RoundedCornerShape(16.dp),
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = when {
                                    imageSeedLoading && imageDerivedSeed != null -> "Restoring image preview…"
                                    imageDerivedSeed != null -> "Image preview unavailable"
                                    else -> "Choose an image to preview it here"
                                },
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                } else {
                    Image(
                        bitmap = selectedImage.asImageBitmap(),
                        contentDescription = "Selected image used to extract a seed color",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(160.dp),
                        contentScale = ContentScale.Crop,
                    )
                }
                SeedStatus(
                    label = "Active theme seed",
                    seed = activeSeed,
                    detail = when {
                        imageDerivedSeed == null -> "Set by manual input"
                        imageDerivedSeed == activeSeed -> "Matches the selected image"
                        else -> "Manual override; image seed is ${imageDerivedSeed.toHex()}"
                    },
                )
                if (imageDerivedSeed != null && imageDerivedSeed != activeSeed) {
                    SeedStatus(
                        label = "Seed from selected image",
                        seed = imageDerivedSeed,
                        detail = "The image remains selected; the active theme uses the manual value.",
                    )
                }
                OutlinedButton(
                    onClick = onChooseImage,
                    enabled = !imageSeedLoading,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(if (imageSeedLoading) "Extracting seed…" else "Choose image")
                }
                val focusManager = LocalFocusManager.current
                OutlinedTextField(
                    value = seedInput,
                    onValueChange = onSeedInputChange,
                    label = { Text("Hexadecimal seed") },
                    supportingText = {
                        Text(
                            if (seedInputError) {
                                "Enter 6 or 8 hex digits, optionally prefixed by #."
                            } else {
                                "Use RRGGBB or AARRGGBB."
                            },
                        )
                    },
                    isError = seedInputError,
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                )
                imageSeedMessage?.let { message ->
                    Text(
                        message,
                        modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

@Composable
private fun ThemeConfigurationSection(
    themeStyle: ThemeStyle,
    styleMenuExpanded: Boolean,
    themeMode: ThemeMode,
    contrastLevel: Float,
    onStyleMenuExpandedChange: (Boolean) -> Unit,
    onThemeStyleChange: (ThemeStyle) -> Unit,
    onThemeModeChange: (ThemeMode) -> Unit,
    onContrastChange: (Float) -> Unit,
) {
    val focusManager = LocalFocusManager.current
    SectionContent(title = "Theme configuration", subtitle = "Adjust the generated palette.") {
        Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Theme style", style = MaterialTheme.typography.labelLarge)
                    Box {
                        OutlinedButton(
                            onClick = {
                                focusManager.clearFocus()
                                onStyleMenuExpandedChange(true)
                            },
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Text(themeStyle.label(), modifier = Modifier.weight(1f))
                            Text("⌄")
                        }
                        DropdownMenu(
                            expanded = styleMenuExpanded,
                            onDismissRequest = { onStyleMenuExpandedChange(false) },
                            properties = PopupProperties(focusable = false),
                        ) {
                            ThemeStyle.entries.forEach { style ->
                                DropdownMenuItem(
                                    text = { Text(style.label()) },
                                    onClick = {
                                        onThemeStyleChange(style)
                                        onStyleMenuExpandedChange(false)
                                    },
                                )
                            }
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text("Dark appearance", style = MaterialTheme.typography.titleSmall)
                        Text(
                            if (themeMode == ThemeMode.DARK) "On" else "Off",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    Switch(
                        modifier = Modifier.semantics {
                            contentDescription = "Dark appearance"
                            stateDescription = if (themeMode == ThemeMode.DARK) "On" else "Off"
                        },
                        checked = themeMode == ThemeMode.DARK,
                        onCheckedChange = { isDark ->
                            onThemeModeChange(if (isDark) ThemeMode.DARK else ThemeMode.LIGHT)
                        },
                    )
                }

                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text("Contrast", style = MaterialTheme.typography.titleSmall)
                        Text("${"%.1f".format(contrastLevel)}", style = MaterialTheme.typography.labelLarge)
                    }
                    Slider(
                        value = contrastLevel,
                        onValueChange = onContrastChange,
                        valueRange = -1f..1f,
                        modifier = Modifier.semantics(mergeDescendants = true) {
                            contentDescription = "Contrast level"
                        },
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Text("-1.0", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("1.0", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }
}

@Composable
private fun CustomSemanticColorsSection(
    customColors: Map<String, dev.rem.api.CustomColorSchemes>,
    themeMode: ThemeMode,
) {
    SectionContent(title = "Custom semantic colors", subtitle = "Additional roles generated separately from the Material palette.") {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            customColors.forEach { (id, customSchemes) ->
                val roles = if (themeMode == ThemeMode.DARK) customSchemes.dark else customSchemes.light
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        Text(id.replaceFirstChar { it.uppercase() }, style = MaterialTheme.typography.titleMedium)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            CustomRolePreview(
                                label = "Color",
                                background = roles.color,
                                foreground = roles.onColor,
                                modifier = Modifier.weight(1f),
                            )
                            CustomRolePreview(
                                label = "Container",
                                background = roles.colorContainer,
                                foreground = roles.onColorContainer,
                                modifier = Modifier.weight(1f),
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SectionContent(
    title: String,
    subtitle: String,
    content: @Composable () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
            Text(title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Medium)
            Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        content()
    }
}

@Composable
private fun SeedStatus(
    label: String,
    seed: Argb,
    detail: String,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Surface(
            modifier = Modifier
                .height(36.dp)
                .weight(0.7f),
            color = Color(seed.value),
            shape = RoundedCornerShape(10.dp),
        ) {}
        Column(modifier = Modifier.weight(2.3f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(label, style = MaterialTheme.typography.labelLarge)
                Text(seed.toHex(), style = MaterialTheme.typography.labelMedium)
            }
            Text(detail, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun CustomRolePreview(
    label: String,
    background: Argb,
    foreground: Argb,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(label, style = MaterialTheme.typography.labelMedium)
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = Color(background.value),
            contentColor = Color(foreground.value),
            shape = RoundedCornerShape(12.dp),
        ) {
            Text("Sample text", modifier = Modifier.padding(horizontal = 10.dp, vertical = 12.dp), style = MaterialTheme.typography.labelMedium)
        }
    }
}

private fun ThemeStyle.label(): String = when (this) {
    ThemeStyle.TONAL_SPOT -> "Tonal Spot"
    ThemeStyle.FIDELITY -> "Fidelity"
    ThemeStyle.CONTENT -> "Content"
    ThemeStyle.EXPRESSIVE -> "Expressive"
    ThemeStyle.MONOCHROME -> "Monochrome"
    ThemeStyle.VIBRANT -> "Vibrant"
}

@android.annotation.SuppressLint("NewApi")
private fun decodeImageAndExtractSeed(context: android.content.Context, uri: Uri): DecodedImage {
    val source = ImageDecoder.createSource(context.contentResolver, uri)
    val bitmap = ImageDecoder.decodeBitmap(source) { decoder, info, _ ->
        decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
        val maxDimension = 512
        val scale = minOf(
            1f,
            maxDimension.toFloat() / maxOf(info.size.width, info.size.height),
        )
        if (scale < 1f) {
            decoder.setTargetSize(
                (info.size.width * scale).toInt().coerceAtLeast(1),
                (info.size.height * scale).toInt().coerceAtLeast(1),
            )
        }
    }
    val extractedSeed = try {
        val pixels = IntArray(bitmap.width * bitmap.height)
        bitmap.getPixels(pixels, 0, bitmap.width, 0, 0, bitmap.width, bitmap.height)
        Rem.extractSeedColor(pixels)
    } catch (failure: Throwable) {
        bitmap.recycle()
        throw failure
    }
    return DecodedImage(bitmap, extractedSeed)
}

private fun startPaletteExport(
    context: android.content.Context,
    destination: Uri,
    format: PaletteExportFormat,
    roles: List<PaletteRole>,
    customColors: Map<String, dev.rem.api.CustomColorSchemes>,
    themeMode: ThemeMode,
    executor: java.util.concurrent.ExecutorService,
    mainHandler: Handler,
    screenIsActive: AtomicBoolean,
    onComplete: (Result<Unit>) -> Unit,
) {
    try {
        executor.execute {
            val result = runCatching {
                writePaletteExport(context, destination, format, roles, customColors, themeMode)
            }
            mainHandler.post {
                if (screenIsActive.get()) onComplete(result)
            }
        }
    } catch (failure: RejectedExecutionException) {
        if (screenIsActive.get()) {
            onComplete(Result.failure(failure))
        }
    }
}

private fun releaseUriPermission(context: android.content.Context, uri: Uri, persisted: Boolean) {
    if (persisted) {
        runCatching {
            context.contentResolver.releasePersistableUriPermission(
                uri,
                Intent.FLAG_GRANT_READ_URI_PERMISSION,
            )
        }
    }
}

private fun parseSeedColor(input: String): Argb? {
    val digits = input.removePrefix("#")
    if (digits.length != 6 && digits.length != 8) return null
    if (digits.any { it.digitToIntOrNull(16) == null }) return null

    val argb = if (digits.length == 6) "FF$digits" else digits
    return Argb.fromLong(argb.toLong(16))
}

private fun Argb.toHex(): String = "#%08X".format(value.toLong() and 0xFFFFFFFFL)
