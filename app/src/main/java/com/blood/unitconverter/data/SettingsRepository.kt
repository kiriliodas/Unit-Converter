package com.blood.unitconverter.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.blood.unitconverter.logic.Precision
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * Single on-device store for user preferences AND conversion history.
 *
 * PRIVACY: this writes to the app's private DataStore file on the device. It is
 * never uploaded, synced, or shared — the app has no INTERNET permission, so it
 * physically cannot leave the device. Fully compatible with the "100% offline,
 * zero data collection" promise. History can be cleared by the user at any time.
 */
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "converter_prefs")

class SettingsRepository(private val context: Context) {

    private val json = Json { ignoreUnknownKeys = true }

    private object Keys {
        val PRECISION = stringPreferencesKey("precision")
        val SHOW_TAGLINE = stringPreferencesKey("show_tagline") // "1"/"0"
        val HISTORY = stringPreferencesKey("history_json")
        val LAST = stringPreferencesKey("last_selection_json")
        val FAVORITES = stringPreferencesKey("favorites_json")
    }

    // ---- Precision -----------------------------------------------------------

    val precision: Flow<Precision> = context.dataStore.data.map { prefs ->
        Precision.fromName(prefs[Keys.PRECISION])
    }

    suspend fun setPrecision(p: Precision) {
        context.dataStore.edit { it[Keys.PRECISION] = p.name }
    }

    // ---- Header tagline (hide after first session) ---------------------------

    val showTagline: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[Keys.SHOW_TAGLINE] != "0"
    }

    suspend fun setShowTagline(show: Boolean) {
        context.dataStore.edit { it[Keys.SHOW_TAGLINE] = if (show) "1" else "0" }
    }

    // ---- Last used category/units (so the app reopens where you left off) ----

    val lastSelection: Flow<LastSelection?> = context.dataStore.data.map { prefs ->
        prefs[Keys.LAST]?.let {
            runCatching { json.decodeFromString<LastSelection>(it) }.getOrNull()
        }
    }

    suspend fun setLastSelection(sel: LastSelection) {
        context.dataStore.edit { it[Keys.LAST] = json.encodeToString(sel) }
    }

    // ---- History -------------------------------------------------------------

    val history: Flow<List<HistoryEntry>> = context.dataStore.data.map { prefs ->
        prefs[Keys.HISTORY]?.let {
            runCatching { json.decodeFromString<List<HistoryEntry>>(it) }.getOrNull()
        } ?: emptyList()
    }

    suspend fun addHistory(entry: HistoryEntry, maxEntries: Int = 50) {
        context.dataStore.edit { prefs ->
            val current = prefs[Keys.HISTORY]?.let {
                runCatching { json.decodeFromString<List<HistoryEntry>>(it) }.getOrNull()
            } ?: emptyList()
            // De-dupe (move existing identical conversion to top) then cap size.
            val deduped = current.filter { it.dedupeKey != entry.dedupeKey }
            val updated = (listOf(entry) + deduped).take(maxEntries)
            prefs[Keys.HISTORY] = json.encodeToString(updated)
        }
    }

    suspend fun clearHistory() {
        context.dataStore.edit { it.remove(Keys.HISTORY) }
    }

    // ---- Favorites (pinned unit ids, keyed "categoryId:unitId") --------------

    val favorites: Flow<Set<String>> = context.dataStore.data.map { prefs ->
        prefs[Keys.FAVORITES]?.let {
            runCatching { json.decodeFromString<Set<String>>(it) }.getOrNull()
        } ?: emptySet()
    }

    suspend fun toggleFavorite(key: String) {
        context.dataStore.edit { prefs ->
            val current = prefs[Keys.FAVORITES]?.let {
                runCatching { json.decodeFromString<Set<String>>(it) }.getOrNull()
            } ?: emptySet()
            val updated = if (key in current) current - key else current + key
            prefs[Keys.FAVORITES] = json.encodeToString(updated)
        }
    }
}

@kotlinx.serialization.Serializable
data class LastSelection(
    val categoryId: String,
    val fromUnitId: String,
    val toUnitId: String,
)
