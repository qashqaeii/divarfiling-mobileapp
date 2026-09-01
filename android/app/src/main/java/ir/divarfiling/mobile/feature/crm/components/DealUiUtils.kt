package ir.divarfiling.mobile.feature.crm.components

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import ir.divarfiling.mobile.core.design.DfColors
import ir.divarfiling.mobile.core.design.DfIcons
import ir.divarfiling.mobile.core.network.DealStageDefDto
import kotlin.math.absoluteValue

object DealUiUtils {
    fun dealAccentColor(seed: String): Color {
        val palette = listOf(DfColors.Purple, DfColors.Blue, DfColors.Green, DfColors.Amber, DfColors.Rose)
        return palette[seed.hashCode().absoluteValue % palette.size]
    }

    fun hexColor(hex: String?, fallback: Color = DfColors.Purple): Color {
        if (hex.isNullOrBlank()) return fallback
        return parseFolderColor(hex)
    }

    fun dealStageColors(
        stage: String,
        def: DealStageDefDto? = null,
    ): Pair<Color, Color> {
        if (def != null) {
            return hexColor(def.color) to hexColor(def.bg, DfColors.SurfaceVariant)
        }
        return when {
            stage.contains("از دست") || stage.contains("سرد") -> DfColors.OverdueAccent to DfColors.RoseLight
            stage.contains("بسته") -> DfColors.Green to DfColors.GreenLight
            stage.contains("قرارداد") || stage.contains("پیش") -> DfColors.Pink to DfColors.PinkLight
            stage.contains("بازدید") -> DfColors.Blue to DfColors.BlueLight
            stage.contains("مذاکره") -> DfColors.Amber to DfColors.AmberLight
            stage == "سرنخ" || stage == "جدید" -> DfColors.Blue to DfColors.BlueLight
            else -> DfColors.Purple to DfColors.PurpleContainer
        }
    }

    fun stageIcon(stage: String, def: DealStageDefDto? = null): ImageVector {
        val key = def?.icon.orEmpty()
        if (key.isNotBlank()) return stageIconVector(key)
        return when {
            stage.contains("از دست") || stage.contains("سرد") -> DfIcons.TrendingDown
            stage.contains("بسته") -> DfIcons.Trophy
            stage.contains("قرارداد") || stage.contains("پیش") -> DfIcons.File
            stage.contains("بازدید") -> DfIcons.Building
            stage.contains("مذاکره") -> DfIcons.Handshake
            else -> DfIcons.Sparkles
        }
    }

    fun stageIconVector(faIcon: String): ImageVector = when (faIcon) {
        "fa-seedling", "fa-lightbulb" -> DfIcons.Lightbulb
        "fa-phone" -> DfIcons.Phone
        "fa-comments", "fa-comment-dots" -> DfIcons.MessageCircle
        "fa-handshake" -> DfIcons.Handshake
        "fa-door-open", "fa-building" -> DfIcons.Building
        "fa-calendar-check" -> DfIcons.Calendar
        "fa-file-signature", "fa-file-contract" -> DfIcons.File
        "fa-paper-plane" -> DfIcons.Send
        "fa-folder-open" -> DfIcons.Folder
        "fa-ellipsis" -> DfIcons.MoreVertical
        "fa-key" -> DfIcons.KeyRound
        "fa-coins" -> DfIcons.Coins
        "fa-star" -> DfIcons.Star
        "fa-bolt" -> DfIcons.Zap
        "fa-trophy" -> DfIcons.Trophy
        "fa-circle-xmark" -> DfIcons.TrendingDown
        else -> DfIcons.Handshake
    }

    fun stageDescription(stage: String, def: DealStageDefDto? = null): String {
        def?.description?.takeIf { it.isNotBlank() }?.let { return it }
        return when {
            stage.contains("از دست") || stage.contains("سرد") -> "فرصت از دست رفته یا سرد شده"
            stage.contains("بسته") -> "معامله با موفقیت بسته شد"
            stage.contains("قرارداد") -> "قرارداد نهایی در حال انجام"
            stage.contains("پیش") -> "پیش‌قرارداد یا توافق اولیه"
            stage.contains("بازدید") -> "بازدید ملک یا جلسه حضوری"
            stage.contains("مذاکره") -> "مذاکره قیمت و شرایط"
            stage == "سرنخ" || stage == "جدید" -> "سرنخ اولیه — نیاز به پیگیری"
            else -> "مرحله فعلی فرصت فروش"
        }
    }

    fun defFor(stage: String?, defs: List<DealStageDefDto>): DealStageDefDto? =
        defs.firstOrNull { it.name == stage }

    fun nextActionIcon(type: String?, faIcon: String? = null): ImageVector {
        if (!faIcon.isNullOrBlank()) return stageIconVector(faIcon)
        return when (type) {
            "call" -> DfIcons.Phone
            "message", "note" -> DfIcons.MessageCircle
            "send_file" -> DfIcons.Send
            "visit" -> DfIcons.Building
            "negotiation" -> DfIcons.Handshake
            "contract_followup" -> DfIcons.File
            "documents" -> DfIcons.Folder
            "commission" -> DfIcons.Coins
            else -> DfIcons.Clock
        }
    }

    fun timelineIcon(kind: String?, faIcon: String? = null, typeHint: String? = null): ImageVector {
        if (!faIcon.isNullOrBlank()) return stageIconVector(faIcon)
        return when (kind) {
            "deal_created" -> DfIcons.Handshake
            "deal_won" -> DfIcons.Trophy
            "deal_lost" -> DfIcons.TrendingDown
            "stage_change" -> DfIcons.RefreshCw
            "contract" -> DfIcons.File
            "follow_up" -> nextActionIcon(typeHint)
            else -> DfIcons.Clock
        }
    }

    fun timelineAccent(kind: String?): Color = when (kind) {
        "deal_won" -> DfColors.Green
        "deal_lost" -> DfColors.OverdueAccent
        "stage_change" -> DfColors.Purple
        "contract" -> DfColors.Pink
        "follow_up" -> DfColors.Blue
        else -> DfColors.TextMuted
    }
}
