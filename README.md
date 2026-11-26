# xLock

Android-native app-lock MVP with a Liquid Glass-inspired dark UI.

## Features in this starter
- Select installed apps to protect.
- PIN setup (4–8 digits), stored as a salted PBKDF2 hash.
- Android BiometricPrompt for supported fingerprint/face biometrics.
- Accessibility service detects selected app launches and displays the lock screen.
- `FLAG_SECURE` on the lock screen.
- GitHub Actions workflow builds a debug APK artifact.

## Build
Requires JDK 17, Android SDK 35, Gradle 8.9.

```bash
gradle assembleDebug
```

APK: `app/build/outputs/apk/debug/app-debug.apk`

## Enable on device
1. Install the APK.
2. Open xLock and set a PIN.
3. Choose apps to protect.
4. Enable **xLock App Monitor** in Android Accessibility settings.
5. Test by opening a selected app.

## Important limitations
- Android does not expose raw face/fingerprint data to apps. BiometricPrompt asks the OS to authenticate; face unlock appears only if the device offers a compatible enrolled biometric.
- App monitoring requires the user to explicitly enable xLock's Accessibility service. Some OEM battery/background restrictions may affect reliability.
- This is an MVP starter, not a tamper-proof system app. Users can disable its Accessibility permission or force-stop it.
- `QUERY_ALL_PACKAGES` is used for the installed-app picker. Google Play distribution requires policy review and an allowed core use case.
- Debug APK is unsigned for production distribution. Configure a release signing key before public release.
