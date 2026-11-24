# Plan — 001 Medicine Tracker (offline Android app)

Source: `spec.md` (approved, FR-1…FR-19, NFR-1…NFR-7).

## Architecture

```
 UI (Jetpack Compose)          Today · History · Medicines · Edit · Settings
        │  ViewModels (StateFlow)
        ▼
 Repository  ──────────────►  Room DB (medicines, dose_logs)  +  DataStore (slot times)
        │
        ├── domain/ (pure Kotlin, no Android) ← most logic + most tests live here
        │     DosePattern · Schedule · Course · Stock · DoseStatus · Backup codec
        │
        └── reminders/
              ReminderScheduler → AlarmManager (one alarm per slot = max 3)
              ReminderReceiver  → posts notification, books next alarm
              MarkTakenReceiver → "Mark all taken" button
              RescheduleReceiver← BOOT_COMPLETED, MY_PACKAGE_REPLACED, TIME_SET, TIMEZONE_CHANGED
```

Single Gradle module `app`, packages: `domain`, `data`, `reminders`, `ui`.

## Design decisions

| # | Choice | Alternatives | Why |
|---|---|---|---|
| D1 | Kotlin + Jetpack Compose, Material 3 | Flutter, React Native | Your pick; native alarms are most reliable |
| D2 | minSdk 26, target/compile 35 | lower minSdk | NFR-2; java.time available without desugaring |
| D3 | Room for medicines + logs; DataStore for slot times | SQLite by hand, one JSON file | Typed queries, migrations, testable with Robolectric |
| D4 | Store only Taken/Skipped in `dose_logs`; **Missed is computed** (past day, no log) | Write Missed rows at midnight | No midnight job to fail; FR-8 always correct |
| D5 | Soft delete medicines (`deletedAt`) | Hard delete | FR-4 says past history stays |
| D6 | Store `tabletsLeft` on the medicine; Taken/Undo change it in the same DB transaction as the log | Recompute from logs | Refills (FR-16) are simple; transaction keeps them in step |
| D7 | One exact alarm per slot (next time that slot has any due dose), booked again after each fire and after any data change | One alarm per dose; WorkManager | Max 3 alarms; WorkManager can't promise exact time |
| D8 | `setExactAndAllowWhileIdle` + `SCHEDULE_EXACT_ALARM`; if not granted, fall back to `setAndAllowWhileIdle` and show FR-13 banner | `setAlarmClock` | Fires in Doze; fallback still reminds, just maybe late |
| D9 | Notification id = slot + date → same slot/day never shows twice | Random ids | Edge case: clock moved backwards |
| D10 | Backup = JSON via kotlinx.serialization, `{"version":1,...}`, saved/opened with Android's file picker (SAF) | Raw DB file copy | Readable, version-checked, no storage permission |
| D11 | No `INTERNET` permission; a test reads the merged manifest | Trust | NFR-1 proved, not assumed |
| D12 | Release APK signed with **your** keystore stored as GitHub Secrets | Debug signing | Same key every release, so updates install over the old app without losing data |
| D13 | Tests: JUnit on JVM for domain; Robolectric for DB, receivers, alarms; Compose UI tests under Robolectric | Emulator tests in CI | Runs on a normal GitHub runner, fast and free |

## Data model

- `Medicine(id, name, strength?, morning, noon, night [tablets, Double], startDate, lengthDays?, everyNDays=1, tabletsLeft?, note?, deletedAt?)`
- `DoseLog(medicineId, date, slot, status[TAKEN|SKIPPED], tablets)` — primary key `(medicineId, date, slot)`
- DataStore: `morningTime=08:00, noonTime=14:00, nightTime=21:00`

## Files to add

```
CLAUDE.md, README.md, .gitignore
settings.gradle.kts, build.gradle.kts, gradle/libs.versions.toml, gradlew + wrapper
app/build.gradle.kts, app/src/main/AndroidManifest.xml
app/src/main/java/.../domain/{DosePattern, Schedule, Course, Stock, DoseStatus, BackupCodec}.kt
app/src/main/java/.../data/{AppDb, MedicineDao, DoseLogDao, SettingsStore, Repository}.kt
app/src/main/java/.../reminders/{ReminderScheduler, ReminderReceiver, MarkTakenReceiver, RescheduleReceiver, Notifications}.kt
app/src/main/java/.../ui/{MainActivity, Nav, TodayScreen, HistoryScreen, MedicinesScreen, EditMedicineScreen, SettingsScreen, PermissionBanner}.kt + ViewModels
app/src/test/java/...  (all tests, see test map)
.github/workflows/ci.yml        # every push/PR: unit tests + lint + debug build
.github/workflows/release.yml   # tag v*: signed release APK → GitHub Release
```

