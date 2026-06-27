package ir.divarfiling.mobile.core.design.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import ir.divarfiling.mobile.core.design.AppShapes
import ir.divarfiling.mobile.core.design.AppSpacing

@Composable
fun DfListSkeleton(
    count: Int = 5,
    modifier: Modifier = Modifier,
    itemHeight: Dp = 72.dp,
    spacedBy: Dp = 10.dp,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(spacedBy),
    ) {
        repeat(count) {
            DfShimmerBox(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(itemHeight),
            )
        }
    }
}

@Composable
fun DfContactListSkeleton(
    count: Int = 6,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        repeat(count) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(AppSpacing.listRowMinHeight),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                DfShimmerBox(modifier = Modifier.size(44.dp))
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    DfShimmerBox(modifier = Modifier.fillMaxWidth(0.55f).height(14.dp))
                    DfShimmerBox(modifier = Modifier.fillMaxWidth(0.35f).height(12.dp))
                }
                DfShimmerBox(modifier = Modifier.width(52.dp).height(22.dp))
            }
        }
    }
}

@Composable
fun DfNotificationListSkeleton(
    count: Int = 5,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        repeat(count) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                DfShimmerBox(modifier = Modifier.size(44.dp))
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    DfShimmerBox(modifier = Modifier.fillMaxWidth(0.8f).height(14.dp))
                    DfShimmerBox(modifier = Modifier.fillMaxWidth(0.95f).height(12.dp))
                    DfShimmerBox(modifier = Modifier.width(64.dp).height(10.dp))
                }
            }
        }
    }
}

@Composable
fun DfCardListSkeleton(
    count: Int = 4,
    modifier: Modifier = Modifier,
    itemHeight: Dp = 108.dp,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        repeat(count) {
            DfShimmerBox(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(itemHeight),
            )
        }
    }
}

@Composable
fun DfDatasetCardSkeleton(
    count: Int = 4,
    modifier: Modifier = Modifier,
) {
    DfCardListSkeleton(count = count, modifier = modifier, itemHeight = 120.dp)
}

@Composable
fun DfDetailSkeleton(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        DfShimmerBox(modifier = Modifier.fillMaxWidth().height(200.dp))
        DfShimmerBox(modifier = Modifier.fillMaxWidth(0.7f).height(22.dp))
        DfShimmerBox(modifier = Modifier.fillMaxWidth(0.45f).height(18.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            DfShimmerBox(modifier = Modifier.width(80.dp).height(28.dp))
            DfShimmerBox(modifier = Modifier.width(80.dp).height(28.dp))
        }
        DfShimmerBox(modifier = Modifier.fillMaxWidth().height(120.dp))
    }
}
