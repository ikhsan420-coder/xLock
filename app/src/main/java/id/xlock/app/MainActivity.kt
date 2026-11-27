package id.xlock.app
import android.accessibilityservice.AccessibilityServiceInfo
import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.provider.Settings
import android.view.accessibility.AccessibilityManager
import android.widget.*
class MainActivity : Activity() {
 private val bg=Color.rgb(12,19,34); private val glass=Color.argb(48,255,255,255); private lateinit var status:TextView
 override fun onCreate(savedInstanceState:Bundle?) { super.onCreate(savedInstanceState); render() }
 private fun render() {
  val root=LinearLayout(this).apply { orientation=LinearLayout.VERTICAL; setPadding(24,44,24,24); setBackgroundColor(bg) }
  root.addView(TextView(this).apply { text="◈  xLock"; textSize=30f; setTextColor(Color.WHITE); typeface=Typeface.DEFAULT_BOLD })
  root.addView(label("LIQUID GLASS  •  PRIVACY PROTECTION",11,0xFF9CCEFF.toInt()))
  val panel=LinearLayout(this).apply { orientation=LinearLayout.VERTICAL; setPadding(18,18,18,18); setBackgroundColor(glass) }
  status=label("",14,Color.WHITE); panel.addView(label("Protection status",20,Color.WHITE,true)); panel.addView(status)
  panel.addView(action("Choose protected apps"){chooseApps()}); panel.addView(action("Set / change PIN"){setPin()})
  panel.addView(action("Enable app monitoring"){startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))})
  panel.addView(action("Biometric settings"){startActivity(Intent(Settings.ACTION_SECURITY_SETTINGS))})
  panel.addView(label("Biometric unlock uses Android's system prompt. Face unlock appears only when the device exposes an enrolled biometric accepted by Android.",12,0xFFD0D9E8.toInt()))
  root.addView(panel,LinearLayout.LayoutParams(-1,-2).apply { topMargin=28 })
  root.addView(label("Security note: xLock never stores face images or fingerprint data. Biometrics remain on your device.",12,0xFFB8C5D8.toInt()).apply { setPadding(4,22,4,0) })
  setContentView(ScrollView(this).apply { addView(root) }); updateStatus()
 }
 private fun updateStatus() {
  val enabled=isAccessibilityEnabled(); status.text="Monitor: "+if(enabled)"ON" else "OFF — enable in Accessibility settings"
  status.setTextColor(if(enabled)0xFF8FF0C2.toInt() else 0xFFFFC879.toInt())
 }
 override fun onResume() { super.onResume(); if(::status.isInitialized) updateStatus() }
 private fun isAccessibilityEnabled():Boolean {
  val am=getSystemService(ACCESSIBILITY_SERVICE) as AccessibilityManager
  return am.getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_ALL_MASK).any {
   it.resolveInfo.serviceInfo.packageName==packageName && it.resolveInfo.serviceInfo.name.endsWith("AppMonitorService")
  }
 }
 private fun label(text:String,size:Int,color:Int,bold:Boolean=false)=TextView(this).apply {
  this.text=text; textSize=size.toFloat(); setTextColor(color); if(bold)typeface=Typeface.DEFAULT_BOLD; setPadding(0,8,0,8)
 }
 private fun action(text:String,click:()->Unit)=Button(this).apply { this.text=text; isAllCaps=false; setOnClickListener{click()} }
 private fun setPin() {
  val input=EditText(this).apply { hint="PIN 4–8 digit"; inputType=2 }
  AlertDialog.Builder(this).setTitle("Set xLock PIN").setView(input).setPositiveButton("Save"){_,_->
   val pin=input.text.toString()
   if(pin.length in 4..8 && pin.all(Char::isDigit)) { SecurityStore.setPin(this,pin); Toast.makeText(this,"PIN saved",Toast.LENGTH_SHORT).show() }
   else Toast.makeText(this,"PIN harus 4–8 digit",Toast.LENGTH_LONG).show()
  }.setNegativeButton("Cancel",null).show()
 }
 private fun chooseApps() {
  val pm=packageManager; val apps=pm.getInstalledApplications(0).filter { it.packageName!=packageName }.sortedBy { pm.getApplicationLabel(it).toString().lowercase() }
  val selected=SecurityStore.protectedApps(this); val names=apps.map { pm.getApplicationLabel(it).toString()+"  ·  "+it.packageName }.toTypedArray()
  val checked=BooleanArray(apps.size){selected.contains(apps[it].packageName)}
  AlertDialog.Builder(this).setTitle("Protected apps").setMultiChoiceItems(names,checked){_,which,isChecked->
   if(isChecked)selected.add(apps[which].packageName) else selected.remove(apps[which].packageName)
  }.setPositiveButton("Save"){_,_-> SecurityStore.saveApps(this,selected); Toast.makeText(this,"Apps protected: "+selected.size,Toast.LENGTH_SHORT).show() }.setNegativeButton("Cancel",null).show()
 }
}
