# K-Bridge AI

An offline-first, Bengali-first Korean learning and conversation companion for Android.

## Product direction
- Local AI tutor using an on-device open-weight language model; no paid AI API required for core chat.
- Bengali explanations for Korean phrases, grammar, and real-life scenarios.
- Role-play missions: restaurant, shopping, university, transport, housing, and part-time work.
- Headphone-first conversation mode as a separately tested feature.
- Local-first progress and saved vocabulary.

## Implementation status
This is an early Android scaffold, not a finished app. The initial Compose UI is a visual foundation. The on-device model runtime dependency is included, but model downloading/loading, inference, offline speech recognition, Korean/Bengali TTS, local persistence, and end-to-end headphone routing still need implementation and device testing.

## Cost approach
Core inference should run on-device. This avoids per-request cloud AI fees, but users still need to download a model, which consumes storage, battery, memory, and CPU/GPU resources. Optional cloud features must never be required for the offline core.

## Privacy principles
- No AI API keys in the APK.
- Ask for microphone permission only when the user starts a voice feature.
- Keep learning history on-device by default.
- Make optional network features explicit and opt-in.

## Roadmap
1. Verify clean Android build and app launch.
2. Integrate and test a compatible on-device LLM.
3. Implement structured tutor prompts and scenario engine.
4. Add offline speech recognition and Korean/Bengali TTS.
5. Build headphone turn detection and audio routing.
6. Add local progress storage, tests, accessibility, and release signing.
7. Produce and test an installable APK.
