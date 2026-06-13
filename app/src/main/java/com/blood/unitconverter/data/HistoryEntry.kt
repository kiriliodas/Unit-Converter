package com.blood.unitconverter.data

import kotlinx.serialization.Serializable

/**
 * One past conversion. Stored ON-DEVICE ONLY (never transmitted) so users can
 * re-run frequent conversions. Kept tiny + serializable for DataStore.
 */
@Serializable
data class HistoryEntry(
    val categoryId: String,
    val fromUnitId: String,
    val toUnitId: String,
    val inputText: String,
    val resultText: String,
    val timestamp: Long,
) {
    /** Stable key so the same conversion isn't duplicated back-to-back. */
    val dedupeKey: String
        get() = "$categoryId|$fromUnitId|$toUnitId|$inputText"
}
