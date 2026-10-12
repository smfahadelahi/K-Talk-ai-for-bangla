# K-Talk-ai-for-bangla

K-Bridge AI is an Android app for private Korean ↔ Bangla communication and practical Korean phrase practice, built around a strict offline-first requirement.

## Implemented in this branch
- Kotlin + Jetpack Compose Android app (minimum Android 12 / API 31).
- Android's explicit on-device speech-recognition adapter for Korean and Bengali language tags; no cloud recognizer fallback.
- Role-based offline phrasebook with Korean text, Bengali meanings, and pronunciation hints.
- Exact-match phrasebook translation available without any model files.
- Local SMaLL-100 INT8 ONNX translation engine for general Korean↔Bengali text after importing the model pack.
- Deterministic offline reply suggestions for common restaurant, payment, and clarification situations.
- Android TTS adapter that selects only voices marked as not requiring network access.
- Model ZIP import into app-private storage; no in-app model download.
- No Android INTERNET permission in the manifest.
- GitHub Actions workflow to build a debug APK and run unit tests.

## Install the translation model once
The model pack is separate from the APK because the INT8 ONNX model is about 600 MB.

1. Open the repository's **Actions** tab.
2. Select **Package Offline Korean-Bengali Translation Model**.
3. Click **Run workflow**, wait for completion, then download the `small100-ko-bn-model-pack` artifact ZIP.
4. Install the debug APK.
5. In K-Bridge AI, tap **Import offline model ZIP** and select the downloaded ZIP.
6. After import completes, the app can run model inference without internet access.

Model source and license: https://huggingface.co/casawolice/small100-onnx (SMaLL-100 ONNX, MIT license). Model-pack generation uses GitHub Actions; the Android app itself does not download the model.

## Important limitations
- Android on-device speech recognition only works if the phone has a compatible on-device recognizer and offline language packs for the selected language. The app does not silently fall back to cloud recognition.
- Offline speech output requires local Korean/Bengali voices installed on the phone; voices marked as network-required are rejected.
- The translation model is wired into the app, but its runtime quality, latency, and memory use must be verified on a real Android device before production use.
- Contextual replies are deterministic local rules/phrasebook suggestions, not a generative AI model.

## Build and CI
The GitHub Actions workflow uses JDK 17 and Gradle 8.9 to run:
- `gradle assembleDebug`
- `gradle testDebugUnitTest`

After a successful run, download the `k-bridge-ai-debug-apk` artifact from that workflow run.

## Acceptance test
1. Install the APK and import the model ZIP.
2. Enable airplane mode and restart the app.
3. Test Korean→Bengali and Bengali→Korean translation with several real sentences.
4. Test Korean and Bengali on-device speech recognition with offline language packs.
5. Test local Korean and Bengali speech output.
6. Measure quality, latency, memory, battery, and confirm no network traffic.

Never claim the full conversation loop is production-ready until these tests pass on the target phone.
