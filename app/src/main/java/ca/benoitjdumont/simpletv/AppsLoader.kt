package ca.benoitjdumont.simpletv

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.drawable.Drawable

/** One launchable app. [key] is a stable id ("package/activity") used by folders. */
data class AppEntry(val key: String, val label: String, val icon: Drawable, val intent: Intent)

object AppsLoader {
    fun load(context: Context): List<AppEntry> {
        val pm = context.packageManager
        val leanback = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LEANBACK_LAUNCHER)
        val mobile = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)

        return (pm.queryIntentActivities(leanback, 0) + pm.queryIntentActivities(mobile, 0))
            .filter { it.activityInfo.packageName != context.packageName }
            .distinctBy { it.activityInfo.packageName }
            .map { info ->
                val component = ComponentName(info.activityInfo.packageName, info.activityInfo.name)
                AppEntry(
                    key = component.flattenToString(),
                    label = info.loadLabel(pm).toString(),
                    icon = info.loadIcon(pm),
                    intent = Intent(Intent.ACTION_MAIN)
                        .addCategory(Intent.CATEGORY_LAUNCHER)
                        .setComponent(component)
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                )
            }
            .sortedBy { it.label.lowercase() }
    }
}
