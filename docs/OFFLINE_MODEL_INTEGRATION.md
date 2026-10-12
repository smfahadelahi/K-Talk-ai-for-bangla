# Offline model integration decisions

## Speech recognition candidates

The official sherpa-onnx pretrained-model catalog lists a Korean streaming Zipformer model and a Bengali streaming Zipformer/Vosk model. sherpa-onnx performs speech recognition locally, without internet access during inference:

- Catalog: https://k2-fsa.github.io/sherpa/onnx/pretrained_models/index.html
- Korean model: https://k2-fsa.github.io/sherpa/onnx/pretrained_models/offline-transducer/zipformer-transducer-models.html
- Android integration/build guide: https://k2-fsa.github.io/sherpa/onnx/android/build-sherpa-onnx.html

Before shipping, verify each model's license, model size, Android ABI support, latency, memory use, and accuracy with real Korean/Bengali conversations. The current app does not yet bundle sherpa-onnx or these models.

## Translation is the main unresolved model decision

A reliable, compact, directly paired Korean↔Bengali model with an Android-friendly runtime has not yet been verified for this project. Do not substitute a cloud API. Evaluate multilingual local models that support both Korean (ko) and Bengali (bn) and benchmark them on real-life Korea situations. The chosen model must support both directions and have a redistributable license compatible with the app.

A candidate must be evaluated for:
- Correctness for polite Korean, honorifics, food orders, work instructions, transport, and emergency phrases.
- Quantized model size and RAM usage on an Android device with 8 GB RAM.
- CPU latency and battery drain.
- Tokenizer/runtime compatibility with Android.
- Redistribution license and attribution requirements.
- Airplane-mode operation after installation.

## Packaging policy

- Never commit large model weights directly to Git unless repository size and model licensing have been reviewed.
- A future model-pack installer may download model files during setup, but the core translator must continue working in airplane mode after installation.
- Show model download/setup status and an explicit missing-model message. Do not generate made-up translation output.
- Keep runtime network access disabled unless a future, separately reviewed feature absolutely requires it.

## Acceptance test

1. Install the debug APK on a real Android phone.
2. Install/import the approved model pack.
3. Enable airplane mode and force-stop/relaunch the app.
4. Speak Korean and verify transcription and Bengali translation.
5. Speak Bengali and verify Korean translation and suggested replies.
6. Verify Korean and Bengali audio output with local voices.
7. Inspect logs and Android permissions to confirm no network requests occur.
8. Record latency, memory use, battery impact, and test failures before calling it production-ready.
