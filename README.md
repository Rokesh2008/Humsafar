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

For the single-device fallback, serve the directory with any static web server, for example:

```bash
python3 -m http.server 4173
```

Then open <http://localhost:4173> in a browser. The app is designed to remain usable without internet after the files are available locally.

## Tier 1 local room

The repository also includes a dependency-free Node HTTP server. It serves the app and exposes a shared room API backed by a durable local JSON file:

```bash
node server.js
```

The terminal prints a nearby-device URL such as `http://192.168.x.x:4173`. Open that URL on the host device and on another phone or laptop connected to the same hotspot. Reports submitted from either browser are posted to the local room and polled into the shared feed every three seconds. No external network or package install is required. Runtime data is stored in `data/humsafar-room.json` and is ignored by Git.

## Tier 2 organizer reporting

The **Organizer Insights** tab adds a post-event view over the persistent room history. It shows total reports, distinct reporters, safety signals, contradictions, category distribution, language mix, and the room timeline. Organizers can export the current room as CSV for spreadsheet analysis or JSON for archival and future integrations.

The server exposes these reporting endpoints:

- `GET /api/analytics` — aggregate room metrics.
- `GET /api/export.csv` — flat report export.
- `GET /api/export.json` — room data plus analytics snapshot.
- `DELETE /api/state` — reset the persistent room intentionally.

## Scope boundary

This web prototype demonstrates the product loop and interaction design. The Android submission build should replace the browser-side heuristic `inferReport()` function with the on-device Gemma / MediaPipe LLM Inference pipeline described in the MVP specification. The local server is intentionally a hub-and-spoke demo, not a claim of true mesh networking. True multi-hop Nearby Connections mesh, iOS support, hosted multi-tenant storage, accounts, and cloud inference remain out of scope for the hackathon MVP. Tier 2 persistence is local organizer storage for the prototype, not a production backend.

## Suggested Android implementation next

1. Validate quantized Gemma loading on the target Android device.
2. Port the report and fused-event data structures to Kotlin in-memory state.
3. Replace `inferReport()` with strict JSON prompting and defensive parsing around MediaPipe output.
4. Add Android SpeechRecognizer for voice reports.
5. Add the local HTTP server only after the Tier 0 Android flow is reliable.
