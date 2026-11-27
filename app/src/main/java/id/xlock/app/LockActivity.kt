package id.xlock.app
import android.app.Activity
import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.view.WindowManager
import android.widget.*
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
class LockActivity : Activity() {
 private var target=""; private var unlocked=false
 override fun onCreate(savedInstanceState:Bundle?) { super.onCreate(savedInstanceState); target=intent.getStringExtra("target_package")?:""; window.addFlags(WindowManager.LayoutParams.FLAG_SECURE); showLock() }
 private fun showLock() {
  val root=LinearLayout(this).apply { orientation=LinearLayout.VERTICAL; gravity=Gravity.CENTER; setPadding(28,28,28,28); setBackgroundColor(Color.rgb(12,19,34)) }
  root.addView(TextView(this).apply { text="◈  xLock"; textSize=32f; setTextColor(Color.WHITE); gravity=Gravity.CENTER })
  root.addView(TextView(this).apply { text="App protected"; textSize=17f; setTextColor(0xFFBFD9F6.toInt()); gravity=Gravity.CENTER; setPadding(0,12,0,26) })
  root.addView(Button(this).apply { text="Unlock with biometrics"; isAllCaps=false; setOnClickListener{authenticate()} })
  val pin=EditText(this).apply { hint="Enter PIN"; inputType=2; setTextColor(Color.WHITE); setHintTextColor(0xFFB0BED0.toInt()) }; root.addView(pin)
  root.addView(Button(this).apply { text="Unlock"; isAllCaps=false; setOnClickListener {
   if(SecurityStore.verifyPin(this@LockActivity,pin.text.toString()))unlock()
   else { SecurityStore.addFailure(this@LockActivity); Toast.makeText(this@LockActivity,"Incorrect PIN",Toast.LENGTH_SHORT).show() }
  }})
  root.addView(TextView(this).apply { text="Your biometric data stays securely on this device."; textSize=12f; setTextColor(0xFF9EADC2.toInt()); gravity=Gravity.CENTER; setPadding(0,24,0,0) })
  setContentView(root); if(SecurityStore.hasPin(this))authenticate()
 }
 private fun authenticate() {
  val manager=BiometricManager.from(this); val authenticators=BiometricManager.Authenticators.BIOMETRIC_WEAK
  if(manager.canAuthenticate(authenticators)!=BiometricManager.BIOMETRIC_SUCCESS) { Toast.makeText(this,"Biometrik belum tersedia/terdaftar. Gunakan PIN.",Toast.LENGTH_LONG).show(); return }
  val prompt=BiometricPrompt(this,ContextCompat.getMainExecutor(this),object:BiometricPrompt.AuthenticationCallback(){
   override fun onAuthenticationSucceeded(result:BiometricPrompt.AuthenticationResult){unlock()}
   override fun onAuthenticationError(errorCode:Int,errString:CharSequence){Toast.makeText(this@LockActivity,errString,Toast.LENGTH_SHORT).show()}
  })
  val info=BiometricPrompt.PromptInfo.Builder().setTitle("Unlock xLock").setSubtitle("Verify your identity").setAllowedAuthenticators(authenticators).setNegativeButtonText("Use PIN").build()
  prompt.authenticate(info)
 }
 private fun unlock() {
  if(unlocked)return; unlocked=true; SecurityStore.clearFailures(this)
  if(target.isNotBlank())SecurityStore.grantApp(this,target); finish()
 }
 override fun onBackPressed() { /* Keep the protected app behind the lock screen. */ }
}
