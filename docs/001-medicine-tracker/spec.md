# Spec — 001 Medicine Tracker (offline Android app)

Source: `intent.md` (approved).

## Words used
- **Medicine** — one line of a prescription (e.g. "Tab. X 10mg, 1+0+1, 1 month").
- **Slot** — a time of day: Morning, Noon, Night. Each has a default time.
- **Dose** — one medicine due in one slot on one day.
- **Course** — the days a medicine must be taken (start date + length).

## Functional requirements

**Medicines**
- **FR-1** The user can add a medicine with: name (required), strength (optional text),
  slots taken, tablets per dose (default 1, allows 0.5), start date (default today),
  length in days or "no end", schedule, starting tablet count (optional), note (optional).
- **FR-2** The user can enter slots with the prescription pattern `M+N+E`
  (e.g. `1+0+1`); each number is the tablets for Morning, Noon, Night.
- **FR-3** Schedule can be "every day" or "every N days" (N = 2–30), counted from the start date.
- **FR-4** The user can edit or delete a medicine. Delete asks for confirmation.
- **FR-5** The user can change the default time of each slot (defaults: 08:00, 14:00, 21:00).

**Today and check-off**
- **FR-6** The Today screen lists every dose due today, grouped by slot, in time order.
- **FR-7** The user can mark a dose Taken or Skipped, and undo it.
- **FR-8** A dose not marked by the end of its day is shown as Missed.
- **FR-9** A History screen shows, for each past day, each dose as Taken, Skipped or Missed.

**Reminders**
- **FR-10** At each slot time on a day with due doses, the phone shows one notification
  listing that slot's medicines.
- **FR-11** The notification has a "Mark all taken" button that marks those doses Taken.
- **FR-12** Reminders keep working after a phone restart, an app update,
  and a change of time or time zone.
- **FR-13** If notification or exact-alarm permission is off, the app shows a banner
  that opens the right settings page.

**Course and tablet count**
- **FR-14** Each medicine shows days left and its end date. After the end date it shows
  "Course done", has no more doses and sends no more reminders.
- **FR-15** If a starting count is set, marking Taken subtracts the tablets per dose;
  undo adds them back. Skipped and Missed do not change the count.
- **FR-16** The user can add tablets (refill) to a medicine's count.
- **FR-17** When tablets left will last fewer than 3 days of doses (and fewer than the
  doses left in the course), the medicine shows a "Buy more" warning on Today.

**Backup (local file, offline)**
- **FR-18** The user can export all data (medicines, history, slot times) to one JSON
  file saved anywhere on the phone (Downloads, Drive app folder etc.).
- **FR-19** The user can import such a file. Import replaces all current data after a
  confirmation, then reschedules reminders. A bad or newer-version file is rejected
  with a message and nothing changes.

## Non-functional requirements
- **NFR-1** Fully offline: the app does not request the INTERNET permission.
- **NFR-2** Runs on Android 8.0 (API 26) and newer.
- **NFR-3** All data stored only on the phone; survives app updates.
- **NFR-4** Today screen opens in under 1 second on a mid-range phone.
- **NFR-5** APK built by GitHub Actions from the public repo and attached to a GitHub Release; no paid services.
- **NFR-6** No real patient data in the repo (code, tests or docs).
- **NFR-7** English labels only.

## Acceptance criteria

| FR | Given | When | Then |
|---|---|---|---|
| FR-1 | Add form open | user saves name only | medicine saved with 1 tablet, today's start, every day |
| FR-1 | Add form open | name empty | Save is blocked with a message |
| FR-2 | Add form | user types `1+0+1` | Morning=1, Noon=0, Night=1 |
| FR-2 | Add form | user types `1+x+1` or `0+0+0` | error shown, not saved |
| FR-3 | every 3 days, start 1 Dec | Today = 4 Dec | medicine due; on 2–3 Dec not due |
| FR-4 | a medicine exists | user deletes and confirms | it and its future reminders are gone; past history kept |
| FR-5 | Night = 21:00 | user sets 22:00 | Night doses and reminders move to 22:00 |
| FR-6 | 2 medicines with `1+0+1` | Today opens | Morning: 2 doses, Night: 2 doses, no Noon group |
| FR-7 | dose is pending | user taps Taken, then Undo | dose back to pending |
| FR-8 | dose pending yesterday | Today opens | yesterday's dose shown Missed in History |
| FR-9 | 3 days of use | History opens | each day lists each dose with its status |
| FR-10 | 2 meds due Morning 08:00 | clock reaches 08:00 | one notification naming both |
| FR-11 | that notification | user taps "Mark all taken" | both doses Taken, notification gone |
| FR-12 | reminders set | phone restarts | next reminder still fires on time |
| FR-13 | notifications blocked | Today opens | banner shown; tap opens settings |
| FR-14 | 30-day course started 1 Dec | Today = 30 Dec | "1 day left"; on 31 Dec "Course done", no doses |
| FR-15 | count 20, 1 per dose | user marks Taken | count 19; Undo → 20 |
| FR-16 | count 2 | user adds 30 | count 32 |
| FR-17 | `1+0+1`, count 5, 20 days left | Today opens | "Buy more" shown (5 < 6 tablets for 3 days) |
| FR-18 | 2 medicines, 3 days history | user exports | one `.json` file containing both and all logs |
| FR-19 | that file, fresh install | user imports and confirms | same medicines, history and reminders as before |
| FR-19 | a broken file | user imports | "Not a valid backup", data unchanged |

## Edge cases and errors
- Medicine added after a slot time today: that past dose is shown pending, not Missed, until the day ends; no reminder for it.
- Tablet count would go below 0: stop at 0 and show "Buy more".
- "No end" medicines never show "Course done".
- Two medicines with the same name are allowed.
- Changing start date or schedule only changes doses from today on; past history stays.
- Phone date moved backwards: no duplicate notifications for the same slot and day.

## Concerns flagged
1. **Battery savers** (Xiaomi, Oppo, Samsung etc.) can delay or kill alarms. The app can only point the user to settings; FR-12 may still fail on some phones.
2. **Backup is manual**: FR-18/19 only help if the user exports. Uninstalling without an export still loses data.
3. **Public repo**: anyone can see the code and download the APK, so the repo must never hold personal data (NFR-6). Installing needs "Install unknown apps" allowed.
4. **Not medical advice**: the app trusts what the user types and never checks doses.
5. Odd schedules like "1 tablet, every 3 days, 3 times" are covered by: every 3 days + length 7 days (gives days 1, 4, 7).
