# LifeOS

A personal life management Android application built with Kotlin and Jetpack Compose. LifeOS brings together ten independent modules covering nutrition, habits, journaling, media consumption, finance, sports, sleep, notes, physical collections, and AI-generated insights into a single app with a unified home screen.

The app is local-first. All data lives on your device in per-module Room databases. External APIs are optional and require your own credentials.

---

## Features

* **Meal Tracker**: Log daily meals with macros and calories, scan barcodes to look up nutrition data from the FatSecret database, track body weight, and view trend charts
* **Habit Tracker**: Create habits with flexible schedules, check in daily, monitor streaks, and receive configurable reminders via WorkManager
* **Journal**: Write daily entries with mood tracking and photo attachments, review weekly image galleries, and get AI-generated mood pattern insights
* **Notes**: Quick notes with checklist support, pinning, and scheduled reminder notifications
* **Sleep Tracker**: Log sleep sessions with quality ratings, dream notes, medication tracking, and weekly statistics
* **Media Logger**: Track books, manga series, movies, and games with ratings, cover art, and completion dates alongside a statistics screen
* **Sports**: Live scores, standings, schedules, and game alerts for NFL, NBA, MLB, NHL, and MLS via the ESPN API
* **Physical Media**: Inventory your physical book, movie, game, and TV series collection with format details, cover art, and a random picker
* **Finance Tracker**: Manage accounts, log transactions, set category budgets, track sinking funds, schedule recurring transactions, and monitor net worth over time
* **AI Insights**: Pull data from every module and generate weekly and monthly insight reports using the Groq LLM API

---

## Screenshots

_Coming soon._

---

## Tech Stack

| Layer | Technology |
|---|---|
| Language | Kotlin |
| UI | Jetpack Compose, Material 3 |
| Architecture | MVVM, multi-module Gradle project |
| Dependency Injection | Hilt |
| Database | Room (SQLite, one database per module) |
| Async | Kotlin Coroutines, Flow |
| Background Tasks | WorkManager |
| Networking | OkHttp, Kotlin Serialization |
| Camera / Barcode | CameraX, ML Kit |
| Charts | Vico |
| Image Loading | Coil |
| Min SDK | Android 13 (API 33) |

---

## Project Structure

```
LifeOS/
  app/                        Main application shell and navigation
  modules/
    lifeos_core/              Shared interfaces and module registry
    lifeos_mealtracker/       Nutrition and calorie tracking
    lifeos_notes/             Note taking with checklists
    lifeos_habittracker/      Habit building and streaks
    lifeos_medialogger/       Books, manga, movies, and games log
    lifeos_sleeptracker/      Sleep session and quality tracking
    lifeos_journal/           Daily journaling with mood and photos
    lifeos_sports/            Live sports scores and alerts
    lifeos_physicalmedia/     Physical collection inventory
    lifeos_financetracker/    Personal finance and net worth
    lifeos_aiinsights/        AI-generated life insights
```

### Module Architecture

Each module is a self-contained Gradle subproject with its own:

* Room database and DAOs
* Repository and domain layer
* Hilt dependency injection module
* Jetpack Compose UI and ViewModel
* WorkManager workers for notifications and background tasks

Modules expose themselves to the app shell through the `LifeOSModule` interface defined in `lifeos_core`. The `ModuleRegistry` in the main app registers all modules at startup and drives navigation.

Modules that contribute to AI reports implement the `InsightProvider` and `ReportDataProvider` interfaces so the AI Insights module can pull structured data from them without creating direct dependencies between feature modules.

---

## Setup

### 1. Clone the repository

```bash
git clone https://github.com/tristinbaker/LifeOS-Public.git
cd LifeOS-Public
```

### 2. Configure local.properties

Copy the example file and fill in your own credentials:

```bash
cp local.properties.example local.properties
```

Edit `local.properties`:

```
sdk.dir=/path/to/your/android-sdk

# Groq API key for AI Insights (https://console.groq.com)
GROQ_API_KEY=your_groq_api_key_here

# FatSecret credentials for barcode food lookup (https://platform.fatsecret.com)
FATSECRET_CLIENT_ID=your_client_id_here
FATSECRET_CLIENT_SECRET=your_client_secret_here
```

All three keys are optional. The app runs without them but the Meal Tracker barcode lookup and AI Insights features will not function.

### 3. Build and install

```bash
./gradlew assembleRelease
```

For development builds:

```bash
./gradlew assembleDebug
```

---

## External APIs

### Open-Meteo (no key required)

The home screen weather card uses the Open-Meteo geocoding, weather forecast, and air quality APIs. No account or API key is needed.

* Geocoding: `https://geocoding-api.open-meteo.com/v1/search`
* Forecast: `https://api.open-meteo.com/v1/forecast`
* Air Quality: `https://air-quality-api.open-meteo.com/v1/air-quality`

### ESPN (no key required)

The Sports module pulls scores, standings, schedules, and game details from the public ESPN site API. No account is needed.

* `https://site.api.espn.com/apis/site/v2/sports`

### FatSecret (account required)

