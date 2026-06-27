package ir.divarfiling.mobile.feature.filing

import ir.divarfiling.mobile.R
import ir.divarfiling.mobile.core.image.ImageUrlFormatter
import ir.divarfiling.mobile.core.design.DfColors
import ir.divarfiling.mobile.core.design.DfIcons
import ir.divarfiling.mobile.core.design.FormatUtils
import ir.divarfiling.mobile.core.design.ListingMessageFormatter
import ir.divarfiling.mobile.core.design.components.DfActionButton
import ir.divarfiling.mobile.core.design.components.DfBadge
import ir.divarfiling.mobile.core.design.components.DfDetailSkeleton
import ir.divarfiling.mobile.core.design.components.DfEmptyState
import ir.divarfiling.mobile.core.design.components.DfErrorBanner
import ir.divarfiling.mobile.core.design.components.DfPremiumCard
import ir.divarfiling.mobile.core.design.components.DfPullRefresh
import ir.divarfiling.mobile.core.design.components.DfTopBar

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import ir.divarfiling.mobile.core.design.AppTypography
import ir.divarfiling.mobile.core.network.ListingDetailDto
import ir.divarfiling.mobile.feature.crm.ContactPickerSheet

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ListingDetailScreen(
    onBack: () -> Unit,
    viewModel: ListingDetailViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val listing = state.listing
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(state.pendingWhatsAppShare) {
        val message = state.pendingWhatsAppShare ?: return@LaunchedEffect
        val text = Uri.encode(message)
        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://wa.me/?text=$text")))
        viewModel.clearPendingWhatsAppShare()
    }

    LaunchedEffect(state.successMessage, state.error) {
        state.successMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessage()
        }
        state.error?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessage()
        }
    }

    Scaffold(
        topBar = {
            DfTopBar(
                title = listing?.title?.take(36) ?: "جزئیات آگهی",
                onBack = onBack,
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { padding ->
        DfPullRefresh(
            isRefreshing = state.isRefreshing,
            onRefresh = viewModel::refresh,
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(DfColors.Background),
        ) {
            when {
                state.isLoading -> DfDetailSkeleton()
                state.error != null && listing == null -> {
                    Column(Modifier.padding(16.dp)) { DfErrorBanner(state.error!!) }
                }
                listing != null -> {
                    ListingDetailContent(
                        listing = listing,
                        onSendToContact = { viewModel.toggleContactPicker(true) },
                        onOpenDivar = listing.shareLink?.takeIf { it.isNotBlank() }?.let { link ->
                            { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(link))) }
                        },
                        onShare = {
                            val message = ListingMessageFormatter.fromDetail(listing)
                            context.startActivity(
                                Intent.createChooser(
                                    Intent(Intent.ACTION_SEND).apply {
                                        type = "text/plain"
                                        putExtra(Intent.EXTRA_TEXT, message)
                                    },
                                    "اشتراک فایل",
                                ),
                            )
                        },
                    )
                }
            }
        }
    }

    if (state.showContactPicker) {
        ContactPickerSheet(
            onDismiss = { viewModel.toggleContactPicker(false) },
            onContactSelected = { contact -> viewModel.onContactSelectedForSend(contact.id) },
        )
    }

    if (state.showSendDialog) {
        AlertDialog(
            onDismissRequest = viewModel::dismissSendDialog,
            title = { Text("ارسال به مخاطب") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        "پیام حرفه‌ای بدون لینک دیوار ارسال می‌شود.",
                        style = MaterialTheme.typography.bodySmall,
                        color = DfColors.TextSecondary,
                    )
                    OutlinedTextField(
                        value = state.sendNote,
                        onValueChange = viewModel::onSendNoteChange,
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("یادداشت شخصی (اختیاری)") },
                        minLines = 3,
                        placeholder = { Text("مثلاً: برای بازدید فردا تماس بگیرید") },
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { viewModel.sendToContact(false) }, enabled = !state.isLinking) {
                    Text("ارسال")
                }
            },
            dismissButton = {
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    TextButton(
                        onClick = { viewModel.sendToContact(true) },
                        enabled = !state.isLinking,
                    ) {
                        Text("واتساپ")
                    }
                    TextButton(onClick = viewModel::dismissSendDialog) { Text("انصراف") }
                }
            },
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ListingDetailContent(
    listing: ListingDetailDto,
    onSendToContact: () -> Unit,
    onOpenDivar: (() -> Unit)?,
    onShare: () -> Unit,
) {
    val hero = ImageUrlFormatter.firstOf(
        listing.images.firstOrNull(),
        listing.thumbnailUrl,
    )
    val galleryImages = listing.images.mapNotNull { ImageUrlFormatter.normalize(it) }

    LazyColumn(
        contentPadding = PaddingValues(bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(0.dp),
    ) {
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(240.dp)
                    .background(DfColors.SurfaceVariant),
            ) {
                if (hero != null) {
                    AsyncImage(
                        model = hero,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop,
                    )
                } else {
                    Icon(
                        DfIcons.Building,
                        contentDescription = null,
                        modifier = Modifier
                            .align(Alignment.Center)
                            .size(64.dp),
                        tint = DfColors.TextMuted,
                    )
                }
            }
        }

        if (galleryImages.size > 1) {
            item {
                LazyRow(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    items(galleryImages) { url ->
                        AsyncImage(
                            model = url,
                            contentDescription = null,
                            modifier = Modifier
                                .size(72.dp)
                                .clip(RoundedCornerShape(10.dp)),
                            contentScale = ContentScale.Crop,
                        )
                    }
                }
            }
        }

        item {
            Column(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Text(
                    listing.title ?: "—",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                )
                val location = listOfNotNull(listing.district, listing.city).joinToString("، ")
                if (location.isNotBlank()) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(DfIcons.MapPin, null, tint = DfColors.Purple, modifier = Modifier.size(18.dp))
                        Text(location, style = AppTypography.bodyDescription, color = DfColors.TextSecondary)
                    }
                }
                listing.price?.takeIf { it > 0 }?.let {
                    Text(
                        FormatUtils.formatPriceToman(it),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = DfColors.Purple,
                    )
                }
                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    listing.deposit?.takeIf { it > 0 }?.let {
                        DfBadge("ودیعه ${FormatUtils.formatPriceShort(it)}", color = DfColors.BlueLight, textColor = DfColors.Blue)
                    }
                    listing.rent?.takeIf { it > 0 }?.let {
                        DfBadge("اجاره ${FormatUtils.formatPriceShort(it)}", color = DfColors.GreenLight, textColor = DfColors.Green)
                    }
                    listing.area?.let {
                        DfBadge(FormatUtils.formatArea(it), color = DfColors.SurfaceVariant, textColor = DfColors.TextSecondary)
                    }
                    listing.rooms?.let {
                        DfBadge(FormatUtils.formatRooms(it), color = DfColors.SurfaceVariant, textColor = DfColors.TextSecondary)
                    }
                    listing.advertiserType?.let {
                        DfBadge(it, color = DfColors.AmberLight, textColor = DfColors.Amber)
                    }
                }
            }
        }

        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                DfActionButton(
                    text = "ارسال به مخاطب",
                    onClick = onSendToContact,
                    icon = Icons.Default.PersonAdd,
                    modifier = Modifier.weight(1f),
                    filled = true,
                )
                DfActionButton(
                    text = "اشتراک",
                    onClick = onShare,
                    iconRes = R.drawable.ic_whatsapp,
                    contentColor = DfColors.Green,
                    modifier = Modifier.weight(1f),
                )
            }
        }

        if (onOpenDivar != null) {
            item {
                Row(Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                    DfActionButton(
                        text = "مشاهده در دیوار",
                        onClick = onOpenDivar,
                        icon = Icons.AutoMirrored.Filled.OpenInNew,
                        containerColor = DfColors.BlueLight,
                        contentColor = DfColors.Blue,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
        }

        if (listing.latitude != null && listing.longitude != null) {
            item {
                val context = LocalContext.current
                Row(Modifier.padding(horizontal = 16.dp, vertical = 4.dp)) {
                    DfActionButton(
                        text = "موقعیت روی نقشه",
                        onClick = {
                            val uri = Uri.parse("geo:${listing.latitude},${listing.longitude}")
                            context.startActivity(Intent(Intent.ACTION_VIEW, uri))
                        },
                        icon = DfIcons.MapPin,
                        containerColor = DfColors.GreenLight,
                        contentColor = DfColors.Green,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
        }

        item {
            DfPremiumCard(modifier = Modifier.padding(16.dp)) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("مشخصات ملک", style = AppTypography.sectionTitle, fontWeight = FontWeight.SemiBold)
                    HorizontalDivider(color = DfColors.OutlineSubtle)
                    SpecGrid(listing)
                    listing.description?.takeIf { it.isNotBlank() }?.let { desc ->
                        HorizontalDivider(color = DfColors.OutlineSubtle)
                        Text("توضیحات", style = AppTypography.cardTitle)
                        Text(desc, style = AppTypography.bodyDescription, color = DfColors.TextSecondary)
                    }
                }
            }
        }

        item {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(12.dp),
                color = DfColors.PurpleContainer,
            ) {
                Text(
                    ListingMessageFormatter.fromDetail(listing),
                    modifier = Modifier.padding(14.dp),
                    style = AppTypography.bodyDescription,
                    color = DfColors.PurpleDark,
                )
            }
        }
    }
}

@Composable
private fun SpecGrid(listing: ListingDetailDto) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        SpecRow("📐 متراژ", listing.area?.let { FormatUtils.formatArea(it) })
        SpecRow("🛏 اتاق", listing.rooms?.let { FormatUtils.formatRooms(it) })
        SpecRow("🏗 سال ساخت", listing.yearBuilt)
        SpecRow("🏢 طبقه", listing.floor)
        SpecRow("🏗 کل طبقات", listing.totalFloors)
        SpecRow("💰 قیمت کل", listing.price?.takeIf { it > 0 }?.let { FormatUtils.formatPriceToman(it) })
        SpecRow("🔑 ودیعه", listing.deposit?.takeIf { it > 0 }?.let { FormatUtils.formatPriceShort(it) })
        SpecRow("📅 اجاره", listing.rent?.takeIf { it > 0 }?.let { FormatUtils.formatPriceShort(it) })
        SpecRow("📊 قیمت هر متر", listing.pricePerSqm?.let { FormatUtils.formatPriceToman(it.toLong()) })
        SpecRow("👤 نوع آگهی‌دهنده", listing.advertiserType)
        SpecRow("🏷 دسته", listing.businessType)
        if (listing.isExpired) {
            SpecRow("⚠️ وضعیت", "منقضی شده")
        }
        listing.scrapedAt?.takeIf { it.isNotBlank() }?.let {
            SpecRow("🕐 بروزرسانی", it.take(10))
        }
    }
}

@Composable
private fun SpecRow(label: String, value: String?) {
    if (!value.isNullOrBlank()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(label, style = AppTypography.bodyDescription, color = DfColors.TextMuted)
            Text(value, style = AppTypography.bodyDescription, fontWeight = FontWeight.Medium)
        }
    }
}
