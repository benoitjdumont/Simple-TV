package ca.benoitjdumont.simpletv

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.os.SystemClock
import android.view.accessibility.AccessibilityEvent

/**
 * Optional. When enabled in Accessibility settings, this service watches for
 * Google's launcher coming back to the front (after a reboot, for example) and
 * brings Simple TV back on top. It does nothing else: it reads no window
 * content, and it only listens to the one package it reacts to.
 */
class HomeGuardService : AccessibilityService() {

    private var lastBring = 0L

    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        if (event.eventType != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) return
        if (event.packageName?.toString() != GOOGLE_LAUNCHER) return

        // Small pause between two bring-to-front actions, so the two apps
        // cannot keep taking turns on screen.
        val now = SystemClock.uptimeMillis()
        if (now - lastBring < MIN_GAP_MS) return
        lastBring = now

        startActivity(
            Intent(this, MainActivity::class.java)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP)
        )
    }

    override fun onInterrupt() {}

    companion object {
        private const val GOOGLE_LAUNCHER = "com.google.android.apps.tv.launcherx"
        private const val MIN_GAP_MS = 1500L
    }
}
