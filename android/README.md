# Humsafar Android

This is the primary native submission path: **Kotlin + Jetpack Compose + MediaPipe GenAI**.

## Open and run

Open this `android/` directory in Android Studio, allow Gradle sync, and run on a physical Android device. The LLM runtime is optimized for high-end devices and is not expected to work reliably on an emulator.

## Model spike

The app deliberately starts without bundling a 1GB+ model. Copy a compatible quantized `.task` model to the app's internal storage path:

```text
/data/data/com.humsafar.app/files/humsafar/gemma-3-1b-it-int4.task
```

The `FusionEngine` checks for that file at startup. If present, it initializes `LlmInference` with a short, low-temperature JSON prompt. If absent or initialization fails, it uses the deterministic local fallback so the UI and demo flow remain usable.

For a submission build, validate the exact model/runtime pairing on the target phone first. The official Google guide currently documents `com.google.mediapipe:tasks-genai:0.10.27` and notes that the API is maintenance-only, with LiteRT-LM as the forward-looking replacement. The runtime boundary is isolated in `FusionModel.kt` so this can be swapped without rewriting the Compose screens or data model.

## Current native scope

- Tier 0: one-device report submission, in-memory fusion, multilingual seed script, confidence and evidence feed.
- Voice: Android `SpeechRecognizer`, with runtime microphone permission.
- Organizer view: basic in-session category, safety, language, and reporter counts.
- Deferred: NanoHTTPD local server and ZXing QR generation. The existing root-level browser prototype remains the Tier 1/Tier 2 rehearsal path until the native Tier 0 model spike is confirmed.
