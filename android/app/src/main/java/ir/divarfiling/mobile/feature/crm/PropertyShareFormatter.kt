package ir.divarfiling.mobile.feature.crm

import ir.divarfiling.mobile.core.design.DossierShareFormatter
import ir.divarfiling.mobile.core.design.DossierShareOptions
import ir.divarfiling.mobile.core.network.PropertyDto

object PropertyShareFormatter {
    fun buildShareText(
        property: PropertyDto,
        options: DossierShareOptions = DossierShareOptions(
            footer = DossierShareOptions.PERSONAL_FOOTER,
        ),
    ): String = DossierShareFormatter.fromProperty(property, options)
}
