package com.example.careerpilot.ui.components

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.careerpilot.data.repository.ResumeParser
import com.example.careerpilot.data.repository.SamplePdfResume
import com.example.careerpilot.ui.theme.*
import com.example.careerpilot.ui.viewmodel.CareerViewModel
import com.example.careerpilot.ui.viewmodel.ResumeUploadState
import com.example.careerpilot.ui.viewmodel.UploadedPdfInfo

/**
 * Mobile-first native Resume File Picker & Drag-and-Drop Dropzone Component.
 * Supports PDF, TXT, DOCX file selection, drag hover feedback, multi-stage
 * parsing progress, rich candidate preview, and explicit error recovery states.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ResumeFilePickerDropZone(
    viewModel: CareerViewModel,
    modifier: Modifier = Modifier,
    onViewAuditReport: (() -> Unit)? = null,
    onSwitchToRawText: (() -> Unit)? = null
) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val uploadState by viewModel.resumeUploadState.collectAsState()
    val uploadedPdf by viewModel.uploadedPdfInfo.collectAsState()

    // Native Activity Result Launcher for Document & PDF Picking
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            viewModel.importResumeFromPdfUri(uri, context)
        }
    }

    var isSimulatedHover by remember { mutableStateOf(false) }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        AnimatedContent(
            targetState = uploadState,
            transitionSpec = {
                fadeIn(animationSpec = tween(250)) togetherWith fadeOut(animationSpec = tween(200))
            },
            label = "ResumeUploadStateTransition"
        ) { state ->
            when (state) {
                is ResumeUploadState.Idle -> {
                    IdleFilePickerDropZone(
                        isHovering = isSimulatedHover,
                        onHoverChange = { isSimulatedHover = it },
                        onPickFile = {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            filePickerLauncher.launch("application/pdf")
                        },
                        onSelectSample = { sample ->
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            viewModel.importResumeFromSamplePdf(sample)
                        },
                        onSwitchToRawText = onSwitchToRawText
                    )
                }

                is ResumeUploadState.DragHover -> {
                    ActiveDragDropZone(
                        onDrop = {
                            viewModel.setDragHovering(false)
                            filePickerLauncher.launch("application/pdf")
                        },
                        onCancel = { viewModel.setDragHovering(false) }
                    )
                }

                is ResumeUploadState.Processing -> {
                    ProcessingUploadDropZone(
                        state = state,
                        onCancel = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            viewModel.resetResumeUploadState()
                        }
                    )
                }

                is ResumeUploadState.Success -> {
                    SuccessUploadDropZone(
                        state = state,
                        onPickDifferentFile = {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            filePickerLauncher.launch("application/pdf")
                        },
                        onClear = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            viewModel.clearUploadedPdf()
                        },
                        onViewAuditReport = onViewAuditReport
                    )
                }

                is ResumeUploadState.Error -> {
                    ErrorUploadDropZone(
                        state = state,
                        onRetry = {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            viewModel.retryLastPdfUpload(context)
                        },
                        onPickNewFile = {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            filePickerLauncher.launch("application/pdf")
                        },
                        onSelectSample = { sample ->
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            viewModel.importResumeFromSamplePdf(sample)
                        },
                        onSwitchToRawText = onSwitchToRawText
                    )
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// STATE 1: IDLE / READY DROPZONE
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun IdleFilePickerDropZone(
    isHovering: Boolean,
    onHoverChange: (Boolean) -> Unit,
    onPickFile: () -> Unit,
    onSelectSample: (SamplePdfResume) -> Unit,
    onSwitchToRawText: (() -> Unit)?
) {
    val scaleAnim by animateFloatAsState(
        targetValue = if (isHovering) 1.02f else 1.0f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "DropzoneScale"
    )

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Main Interactive File Dropzone Card
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .scale(scaleAnim)
                .testTag("resume_file_dropzone_idle")
                .clip(RoundedCornerShape(12.dp))
                .border(
                    BorderStroke(
                        width = if (isHovering) 2.dp else 1.dp,
                        color = if (isHovering) PrimaryBlueLighter else BorderSubtle
                    ),
                    shape = RoundedCornerShape(12.dp)
                )
                .pointerInput(Unit) {
                    detectTapGestures(
                        onPress = {
                            onHoverChange(true)
                            tryAwaitRelease()
                            onHoverChange(false)
                        },
                        onTap = { onPickFile() }
                    )
                },
            color = if (isHovering) BgSurfaceElevated else BgCard,
            tonalElevation = 4.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Upload Hero Icon with Pulsing Halo
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(PrimaryBlue.copy(alpha = 0.12f))
                        .border(1.dp, PrimaryBlue.copy(alpha = 0.3f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.CloudUpload,
                        contentDescription = "Upload Resume",
                        tint = AccentCyan,
                        modifier = Modifier.size(32.dp)
                    )
                }

                Text(
                    text = "Drop Resume File or Browse",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = "Upload PDF to extract technical skills, experience & calculate ATS compatibility",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 12.dp)
                )

                // Format & Size Badges
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    StatusBadge(text = "PDF (.pdf)", statusType = "primary")
                    StatusBadge(text = "TEXT (.txt)", statusType = "info")
                    StatusBadge(text = "MAX 10 MB", statusType = "success")
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Primary 48dp Touch Target CTA Button
                Button(
                    onClick = onPickFile,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PrimaryBlue,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .defaultMinSize(minHeight = 48.dp)
                        .testTag("browse_resume_files_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.FolderOpen,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Select Resume from Device",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
            }
        }

        // Fast-Track Sample Resumes Row
        GlassCard(
            modifier = Modifier.fillMaxWidth(),
            borderColor = BorderSubtle
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "1-Tap Preloaded Candidate Resumes",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = "Test instant PDF parsing & ATS auditing without uploading files",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextMuted
                    )
                }
                Icon(
                    imageVector = Icons.Default.Bolt,
                    contentDescription = null,
                    tint = WarningAmber,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                ResumeParser.SAMPLE_PDF_RESUMES.forEach { sample ->
                    Surface(
                        onClick = { onSelectSample(sample) },
                        color = BgSurfaceElevated,
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, BorderSubtle),
                        modifier = Modifier
                            .fillMaxWidth()
                            .defaultMinSize(minHeight = 48.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(DangerRed.copy(alpha = 0.15f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.PictureAsPdf,
                                        contentDescription = null,
                                        tint = DangerRed,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                Column {
                                    Text(
                                        text = sample.title,
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimary
                                    )
                                    Text(
                                        text = "${sample.fileName} • ${sample.fileSizeFormatted} • ${sample.pageCount} pgs",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = TextSecondary
                                    )
                                }
                            }

                            Text(
                                text = "Load PDF",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = PrimaryBlueGlow
                            )
                        }
                    }
                }
            }

            if (onSwitchToRawText != null) {
                Spacer(modifier = Modifier.height(8.dp))
                TextButton(
                    onClick = onSwitchToRawText,
                    modifier = Modifier
                        .fillMaxWidth()
                        .defaultMinSize(minHeight = 44.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.EditNote,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = AccentCyan
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Or paste raw resume text manually",
                        color = AccentCyan,
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// STATE 2: ACTIVE DRAG-OVER STATE
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun ActiveDragDropZone(
    onDrop: () -> Unit,
    onCancel: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .border(
                BorderStroke(
                    width = 2.dp,
                    color = PrimaryBlue
                ),
                shape = RoundedCornerShape(12.dp)
            )
            .clickable { onDrop() }
            .testTag("resume_dropzone_drag_active"),
        color = BgSurfaceElevated,
        tonalElevation = 8.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(CircleShape)
                    .background(AccentCyan.copy(alpha = 0.2f))
                    .border(2.dp, AccentCyan, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowDownward,
                    contentDescription = null,
                    tint = AccentCyan,
                    modifier = Modifier.size(36.dp)
                )
            }

            Text(
                text = "Release to Scan Resume",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.ExtraBold,
                color = AccentCyan
            )

            Text(
                text = "Drop your PDF file here to initiate ATS extraction & Gemini parsing",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary,
                textAlign = TextAlign.Center
            )

            OutlinedButton(
                onClick = onCancel,
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = TextMuted)
            ) {
                Text("Cancel")
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// STATE 3: PROCESSING / UPLOADING STATE
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun ProcessingUploadDropZone(
    state: ResumeUploadState.Processing,
    onCancel: () -> Unit
) {
    val animatedProgress by animateFloatAsState(
        targetValue = state.progress,
        animationSpec = tween(400, easing = FastOutSlowInEasing),
        label = "UploadProgressAnim"
    )

    GlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("resume_dropzone_processing"),
        borderColor = PrimaryBlueGlow.copy(alpha = 0.7f)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                CircularProgressIndicator(
                    progress = { animatedProgress },
                    color = AccentCyan,
                    trackColor = BorderSubtle,
                    strokeWidth = 3.5.dp,
                    modifier = Modifier.size(36.dp)
                )
                Column {
                    Text(
                        text = "Analyzing Resume Document",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = state.fileName,
                        style = MaterialTheme.typography.labelMedium,
                        color = PrimaryBlueGlow,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Text(
                text = "${(animatedProgress * 100).toInt()}%",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = AccentCyan
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Progress Bar
        LinearProgressIndicator(
            progress = { animatedProgress },
            color = AccentCyan,
            trackColor = BgMuted,
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp))
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Status step detail
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = Icons.Default.AutoFixHigh,
                contentDescription = null,
                tint = AccentPurple,
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = state.step,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Medium,
                color = TextPrimary
            )
        }

        if (state.details.isNotBlank()) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = state.details,
                style = MaterialTheme.typography.labelSmall,
                color = TextMuted,
                modifier = Modifier.padding(start = 24.dp)
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Pipeline Stages Visual Indicator
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            PipelineStagePill(label = "Stream", isActive = state.progress >= 0.25f)
            PipelineStagePill(label = "Extract", isActive = state.progress >= 0.55f)
            PipelineStagePill(label = "AI Parse", isActive = state.progress >= 0.75f)
            PipelineStagePill(label = "Audit", isActive = state.progress >= 0.90f)
        }

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedButton(
            onClick = onCancel,
            shape = RoundedCornerShape(10.dp),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = TextSecondary),
            modifier = Modifier
                .fillMaxWidth()
                .defaultMinSize(minHeight = 44.dp)
        ) {
            Text("Cancel Extraction", fontSize = 12.sp)
        }
    }
}

@Composable
private fun PipelineStagePill(label: String, isActive: Boolean) {
    Surface(
        color = if (isActive) PrimaryBlue.copy(alpha = 0.2f) else BgSurface,
        shape = RoundedCornerShape(6.dp),
        border = BorderStroke(
            1.dp,
            if (isActive) PrimaryBlueGlow.copy(alpha = 0.5f) else BorderSubtle
        )
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            if (isActive) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = PrimaryBlueGlow,
                    modifier = Modifier.size(11.dp)
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(4.dp)
                        .clip(CircleShape)
                        .background(TextMuted)
                )
            }
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = if (isActive) PrimaryBlueGlow else TextMuted,
                fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// STATE 4: SUCCESS STATE WITH RICH PREVIEW
// ─────────────────────────────────────────────────────────────────────────────
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun SuccessUploadDropZone(
    state: ResumeUploadState.Success,
    onPickDifferentFile: () -> Unit,
    onClear: () -> Unit,
    onViewAuditReport: (() -> Unit)?
) {
    val info = state.info
    val parsed = info.parsedData

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Success Header Banner
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .border(
                    BorderStroke(1.dp, SuccessGreen.copy(alpha = 0.6f)),
                    shape = RoundedCornerShape(14.dp)
                )
                .testTag("resume_upload_success_card"),
            color = BgSurfaceElevated,
            tonalElevation = 4.dp
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                // Header with File Info
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(SuccessGreen.copy(alpha = 0.15f))
                                .border(1.dp, SuccessGreen.copy(alpha = 0.5f), RoundedCornerShape(10.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = "Success",
                                tint = SuccessGreen,
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        Column {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = info.fileName,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                StatusBadge(text = "PARSED", statusType = "success")
                            }
                            Text(
                                text = "${info.fileSizeFormatted} • ${info.pageCount} page(s) • ${info.charCount} chars",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextSecondary
                            )
                        }
                    }

                    IconButton(
                        onClick = onClear,
                        modifier = Modifier.defaultMinSize(minHeight = 48.dp, minWidth = 48.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Remove File",
                            tint = TextMuted
                        )
                    }
                }

                // Extracted Candidate Profile Snapshot
                if (parsed != null) {
                    Spacer(modifier = Modifier.height(14.dp))
                    HorizontalDivider(color = BorderSubtle)
                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Extracted Candidate Profile:",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = AccentCyan
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Candidate Name", fontSize = 10.sp, color = TextMuted)
                            Text(
                                text = parsed.fullName.ifBlank { "Candidate" },
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary,
                                maxLines = 1
                            )
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Extracted Target Role", fontSize = 10.sp, color = TextMuted)
                            Text(
                                text = parsed.targetRole.ifBlank { "Software Engineer" },
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = PrimaryBlueGlow,
                                maxLines = 1
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Education", fontSize = 10.sp, color = TextMuted)
                            Text(
                                text = parsed.education.ifBlank { "B.S. in Computer Science" },
                                style = MaterialTheme.typography.bodySmall,
                                color = TextPrimary,
                                maxLines = 1
                            )
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Experience Detected", fontSize = 10.sp, color = TextMuted)
                            Text(
                                text = "${parsed.experienceYears} Years",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold,
                                color = SuccessGreen
                            )
                        }
                    }

                    // Detected Skills Chips
                    if (parsed.skillsDetected.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "Detected Technical Skills (${parsed.skillsDetected.size}):",
                            fontSize = 11.sp,
                            color = TextMuted
                        )
                        Spacer(modifier = Modifier.height(6.dp))

                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            parsed.skillsDetected.take(12).forEach { skill ->
                                Surface(
                                    color = PrimaryBlue.copy(alpha = 0.15f),
                                    shape = RoundedCornerShape(6.dp),
                                    border = BorderStroke(1.dp, PrimaryBlueGlow.copy(alpha = 0.3f))
                                ) {
                                    Text(
                                        text = skill,
                                        color = TextPrimary,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Medium,
                                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                                    )
                                }
                            }
                            if (parsed.skillsDetected.size > 12) {
                                Surface(
                                    color = BgSurface,
                                    shape = RoundedCornerShape(6.dp),
                                    border = BorderStroke(1.dp, BorderSubtle)
                                ) {
                                    Text(
                                        text = "+${parsed.skillsDetected.size - 12} more",
                                        color = AccentCyan,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Action Buttons with 48dp Minimum Interactive Size
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    if (onViewAuditReport != null) {
                        Button(
                            onClick = onViewAuditReport,
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .weight(1.5f)
                                .defaultMinSize(minHeight = 48.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Assessment,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("View ATS Score", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }
                        }
                    }

                    OutlinedButton(
                        onClick = onPickDifferentFile,
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = AccentCyan),
                        modifier = Modifier
                            .weight(1f)
                            .defaultMinSize(minHeight = 48.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Replace File", fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// STATE 5: CLEAR ERROR & RECOVERY STATE
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun ErrorUploadDropZone(
    state: ResumeUploadState.Error,
    onRetry: () -> Unit,
    onPickNewFile: () -> Unit,
    onSelectSample: (SamplePdfResume) -> Unit,
    onSwitchToRawText: (() -> Unit)?
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .border(
                BorderStroke(1.5.dp, DangerRed.copy(alpha = 0.8f)),
                shape = RoundedCornerShape(16.dp)
            )
            .testTag("resume_upload_error_card"),
        color = BgCard,
        tonalElevation = 6.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Error Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(DangerRed.copy(alpha = 0.18f))
                        .border(1.dp, DangerRed, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.ErrorOutline,
                        contentDescription = "Error",
                        tint = DangerRed,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Column {
                    Text(
                        text = "Resume Extraction Failed",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = DangerRed
                    )
                    Text(
                        text = state.errorMessage,
                        style = MaterialTheme.typography.bodySmall,
                        color = TextPrimary
                    )
                }
            }

            // Troubleshooting Helpful Guidelines
            Surface(
                color = BgSurfaceElevated,
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(1.dp, BorderSubtle),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = "Resolution Steps:",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = WarningAmber
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        listOf(
                            "Ensure the PDF contains selectable text (not a scanned image).",
                            "Check that the file is not password-protected or encrypted.",
                            "Try exporting directly from Google Docs, Word, or LaTeX as .PDF or .TXT."
                        ).forEach { tip ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(3.5.dp)
                                        .clip(CircleShape)
                                        .background(TextMuted)
                                )
                                Text(
                                    text = tip,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = TextSecondary,
                                    lineHeight = 16.sp
                                )
                            }
                        }
                    }
                }
            }

            // Action Recovery Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = onPickNewFile,
                    colors = ButtonDefaults.buttonColors(containerColor = DangerRed),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .weight(1f)
                        .defaultMinSize(minHeight = 48.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(Icons.Default.FolderOpen, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Select Another File", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }

                if (onSwitchToRawText != null) {
                    OutlinedButton(
                        onClick = onSwitchToRawText,
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = AccentCyan),
                        modifier = Modifier
                            .weight(1f)
                            .defaultMinSize(minHeight = 48.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(Icons.Default.TextFields, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Paste Text", fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }
}
