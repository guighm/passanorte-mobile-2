package br.edu.uea.passanorte2.ui.chatbot

import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

private val dateTimeFormat = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm", Locale("pt", "BR"))

/** "2026-11-23T14:03:00+00:00" → "23/11/2026 14:03". */
fun formatTimestamp(iso: String?): String {
    if (iso.isNullOrBlank()) return ""
    return runCatching { OffsetDateTime.parse(iso).format(dateTimeFormat) }
        .getOrDefault(iso)
}

/** 1290 → "R$ 12,90". */
fun formatCents(cents: Long): String {
    val nf = java.text.NumberFormat.getCurrencyInstance(Locale("pt", "BR"))
    return nf.format(cents / 100.0)
}

/** Nome amigável para o papel da mensagem. */
val String.isTouristRole: Boolean
    get() = equals("tourist", ignoreCase = true)
