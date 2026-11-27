package id.xlock.app
import android.content.Context
import android.util.Base64
import java.security.MessageDigest
import java.security.SecureRandom
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec
object SecurityStore {
 private const val PREF="xlock_secure"; private const val SALT="pin_salt"; private const val HASH="pin_hash"
 private const val APPS="protected_apps"; private const val FAILS="failed_attempts"; private const val GRANTS="temporary_grants"
 private fun prefs(c:Context)=c.getSharedPreferences(PREF,Context.MODE_PRIVATE)
 fun hasPin(c:Context)=prefs(c).contains(HASH)
 fun setPin(c:Context,pin:String) {
  val salt=ByteArray(16).also { SecureRandom().nextBytes(it) }
  prefs(c).edit().putString(SALT,Base64.encodeToString(salt,Base64.NO_WRAP)).putString(HASH,hash(pin,salt)).apply()
 }
 fun verifyPin(c:Context,pin:String):Boolean {
  val p=prefs(c); val salt=p.getString(SALT,null)?:return false; val expected=p.getString(HASH,null)?:return false
  return MessageDigest.isEqual(expected.toByteArray(),hash(pin,Base64.decode(salt,Base64.NO_WRAP)).toByteArray())
 }
 private fun hash(pin:String,salt:ByteArray):String {
  val spec=PBEKeySpec(pin.toCharArray(),salt,120000,256)
  return try { Base64.encodeToString(SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).encoded,Base64.NO_WRAP) } finally { spec.clearPassword() }
 }
 fun protectedApps(c:Context):MutableSet<String> = prefs(c).getStringSet(APPS,emptySet())?.toMutableSet()?:mutableSetOf()
 fun saveApps(c:Context,apps:Set<String>) { prefs(c).edit().putStringSet(APPS,apps.toSet()).apply() }
 fun failures(c:Context)=prefs(c).getInt(FAILS,0)
 fun addFailure(c:Context) { prefs(c).edit().putInt(FAILS,failures(c)+1).apply() }
 fun clearFailures(c:Context) { prefs(c).edit().putInt(FAILS,0).apply() }
 fun grantApp(c:Context,pkg:String) {
  val grants=prefs(c).getStringSet(GRANTS,emptySet())?.toMutableSet()?:mutableSetOf()
  grants.removeAll { it.startsWith(pkg+"|") }; grants.add(pkg+"|"+(System.currentTimeMillis()+300000))
  prefs(c).edit().putStringSet(GRANTS,grants).apply()
 }
 fun isGranted(c:Context,pkg:String):Boolean {
  val grants=prefs(c).getStringSet(GRANTS,emptySet())?:return false
  val entry=grants.firstOrNull { it.startsWith(pkg+"|") }?:return false
  val expiry=entry.substringAfter("|").toLongOrNull()?:return false
  if(System.currentTimeMillis()>expiry) { prefs(c).edit().putStringSet(GRANTS,grants-entry).apply(); return false }
  return true
 }
}
