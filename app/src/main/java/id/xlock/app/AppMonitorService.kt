package id.xlock.app
import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.view.accessibility.AccessibilityEvent
class AppMonitorService : AccessibilityService() {
 private var lastPackage = ""
 private var lastLaunchAt = 0L
 override fun onAccessibilityEvent(event: AccessibilityEvent?) {
  if (event?.eventType != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) return
  val pkg = event.packageName?.toString() ?: return
  if (pkg == packageName || pkg == "com.android.systemui" || pkg.startsWith("com.android.permissioncontroller")) return
  if (pkg !in SecurityStore.protectedApps(this) || SecurityStore.isGranted(this, pkg)) return
  val now = System.currentTimeMillis()
  if (pkg == lastPackage && now - lastLaunchAt < 1800) return
  lastPackage = pkg; lastLaunchAt = now
  startActivity(Intent(this, LockActivity::class.java).apply {
   addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS)
   putExtra("target_package", pkg)
  })
 }
 override fun onInterrupt() {}
}