## Steps (each = small PR, reviewed by you)

1. **Skeleton + CI** — Gradle project, empty Compose screen, CLAUDE.md, `ci.yml`, manifest test (NFR-1). *No release.*
2. **Domain logic** — DosePattern, Schedule, Course, Stock, DoseStatus with tests (FR-2, 3, 8, 14, 15, 17).
3. **Storage** — Room + DataStore + Repository, soft delete, Taken/Undo transaction (FR-1, 4, 5, 7, 15, 16).
4. **Medicines screens** — list, add/edit form with `1+0+1` box, delete confirm, refill, Settings slot times.
5. **Today + History** — check-off, Missed, Buy more, Course done (FR-6…9, 14, 17).
   → `release.yml` + signing secrets → **v0.1.0** (usable, no reminders yet).
6. **Reminders** — scheduler, receivers, notification with Mark all taken, permission banner (FR-10…13). → **v0.2.0**
7. **Backup** — BackupCodec + export/import in Settings (FR-18, 19). → **v1.0.0**

You install v0.1.0 and v0.2.0 on your phone between steps; anything found goes into `monitor.md`.

## Test map

| Req | Test name |
|---|---|
| FR-1 | `fr1_saveWithNameOnly_usesDefaults`, `fr1_emptyName_isRejected` |
| FR-2 | `fr2_parse_1plus0plus1`, `fr2_parse_halfTablet`, `fr2_rejectsLettersAndAllZero` |
| FR-3 | `fr3_everyThreeDays_dueOnStartAndEveryThird` |
| FR-4 | `fr4_delete_removesFutureDoses_keepsHistory` |
| FR-5 | `fr5_changeNightTime_movesDosesAndAlarm` |
| FR-6 | `fr6_today_groupsBySlot_hidesEmptySlots` |
| FR-7 | `fr7_markTaken_thenUndo_backToPending`, `fr7_markSkipped` |
| FR-8 | `fr8_unmarkedPastDose_isMissed`, `fr8_todayPastSlot_isStillPending` |
| FR-9 | `fr9_history_listsEachDoseWithStatus` |
| FR-10 | `fr10_alarmFires_postsOneNotificationNamingAllDueMeds` |
| FR-11 | `fr11_markAllTaken_marksDoses_andCancelsNotification` |
| FR-12 | `fr12_bootCompleted_reschedulesAlarms`, `fr12_timezoneChange_reschedules` |
| FR-13 | `fr13_notificationsBlocked_showsBanner` |
| FR-14 | `fr14_daysLeft_andCourseDoneAfterEnd`, `fr14_noEnd_neverDone` |
| FR-15 | `fr15_taken_subtracts_undo_adds_skipNoChange`, `fr15_neverBelowZero` |
| FR-16 | `fr16_refill_addsTablets` |
| FR-17 | `fr17_buyMore_whenUnderThreeDays_andCourseNeedsMore` |
| FR-18 | `fr18_export_containsAllMedicinesLogsAndSettings` |
| FR-19 | `fr19_import_roundTrip_restoresAll`, `fr19_badOrNewerFile_rejected_dataUnchanged` |
| NFR-1 | `nfr1_manifest_hasNoInternetPermission` |
| NFR-6 | test data uses made-up names only (checked in review) |

## Risks and rollback

- **No Android SDK in my workspace** — I can't build locally; GitHub Actions is the build. Each step's PR must show green CI before you review it.
- **Battery savers kill alarms** (spec concern 1) — README gets a "keep reminders working" section per phone brand; watch in `monitor.md`.
- **Lost signing key** = users must uninstall to update (data lost). Keep the keystore file + passwords backed up outside GitHub.
- **Room schema changes later** — export schema JSON from step 3; every change ships with a migration and a test.
- **Rollback**: each release is a tag; install the previous APK from Releases (same key, so it installs; downgrade may need uninstall → export a backup first once v1.0 exists).

## Needed from you before step 1
- A private repo name (e.g. `Samiul2651/med-tracker`) and that I may push to it.
- For step 5: create a keystore (I'll give the exact command) and add 4 repo secrets.
