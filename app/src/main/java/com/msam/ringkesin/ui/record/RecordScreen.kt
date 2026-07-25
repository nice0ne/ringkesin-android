package com.msam.ringkesin.ui.record

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.viewmodel.compose.viewModel
import com.msam.ringkesin.ui.localization.S
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecordScreen(
    viewModel: RecordViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsState()

    // ── Permission Launcher ──
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        viewModel.onPermissionResult(granted)
    }

    LaunchedEffect(state.isPermissionGranted) {
        if (state.isPermissionGranted == false) {
            permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }
    }

    // Check for restored data from History every time screen resumes
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.checkRestoredData()
                viewModel.reloadUiLanguage()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    // Timer tick — di composable, bukan ViewModel, jadi mati/hidup sesuai lifecycle screen
    if (state.isRecording) {
        LaunchedEffect(Unit) {
            while (true) {
                delay(1000)
                viewModel.tick()
            }
        }
    }

    val primaryColor = MaterialTheme.colorScheme.primary
    val surfaceColor = MaterialTheme.colorScheme.surface
    val onSurfaceColor = MaterialTheme.colorScheme.onSurface

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        // ════════════════════════════════════════
        // 🎤 HEADER CARD
        // ════════════════════════════════════════
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = CircleShape,
                        color = primaryColor.copy(alpha = 0.15f),
                        modifier = Modifier.size(36.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text("🎙️", fontSize = 18.sp)
                        }
                    }
                    Spacer(Modifier.width(10.dp))
                    Column {
                        Text(
                            "Ringkesin",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = onSurfaceColor
                        )
                        Text(
                            "Speech-to-Text & AI Summarizer",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Language selector
                var expanded by remember { mutableStateOf(false) }
                val languages = mapOf(
                    "en-US" to "🇺🇸 EN",
                    "id-ID" to "🇮🇩 ID",
                    "ja-JP" to "🇯🇵 JP"
                )

                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = it }
                ) {
                    SuggestionChip(
                        onClick = { expanded = true },
                        label = {
                            Text(
                                languages[state.selectedLanguage] ?: "🇺🇸 EN",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.SemiBold
                            )
                        },
                        modifier = Modifier.menuAnchor(),
                        shape = RoundedCornerShape(20.dp),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
                    )
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        languages.forEach { (code, name) ->
                            DropdownMenuItem(
                                text = { Text(name, fontWeight = FontWeight.Medium) },
                                onClick = {
                                    viewModel.setLanguage(code)
                                    expanded = false
                                }
                            )
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(20.dp))

        // ════════════════════════════════════════
        // 🎤 MIC / RECORDING CARD
        // ════════════════════════════════════════
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = when {
                    state.isRecording -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                    else -> MaterialTheme.colorScheme.surface
                }
            ),
            elevation = CardDefaults.cardElevation(
                defaultElevation = if (state.isRecording) 6.dp else 3.dp
            ),
            border = BorderStroke(
                width = 1.dp,
                color = when {
                    state.isRecording -> MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                    else -> MaterialTheme.colorScheme.outlineVariant
                }
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // ── Pulsing ring when recording ──
                val isRecording = state.isRecording

                Box(contentAlignment = Alignment.Center) {
                    // Outer glow ring when recording
                    if (isRecording) {
                        Surface(
                            modifier = Modifier.size(104.dp),
                            shape = CircleShape,
                            color = primaryColor.copy(alpha = 0.06f)
                        ) {}
                        Surface(
                            modifier = Modifier.size(92.dp),
                            shape = CircleShape,
                            color = primaryColor.copy(alpha = 0.1f)
                        ) {}
                    }

                    // Mic button
                    Surface(
                        modifier = Modifier.size(80.dp),
                        shape = CircleShape,
                        color = when {
                            isRecording -> primaryColor
                            else -> surfaceColor
                        },
                        border = BorderStroke(
                            width = 1.5.dp,
                            color = when {
                                isRecording -> primaryColor
                                else -> MaterialTheme.colorScheme.outline.copy(alpha = 0.6f)
                            }
                        ),
                        shadowElevation = if (isRecording) 8.dp else 2.dp
                    ) {
                        FilledIconButton(
                            onClick = { viewModel.toggleRecording() },
                            modifier = Modifier.fillMaxSize(),
                            shape = CircleShape,
                            colors = IconButtonDefaults.filledIconButtonColors(
                                containerColor = Color.Transparent,
                                contentColor = when {
                                    isRecording -> Color.White
                                    else -> primaryColor
                                }
                            )
                        ) {
                            Icon(
                                imageVector = if (isRecording) Icons.Default.Stop
                                else Icons.Default.Mic,
                                contentDescription = if (isRecording) "Berhenti" else "Mulai",
                                modifier = Modifier.size(36.dp)
                            )
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))

                // Timer
                Text(
                    text = formatTimer(state.timerSeconds),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = onSurfaceColor,
                    fontSize = if (state.timerSeconds >= 3600) 28.sp else 36.sp
                )

                Spacer(Modifier.height(6.dp))

                // Status label or error
                val errorMsg = state.errorMessage
                if (errorMsg != null) {
                    SuggestionChip(
                        onClick = {},
                        label = {
                            Text(
                                text = errorMsg,
                                style = MaterialTheme.typography.bodySmall
                            )
                        },
                        colors = SuggestionChipDefaults.suggestionChipColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer,
                            labelColor = MaterialTheme.colorScheme.onErrorContainer
                        ),
                        shape = RoundedCornerShape(20.dp)
                    )
                } else {
                    Text(
                        text = state.statusText,
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (isRecording) primaryColor
                        else MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = if (isRecording) FontWeight.Medium else FontWeight.Normal,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        Spacer(Modifier.height(20.dp))

        // ════════════════════════════════════════
        // 📋 ACTION ROW
        // ════════════════════════════════════════
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf(
                Triple(Icons.Default.ContentCopy, S.copy(state.uiLanguage),
                    { viewModel.copyToClipboard() }),
                Triple(Icons.Default.Save, S.save(state.uiLanguage),
                    { viewModel.saveTranscript() }),
                Triple(Icons.Default.Delete, S.delete(state.uiLanguage),
                    { viewModel.clearTranscript() }),
            ).forEach { (icon, label, onClick) ->
                OutlinedButton(
                    onClick = onClick,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    border = BorderStroke(
                        1.dp,
                        MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                    )
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier.padding(vertical = 4.dp)
                    ) {
                        Icon(icon, contentDescription = label, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.height(2.dp))
                        Text(label, fontSize = 11.sp, fontWeight = FontWeight.Medium)
                    }
                }
            }
        }

        Spacer(Modifier.height(24.dp))

        // ════════════════════════════════════════
        // 📝 TRANSCRIPT AREA (auto-scroll)
        // ════════════════════════════════════════
        val transcript by remember { derivedStateOf {
            state.transcript + if (state.partialText.isNotBlank()) " ${state.partialText}" else ""
        } }
        val scrollState = rememberScrollState()
        var editMode by remember { mutableStateOf(false) }

        // Auto-scroll saat transcript bertambah
        LaunchedEffect(transcript) {
            if (transcript.isNotBlank()) {
                scrollState.animateScrollTo(scrollState.maxValue)
            }
        }

        Card(
            modifier = Modifier.weight(1f).fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            border = BorderStroke(
                1.dp,
                when {
                    editMode -> primaryColor
                    transcript.isNotBlank() -> MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
                    else -> MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)
                }
            )
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                if (editMode) {
                    // ── Edit mode ──
                    BasicTextField(
                        value = state.transcript,
                        onValueChange = { viewModel.updateTranscript(it) },
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(14.dp),
                        textStyle = MaterialTheme.typography.bodyMedium.copy(
                            lineHeight = 24.sp,
                            color = onSurfaceColor
                        ),
                    )
                } else {
                    // ── Display mode ──
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(scrollState)
                            .padding(14.dp)
                    ) {
                        if (transcript.isBlank()) {
                            Column(
                                modifier = Modifier.fillMaxSize().padding(vertical = 32.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.EditNote,
                                    contentDescription = null,
                                    modifier = Modifier.size(40.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
                                )
                                Spacer(Modifier.height(8.dp))
                                Text(
                                    text = if (state.isRecording) S.startSpeaking(state.uiLanguage)
                                    else S.transcriptWillAppearHere(state.uiLanguage),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                                    textAlign = TextAlign.Center
                                )
                            }
                        } else {
                            SelectionContainer {
                                Text(
                                    text = transcript,
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        lineHeight = 24.sp,
                                        color = onSurfaceColor
                                    )
                                )
                            }
                        }
                    }
                }

                // Edit toggle button
                if (!state.isRecording && transcript.isNotBlank()) {
                    FilledTonalIconButton(
                        onClick = { editMode = !editMode },
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(6.dp)
                            .size(32.dp),
                        shape = CircleShape
                    ) {
                        Icon(
                            imageVector = if (editMode) Icons.Default.Close else Icons.Default.Edit,
                            contentDescription = if (editMode) "Selesai edit" else "Edit",
                            modifier = Modifier.size(16.dp),
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        // ════════════════════════════════════════
        // ✅ CONFIRMATION SNACKBAR
        // ════════════════════════════════════════
        state.saveConfirmation?.let { msg ->
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f),
                shadowElevation = 2.dp
            ) {
                Text(
                    text = msg,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
            LaunchedEffect(msg) {
                delay(2500)
                viewModel.clearConfirmation()
            }
        }

        Spacer(Modifier.height(if (state.summaryResult != null) 16.dp else 0.dp))

        // ════════════════════════════════════════
        // 📄 SUMMARY RESULT CARD
        // ════════════════════════════════════════
        if (state.summaryResult != null) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            Icons.Default.AutoAwesome,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                            tint = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            "Ringkasan AI",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(Modifier.weight(1f))
                        IconButton(
                            onClick = { viewModel.clearSummary() },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                Icons.Default.Close,
                                contentDescription = "Tutup",
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                    val summaryText = state.summaryResult
                    if (summaryText != null) {
                        Spacer(Modifier.height(8.dp))
                        SelectionContainer {
                            Text(
                                text = summaryText,
                            style = MaterialTheme.typography.bodySmall.copy(lineHeight = 20.sp),
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                }
            }
            Spacer(Modifier.height(16.dp))
            }
        }

        // ════════════════════════════════════════
        // ✨ SUMMARIZE BUTTON
        // ════════════════════════════════════════
        Button(
            onClick = { viewModel.summarizeText() },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            enabled = state.transcript.isNotBlank() && !state.isSummarizing,
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = primaryColor,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                disabledContainerColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f),
                disabledContentColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
            ),
            elevation = ButtonDefaults.buttonElevation(
                defaultElevation = 2.dp,
                pressedElevation = 4.dp
            )
        ) {
            if (state.isSummarizing) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    strokeWidth = 2.dp,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            } else {
                Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(20.dp))
            }
            Spacer(Modifier.width(8.dp))
            Text(
                if (state.isSummarizing) S.summarizing(state.uiLanguage) else S.summarize(state.uiLanguage),
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp
            )
        }

        Spacer(Modifier.height(8.dp))
    }
}

private fun formatTimer(seconds: Int): String {
    val h = seconds / 3600
    val m = (seconds % 3600) / 60
    val s = seconds % 60
    return if (h > 0) String.format("%d:%02d:%02d", h, m, s)
    else String.format("%02d:%02d", m, s)
}
