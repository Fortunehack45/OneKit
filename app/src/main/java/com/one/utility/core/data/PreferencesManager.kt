package com.one.utility.core.data

import android.content.Context
import android.content.SharedPreferences
import org.json.JSONArray
import org.json.JSONObject

data class RecentToolEntry(
    val toolId: String,
    val title: String,
    val route: String,
    val timestamp: Long
)

class PreferencesManager(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences("one_preferences", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_HISTORY_ENABLED = "history_enabled"
        private const val KEY_RECENT_TOOLS = "recent_tools"
        private const val KEY_HAPTICS = "haptics_enabled"
        private const val KEY_THEME = "app_theme" // SYSTEM, LIGHT, DARK
        private const val KEY_AUTOSAVE = "autosave_outputs"
    }

    var isHistoryEnabled: Boolean
        get() = prefs.getBoolean(KEY_HISTORY_ENABLED, true)
        set(value) = prefs.edit().putBoolean(KEY_HISTORY_ENABLED, value).apply()

    var isHapticsEnabled: Boolean
        get() = prefs.getBoolean(KEY_HAPTICS, true)
        set(value) = prefs.edit().putBoolean(KEY_HAPTICS, value).apply()

    var isAutosaveEnabled: Boolean
        get() = prefs.getBoolean(KEY_AUTOSAVE, true)
        set(value) = prefs.edit().putBoolean(KEY_AUTOSAVE, value).apply()

    var themeMode: String
        get() = prefs.getString(KEY_THEME, "SYSTEM") ?: "SYSTEM"
        set(value) = prefs.edit().putString(KEY_THEME, value).apply()

    fun addRecentTool(toolId: String, title: String, route: String) {
        if (!isHistoryEnabled) return

        val recents = getRecentTools().toMutableList()
        recents.removeAll { it.toolId == toolId }
        recents.add(0, RecentToolEntry(toolId, title, route, System.currentTimeMillis()))

        // Keep maximum 8 recent tools
        val trimmed = recents.take(8)

        val array = JSONArray()
        trimmed.forEach { item ->
            val obj = JSONObject().apply {
                put("id", item.toolId)
                put("title", item.title)
                put("route", item.route)
                put("timestamp", item.timestamp)
            }
            array.put(obj)
        }

        prefs.edit().putString(KEY_RECENT_TOOLS, array.toString()).apply()
    }

    fun getRecentTools(): List<RecentToolEntry> {
        val jsonStr = prefs.getString(KEY_RECENT_TOOLS, null) ?: return emptyList()
        return try {
            val array = JSONArray(jsonStr)
            val list = mutableListOf<RecentToolEntry>()
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                list.add(
                    RecentToolEntry(
                        toolId = obj.getString("id"),
                        title = obj.getString("title"),
                        route = obj.getString("route"),
                        timestamp = obj.getLong("timestamp")
                    )
                )
            }
            list
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun clearRecentTools() {
        prefs.edit().remove(KEY_RECENT_TOOLS).apply()
    }
}
