# Converter — fast, offline, private unit converter

A premium, ultra-clean Android unit converter built with **Jetpack Compose**,
**Kotlin**, and **Material 3 Expressive + Material You**.

- **Package:** `com.blood.unitconverter`
- **14 categories**, hundreds of units (length, weight, temperature, data,
  area, volume, speed, time, pressure, energy, power, angle, frequency, fuel).
- **100% offline.** The app declares **no `INTERNET` permission**, so it
  physically cannot phone home — zero telemetry, zero trackers, zero accounts.
- **BigDecimal precision** (34 significant digits) so chained conversions never
  drift.

### Features
- **All-units live list** (Google-style): type once, see every unit in the
  category convert instantly. The chosen target unit is **pinned to the top and
  shown big & blue**; tap any row to make it the new target (it springs to the
  top), or tap its copy button to copy (with the unit symbol).
- **Fredoka** typeface throughout (bundled offline). Crisp, perfectly-centered
  Material vector icons. Shifting result numbers use a monospace family so the
  layout never jumps as you type.
- **Smart number formatting**: grouped both sides of the decimal
  (`1 234 567.891 234`), significant-figure aware for tiny values.
- **Adjustable precision**: Auto / 2 / 4 / 6 / 8 decimals (Settings).
- **Conversion history**: recent conversions, tap to restore, one-tap clear.
- **Searchable unit picker** bottom sheet.
- **Negatives supported** (e.g. -40 °C), inline active-unit label, haptics on
  swap/copy, ≥48dp touch targets.
- **On-device storage only** (DataStore). History, last-used units, and settings
  never leave the device — fully compatible with the no-`INTERNET` model.

## Design notes (Material 3 Expressive)

| Requirement | Where it lives |
|---|---|
| Surface container levels for clean hierarchy | `ConverterScreen.kt`, `Color.kt` |
| Expressive Extra-Large (28–32dp) shapes + pill buttons | `Shape.kt`, hero/input cards, action pills |
| Bold display type with **tabular numbers** | `Type.kt` → `DisplayNumberStyle`, `InputNumberStyle` |
| Placeholder & input never overlap / wrap | `ValueField` in `ConverterScreen.kt` (single Box, single-line, fixed bounds) |
| Material You dynamic color | `Theme.kt` |

## Expressive motion & shape morphing

Powered by `androidx.graphics:graphics-shapes`. Everything moves on **spring
physics** (mass / stiffness / damping) — no fixed easing curves — giving the
"overshoot & squeeze" cadence.

| Effect | Where |
|---|---|
| Spring motion scheme (spatial / fast / expressive / effects) | `ui/theme/Motion.kt` |
| Squircle / cookie / star / flower polygons + `MorphPolygonShape` (a real Compose `Shape`, so components actually clip & morph) | `ui/morph/MorphShapes.kt` |
| Reusable morphing icon button, press-squeeze modifier, morph badge | `ui/morph/MorphComponents.kt` |
| Category chips morph **squircle ⇄ 8-point star** + pop on select | `ExpressiveCategoryChip` |
| Hero result morphs its shell + figure springs in (scale overshoot + slide) | `ResultCard` |
| Swap button morphs **circle ⇄ star** on press + bouncy 180° spin | `MorphIconButton` / `InputCard` |
| Unit picker rows morph + check mark pops with a bouncy spring | `UnitPickerSheet.kt` |
| Header "live" badge breathes **circle ⇄ flower** while a result is shown | `Header` / `MorphBadge` |
| Springy press squeeze on every button / picker | `pressSqueeze` |
| Error state: field color cross-fade + spring nudge | `ValueField` |

## Build it on GitHub (no local Android SDK needed)

1. Create a new GitHub repository.
2. Push this whole folder to it:
   ```bash
   git init
   git add .
   git commit -m "Initial commit: Converter"
   git branch -M main
   git remote add origin https://github.com/<you>/<repo>.git
   git push -u origin main
   ```
3. Go to the repo's **Actions** tab. The **Build APK** workflow runs
   automatically (or click **Run workflow**).
4. When it finishes, open the run and download the **`app-debug`** or
   **`app-release`** artifact — that zip contains your installable `.apk`.

> The release build is signed with the standard debug key so the artifact
> installs immediately. For a Play Store upload, swap in your own keystore in
> `app/build.gradle.kts`.

## Build locally (optional)

```bash
./gradlew assembleDebug
# APK at app/build/outputs/apk/debug/app-debug.apk
```

Requires JDK 17 and the Android SDK (API 35).
