package ir.divarfiling.mobile.feature.ai.smartcommand

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import ir.divarfiling.mobile.core.design.AppShapes
import ir.divarfiling.mobile.core.design.AppSpacing
import ir.divarfiling.mobile.core.design.AppTypography
import ir.divarfiling.mobile.core.design.DfColors
import ir.divarfiling.mobile.core.design.DfIcons
import ir.divarfiling.mobile.core.design.components.DfPrimaryButton
import ir.divarfiling.mobile.core.design.components.DfSoftChip
import ir.divarfiling.mobile.core.network.SmartCommandExampleDto
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import ir.divarfiling.mobile.feature.ai.voice.VoiceInputPhase
import ir.divarfiling.mobile.feature.ai.voice.VoiceMicButton

private val ComposerShape = RoundedCornerShape(18.dp)
private val HeroShape = RoundedCornerShape(20.dp)

private object SmartAssistantBrushes {
    val heroGlow = Brush.linearGradient(
        colors = listOf(
            DfColors.Purple.copy(alpha = 0.18f),
            DfColors.PurpleContainer.copy(alpha = 0.08f),
            Color.Transparent,
        ),
    )
    val composerBorder = Brush.linearGradient(
        colors = listOf(
            DfColors.Purple.copy(alpha = 0.45f),
            DfColors.PurpleLight.copy(alpha = 0.25f),
            DfColors.Purple.copy(alpha = 0.35f),
        ),
    )
    val composerBorderListening = Brush.linearGradient(
        colors = listOf(
            DfColors.Purple.copy(alpha = 0.75f),
            DfColors.PurpleLight.copy(alpha = 0.55f),
            DfColors.Purple.copy(alpha = 0.65f),
        ),
    )
    val iconOrb = Brush.radialGradient(
        colors = listOf(
            DfColors.Purple.copy(alpha = 0.35f),
            DfColors.PurpleContainer.copy(alpha = 0.12f),
        ),
    )
}

