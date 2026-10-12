# K-Talk-ai-for-bangla

K-Bridge AI is an Android app foundation for private Korean ↔ Bangla communication and practical Korean phrase practice, designed around a strict offline-first requirement.

## Implemented in this branch
- Kotlin + Jetpack Compose Android app (minimum Android 8.0 / API 26).
- Local microphone capture at 16 kHz mono PCM16.
- Basic pause-based speech segmentation; this is an energy-based first-pass detector and needs real-world tuning.
- Role-based offline phrasebook with Korean text, Bengali meanings, and some Bengali-script pronunciation hints.
- Android text-to-speech test that selects only voices marked as not requiring a network connection.
- Interfaces for local speech recognition and translation.
- No Android INTERNET permission in the manifest.
- GitHub Actions workflow to build a debug APK and run unit tests.

## Important: not yet a complete translator
The current project does **not** bundle or integrate production Korean/Bengali ASR models or a Korean↔Bangla translation model. Live speech transcription, automatic translation, contextual AI reply generation, and the complete headphone conversation loop are not yet implemented. The app explicitly reports that translation is unavailable rather than inventing output.

Offline voice output depends on the user having local Korean/Bengali voices installed on their Android device. Some device TTS voices require network access; this app refuses voices marked as network-required.

## Build
The GitHub Actions workflow uses JDK 17 and Gradle 8.9 to run:
- `gradle assembleDebug`
- `gradle testDebugUnitTest`

After a successful run, download the `k-bridge-ai-debug-apk` artifact from the workflow run.

## Next milestones
1. Confirm the Android CI build and unit tests pass.
2. Integrate a supported offline Korean and Bengali ASR runtime/model.
3. Select, license-check, and integrate a local Korean↔Bangla translation model.
4. Add local contextual reply suggestions and connect the translated output to local speech.
5. Add model-pack installation/setup and transparent storage/size information.
6. Test end-to-end in airplane mode on a real Android device.

See [offline model integration decisions](docs/OFFLINE_MODEL_INTEGRATION.md) for candidate ASR resources and acceptance criteria.

Never add a cloud fallback or claim full offline support until real models and voices pass airplane-mode testing.
