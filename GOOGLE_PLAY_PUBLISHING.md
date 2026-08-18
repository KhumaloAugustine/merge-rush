# Publish Merge Rush on Google Play — Beginner Guide

This guide is written for the current Merge Rush project. Complete the steps in order and tick each box as you go. It was verified against Google's official requirements on **18 August 2026**. Play Console labels can change, so follow any newer instructions shown inside your account.

## What you will upload

| Order | Upload | Current status |
|---|---|---|
| 1 | 512 x 512 app icon | Ready: `store-assets/play-store-icon-512.png` |
| 2 | 1024 x 500 feature graphic | Ready: `store-assets/feature-graphic-1024x500.png` |
| 3 | At least two real phone screenshots | You must capture these from the running app |
| 4 | Public privacy-policy URL | Prepared; publish the `docs/` folder with GitHub Pages |
| 5 | Signed release Android App Bundle (`.aab`) | Package is configured; you must create a private upload key |

The upload inventory and screenshot names are documented in [`store-assets/README.md`](store-assets/README.md).

> Important: do not upload `app-debug.apk` to production. Google Play expects a signed release Android App Bundle (`.aab`). Never commit a `.jks` keystore or its passwords to GitHub.

## 1. Information you need first

Write down the following before opening Play Console:

- [x] Developer display name: `Khumalo Augustine Games`
- [x] Legal/account name: `Augustine Khumalo`
- [x] Public support email: `augustinekhumalo96@gmail.com`
- [x] Privacy-policy URL: `https://khumaloaugustine.github.io/merge-rush/privacy-policy.html`
- [x] Permanent package name: `com.khumaloaugustine.mergerush`
- [ ] Countries where the game will be available
- [ ] Whether the launch build contains ads

The current source does **not** contain an advertising SDK. Its rewarded-continue button is only a demonstration. For this exact build, answer **No** when Play Console asks whether the app contains ads. Change that declaration, the Data safety form, and the privacy policy before publishing any future build containing AdMob.

### Choose the package name carefully

The configured package is `com.khumaloaugustine.mergerush`. The Gradle application ID, namespace, Kotlin packages, and source directories have already been updated. Treat this as permanent after the first Play Console upload: future updates must use the same application ID and signing identity.

## 2. Prepare the privacy policy

The draft is in `docs/privacy-policy.html`.

Before publishing:

1. The policy already identifies Khumalo Augustine Games and `augustinekhumalo96@gmail.com`.
2. Commit and push the `docs/` folder to the `merge-rush` GitHub repository.
3. In GitHub, open **Settings > Pages**.
4. Under **Build and deployment**, choose **Deploy from a branch**.
5. Select the main branch and `/docs` folder, then save.
6. Open `https://khumaloaugustine.github.io/merge-rush/privacy-policy.html` in a private browser window.

