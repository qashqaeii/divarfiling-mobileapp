package ir.divarfiling.mobile.feature.filing

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import ir.divarfiling.mobile.core.design.AppTypography
import ir.divarfiling.mobile.core.design.DfColors
import ir.divarfiling.mobile.core.design.components.DfChartCard
import ir.divarfiling.mobile.core.design.components.DfGlassCard
import ir.divarfiling.mobile.core.design.components.DfGlassChip
import ir.divarfiling.mobile.core.design.components.DfMarketDepthBar
import ir.divarfiling.mobile.core.design.components.DfConfidenceRing
import ir.divarfiling.mobile.core.network.DatasetInsightsDto
import ir.divarfiling.mobile.core.network.L2TabDto
import ir.divarfiling.mobile.feature.filing.insights.InsightsJson
import kotlinx.serialization.json.JsonObject

@Composable
fun InsightsLevel1Content(insights: DatasetInsightsDto, modifier: Modifier = Modifier) {
  val snap = insights.quickSnapshot
  val conf = insights.confidence
  val depth = insights.marketDepth
  val score = InsightsJson.int(conf, "score") ?: InsightsJson.int(conf, "score_pct") ?: 0
  val confLabel = InsightsJson.string(conf, "level_plain")
    ?: InsightsJson.string(conf, "level")
    ?: "اعتماد متوسط"
  val confHint = InsightsJson.string(conf, "level_hint")

  Column(modifier.padding(horizontal = 4.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
    InsightsJson.string(insights.header, "headline")?.let {
      DfGlassCard {
        Text(it, style = AppTypography.cardTitle, fontWeight = FontWeight.Bold)
        InsightsJson.string(insights.header, "subtitle")?.let { sub ->
          Text(sub, style = AppTypography.bodyDescription, color = DfColors.TextSecondary)
        }
      }
    }

    DfConfidenceRing(score = score, label = confLabel, hint = confHint)

    val sentiment = InsightsJson.objectAt(snap, "market_sentiment")
    InsightsJson.string(sentiment, "label")?.let { label ->
      DfGlassCard {
        Text("وضعیت بازار", style = AppTypography.cardTitle, fontWeight = FontWeight.SemiBold)
        Text(label, style = MaterialTheme.typography.titleMedium, color = DfColors.PurpleDark)
        val factors = InsightsJson.stringList(sentiment, "factors")
        if (factors.isNotEmpty()) {
          Text(factors.joinToString(" · "), style = AppTypography.bodyDescription, color = DfColors.TextSecondary)
        }
      }
    }

    val marketValue = InsightsJson.objectAt(snap, "market_value")
    val rows = InsightsJson.rows(marketValue, "rows")
    if (rows.isNotEmpty()) {
      DfGlassCard {
        Text(
          InsightsJson.string(marketValue, "title") ?: "ارزش بازار",
          style = AppTypography.cardTitle,
          fontWeight = FontWeight.SemiBold,
        )
        rows.forEach { row ->
          InsightValueRow(
            InsightsJson.string(row, "label").orEmpty(),
            InsightsJson.string(row, "value").orEmpty(),
          )
        }
      }
    }

    val under = InsightsJson.float(depth, "under_pct") ?: 0f
    val fair = InsightsJson.float(depth, "fair_pct") ?: 0f
    val over = InsightsJson.float(depth, "over_pct") ?: 0f
    if (under + fair + over > 0f) {
      DfMarketDepthBar(under, fair, over)
    }

    if (insights.insights.isNotEmpty()) {
      DfGlassCard {
        Text("سیگنال‌های کلیدی", style = AppTypography.cardTitle, fontWeight = FontWeight.SemiBold)
        insights.insights.take(6).forEach { line ->
          Text("• $line", style = AppTypography.bodyDescription, modifier = Modifier.padding(top = 6.dp))
        }
      }
    }
  }
}

@Composable
fun InsightsLevel2Content(
  insights: DatasetInsightsDto,
  selectedTab: Int,
  onTabSelected: (Int) -> Unit,
  modifier: Modifier = Modifier,
) {
  val tabs = insights.l2?.tabs.orEmpty()
  Column(modifier.padding(horizontal = 4.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
  if (tabs.isEmpty()) {
    insights.charts.take(4).forEach { DfChartCard(it) }
    return@Column
  }
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .horizontalScroll(rememberScrollState()),
    horizontalArrangement = Arrangement.spacedBy(8.dp),
  ) {
    tabs.forEachIndexed { index, tab ->
      DfGlassChip(
        text = tab.label,
        selected = index == selectedTab,
        onClick = { onTabSelected(index) },
      )
    }
  }
  val tab = tabs.getOrNull(selectedTab) ?: return@Column
  InsightsL2TabContent(tab, insights)
  insights.l2?.qualityNote?.takeIf { it.isNotBlank() }?.let {
    DfGlassCard {
      Text("کیفیت داده", style = AppTypography.cardTitle, fontWeight = FontWeight.SemiBold)
      Text(it, style = AppTypography.bodyDescription, color = DfColors.TextSecondary)
    }
  }
  }
}

@Composable
private fun InsightsL2TabContent(tab: L2TabDto, insights: DatasetInsightsDto) {
  tab.insights.forEach { line ->
    DfGlassCard {
      Text(line, style = AppTypography.bodyDescription)
    }
  }
  tab.charts.forEach { DfChartCard(it) }
  if (tab.hasDepth) {
    val depth = insights.marketDepth
    DfMarketDepthBar(
      InsightsJson.float(depth, "under_pct") ?: 0f,
      InsightsJson.float(depth, "fair_pct") ?: 0f,
      InsightsJson.float(depth, "over_pct") ?: 0f,
    )
  }
  if (tab.hasSegments && insights.areaSegments.isNotEmpty()) {
    DfGlassCard {
      Text("باکت‌های متراژ", style = AppTypography.cardTitle, fontWeight = FontWeight.SemiBold)
      insights.areaSegments.take(8).forEach { seg ->
        val segment = seg as? JsonObject ?: return@forEach
        InsightValueRow(
          InsightsJson.string(segment, "bucket_label") ?: InsightsJson.string(segment, "bucket").orEmpty(),
          InsightsJson.string(segment, "median_pps_fmt")
            ?: InsightsJson.string(segment, "median_price_fmt")
            ?: InsightsJson.string(segment, "count")?.let { "$it آگهی" }.orEmpty(),
        )
      }
    }
  }
  if (tab.hasNeighborhoods && insights.neighborhoods.isNotEmpty()) {
    DfGlassCard {
      Text("محله‌ها", style = AppTypography.cardTitle, fontWeight = FontWeight.SemiBold)
      insights.neighborhoods.take(10).forEach { nb ->
        val neighborhood = nb as? JsonObject ?: return@forEach
        InsightValueRow(
          InsightsJson.string(neighborhood, "name") ?: InsightsJson.string(neighborhood, "neighborhood").orEmpty(),
          InsightsJson.string(neighborhood, "median_pps_fmt")
            ?: InsightsJson.string(neighborhood, "median_fmt").orEmpty(),
        )
      }
    }
  }
}

@Composable
fun InsightsLevel3Content(insights: DatasetInsightsDto, modifier: Modifier = Modifier) {
  val l3 = insights.l3 ?: return
  if (!l3.show) {
    Text("تحلیل کارشناسی برای این فایل در دسترس نیست.", color = DfColors.TextMuted)
    return
  }
  Column(modifier.padding(horizontal = 4.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
    val verdict = l3.verdict
    InsightsJson.string(verdict, "headline")?.let { headline ->
      DfGlassCard {
        Text("نتیجه کارشناسی", style = AppTypography.cardTitle, fontWeight = FontWeight.SemiBold)
        Text(headline, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        InsightsJson.string(verdict, "subline")?.let {
          Text(it, style = AppTypography.bodyDescription, color = DfColors.TextSecondary)
        }
      }
    }
    l3.sections.forEach { section ->
      DfGlassCard {
        Text(
          InsightsJson.string(section, "title") ?: "بخش تحلیل",
          style = AppTypography.cardTitle,
          fontWeight = FontWeight.SemiBold,
        )
        InsightsJson.textItems(section).forEach { item ->
          Text("• $item", style = AppTypography.bodyDescription, modifier = Modifier.padding(top = 6.dp))
        }
      }
    }
    val bands = l3.bands
    InsightsJson.rows(bands, "items").takeIf { it.isNotEmpty() }?.let { items ->
      DfGlassCard {
        Text(
          InsightsJson.string(bands, "title") ?: "بازه‌های قیمت",
          style = AppTypography.cardTitle,
          fontWeight = FontWeight.SemiBold,
        )
        items.forEach { band ->
          InsightValueRow(
            InsightsJson.string(band, "label").orEmpty(),
            InsightsJson.string(band, "value").orEmpty(),
          )
        }
      }
    }
    if (insights.negotiation.isNotEmpty()) {
      DfGlassCard {
        Text("راهبرد مذاکره", style = AppTypography.cardTitle, fontWeight = FontWeight.SemiBold)
        insights.negotiation.forEach { tip ->
          Text("• $tip", style = AppTypography.bodyDescription, modifier = Modifier.padding(top = 6.dp))
        }
      }
    }
  }
}

@Composable
private fun InsightValueRow(label: String, value: String) {
  if (label.isBlank() && value.isBlank()) return
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .padding(vertical = 6.dp),
    horizontalArrangement = Arrangement.spacedBy(12.dp),
  ) {
    Text(
      label,
      style = AppTypography.bodyDescription,
      color = DfColors.TextMuted,
      modifier = Modifier.weight(1f),
      maxLines = 2,
      overflow = TextOverflow.Ellipsis,
    )
    Text(
      value,
      style = AppTypography.bodyDescription,
      fontWeight = FontWeight.Medium,
      modifier = Modifier.weight(1f),
      maxLines = 2,
      overflow = TextOverflow.Ellipsis,
    )
  }
}

@Composable
fun InsightsOpportunitiesSection(insights: DatasetInsightsDto) {
  if (insights.opportunities.isEmpty()) return
  DfGlassCard {
    Text("فرصت‌های برتر", style = AppTypography.cardTitle, fontWeight = FontWeight.SemiBold)
    insights.opportunities.take(5).forEach { opp ->
      val item = opp as? JsonObject ?: return@forEach
      val title = InsightsJson.string(item, "title") ?: InsightsJson.string(item, "neighborhood")
      val note = InsightsJson.string(item, "reason") ?: InsightsJson.string(item, "verdict")
        Column(Modifier.padding(top = 8.dp)) {
          title?.let { Text(it, fontWeight = FontWeight.SemiBold) }
          note?.let { Text(it, style = AppTypography.bodyDescription, color = DfColors.TextSecondary) }
        }
      }
  }
}
