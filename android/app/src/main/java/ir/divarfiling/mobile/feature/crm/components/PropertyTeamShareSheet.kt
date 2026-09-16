package ir.divarfiling.mobile.feature.crm.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import ir.divarfiling.mobile.core.design.AppSpacing
import ir.divarfiling.mobile.core.design.AppTypography
import ir.divarfiling.mobile.core.design.DfIcons
import ir.divarfiling.mobile.core.design.DfThemeColors
import ir.divarfiling.mobile.core.design.components.DfCardListSkeleton
import ir.divarfiling.mobile.core.design.components.DfConfirmBottomSheet
import ir.divarfiling.mobile.core.design.components.DfDecorIcons
import ir.divarfiling.mobile.core.design.components.DfEmptyState
import ir.divarfiling.mobile.core.design.components.DfEmptyVariant
import ir.divarfiling.mobile.core.design.components.DfModalBottomSheet
import ir.divarfiling.mobile.core.design.components.DfSheetOptionRow
import ir.divarfiling.mobile.core.design.components.DfSheetScaffold
import ir.divarfiling.mobile.core.design.components.DfTextField
import ir.divarfiling.mobile.core.network.PropertyTeamSharesData
import ir.divarfiling.mobile.core.network.TeamMemberDto

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PropertyTeamShareSheet(
    visible: Boolean,
    data: PropertyTeamSharesData?,
    loading: Boolean,
    memberQuery: String,
    onMemberQueryChange: (String) -> Unit,
    onGrant: (memberId: Long, shareMode: String) -> Unit,
    onRevoke: (shareId: Long) -> Unit,
    onDismiss: () -> Unit,
) {
    if (!visible) return
    var pendingRevoke by remember { mutableStateOf<Long?>(null) }
    DfModalBottomSheet(onDismissRequest = onDismiss) {
        DfSheetScaffold(
            title = "اشتراک با تیم",
            subtitle = "اعضای آژانس با سطح دسترسی مناسب",
            iconRes = DfDecorIcons.Users,
            onClose = onDismiss,
            bodyHeightFraction = 0.7f,
        ) {
            when {
                loading && data == null -> DfCardListSkeleton(count = 3)
                data?.canShare != true -> {
                    Text(
                        text = "فقط مالک فایل با لایسنس آژانس می‌تواند با هم‌تیمی به اشتراک بگذارد.",
                        style = AppTypography.bodyDescription,
                        color = DfThemeColors.textMuted(),
                    )
                }
                else -> {
                    DfTextField(
                        value = memberQuery,
                        onValueChange = onMemberQueryChange,
                        modifier = Modifier.fillMaxWidth(),
                        label = "جستجوی عضو",
                        placeholder = "نام مشاور…",
                    )
                    Text(
                        text = "دارای دسترسی",
                        style = AppTypography.labelLarge,
                        fontWeight = FontWeight.SemiBold,
                    )
                    if (data?.shares.isNullOrEmpty()) {
                        DfEmptyState(
                            title = "هنوز با کسی به اشتراک گذاشته نشده",
                            subtitle = "عضو تیم را از لیست زیر انتخاب کنید",
                            variant = DfEmptyVariant.Empty,
                        )
                    } else {
                        data?.shares.orEmpty().forEach { share ->
                            Column(modifier = Modifier.fillMaxWidth()) {
                                Text(text = share.name, fontWeight = FontWeight.SemiBold)
                                Text(
                                    text = share.shareModeLabel.ifBlank { share.shareMode },
                                    style = AppTypography.labelSmall,
                                    color = DfThemeColors.textMuted(),
                                )
                                TextButton(onClick = { pendingRevoke = share.id }) {
                                    Text("لغو دسترسی", color = DfThemeColors.error())
                                }
                            }
                        }
                    }
                    Text(
                        text = "افزودن عضو",
                        style = AppTypography.labelLarge,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(top = AppSpacing.sm),
                    )
                    val members = data?.members.orEmpty().filter { member ->
                        val q = memberQuery.trim()
                        q.isBlank() || member.name.contains(q, ignoreCase = true)
                    }
                    if (members.isEmpty()) {
                        Text(
                            text = "عضوی با این نام پیدا نشد",
                            style = AppTypography.labelSmall,
                            color = DfThemeColors.textMuted(),
                        )
                    } else {
                        members.forEach { member: TeamMemberDto ->
                            DfSheetOptionRow(
                                label = member.name,
                                selected = false,
                                onClick = {
                                    val mode = data?.shareModes?.firstOrNull()?.value ?: "confidential"
                                    onGrant(member.id, mode)
                                },
                                icon = DfIcons.UserPlus,
                            )
                        }
                    }
                }
            }
        }
    }
    pendingRevoke?.let { shareId ->
        DfConfirmBottomSheet(
            title = "حذف دسترسی",
            message = "دسترسی این هم‌تیمی حذف شود؟",
            confirmText = "حذف",
            destructive = true,
            onConfirm = {
                onRevoke(shareId)
                pendingRevoke = null
            },
            onDismiss = { pendingRevoke = null },
        )
    }
}
