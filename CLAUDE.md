# CLAUDE.md — Med Tracker

Offline Android app to track prescription doses. Kotlin + Jetpack Compose, minSdk 26.

## Rules
- **No implementation code without an approved `plan.md` in `docs/NNN-feature/`.**
- Work one plan step at a time, one small PR per step. Nothing outside the spec.
- Tests are written from `spec.md` and named after requirements: `fr7_markTaken_thenUndo_backToPending`.
- Run tests and paste the real output (or the green CI link) before saying a step is done.
- Never add `android.permission.INTERNET` (NFR-1).
- No real patient names or prescriptions anywhere in the repo (NFR-6). Use made-up data.

## Commands
| What | Command | Healthy output |
|---|---|---|
| Unit tests | `./gradlew testDebugUnitTest` | `BUILD SUCCESSFUL`, 0 failures |
| Lint | `./gradlew lintDebug` | `BUILD SUCCESSFUL`, no errors |
| Debug APK | `./gradlew assembleDebug` | `app/build/outputs/apk/debug/app-debug.apk` |

The Claude workspace has no Android SDK; GitHub Actions (`ci.yml`) is the build of record.

## Architecture
Packages under `dev.samiul.medtracker`:
- `domain` — pure Kotlin rules (dose pattern, schedule, course, stock). Most tests here.
- `data` — Room + DataStore + Repository.
- `reminders` — AlarmManager scheduling and receivers (one alarm per slot).
- `ui` — Compose screens and ViewModels.

## Things Claude gets wrong
- (add a line when the same mistake happens twice)
