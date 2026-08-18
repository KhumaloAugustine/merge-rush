# Merge Rush

An offline-first Android number-merging game built with Kotlin, Jetpack Compose, MVVM-style state, and DataStore. No backend or paid hosting is required.

## Run locally

1. Install Android Studio with Android SDK 36 and JDK 17.
2. Open this directory and let Gradle sync.
3. Run the `app` configuration on an API 26+ emulator or device.

The GitHub Actions workflow can build the debug APK without committing a Gradle wrapper. Run `gradle wrapper --gradle-version 8.11.1` locally if you want wrapper scripts in the repository.

## Current v1 scope

- 4×4 swipe-to-merge game with scoring and game-over detection
- Combo multipliers (up to ×5), tile goals, milestone bonuses, and three undo power-ups per run
- Ten-level campaign with progressive targets, strict move budgets, coin rewards, and saved unlocking
- Guided help, strategic move hints, star ratings, haptic swipe feedback, animated tiles, and one-tap next-level transitions
- Persistent onboarding and forty-stage, four-chapter campaign with mixed tile, score, combo, and move-limit objectives
- Modern synthesized chimes and major-key arpeggios, mute control, board danger meter, and one tactical shuffle per run
- Animated victory trophy and defeat feedback plus consistent Home, Challenges, and Statistics navigation
- Persistent System, Light, and Dark appearance modes with contrast-safe text and a dedicated Settings screen
- One rewarded-continue simulation per game
- Persistent high score, coins, games played, and highest tile
- Home, game, and statistics screens
- Static GitHub Pages landing page and privacy policy in `docs/`
- Unit tests for core merge rules

## Before Google Play release

- Replace the application ID with a package name you control.
- Add release signing; never commit the keystore or passwords.
- Add production AdMob only after creating the app/ad units, implementing consent, using test ads during development, and updating the privacy policy/Data safety form.
- Replace the privacy-policy contact placeholder and publish `docs/` with GitHub Pages.
- Produce a signed Android App Bundle (`bundleRelease`) and complete store listing, content rating, ads declaration, and testing requirements.

Rewarded ads are intentionally abstracted as a demo button in this starter so the app is playable without network access or an AdMob account.
