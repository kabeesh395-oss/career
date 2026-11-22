# Career Hub

Career Hub is a multi-part career-readiness project. The repository contains an Android application, a React/Vite web client, and a Node.js/Express TypeScript backend. The backend uses SQLite for application persistence; AI integrations are configured through environment-specific settings.

## Repository components

- `app/` — native Android application built with Kotlin and Jetpack Compose.
- `client/` — React 18 + Vite web client.
- `server/` — TypeScript/Express API and backend tests.
- `docs/` — architecture, AI model, edge-AI, performance, privacy, and demo documentation.
- `backend-security-performance-audit.md`, `career-hub-complete-audit-and-execution-plan.md`, and `career-hub-implementation-roadmap.md` — project audit and planning documents.

## Requirements

- Node.js 22.5 or newer for the backend (see `server/package.json`).
- JDK 21 for Android builds.
- Android SDK platform 35 for the Android CI configuration.
- npm.

## Install web and backend dependencies

From the repository root:

```bash
npm run install:all
```

Start the backend and web client in separate terminals:

```bash
npm run server
```

```bash
npm run client
```

Use the Vite URL printed by the client development server. Backend environment and integration settings should be supplied locally; the root `.env.example` documents the Gemini API key placeholder. Never commit real API keys, tokens, database files, uploaded resumes, or signing credentials.

## Android build

From the repository root:

```bash
./gradlew testDebugUnitTest
./gradlew assembleDebug
```

On Windows PowerShell, use `gradlew.bat`. The Android build applies the Google Services plugin only when `app/google-services.json` is present; Firebase-backed runtime features still need configuration for the Firebase project being used.

## Tests and quality checks

The CI workflow is the source of truth for the full verification matrix. Common local checks include:

```bash
cd server
npm ci
npm run build
npm run test:database
npm run test:upload-validation
npm run test:security
```

```bash
cd client
npm ci
npm run build
npm test
```

For Android, run `./gradlew testDebugUnitTest`. The workflow also runs additional backend persistence, authentication, CORS, error-handling, AI queue, load, and runtime QA suites.

## Documentation

- [Architecture](docs/ARCHITECTURE.md)
- [AI model integration](docs/AI_MODEL.md)
- [Edge AI](docs/EDGE_AI.md)
- [Performance](docs/PERFORMANCE.md)
- [Privacy](docs/PRIVACY.md)
- [Demo notes](docs/DEMO.md)

See [Career Hub Automation and Quality Gate](.github/workflows/career_hub_automation.yml) for CI jobs and exact commands. CI success is not a substitute for verifying the app on a physical Android device and validating external integrations with credentials configured by the developer.
