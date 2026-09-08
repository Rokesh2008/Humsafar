# Humsafar

> **Your phone doesn't just carry information. It helps verify it.**

Humsafar is an offline-first crowd intelligence MVP for crowded places. It turns independent, multilingual observations into one evidence-backed feed with local fusion logic, confidence scoring, contradiction visibility, and evidence trails.

## Current MVP

This repository contains a zero-dependency browser prototype that demonstrates the guaranteed Tier 0 experience:

- Crowd Feed with fused events, category icons, confidence bars, evidence counts, and language badges.
- New Report form with reporter and language selection.
- Local in-memory-style fusion behavior persisted only in the browser session via `localStorage` for convenient rehearsal.
- Multilingual/demo report handling for parking, food, washroom, entry, safety, and other observations.
- Contradiction handling that lowers confidence and keeps the conflict visible.
- Evidence detail modal showing the reports behind each fused event.
- About screen explaining the on-device reasoning thesis.
- No backend, cloud LLM call, account, or external API.

The seeded demo data is intentionally aligned with the submission script: three Gate 3 parking observations in English, Hindi, and Tamil; a food queue; a contradictory parking report; and a safety report.

## Run locally

Serve the directory with any static web server, for example:

```bash
python3 -m http.server 4173
```

Then open <http://localhost:4173> in a browser. The app is designed to remain usable without internet after the files are available locally.

## Scope boundary

This web prototype demonstrates the product loop and interaction design. The Android submission build should replace the browser-side heuristic `inferReport()` function with the on-device Gemma / MediaPipe LLM Inference pipeline described in the MVP specification. True multi-hop Nearby Connections mesh, iOS support, persistent backend storage, accounts, and cloud inference remain explicitly out of scope for the hackathon MVP.

## Suggested Android implementation next

1. Validate quantized Gemma loading on the target Android device.
2. Port the report and fused-event data structures to Kotlin in-memory state.
3. Replace `inferReport()` with strict JSON prompting and defensive parsing around MediaPipe output.
4. Add Android SpeechRecognizer for voice reports.
5. Add the local HTTP server only after the Tier 0 Android flow is reliable.
