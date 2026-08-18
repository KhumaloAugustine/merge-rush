# Google Play upload assets

This folder contains the graphics prepared for the Merge Rush main store listing.

## Ready to upload

| Play Console field | File | Validation |
|---|---|---|
| App icon | `play-store-icon-512.png` | 512 x 512, 32-bit PNG, under 1 MB |
| Feature graphic | `feature-graphic-1024x500.png` | 1024 x 500, 24-bit PNG, no alpha |

Upload these under **Grow users > Store presence > Main store listing > Graphics**.

## Still needs to be captured from the real app

Google Play requires at least two screenshots. Capture 4-6 portrait screenshots from the running release candidate and place them here with these names:

1. `phone-01-home.png`
2. `phone-02-gameplay-combo.png`
3. `phone-03-challenges.png`
4. `phone-04-objectives.png`
5. `phone-05-victory.png`
6. `phone-06-light-mode.png`

Recommended capture size: 1080 x 1920 portrait. Use PNG or JPEG without transparency. Do not generate fake screenshots; every screenshot must accurately show the released app.

In Android Studio, run the app on a Pixel emulator, open **View > Tool Windows > Device Manager**, select the screenshot control, and save each image here.

## Not a store graphic

The signed Android App Bundle is generated separately and is normally located at:

`app/build/outputs/bundle/release/app-release.aab`

Do not upload `app-debug.apk` as the production release.

## Image-generation record

The icon and feature graphic were created using the built-in image-generation workflow. Their final prompts requested a premium modern mobile-puzzle style, deep indigo background, violet/mint/gold tiles, no ranking claims, no Play branding, and no copied 2048 artwork. The feature graphic additionally requested the exact title `MERGE RUSH`.