The policy must be publicly accessible, not a PDF, not behind a login, and must identify the app/developer and explain collection, sharing, security, retention, deletion, and contact details. Google also requires a privacy policy for apps that collect no user data. See the [Google Play privacy policy requirements](https://support.google.com/googleplay/android-developer/answer/17190352?hl=en&rd=2).

The app now includes a **Privacy Policy** item in Settings that opens the public URL.

## 3. Create store graphics

Prepare these files in a new `store-assets/` folder:

- [ ] App icon: 512 × 512 px, 32-bit PNG, maximum 1 MB
- [ ] Feature graphic: 1024 × 500 px, JPEG or 24-bit PNG without transparency
- [ ] At least two phone screenshots, JPEG or PNG without transparency
- [ ] Recommended: 4–8 portrait screenshots at 1080 × 1920 px

Suggested screenshots:

1. Home screen in dark mode — caption: `A fresh number puzzle for every mood`
2. Active combo — caption: `Build chains for bigger scores`
3. Challenge selection — caption: `40 handcrafted challenges`
4. Objective gameplay — caption: `Balance tiles, moves, score and combos`
5. Victory screen — caption: `Earn stars and unlock the next test`
6. Light mode — caption: `Comfortable in light or dark mode`

Screenshots must show the real app and current features. Do not claim awards, rankings, downloads, or features the build does not contain. Check the official [Google Play preview asset requirements](https://support.google.com/googleplay/android-developer/answer/9866151?hl=en).

## 4. Ready-to-copy store listing

### App name

```text
Merge Rush
```

### Category

```text
Game > Puzzle
```

### Short description

```text
Merge tiles, master combos and conquer 40 increasingly strategic challenges.
```

### Full description

```text
Think ahead, merge matching numbers and turn every move into progress.

Merge Rush is an offline number puzzle built for quick sessions and thoughtful strategy. Swipe the board to combine matching tiles, build scoring combos and work toward increasingly demanding objectives.

TAKE ON 40 CHALLENGES
Progress through four chapters that introduce new goals at a comfortable pace before combining them into expert tests. Reach target tiles, earn required scores, build combo chains and finish before your moves run out.

FIND YOUR STRATEGY
Use up to three undos, request an optional move hint or deploy a tactical board shuffle. A live danger meter helps you recognise when the board is filling up.

CHASE A HIGHER SCORE
Consecutive successful merges build a combo multiplier. Plan efficient chains, earn coins and try to improve your personal best.

PLAY YOUR WAY
Choose System, Light or Dark appearance modes. Clear objectives, bold tile numbers, sound controls and haptic feedback make the game comfortable and easy to understand.

PLAY OFFLINE
Merge Rush requires no account and no game server. Progress, settings and statistics are stored locally on your device.

FEATURES
- 40 progressively harder challenge levels
- Endless mode for relaxed high-score play
- Tile, score, combo and move-limit objectives
- Stars, coins, statistics and saved progress
- Optional hints, undos and board shuffle
- Modern sound effects with mute control
- Light and dark appearance modes
- No account required
- Offline gameplay

How high can your strategy take you?
```

Do not mention AdMob, rewarded ads, or purchases in the listing until those features genuinely exist in the uploaded build.

## 5. Test the release candidate

Before signing:

- [ ] Install and finish onboarding on a clean device/emulator.
- [ ] Complete levels 1–3 and confirm unlocking is saved.
- [ ] Test Endless mode, undo, hint and shuffle.
- [ ] Test sound on/off and device volume controls.
- [ ] Test System, Light and Dark modes.
- [ ] Rotate/restart the app and confirm progress remains.
- [ ] Test without internet.
- [ ] Check all text on a small phone and a large phone.
- [ ] Confirm no button is clipped and the back button behaves correctly.
- [ ] Run `gradlew.bat testDebugUnitTest assembleDebug`.

## 6. Create and protect the upload key

Use Android Studio's safe signing wizard:

1. Select **Build > Generate Signed Bundle / APK**.
2. Choose **Android App Bundle**, then **Next**.
3. Select **Create new** under Key store path.
4. Store the `.jks` file somewhere private and backed up—not inside this repository.
5. Create strong, unique keystore and key passwords.
6. Use an alias such as `merge-rush-upload`.
7. Set a long validity period.
8. Save the passwords in a password manager.
9. Choose the `release` build variant and create the bundle.

The output is normally under `app/build/outputs/bundle/release/app-release.aab`.

Google Play App Signing is mandatory for new apps. Your upload key authorizes uploads; Google protects the app-signing key used for installs. Read [Sign your app](https://developer.android.com/studio/publish/app-signing) before creating the key.

## 7. Create the Play Console app

1. Sign in to [Google Play Console](https://play.google.com/console/).
2. Select **Home > Create app**.
3. Default language: choose the language used in your listing.
4. App name: **Merge Rush**.
5. Select **Game**.
6. Select **Free** for this release.
7. Enter your public support email.
8. Accept the required declarations and Play App Signing terms.
9. Select **Create app**.

Google's current creation steps are documented in [Create and set up your app](https://support.google.com/googleplay/android-developer/answer/9859152?hl=en).

> A free app cannot later be changed into a paid app. You can still add compliant in-app products later.

## 8. Complete the Main store listing

In **Grow users > Store presence > Main store listing**:

1. Paste the app name, short description and full description above.
2. Upload the 512 × 512 icon.
3. Upload the 1024 × 500 feature graphic.
4. Upload the phone screenshots in a logical order.
5. Add support email and privacy-policy URL.
6. Save the page.

Proofread the listing on a phone-sized preview. Every statement must match the uploaded build.

## 9. Complete Policy > App content

Answer based on the exact release build, not future plans.

### Suggested answers for the current build

- **App access:** All functionality is available without special access or login.
- **Ads:** No. The current app contains no ads SDK.
- **Content rating:** Complete the questionnaire truthfully. This puzzle currently contains no violence, sexual content, gambling, drugs, or user-generated content.
- **Target audience:** Choose the age groups you genuinely designed the game for. If you include children, additional Families requirements may apply; do not select or exclude ages merely to avoid policy obligations.
- **News app:** No.
- **Government app:** No.
- **Financial features:** No.
- **Health features:** No.

Content ratings come from your questionnaire responses. See [Content rating requirements](https://support.google.com/googleplay/android-developer/answer/9859655?hl=en).

### Data safety for the current build

The inspected source stores game progress and settings locally and has no Internet permission, analytics SDK, login, backend, or ads SDK. On that basis, the likely declaration for this exact build is:

- Data collected: **No**
- Data shared: **No**
- Account creation: **No**

You are responsible for checking the final bundle and every included SDK. All apps—including apps collecting no data—must complete Data safety and provide a privacy policy. See [Google Play Data safety guidance](https://support.google.com/googleplay/android-developer/answer/10787469?hl=en).

## 10. Upload to Internal testing first

1. Open **Test and release > Testing > Internal testing**.
2. Create a release.
3. Upload `app-release.aab`.
4. Add release notes, for example:

```text
Initial test release with Endless mode, 40 challenge levels, offline progress, accessibility themes, hints, combos and modern sound feedback.
```

5. Add your own email and a few trusted testers.
6. Review and roll out the internal release.
7. Open the tester opt-in link and install from Google Play.

Internal testing is normally the quickest way to validate the actual Play-signed delivery. See [Set up a test](https://support.google.com/googleplay/android-developer/answer/9845334?hl=en).

## 11. Run the required closed test

If your personal developer account was created after 13 November 2023, Google currently requires:

- At least 12 testers
- All 12 continuously opted in for at least 14 days
- A closed test—not only an internal test
- A production-access application after the requirement is met

Steps:

1. Open **Testing > Closed testing**.
2. Create a track, for example `closed-beta`.
3. Create an email list or Google Group with more than 12 people as a safety margin.
4. Upload the tested `.aab` and roll out the closed release.
5. Share the opt-in link.
6. Confirm at least 12 testers actually opt in and remain opted in for the full period.
7. Ask testers to play several levels and report device, Android version, problem, and reproduction steps.
8. Keep a small written testing summary for the production-access questions.

See the current [testing requirements for new personal accounts](https://support.google.com/googleplay/android-developer/answer/14151465?hl=en).

## 12. Apply for production access and publish

After the closed-test requirement is satisfied:

1. Open the Play Console Dashboard.
2. Select **Apply for production access**.
3. Describe who tested, how they used the game, what feedback you received, and what you fixed.
4. Wait for access approval.
5. Open **Test and release > Production**.
6. Create a production release and upload the final signed `.aab`.
7. Add release notes.
8. Resolve every error shown in the release checklist.
9. Choose countries/regions and confirm pricing is Free.
10. Review the release and send it for review.

The project already uses `targetSdk = 36`. From 31 August 2026, Google Play requires new phone/tablet apps and updates to target Android 16/API 36 or higher. Verify requirements again before every release: [Target API requirements](https://support.google.com/googleplay/android-developer/answer/11926878?hl=en-AU).

## 13. How to publish an update later

For every update:

1. Increase `versionCode` in `app/build.gradle.kts` (1, 2, 3…).
2. Change `versionName` when appropriate (`1.0.0`, `1.0.1`, `1.1.0`).
3. Test the upgrade over the installed Play version; do not only test a clean install.
4. Generate a signed `.aab` using the same upload key.
5. Upload it to Internal testing first.
6. Update Data safety, ads, privacy and content declarations if SDKs or behaviour changed.
7. Promote the tested release to Production.

Google Play rejects reused version codes. Keep the package name and signing identity unchanged.

## Final release checklist

- [x] Permanent package name selected and tested
- [x] Developer name and support email finalized
- [ ] Privacy policy updated, public, and linked inside the app
- [ ] No secrets or keystore committed to GitHub
- [ ] Release `.aab` signed with a backed-up upload key
- [ ] Icon, feature graphic and screenshots uploaded
- [ ] Store text matches the app exactly
- [ ] App access, ads, target audience and content rating completed
- [ ] Data safety completed for the final SDK list
- [ ] Internal testing passed
- [ ] Closed-testing requirement completed if applicable
- [ ] Production access approved if applicable
- [ ] Version code is unique
- [ ] Final release reviewed on multiple devices

## Current project status

Already ready:

- API 36 target
- Android App Bundle support
- Offline gameplay
- No sensitive Android permissions
- No login or backend
- No ads/analytics SDK in the current build
- Privacy-policy draft and GitHub Pages folder
- Automated tests and build workflow

Still requires your input or action:

- Publish the prepared privacy-policy URL with GitHub Pages
- Store graphics and screenshots
- Private upload key
- Play Console declarations and tester group

## Do not submit until these three blockers are resolved

1. **Published policy:** enable GitHub Pages and confirm the privacy URL works publicly.
2. **Real screenshots:** capture at least two screenshots from the release candidate; 4-6 are recommended.
3. **Signing:** generate and privately back up the `.jks` upload key, then build the signed `.aab`.

I cannot safely invent keystore passwords or submit legal/policy declarations on behalf of the account owner. Keep the upload key and passwords private.
