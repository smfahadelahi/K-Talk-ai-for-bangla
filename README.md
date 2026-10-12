# K-Talk-ai-for-bangla

K-Bridge AI is an Android app foundation for private Korean ↔ Bangla communication and practical Korean phrase practice, designed around a strict offline-first requirement.

## Implemented in this branch
- Kotlin + Jetpack Compose Android app (minimum Android 12 / API 31).
- Android on-device speech-recognition adapter for Korean and Bengali language tags; availability and offline language support depend on the installed device speech service and language packs.
- Explicit no-cloud-fallback behavior when on-device speech recognition fails.
- Local microphone capture and a basic pause-based segmenter retained as an experimental audio path.
- Role-based offline phrasebook with Korean text, Bengali meanings, and some Bengali-script pronunciation hints.
- Exact-match Korean↔Bengali translation for reviewed phrasebook entries; unknown sentences are not guessed.
- Deterministic, rule-based Korean reply suggestions for common restaurant, payment, and clarification situations.
- Android text-to-speech test that selects only voices marked as not requiring a network connection.
- Interfaces for local speech recognition and translation.
- No Android INTERNET permission in the manifest.
- GitHub Actions workflow to build a debug APK and run unit tests.

## Important: not yet a complete translator
The current project does **not** bundle or integrate a production Korean↔Bangla machine-translation model. The Translate button only translates exact phrases already present in the reviewed phrasebook. On-device speech recognition can only work if the phone provides a compatible local recognition service and the requested offline language pack is installed. Contextual suggestions are a small rule-based phrasebook, not a generative AI model.

Automatic translation of arbitrary speech, a full turn-taking headphone loop, broad context-aware reply generation, and airplane-mode end-to-end verification are not yet complete. The app explicitly reports unavailable translation instead of inventing output.

Offline voice output depends on the user having local Korean/Bengali voices installed on Android. Some device TTS voices require network access; this app refuses voices marked as network-required.

## Build
The GitHub Actions workflow uses JDK 17 and Gradle 8.9 to run:
- `gradle assembleDebug`
- `gradle testDebugUnitTest`

After a successful run, download the `k-bridge-ai-debug-apk` artifact from the workflow run.

## Next milestones
1. Confirm CI passes on the latest branch head.
2. Select, license-check, and integrate a local Korean↔Bangla translation model.
3. Connect recognized speech to translation, then to Korean/Bengali local speech output.
4. Add model-pack installation/setup and transparent storage/size information.
5. Test end-to-end in airplane mode on a real Android device and measure latency/memory.

See [offline model integration decisions](docs/OFFLINE_MODEL_INTEGRATION.md) for ASR candidates and acceptance criteria.

Never add a cloud fallback or claim full offline support until real models and voices pass airplane-mode testing.
