# Intent — 001 Medicine Tracker (offline Android app)

## Problem
A doctor's prescription lists several medicines, each with its own times
(e.g. `1+0+1` = morning and night) and its own length (e.g. "1 month").
It is easy to forget a dose, lose track of which day a course ends, or
run out of tablets before the course is done. Paper gives no reminders.

## Who it is for
- One person tracking their own medicines on their own Android phone.
- First real use: a pregnancy prescription with 6 medicines
  (mostly 1 month, one taken on a gap schedule).
- General enough for any prescription, not tied to this one.

## Desired outcome
- Enter a prescription once; the app tells you what to take and when.
- Tick each dose as taken.
- Get a phone notification at each dose time.
- See how many days are left in each course and how many tablets are left,
  with a warning before they run out.
- Works with no internet, ever.

## Success measures
- Adding one medicine takes under 1 minute.
- Reminders fire on time (within 1 minute) even after the phone restarts.
- No missed check-offs because of a lost or wrong reminder over 2 weeks of use.
- Zero network calls by the app.

## Constraints
- Android APK only (no web, no iOS for now).
- Fully on GitHub: code in a GitHub repo, APK built by GitHub Actions,
  downloaded from GitHub Releases. No server, no paid services.
- All data stays on the phone.
- No real patient data (names, prescriptions) committed to the repo.

## Out of scope (v1)
- Reading the prescription photo automatically (OCR).
- Sync between phones, accounts, cloud backup.
- Tests/advice list (βhCG, TSH, USG…) — maybe v2.
- Multiple patient profiles.
- Medical advice or dose checking. The app only stores what the user types.
- Play Store publishing.

## Decisions (were open questions)
- Repo: public (changed from private on 24 Nov 2026: Actions would not start on the private repo). Still no personal data in it.
- Language: English only.
- Gap schedules ("every N days") are in v1.
