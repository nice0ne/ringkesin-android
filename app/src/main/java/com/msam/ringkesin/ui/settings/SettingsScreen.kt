package com.msam.ringkesin.ui.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.msam.ringkesin.ui.localization.S
import com.msam.ringkesin.ui.theme.RingkesinTheme

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = viewModel(),
    onThemeChanged: ((RingkesinTheme) -> Unit) = {}
) {
    val state by viewModel.uiState.collectAsState()
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .verticalScroll(scrollState)
    ) {
        Text(
            "⚙️ ${S.settings(state.uiLanguage)}",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // ── AI Provider ──
        SettingsGroup("🤖 ${S.aiProvider(state.uiLanguage)}") {
            // Provider dropdown
            Text(
                S.aiProvider(state.uiLanguage),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)
            )
            var providerExpanded by remember { mutableStateOf(false) }
            val currentProvider = PROVIDERS.find { it.id == state.aiProvider }

            ExposedDropdownMenuBox(
                expanded = providerExpanded,
                onExpandedChange = { providerExpanded = it }
            ) {
                OutlinedTextField(
                    value = currentProvider?.label ?: state.aiProvider,
                    onValueChange = {},
                    readOnly = true,
                    modifier = Modifier.fillMaxWidth().menuAnchor(),
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = providerExpanded) },
                    textStyle = MaterialTheme.typography.bodyMedium,
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                    )
                )
                ExposedDropdownMenu(
                    expanded = providerExpanded,
                    onDismissRequest = { providerExpanded = false }
                ) {
                    PROVIDERS.forEach { provider ->
                        DropdownMenuItem(
                            text = { Text(provider.label) },
                            onClick = {
                                viewModel.setAiProvider(provider.id)
                                providerExpanded = false
                            }
                        )
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            // API Key
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        S.apiKey(state.uiLanguage),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)
                    )
                    OutlinedTextField(
                        value = state.apiKey,
                        onValueChange = { viewModel.setApiKey(it) },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("sk-...") },
                        singleLine = true,
                        visualTransformation = if (state.showApiKey)
                            VisualTransformation.None
                        else PasswordVisualTransformation(),
                        trailingIcon = {
                            IconButton(onClick = { viewModel.toggleShowApiKey() }) {
                                Icon(
                                    if (state.showApiKey) Icons.Default.VisibilityOff
                                    else Icons.Default.Visibility,
                                    contentDescription = "Toggle"
                                )
                            }
                        },
                        textStyle = MaterialTheme.typography.bodySmall,
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                        )
                    )
                }
            }

            Spacer(Modifier.height(8.dp))

            // Model
            Text(
                S.model(state.uiLanguage),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)
            )
            OutlinedTextField(
                value = state.model,
                onValueChange = { viewModel.setModel(it) },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("gpt-4o-mini") },
                singleLine = true,
                textStyle = MaterialTheme.typography.bodySmall,
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                )
            )

            Spacer(Modifier.height(8.dp))

            // Base URL
            Text(
                S.baseUrl(state.uiLanguage),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)
            )
            OutlinedTextField(
                value = state.baseUrl,
                onValueChange = { viewModel.setBaseUrl(it) },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("https://api.openai.com/v1") },
                singleLine = true,
                textStyle = MaterialTheme.typography.bodySmall,
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                )
            )

            Spacer(Modifier.height(12.dp))

            // Test API button
            Button(
                onClick = { viewModel.testApi() },
                modifier = Modifier.fillMaxWidth(),
                enabled = !state.isTesting,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.85f),
                )
            ) {
                if (state.isTesting) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary,
                    )
                    Spacer(Modifier.width(8.dp))
                    Text("Menguji...")
                } else {
                    Icon(Icons.Default.NetworkCheck, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Test API")
                }
            }

            // Test result
            state.testResult?.let { result ->
                Spacer(Modifier.height(6.dp))
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = if (result.startsWith("✅"))
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                    else MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.4f),
                    shape = RoundedCornerShape(8.dp),
                ) {
                    Text(
                        text = result,
                        modifier = Modifier.padding(10.dp),
                        style = MaterialTheme.typography.bodySmall,
                        color = if (result.startsWith("✅"))
                            MaterialTheme.colorScheme.onPrimaryContainer
                        else MaterialTheme.colorScheme.onErrorContainer,
                    )
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        // ── System Prompt ──
        SettingsGroup("📝 ${S.systemPrompt(state.uiLanguage)}") {
            OutlinedTextField(
                value = state.systemPrompt,
                onValueChange = { viewModel.setSystemPrompt(it) },
                modifier = Modifier.fillMaxWidth().heightIn(min = 100.dp),
                placeholder = { Text(S.systemPromptPlaceholder(state.uiLanguage)) },
                textStyle = MaterialTheme.typography.bodySmall.copy(lineHeight = 20.sp),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                )
            )
        }

        Spacer(Modifier.height(16.dp))

        // ── Theme ──
        SettingsGroup("🎨 ${S.theme(state.uiLanguage)}") {
            Text(
                S.theme(state.uiLanguage),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            // Theme chips
            val themes = listOf(
                RingkesinTheme.AI_NATIVE to "🌌",
                RingkesinTheme.OLED to "🖤",
                RingkesinTheme.LIGHT to "☀️",
                RingkesinTheme.VIBRANT to "🌈",
                RingkesinTheme.BIOPHILIC to "🌿",
                RingkesinTheme.GLASS to "🪟",
            )
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                themes.forEach { (theme, icon) ->
                    FilterChip(
                        selected = state.selectedTheme == theme,
                        onClick = {
                            viewModel.setTheme(theme)
                            onThemeChanged(theme)
                        },
                        label = {
                            Text("$icon ${theme.label}", fontSize = 11.sp)
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                            selectedLabelColor = MaterialTheme.colorScheme.primary,
                        )
                    )
                }
            }

            Spacer(Modifier.height(8.dp))

            // UI Language
            Text(
                S.uiLanguage(state.uiLanguage),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)
            )
            var langExpanded by remember { mutableStateOf(false) }
            val languages = mapOf("id" to "🇮🇩 Bahasa Indonesia", "en" to "🇺🇸 English")
            ExposedDropdownMenuBox(
                expanded = langExpanded,
                onExpandedChange = { langExpanded = it }
            ) {
                OutlinedTextField(
                    value = languages[state.uiLanguage] ?: "🇮🇩 Bahasa Indonesia",
                    onValueChange = {},
                    readOnly = true,
                    modifier = Modifier.fillMaxWidth().menuAnchor(),
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = langExpanded) },
                    textStyle = MaterialTheme.typography.bodyMedium,
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                    )
                )
                ExposedDropdownMenu(
                    expanded = langExpanded,
                    onDismissRequest = { langExpanded = false }
                ) {
                    languages.forEach { (code, name) ->
                        DropdownMenuItem(
                            text = { Text(name) },
                            onClick = {
                                viewModel.setUiLanguage(code)
                                langExpanded = false
                            }
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        // ── Recording ──
        SettingsGroup("🎤 ${S.recording(state.uiLanguage)}") {
            SettingsToggle(
                label = S.backgroundRecord(state.uiLanguage),
                checked = state.backgroundRecord,
                onToggle = { viewModel.toggleBackgroundRecord() }
            )
            SettingsToggle(
                label = S.autoSave(state.uiLanguage),
                checked = state.autoSave,
                onToggle = { viewModel.toggleAutoSave() }
            )
            SettingsToggle(
                label = S.notification(state.uiLanguage),
                checked = state.notificationOn,
                onToggle = { viewModel.toggleNotification() }
            )
        }

        Spacer(Modifier.height(16.dp))

        // ── Data ──
        SettingsGroup("💾 ${S.data(state.uiLanguage)}") {
            Surface(
                modifier = Modifier.fillMaxWidth().clickable { /* TODO: export */ },
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = MaterialTheme.shapes.small
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(S.exportAllData(state.uiLanguage), style = MaterialTheme.typography.bodyMedium)
                    Text("JSON", style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            Spacer(Modifier.height(4.dp))
            Surface(
                modifier = Modifier.fillMaxWidth().clickable { /* TODO: confirm delete */ },
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = MaterialTheme.shapes.small
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(S.deleteAllData(state.uiLanguage),
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium)
                    Text("❯",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall)
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        // ── About ──
        SettingsGroup("ℹ️ ${S.about(state.uiLanguage)}") {
            SettingsRow(label = S.version(state.uiLanguage), value = "3.1.0 · Android")
            SettingsRow(label = S.createdBy(state.uiLanguage), value = "MSAM-Team")
        }

        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun SettingsGroup(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Column {
        Text(
            title,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 8.dp, start = 4.dp)
        )
        content()
    }
}

@Composable
private fun SettingsRow(
    label: String,
    value: String,
    valueColor: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.onSurfaceVariant
) {
    Surface(
        modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        shape = MaterialTheme.shapes.small
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(label, style = MaterialTheme.typography.bodyMedium)
            Text(value, style = MaterialTheme.typography.bodySmall, color = valueColor)
        }
    }
}

@Composable
private fun SettingsToggle(
    label: String,
    checked: Boolean,
    onToggle: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        shape = MaterialTheme.shapes.small
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(label, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
            Switch(
                checked = checked,
                onCheckedChange = { onToggle() },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = MaterialTheme.colorScheme.primary,
                    checkedTrackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f),
                )
            )
        }
    }
}