@Composable
fun SmartCommandEntryHero(
    title: String,
    subtitle: String,
    placeholder: String,
    quotaHint: String?,
    enabled: Boolean,
    disabledMessage: String?,
    onOpen: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val alpha = if (enabled) 1f else 0.72f
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = AppSpacing.screenHorizontal)
            .alpha(alpha)
            .clip(HeroShape)
            .background(SmartAssistantBrushes.heroGlow)
            .border(1.dp, DfColors.Purple.copy(alpha = 0.12f), HeroShape)
            .padding(AppSpacing.md),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.md)) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(AppSpacing.md),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(CircleShape)
                        .background(SmartAssistantBrushes.iconOrb),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        DfIcons.Sparkles,
                        contentDescription = null,
                        tint = DfColors.Purple,
                        modifier = Modifier.size(26.dp),
                    )
                }
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        "دستیار هوشمند",
                        style = AppTypography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = DfColors.Purple,
                    )
                    Text(
                        title,
                        style = AppTypography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = DfColors.TextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        subtitle,
                        style = AppTypography.bodyDescription,
                        color = DfColors.TextSecondary,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
            SmartCommandInsetPanel {
                Text(
                    placeholder,
                    style = AppTypography.bodyDescription,
                    color = DfColors.TextMuted,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                quotaHint?.let { SmartCommandQuotaPill(text = it) } ?: Box(Modifier.size(1.dp))
                if (!enabled && !disabledMessage.isNullOrBlank()) {
                    Text(
                        disabledMessage,
                        style = AppTypography.labelSmall,
                        color = DfColors.TextSecondary,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.End,
                        maxLines = 2,
                    )
                }
            }
            if (enabled) {
                DfPrimaryButton(
                    text = "شروع گفتگو با دستیار",
                    onClick = onOpen,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}

enum class SmartCommandFlowStep { Compose, Analyze, Confirm }

@Composable
fun SmartCommandFlowStepper(
    activeStep: SmartCommandFlowStep,
    modifier: Modifier = Modifier,
) {
    val steps = listOf(
        SmartCommandFlowStep.Compose to "نوشتن",
        SmartCommandFlowStep.Analyze to "تحلیل",
        SmartCommandFlowStep.Confirm to "ثبت",
    )
    val activeIndex = steps.indexOfFirst { it.first == activeStep }.coerceAtLeast(0)
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        steps.forEachIndexed { index, (_, label) ->
            val isActive = index == activeIndex
            val isDone = index < activeIndex
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.weight(1f),
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(3.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(
                            when {
                                isActive -> DfColors.Purple
                                isDone -> DfColors.Purple.copy(alpha = 0.45f)
                                else -> DfColors.Outline.copy(alpha = 0.25f)
                            },
                        ),
                )
                Text(
                    label,
                    style = AppTypography.labelSmall,
                    fontWeight = if (isActive) FontWeight.SemiBold else FontWeight.Normal,
                    color = if (isActive || isDone) DfColors.Purple else DfColors.TextMuted,
                    maxLines = 1,
                )
            }
        }
    }
}

fun smartCommandFlowStep(
    phase: SmartCommandPhase,
    showConfirm: Boolean,
): SmartCommandFlowStep = when {
    phase == SmartCommandPhase.Parsing -> SmartCommandFlowStep.Analyze
    phase == SmartCommandPhase.Preview && showConfirm -> SmartCommandFlowStep.Confirm
    phase == SmartCommandPhase.Preview -> SmartCommandFlowStep.Analyze
    phase == SmartCommandPhase.Success -> SmartCommandFlowStep.Confirm
    else -> SmartCommandFlowStep.Compose
}

@Composable
fun SmartCommandComposer(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    fieldTitle: String,
    fieldHint: String?,
    minLines: Int,
    voicePhase: VoiceInputPhase,
    sttAvailable: Boolean,
    onMicClick: () -> Unit,
    onMicStop: () -> Unit,
    onMicCancel: () -> Unit,
    helperText: String?,
    charCounter: String?,
    examplesContent: (@Composable () -> Unit)?,
    modifier: Modifier = Modifier,
) {
    val listening = voicePhase == VoiceInputPhase.Listening
    val transition = rememberInfiniteTransition(label = "assistant_pulse")
    val pulse by transition.animateFloat(
        initialValue = 0.92f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "pulse",
    )
    val borderBrush = if (listening) {
        SmartAssistantBrushes.composerBorderListening
    } else {
        SmartAssistantBrushes.composerBorder
    }
    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(AppSpacing.sm)) {
        SmartCommandFieldLabel(text = fieldTitle, hint = fieldHint)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .alpha(if (listening) pulse else 1f)
                .clip(ComposerShape)
                .background(borderBrush)
                .padding(1.dp),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(ComposerShape)
                    .background(DfColors.Surface)
                    .padding(horizontal = AppSpacing.xs, vertical = AppSpacing.xxs),
                verticalArrangement = Arrangement.spacedBy(AppSpacing.xxs),
            ) {
                OutlinedTextField(
                    value = value,
                    onValueChange = onValueChange,
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = {
                        Text(
                            placeholder,
                            style = AppTypography.bodyDescription,
                            color = DfColors.TextMuted,
                        )
                    },
                    minLines = minLines,
                    maxLines = 8,
                    trailingIcon = if (sttAvailable) {
                        {
                            VoiceMicButton(
                                phase = voicePhase,
                                onClick = {
                                    if (listening) onMicStop() else onMicClick()
                                },
                                onCancel = onMicCancel,
                            )
                        }
                    } else {
                        null
                    },
                    shape = AppShapes.Field,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color.Transparent,
                        unfocusedBorderColor = Color.Transparent,
                        disabledBorderColor = Color.Transparent,
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        cursorColor = DfColors.Purple,
                        focusedTextColor = DfColors.TextPrimary,
                        unfocusedTextColor = DfColors.TextPrimary,
                    ),
                )
                if (listening) {
                    SmartCommandListeningBadge()
                }
                helperText?.let {
                    Text(
                        it,
                        style = AppTypography.labelSmall,
                        color = if (voicePhase == VoiceInputPhase.Error) DfColors.Rose else DfColors.TextMuted,
                        modifier = Modifier.padding(horizontal = AppSpacing.sm),
                    )
                }
                examplesContent?.invoke()
                charCounter?.let {
                    Text(
                        it,
                        style = AppTypography.labelSmall,
                        color = DfColors.TextMuted,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = AppSpacing.sm, vertical = AppSpacing.xxs),
                        textAlign = TextAlign.End,
                    )
                }
            }
        }
    }
}

@Composable
fun SmartCommandListeningBadge() {
    Row(
        modifier = Modifier
            .padding(horizontal = AppSpacing.sm)
            .clip(RoundedCornerShape(12.dp))
            .background(DfColors.PurpleContainer.copy(alpha = 0.45f))
            .padding(horizontal = AppSpacing.sm, vertical = AppSpacing.xxs),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(DfIcons.Mic, contentDescription = null, tint = DfColors.Purple, modifier = Modifier.size(14.dp))
        Text("در حال شنیدن…", style = AppTypography.labelSmall, color = DfColors.Purple, fontWeight = FontWeight.Medium)
    }
}

@Composable
fun SmartCommandExampleSuggestions(
    chips: List<SmartCommandExampleDto>,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (chips.isEmpty()) return
    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(AppSpacing.xs)) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(DfIcons.Sparkles, contentDescription = null, tint = DfColors.Purple, modifier = Modifier.size(14.dp))
            Text("ایده برای شروع", style = AppTypography.labelSmall, color = DfColors.TextMuted)
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs),
        ) {
            chips.forEach { example ->
                DfSoftChip(
                    text = example.chipLabel(),
                    selected = false,
                    onClick = { onSelect(example.commandText()) },
                )
            }
        }
    }
}

