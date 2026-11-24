package ca.benoitjdumont.simpletv

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

/** A folder. [apps] holds component keys ("package/activity"). */
data class Folder(val id: String, var name: String, val apps: MutableList<String>)

object Folders {
    /** Built-in: every app. Cannot be renamed, deleted, or filled. */
    const val ALL_ID = "all"
    /** Built-in: the favorites shown at the bottom of the home screen. Can be filled, not renamed or deleted. */
    const val FAVORITES_ID = "favorites"
    private const val KEY = "folders"

    /** Always starts with the two built-in folders, then the user folders. */
    fun load(context: Context): MutableList<Folder> {
        val favorites = Folder(FAVORITES_ID, "", mutableListOf())
        val list = mutableListOf(Folder(ALL_ID, "", mutableListOf()), favorites)
        val raw = Prefs.get(context).getString(KEY, null) ?: return list
        try {
            val arr = JSONArray(raw)
            for (i in 0 until arr.length()) {
                val o = arr.getJSONObject(i)
                val appsJson = o.getJSONArray("apps")
                val apps = mutableListOf<String>()
                for (j in 0 until appsJson.length()) apps.add(appsJson.getString(j))
                if (o.getString("id") == FAVORITES_ID) {
                    favorites.apps.addAll(apps)
                } else {
                    list.add(Folder(o.getString("id"), o.getString("name"), apps))
                }
            }
        } catch (e: Exception) {
            // Corrupted data: keep only the built-in folders.
        }
        return list
    }

    fun save(context: Context, folders: List<Folder>) {
        val arr = JSONArray()
        folders.filter { it.id != ALL_ID }.forEach { f ->
            arr.put(JSONObject().put("id", f.id).put("name", f.name).put("apps", JSONArray(f.apps)))
        }
        Prefs.get(context).edit().putString(KEY, arr.toString()).apply()
    }

    fun newId(): String = UUID.randomUUID().toString()

    fun isBuiltIn(f: Folder): Boolean = f.id == ALL_ID || f.id == FAVORITES_ID

    fun displayName(context: Context, f: Folder): String = when (f.id) {
        ALL_ID -> context.getString(R.string.folder_all)
        FAVORITES_ID -> context.getString(R.string.folder_favorites)
        else -> f.name
    }
}
