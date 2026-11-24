package ca.benoitjdumont.simpletv

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

/** A user folder. [apps] holds component keys ("package/activity"). */
data class Folder(val id: String, var name: String, val apps: MutableList<String>)

object Folders {
    /** Built-in folder that always shows every app. It cannot be renamed or deleted. */
    const val ALL_ID = "all"
    private const val KEY = "folders"

    /** Always starts with the built-in "all apps" folder, then the user folders. */
    fun load(context: Context): MutableList<Folder> {
        val list = mutableListOf(Folder(ALL_ID, "", mutableListOf()))
        val raw = Prefs.get(context).getString(KEY, null) ?: return list
        try {
            val arr = JSONArray(raw)
            for (i in 0 until arr.length()) {
                val o = arr.getJSONObject(i)
                val appsJson = o.getJSONArray("apps")
                val apps = mutableListOf<String>()
                for (j in 0 until appsJson.length()) apps.add(appsJson.getString(j))
                list.add(Folder(o.getString("id"), o.getString("name"), apps))
            }
        } catch (e: Exception) {
            // Corrupted data: keep only the built-in folder.
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

    fun displayName(context: Context, f: Folder): String =
        if (f.id == ALL_ID) context.getString(R.string.folder_all) else f.name
}
