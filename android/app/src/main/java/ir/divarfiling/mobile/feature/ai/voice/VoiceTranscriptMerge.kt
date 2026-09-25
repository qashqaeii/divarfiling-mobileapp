package ir.divarfiling.mobile.feature.ai.voice

/** Web-parity transcript merge — mirrors crm-smart-command-voice.js helpers. */
object VoiceTranscriptMerge {

    fun normalizeWhitespace(text: String): String =
        text.trim().replace(Regex("\\s+"), " ")

    fun appendTranscriptSegment(base: String, segment: String): String {
        val piece = normalizeWhitespace(segment)
        if (piece.isEmpty()) return normalizeWhitespace(base)
        val prev = normalizeWhitespace(base)
        if (prev.isEmpty()) return piece
        return "$prev $piece"
    }

    fun appendTranscriptSegmentSafe(base: String, segment: String): String {
        val piece = normalizeWhitespace(segment)
        if (piece.isEmpty()) return normalizeWhitespace(base)
        val prev = normalizeWhitespace(base)
        if (prev.isEmpty()) return piece
        if (prev == piece || prev.endsWith(" $piece") || prev.endsWith(piece)) {
            return prev
        }
        if (piece.startsWith(prev)) {
            return piece
        }

        val maxOverlap = minOf(prev.length, piece.length, 120)
        for (len in maxOverlap downTo 3) {
            if (prev.takeLast(len) == piece.take(len)) {
                return normalizeWhitespace(prev + piece.drop(len))
            }
        }

        val prevWords = prev.split(' ')
        val pieceWords = piece.split(' ')
        for (n in minOf(6, prevWords.size, pieceWords.size) downTo 1) {
            val suffix = prevWords.takeLast(n).joinToString(" ")
            val prefix = pieceWords.take(n).joinToString(" ")
            if (suffix.isNotEmpty() && suffix == prefix) {
                val rest = pieceWords.drop(n).joinToString(" ")
                return if (rest.isNotEmpty()) appendTranscriptSegment(prev, rest) else prev
            }
        }

        return appendTranscriptSegment(prev, piece)
    }

    fun combineSessionVoiceDisplay(
        sessionTranscript: String,
        cycleFinalTranscript: String,
        cycleInterimTranscript: String,
    ): String {
        var out = normalizeWhitespace(sessionTranscript)
        out = appendTranscriptSegmentSafe(out, cycleFinalTranscript)
        val interim = normalizeWhitespace(cycleInterimTranscript)
        if (interim.isEmpty()) return out
        if (out.isEmpty()) return interim
        if (out.endsWith(interim) || out.endsWith(" $interim")) return out
        return "$out $interim"
    }

    fun buildLiveTextareaValue(
        baseInputText: String,
        sessionTranscript: String,
        cycleFinalTranscript: String,
        cycleInterimTranscript: String,
    ): String {
        val voicePart = combineSessionVoiceDisplay(
            sessionTranscript,
            cycleFinalTranscript,
            cycleInterimTranscript,
        )
        return appendTranscriptSegmentSafe(baseInputText, voicePart)
    }

    fun finalizeVoiceTranscript(
        finalTranscript: String,
        interimTranscript: String,
        flushInterim: Boolean,
    ): String {
        var out = normalizeWhitespace(finalTranscript)
        val interim = normalizeWhitespace(interimTranscript)
        if (flushInterim && interim.isNotEmpty()) {
            out = appendTranscriptSegmentSafe(out, interim)
        }
        return out
    }
}