The Meal Tracker uses FatSecret for barcode to nutrition data lookups. Register at `https://platform.fatsecret.com` and add your client ID and secret to `local.properties`.

### Groq (account required)

The AI Insights module sends structured prompts to the Groq chat completions endpoint. Get a key at `https://console.groq.com` and add it to `local.properties`.

---

## Module Details

### Meal Tracker

Logs meals with calories, protein, carbohydrates, and fat. Meals can be entered manually, selected from saved meals or stored food items, or looked up by scanning a product barcode with the camera. The barcode flow uses CameraX and ML Kit to decode the barcode then queries FatSecret for nutrition data.

Weight entries are tracked separately and appear in the dashboard alongside calorie totals. A trend chart shows calorie intake over the past weeks using Vico.

### Habit Tracker

Habits are created with a name, frequency (daily, specific days of the week, or a custom interval), and an optional reminder time. WorkManager schedules a periodic notification worker that fires at the configured time and posts a reminder notification. Check-ins are stored per day and the module calculates current streaks and completion rates.

### Journal

Each journal entry has a date, text content, a mood value, and any number of attached images. Images are compressed and stored on device. A weekly review screen loads all entries and images from the past seven days. Daily reminder notifications are scheduled through WorkManager at a user-configured time.

### Notes

Notes support both plain text and checklist mode. In checklist mode the content is serialized as JSON and rendered as a list of checkable items. Notes can be pinned to appear at the top of the list. Each note can have a scheduled reminder that posts a notification at a chosen time.

### Sleep Tracker

Sleep sessions record a start time, end time, quality rating, optional notes, optional dream notes, and whether sleep medication was taken. The stats card on the list screen shows weekly averages for sleep duration and quality, calculated using circular mean arithmetic for time values so that times wrapping around midnight are handled correctly.

### Media Logger

Tracks four media types:

* **Books** with title, author, series, volume, start and finish dates, rating, and cover art
* **Manga** organized as series with individual volume entries
* **Movies** with title, year, rating, rewatch flag, and cover art
* **Games** with title, platform, completion status, platinum trophy flag, and 100% flag

A stats screen breaks down counts, average ratings, and rating distributions by media type and year. Cover images are searched via an external image service and cached locally.

### Sports

Supports NFL, NBA, MLB, NHL, and MLS. Each league shows a scores tab with current or recent game results, a standings tab with full table data, and a schedule tab for upcoming games. A last game screen shows detailed recap information for a selected team.

Favorite teams are stored locally. WorkManager schedules a daily job that checks for upcoming games and schedules a notification for game start time.

ESPN API responses are cached in a Room database to limit redundant network calls. The cache handles oversized responses that would exceed SQLite cursor limits.

### Physical Media

Tracks the physical versions of media you own rather than what you have watched or read. Supports:

* **Books** with format (hardcover, paperback, audiobook) and series information
* **Movies** with format (DVD, Blu-ray, 4K UHD), limited edition, steelbook, and slipcover flags
* **Games** with platform (Switch, PS4, PS5, Xbox, PC, and others) and collection grouping
* **TV Series** with format and series volume number

A random picker button selects a random item from any category for decision making. A stats screen shows collection counts and breakdowns by format and platform.

### Finance Tracker

Account types include checking, savings, credit card, loan, mortgage, and investment. Transactions are categorized and can be assigned to a budget. Recurring transactions are scheduled through WorkManager and processed automatically in the background.

Sinking funds track progress toward named savings goals. Investment and snapshot accounts record periodic balance entries to build a net worth history over time. A trends screen shows monthly spending by category and net worth changes. Mortgage accounts store APR, term, and origination date for amortization context.

### AI Insights

Collects a structured text summary from each module's `ReportDataProvider` implementation, constructs a prompt, and sends it to the Groq chat completions API. The response is cached by month in a local Room table so the API is not called again until the next month begins or the cache is cleared.

Insight providers are ordered by priority. The current set includes sleep patterns, habit streaks, mood and journal analysis, media recommendations, physical collection picks, and retirement outlook based on current net worth and age.

---

## Biometric Authentication

The app requires biometric authentication (fingerprint or device credential) on launch. This is implemented in `MainActivity` using the AndroidX Biometric library. The preference to enable or disable the lock screen is stored in DataStore.

---

## Backup and Restore

The app includes a manual backup system that exports all module databases and journal images into a single zip archive to a user-chosen location. Restoring from a backup replaces the current databases with the archived copies and restarts the app.

---

## Permissions

The app requests the following permissions at runtime:

* `CAMERA` for barcode scanning in the Meal Tracker
* `POST_NOTIFICATIONS` for habit, journal, sleep, note, and sports reminders
* `READ_EXTERNAL_STORAGE` or `READ_MEDIA_IMAGES` for journal photo attachments
* `USE_BIOMETRIC` and `USE_FINGERPRINT` for the lock screen

---

## Contributing

This is a personal project but pull requests are welcome. Each module is isolated so changes to one module should not affect others. Follow the existing MVVM pattern and use Hilt for injection.

---

## License

See `LICENSE` for details.
