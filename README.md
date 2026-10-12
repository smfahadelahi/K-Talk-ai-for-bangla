# K-Talk-ai-for-bangla

K-Bridge AI is an Android project foundation for Korean ↔ Bangla communication with headphones, designed around a strict offline-first requirement.

## Current foundation
- Kotlin + Jetpack Compose Android app (minimum Android 8.0 / API 26).
- Translator UI shell.
- Interfaces for local speech recognition, speech output, and translation.
- No Android INTERNET permission in the manifest.
- GitHub Actions workflow to build a debug APK and run unit tests.

## Important: not yet a complete translator
The initial project does **not** bundle or integrate a production Korean speech model, Korean↔Bangla translation model, or offline Bengali speech voice. Until these are integrated and tested, the app explicitly reports that translation is unavailable instead of inventing output.

## Build
The GitHub Actions workflow uses JDK 17 and Gradle 8.9 to run:
- `gradle assembleDebug`
- `gradle testDebugUnitTest`

After a successful run, download the `k-bridge-ai-debug-apk` artifact from the workflow run.

## Next implementation milestones
1. Add audio capture with runtime microphone permission handling and voice activity detection.
2. Integrate and license-check a compatible on-device Korean ASR model.
3. Verify and integrate a genuinely local Korean↔Bangla translation model.
4. Integrate local Korean and Bengali speech output where supported.
5. Test the end-to-end experience in airplane mode on a real Android device.
6. Document model download/setup, model sizes, memory requirements, and license notices.

Never add a cloud fallback or claim full offline support until real models and voices pass airplane-mode testing.
