package ir.divarfiling.mobile.core.design.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.foundation.layout.Column
import ir.divarfiling.mobile.core.design.DfColors
import ir.divarfiling.mobile.core.image.ImageUrlFormatter

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun DfImageGallery(
    images: List<String>,
    modifier: Modifier = Modifier,
    heroHeight: androidx.compose.ui.unit.Dp = 280.dp,
) {
    val urls = remember(images) {
        images.mapNotNull { ImageUrlFormatter.normalize(it) }.distinct()
    }
    if (urls.isEmpty()) {
        DfImagePlaceholder(
            modifier = modifier
                .fillMaxWidth()
                .height(heroHeight),
            shape = RoundedCornerShape(0.dp),
        )
        return
    }

    var fullscreenIndex by remember { mutableIntStateOf(-1) }
    val pagerState = rememberPagerState(pageCount = { urls.size })

    Box(modifier = modifier.fillMaxWidth()) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxWidth()
                .height(heroHeight),
        ) { page ->
            DfAsyncImage(
                url = urls[page],
                modifier = Modifier
                    .fillMaxSize()
                    .clickable { fullscreenIndex = page },
                contentScale = ContentScale.Crop,
            )
        }

        if (urls.size > 1) {
            Row(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                urls.indices.forEach { index ->
                    Box(
                        modifier = Modifier
                            .size(if (index == pagerState.currentPage) 8.dp else 6.dp)
                            .clip(CircleShape)
                            .background(
                                if (index == pagerState.currentPage) Color.White
                                else Color.White.copy(alpha = 0.45f),
                            ),
                    )
                }
            }

            Surface(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(12.dp),
                shape = RoundedCornerShape(20.dp),
                color = Color.Black.copy(alpha = 0.45f),
            ) {
                Text(
                    "${pagerState.currentPage + 1} / ${urls.size}",
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                    style = MaterialTheme.typography.labelMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Medium,
                )
            }
        }
    }

    if (fullscreenIndex >= 0) {
        DfFullscreenImageViewer(
            urls = urls,
            initialIndex = fullscreenIndex,
            onDismiss = { fullscreenIndex = -1 },
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun DfFullscreenImageViewer(
    urls: List<String>,
    initialIndex: Int,
    onDismiss: () -> Unit,
) {
    val pagerState = rememberPagerState(initialPage = initialIndex, pageCount = { urls.size })

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black),
        ) {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize(),
            ) { page ->
                DfAsyncImage(
                    url = urls[page],
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Fit,
                )
            }

            IconButton(
                onClick = onDismiss,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(8.dp),
            ) {
                Icon(Icons.Default.Close, contentDescription = "بستن", tint = Color.White)
            }

            if (urls.size > 1) {
                Text(
                    "${pagerState.currentPage + 1} / ${urls.size}",
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 24.dp),
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White,
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DfBottomSheetPicker(
    visible: Boolean,
    title: String,
    onDismiss: () -> Unit,
    content: @Composable () -> Unit,
) {
    if (!visible) return
    DfModalBottomSheet(onDismissRequest = onDismiss) {
        DfSheetScaffold(
            title = title,
            onClose = onDismiss,
            scrollable = false,
        ) {
            content()
        }
    }
}
