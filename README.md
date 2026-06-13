# Converter — fast, offline, private unit converter

A premium, ultra-clean Android unit converter built with **Jetpack Compose**,
**Kotlin**, and **Material 3 Expressive + Material You**.

- **Package:** `com.blood.unitconverter`
- **14 categories, 120 units** — length, weight, temperature, data, area,
  volume, speed, time, pressure, energy, power, angle, frequency, fuel economy.
- **100% offline.** The app declares **no `INTERNET` permission**, so it
  physically cannot phone home — zero telemetry, zero trackers, zero accounts.
- **`BigDecimal` precision** (34 significant digits) so chained conversions
  never drift. Every factor is verified against authoritative definitions.

## Features

- **All-units live list** (Google-style): type once, see every unit convert
  instantly. The chosen target is **pinned to the top, big & highlighted**.
  - **Tap** a row → make it the target.
  - **Long-press** a row → use its value as the new input (reverse lookup).
  - **★** → pin a unit (pinned units group under a "Starred" header).
  - **Copy** icon → copy that value with its symbol.
- **Smart formatting**: comma thousands separators (`1,234,567.89`), proper
  minus sign (−), and **auto-shrink-to-fit** so long numbers are never
  truncated. Shifting numbers use a monospace family so the layout never jumps.
- **Settings**: decimal places (0 / 2 / 4 / 6 / 8 / Auto) and a
  **Standard / Scientific** number-format toggle (`1.5×10¹⁴`).
- **Conversion history** — recent conversions, tap to restore, one-tap clear.
- **Searchable unit picker** bottom sheet; the opposite unit is tagged so you
  don't pick the same unit twice. Results scroll to the top after any change.
- **Share** a full snapshot of all conversions (with a preview before sending).
- **Negatives supported** (e.g. −40 °C), inline tappable unit chip, haptics,
  ≥48 dp touch targets, keyboard auto-opens on launch.
- **On-device storage only** (DataStore): history, last-used units, favorites,
  and settings never leave the device — fully consistent with no `INTERNET`.

## Design & motion (Material 3 Expressive)

- **Hierarchy** via explicit surface-container levels (`Color.kt`,
  `ConverterScreen.kt`) — clean separation without heavy borders.
- **Expressive shapes** — generous 28–32 dp extra-large cards and full pill
  buttons (`Shape.kt`).
- **Typography** — the Fredoka type scale (`Type.kt`, bundled offline), with a
  monospace family for shifting result numbers to keep the layout steady.
- **Material You** dynamic color on Android 12+ with a curated fallback palette
  (`Theme.kt`).
- **Spring-physics motion** (`ui/theme/Motion.kt`) — no fixed easing. The swap
  control is a real shape-morphing button (circle ⇄ 8-point star on press) via
  `androidx.graphics:graphics-shapes` (`ui/morph/`), and every button has a
  springy press "squeeze".

## Project layout

```
app/src/main/java/com/blood/unitconverter/
  MainActivity.kt            entry point (edge-to-edge Compose)
  ConverterViewModel.kt      all UI state; pure conversion + on-device prefs
  data/                      unit catalog, history model, DataStore repository
  logic/Converter.kt         pure parse / convert / format (unit-testable)
  ui/                        screen, sheets, pickers, auto-size text
  ui/morph/                  shape-morphing button + press-squeeze modifier
  ui/theme/                  color, type (Fredoka), shape, motion, theme
```

## Download (Releases)

Grab a ready-to-install APK from the repo's **[Releases](../../releases)** tab.

To cut a new release, push a version tag — CI builds the APK and publishes a
GitHub Release with it attached automatically:

```bash
git tag v1.5.1
git push origin v1.5.1
```

(You can also trigger the **Release APK** workflow manually from the Actions tab.)

> The APK is signed with the standard debug key so it installs immediately
> (you may need to allow "install from unknown sources"). It is **not** signed
> with a Play Store upload key.

## Build it on GitHub (no local Android SDK needed)

1. Create a new GitHub repository.
2. Push this folder to it:
   ```bash
   git init -b main
   git add .
   git commit -m "Initial commit: Converter"
   git remote add origin https://github.com/<you>/<repo>.git
   git push -u origin main
   ```
3. Open the repo's **Actions** tab — the **Build APK** workflow runs
   automatically (or click **Run workflow**).
4. When it finishes, open the run and download the **`app-debug`** or
   **`app-release`** artifact — the zip contains your installable `.apk`.

> The release build is signed with the standard debug key so the artifact
> installs immediately. For a Play Store upload, swap in your own keystore in
> `app/build.gradle.kts`.

## Build locally (optional)

```bash
./gradlew assembleDebug
# APK at app/build/outputs/apk/debug/app-debug.apk
```

Requires JDK 17 and the Android SDK (API 35).

## License

Released under the MIT License — see [LICENSE](LICENSE). Bundled fonts and other
assets are listed in [THIRD_PARTY_LICENSES.md](THIRD_PARTY_LICENSES.md).