@Composable
fun SmartCommandSuccessBody(
    modifier: Modifier = Modifier,
) {
    SmartCommandInsetPanel(modifier = modifier) {
        Text(
            "انجام شد",
            style = AppTypography.labelLarge,
            fontWeight = FontWeight.Bold,
            color = DfColors.Success,
        )
        Text(
            "می‌توانید همین حالا نتیجه را ببینید یا بعداً از CRM پیگیری کنید.",
            style = AppTypography.bodyDescription,
            color = DfColors.TextSecondary,
        )
    }
}

@Composable
fun SmartCommandInsetPanel(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(AppShapes.Field)
            .background(DfColors.PurpleContainer.copy(alpha = 0.1f))
            .padding(horizontal = AppSpacing.sm, vertical = AppSpacing.sm),
        verticalArrangement = Arrangement.spacedBy(AppSpacing.xs),
        content = content,
    )
}

@Composable
fun SmartCommandFieldLabel(
    text: String,
    modifier: Modifier = Modifier,
    hint: String? = null,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        Text(
            text = text,
            style = AppTypography.labelLarge,
            fontWeight = FontWeight.SemiBold,
            color = DfColors.TextPrimary,
        )
        hint?.takeIf { it.isNotBlank() }?.let {
            Text(text = it, style = AppTypography.labelSmall, color = DfColors.TextMuted)
        }
    }
}

@Composable
fun SmartCommandQuotaPill(
    text: String,
    modifier: Modifier = Modifier,
) {
    Text(
        text = text,
        style = AppTypography.labelSmall,
        fontWeight = FontWeight.Medium,
        color = DfColors.Purple,
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(DfColors.PurpleContainer.copy(alpha = 0.4f))
            .padding(horizontal = AppSpacing.sm, vertical = AppSpacing.xxs),
    )
}

@Composable
fun SmartCommandStatusRow(
    message: String,
    modifier: Modifier = Modifier,
    showSpinner: Boolean = false,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(AppShapes.Chip)
            .background(DfColors.SurfaceVariant.copy(alpha = 0.5f))
            .padding(horizontal = AppSpacing.sm, vertical = AppSpacing.sm),
        horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (showSpinner) {
            androidx.compose.material3.CircularProgressIndicator(
                modifier = Modifier.size(16.dp),
                strokeWidth = 2.dp,
                color = DfColors.Purple,
            )
        } else {
            Icon(DfIcons.Sparkles, contentDescription = null, tint = DfColors.Purple, modifier = Modifier.size(16.dp))
        }
        Text(message, style = AppTypography.bodyDescription, color = DfColors.TextSecondary)
    }
}

@Composable
fun SmartCommandDetailGroup(
    title: String,
    modifier: Modifier = Modifier,
    accent: String? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
    ) {
        Box(
            modifier = Modifier
                .padding(top = 4.dp)
                .size(width = 3.dp, height = 36.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(
                    Brush.verticalGradient(
                        listOf(DfColors.Purple, DfColors.PurpleLight.copy(alpha = 0.5f)),
                    ),
                ),
        )
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.sm),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = title,
                    style = AppTypography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = DfColors.TextPrimary,
                )
                accent?.takeIf { it.isNotBlank() }?.let {
                    Text(text = it, style = AppTypography.labelSmall, color = DfColors.Amber)
                }
            }
            Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.xs), content = content)
        }
    }
}

@Composable
fun SmartCommandKeyValueRow(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
) {
    if (value.isBlank()) return
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(DfColors.SurfaceVariant.copy(alpha = 0.35f))
            .padding(horizontal = AppSpacing.sm, vertical = AppSpacing.xs),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top,
    ) {
        Text(
            label,
            style = AppTypography.labelSmall,
            color = DfColors.TextMuted,
            modifier = Modifier.weight(0.38f),
        )
        Text(
            value,
            style = AppTypography.bodyDescription,
            fontWeight = FontWeight.Medium,
            color = DfColors.TextPrimary,
            modifier = Modifier.weight(0.62f),
            textAlign = TextAlign.End,
        )
    }
}

@Composable
fun SmartCommandMessagePanel(
    title: String,
    body: String,
    modifier: Modifier = Modifier,
) {
    SmartCommandInsetPanel(modifier = modifier) {
        Text(title, style = AppTypography.labelLarge, fontWeight = FontWeight.SemiBold, color = DfColors.TextPrimary)
        Text(body, style = AppTypography.bodyDescription, color = DfColors.TextSecondary)
    }
}
