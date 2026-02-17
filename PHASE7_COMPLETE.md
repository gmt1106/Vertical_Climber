# Phase 7: Google Ads Integration - Complete

## Original Plan Items

### 1. Dependencies & Setup ✅
- Added Google Mobile Ads SDK dependency to `app/build.gradle.kts`: `com.google.android.gms:play-services-ads:23.6.0`
- Added `APPLICATION_ID` meta-data to `AndroidManifest.xml` with AdMob App ID
- Mobile Ads SDK initialized in `MainActivity.onCreate()` via `MobileAds.initialize()`

### 2. Banner Ad Layout ✅
- Changed `MainActivity` from programmatic `setContentView(gameView)` to XML layout inflation
- Created `res/layout/activity_main.xml`: vertical `LinearLayout` with `GameView` (weight=1, fills remaining space) and `AdView` (banner, anchored at bottom)
- Banner size: `AdSize.BANNER` (320x50dp)
- `GameView` resizes to fill remaining space above the ad

### 3. Ad Loading & Lifecycle ✅
- Ad loaded in `MainActivity.onCreate()` after layout inflation: `adView.loadAd(AdRequest.Builder().build())`
- Lifecycle handled: `adView.pause()` in `onPause()`, `adView.resume()` in `onResume()`, `adView.destroy()` in `onDestroy()`

### 4. Screen Adjustment ✅
- `AdView` set to fixed `50dp` height to reserve space immediately (avoids timing issue where `wrap_content` starts at 0 height and causes game content to be clipped when ad loads later)
- `GameView` receives correct reduced dimensions via `surfaceCreated()` — game automatically adapts to smaller screen height
- No changes needed in `GameEngine`, `AsciiRenderer`, or game logic

## Files Modified/Created

| File                           | Change                                                                                        |
|--------------------------------|-----------------------------------------------------------------------------------------------|
| `app/build.gradle.kts`         | Added `com.google.android.gms:play-services-ads:23.6.0` dependency                            |
| `AndroidManifest.xml`          | Added `<meta-data>` for AdMob Application ID                                                  |
| `res/layout/activity_main.xml` | **New** - Layout with `GameView` + `AdView` banner                                            |
| `MainActivity.kt`              | Switched to XML layout inflation, added `MobileAds.initialize()`, `adView` lifecycle handling |

## Notes
- AdMob App ID is set in `AndroidManifest.xml` (identifies the app)
- Ad Unit ID is set in `activity_main.xml` on the `AdView` (identifies the specific ad placement)
- New ad units can take several hours to start serving; use Google's test ad unit ID (`ca-app-pub-3940256099942544/6300978111`) to verify integration
