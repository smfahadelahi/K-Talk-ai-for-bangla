# Offline model integration decisions

## Translation model selected for integration

The project now uses SMaLL-100 converted to INT8 ONNX as the first general Korean↔Bengali translation candidate:

- Model pack: https://huggingface.co/casawolice/small100-onnx
- Model card documents direct translation between 100 languages, including Korean (ko) and Bengali (bn), and a MIT license.
- The ONNX deployment set is approximately 609 MB and contains an encoder, merged decoder, tokenizer, and language-token map.
- The Android example is documented as a reference implementation; this repository has its own Kotlin integration, which still needs successful CI and real-device benchmarking.

The APK does not embed the 600+ MB model files. Run the manual GitHub Actions workflow named **Package Offline Korean-Bengali Translation Model** to create the ZIP artifact. In the app, choose **Import offline model ZIP** and select that ZIP. The import copies model files into app-private storage. Once installed, translation inference uses ONNX Runtime locally and does not need network access.

Model pack import requires a one-time download through GitHub Actions and a transfer to the phone. The app itself does not download model weights and has no Android INTERNET permission.

## Speech recognition candidates

The official sherpa-onnx pretrained-model catalog lists a Korean streaming Zipformer model and a Bengali streaming Zipformer/Vosk model. sherpa-onnx performs speech recognition locally, without internet access during inference:

- Catalog: https://k2-fsa.github.io/sherpa/onnx/pretrained_models/index.html
- Korean model: https://k2-fsa.github.io/sherpa/onnx/pretrained_models/offline-transducer/zipformer-transducer-models.html
- Android integration/build guide: https://k2-fsa.github.io/sherpa/onnx/android/build-sherpa-onnx.html

The current app uses Android's explicit on-device SpeechRecognizer service rather than bundling sherpa-onnx. Availability and Korean/Bengali language support depend on the phone's installed speech service and offline language packs. It does not fall back to a cloud recognizer.

## Packaging and privacy policy

- Never add a cloud translation API or silently upload audio.
- The Android manifest must not declare INTERNET permission for core translation.
- Model ZIP extraction accepts only the four expected files and installs into app-private storage.
- Unknown text must never be replaced with fabricated output; model errors should be surfaced clearly.
- Local TTS is used only when Android marks the voice as not requiring a network connection.

## Acceptance test

1. Build and install the APK.
2. Run the manual model-pack workflow and download its ZIP artifact.
3. Import the ZIP in the app and confirm the model status reports installed.
4. Enable airplane mode and force-stop/relaunch the app.
5. Type Korean and Bengali sentences and verify both translation directions.
6. Speak Korean and Bengali and check on-device recognition only when the relevant offline language packs are available.
7. Verify Korean/Bengali audio output with local voices.
8. Inspect logs and Android permissions to confirm no network requests occur.
9. Record translation quality, latency, peak memory, battery impact, and failures on the target phone before calling it production-ready.
