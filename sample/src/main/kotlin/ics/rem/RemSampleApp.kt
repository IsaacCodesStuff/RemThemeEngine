package ics.rem

import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Handler
import android.os.Looper
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext
import java.util.concurrent.Executors
import dev.rem.api.Argb
import dev.rem.api.CustomColorDefinition
import dev.rem.api.CustomColorRoles
import dev.rem.api.Rem
import dev.rem.api.SchemeOptions
import dev.rem.api.ThemeMode
import dev.rem.api.ThemeStyle
import dev.rem.compose.toMaterial3ColorScheme

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
    DisposableEffect(imageExecutor) {
        onDispose { imageExecutor.shutdown() }
    }
    var themeMode by remember { mutableStateOf(ThemeMode.LIGHT) }
    var themeStyle by remember { mutableStateOf(ThemeStyle.TONAL_SPOT) }
    var contrastLevel by remember { mutableStateOf(0f) }
    var styleMenuExpanded by remember { mutableStateOf(false) }
    var seed by remember { mutableStateOf(Argb.fromLong(0xFF6750A4L)) }
    var seedInput by remember { mutableStateOf("#6750A4") }
    var seedInputError by remember { mutableStateOf(false) }
    var imageSeedMessage by remember { mutableStateOf<String?>(null) }
    var imageSeedLoading by remember { mutableStateOf(false) }
    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
    ) { uri: Uri? ->
        if (uri != null) {
            imageSeedLoading = true
            imageSeedMessage = "Extracting seed color…"
            imageExecutor.execute {
                val result = runCatching {
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
                    try {
                        val pixels = IntArray(bitmap.width * bitmap.height)
                        bitmap.getPixels(pixels, 0, bitmap.width, 0, 0, bitmap.width, bitmap.height)
                        Rem.extractSeedColor(pixels)
                    } finally {
                        bitmap.recycle()
                    }
                }
                mainHandler.post {
                    imageSeedLoading = false
                    result.fold(
                        onSuccess = { extractedSeed ->
                            seed = extractedSeed
                            seedInput = "#%06X".format(extractedSeed.value and 0x00FFFFFF)
                            seedInputError = false
                            imageSeedMessage = "Seed extracted from image."
                        },
                        onFailure = {
                            imageSeedMessage = "Could not extract a seed from that image. Choose another image."
                        },
                    )
                }
            }
        }
    }
    val theme = remember(seed, themeStyle, contrastLevel) {
        Rem.createTheme(
            seed = seed,
            customColors = CUSTOM_COLOR_DEFINITIONS,
            options = SchemeOptions(
                style = themeStyle,
                contrastLevel = contrastLevel.toDouble(),
            ),
        )
    }
    val remScheme = if (themeMode == ThemeMode.DARK) theme.schemes.dark else theme.schemes.light

    MaterialTheme(colorScheme = remScheme.toMaterial3ColorScheme()) {
        Surface(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Text("Rem dynamic theme", style = MaterialTheme.typography.headlineMedium)
                Text(
                    "Generated from a seed color, then applied through MaterialTheme.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                OutlinedTextField(
                    value = seedInput,
                    onValueChange = { input ->
                        seedInput = input
                        val parsedSeed = parseSeedColor(input)
                        seedInputError = parsedSeed == null
                        if (parsedSeed != null) seed = parsedSeed
                    },
                    label = { Text("Seed color") },
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
                    modifier = Modifier.fillMaxWidth(),
                )
                OutlinedButton(
                    onClick = {
                        imagePicker.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly),
                        )
                    },
                    enabled = !imageSeedLoading,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(if (imageSeedLoading) "Extracting seed…" else "Choose image for seed")
                }
                imageSeedMessage?.let { message ->
                    Text(message, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text("Theme style")
                    Box {
                        OutlinedButton(onClick = { styleMenuExpanded = true }) {
                            Text(themeStyle.name.replace('_', ' '))
                        }
                        DropdownMenu(
                            expanded = styleMenuExpanded,
                            onDismissRequest = { styleMenuExpanded = false },
                        ) {
                            ThemeStyle.entries.forEach { style ->
                                DropdownMenuItem(
                                    text = { Text(style.name.replace('_', ' ')) },
                                    onClick = {
                                        themeStyle = style
                                        styleMenuExpanded = false
                                    },
                                )
                            }
                        }
                    }
                }

                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("Contrast level: $contrastLevel")
                    Slider(
                        value = contrastLevel,
                        onValueChange = { contrastLevel = it.coerceIn(-1f, 1f) },
                        valueRange = -1f..1f,
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Text("-1.0")
                        Text("1.0")
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(if (themeMode == ThemeMode.DARK) "Dark appearance" else "Light appearance")
                    Switch(
                        checked = themeMode == ThemeMode.DARK,
                        onCheckedChange = { isDark ->
                            themeMode = if (isDark) ThemeMode.DARK else ThemeMode.LIGHT
                        },
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Button(onClick = {}) { Text("Primary action") }
                    OutlinedButton(onClick = {}) { Text("Secondary action") }
                }

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    ),
                    shape = RoundedCornerShape(20.dp),
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text("Color roles at work", style = MaterialTheme.typography.titleLarge)
                        Spacer(Modifier.height(8.dp))
                        Text(
                            "This card uses secondaryContainer and its content uses the active theme typography.",
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                        )
                    }
                }

                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.surfaceContainer,
                    contentColor = MaterialTheme.colorScheme.onSurface,
                    shape = RoundedCornerShape(16.dp),
                ) {
                    Text(
                        "Surface container preview",
                        modifier = Modifier.padding(20.dp),
                        style = MaterialTheme.typography.titleMedium,
                    )
                }

                Text("Custom semantic colors", style = MaterialTheme.typography.titleLarge)
                theme.customColors.forEach { (id, customSchemes) ->
                    val roles = if (themeMode == ThemeMode.DARK) customSchemes.dark else customSchemes.light
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            Text(id.replaceFirstChar { it.uppercase() }, style = MaterialTheme.typography.titleMedium)
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                CustomRolePreview(
                                    label = "color / onColor",
                                    background = roles.color,
                                    foreground = roles.onColor,
                                    modifier = Modifier.weight(1f),
                                )
                                CustomRolePreview(
                                    label = "colorContainer / onColorContainer",
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
}

@Composable
private fun CustomRolePreview(
    label: String,
    background: Argb,
    foreground: Argb,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        Text(label, style = MaterialTheme.typography.labelSmall)
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = Color(background.value),
            contentColor = Color(foreground.value),
            shape = RoundedCornerShape(12.dp),
        ) {
            Text("Sample text", modifier = Modifier.padding(12.dp))
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
