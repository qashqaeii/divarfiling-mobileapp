package ir.divarfiling.mobile.core.export

enum class ExportFormat(val apiValue: String, val label: String, val extension: String) {
    XLSX("xlsx", "اکسل", "xlsx"),
    JSON("json", "JSON", "json"),
    CSV("csv", "CSV", "csv"),
    ;

    val mimeType: String
        get() = when (this) {
            XLSX -> "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
            JSON -> "application/json"
            CSV -> "text/csv"
        }
}
