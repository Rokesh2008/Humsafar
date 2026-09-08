# Humsafar Android

This is the primary native submission path: **Kotlin + Jetpack Compose + MediaPipe GenAI**.

## Open and run

Open this `android/` directory in Android Studio, allow Gradle sync, and run on a physical Android device. The LLM runtime is optimized for high-end devices and is not expected to work reliably on an emulator.

## Model spike

The app deliberately starts without bundling a 1GB+ model. Copy a compatible quantized `.task` model to the app's internal storage path:

```text
/data/data/com.humsafar.app/files/humsafar/gemma-2b-it-int4.task
```

The `FusionEngine` checks for that file at startup. If present, it initializes `LlmInference` with a short, low-temperature JSON prompt. If absent or initialization fails, it uses the deterministic local fallback so the UI and demo flow remain usable.

For a submission build, validate the exact model/runtime pairing on the target phone first. The official Google guide currently documents `com.google.mediapipe:tasks-genai:0.10.27` and notes that the API is maintenance-only, with LiteRT-LM as the forward-looking replacement. The runtime boundary is isolated in `FusionModel.kt` so this can be swapped without rewriting the Compose screens or data model.

## Current native scope

- Tier 0: one-device report submission, in-memory fusion, multilingual seed script, confidence and evidence feed.
- Voice: Android `SpeechRecognizer`, with runtime microphone permission.
- Organizer view: basic in-session category, safety, language, and reporter counts.
- Tier 1: NanoHTTPD local report page on port 8080; browser submissions use the same fusion engine.
- Event Mode: in-app server control and browser handoff from the Event tab.
- Tier 2: Event Mode also generates a QR code for the detected local report URL.

## Tier 1 demo

1. Connect the demo phone and a laptop/second phone to the same hotspot.
2. Open the Event tab and tap **START LOCAL REPORT PAGE**.
3. Find the Android phone's hotspot IP address and open `http://PHONE_IP:8080` on the second device.
4. Submit a report in the browser and return to Humsafar; it will appear in the same feed.

The guaranteed fallback remains Tier 0: airplane mode, scripted reports, and deterministic local fusion when no `.task` model is installed.
