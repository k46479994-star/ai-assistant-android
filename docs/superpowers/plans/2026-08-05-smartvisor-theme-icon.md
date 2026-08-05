# SmartVisor Theme and Icon Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Rename the app to 스마트비서, add a persistent full color-palette theme picker, improve bottom-navigation contrast, redesign the task empty state, and ship a verified APK with a new launcher icon.

**Architecture:** Store a selected ARGB theme color in SharedPreferences and expose it through a focused `AppThemeStore`. Replace fixed primary colors at view-construction time with a runtime `ThemePalette` derived from the saved color. Recreate the activity after selection so every screen is rebuilt consistently. Keep offline classification, Room repositories, and calendar intents unchanged.

**Tech Stack:** Kotlin, Android Views, Material Components, SharedPreferences, Robolectric, GitHub Actions.

## Global Constraints

- App display name: `스마트비서`.
- Theme picker supports recommended swatches plus direct hexadecimal ARGB/RGB entry from a broad color palette.
- Selected color persists across app restarts.
- Navigation labels must remain readable in selected and unselected states.
- Existing offline manifest restrictions remain unchanged.
- Existing IDs and callback behavior remain compatible with regression tests.

---

### Task 1: Runtime theme model and persistence

**Files:**
- Create: `app/src/main/java/com/example/aiassistant/ui/AppThemeStore.kt`
- Modify: `app/src/main/java/com/example/aiassistant/ui/PremiumUi.kt`
- Test: `app/src/test/java/com/example/aiassistant/ui/AppThemeStoreTest.kt`

**Interfaces:**
- Produces: `AppThemeStore(context).selectedColor(): Int`, `saveSelectedColor(color: Int)`, `ThemePalette(primary: Int)`.

- [ ] Write tests for default color, persistence, hex parsing, and readable contrast.
- [ ] Run `gradle testDebugUnitTest --tests '*AppThemeStoreTest*'` and verify RED.
- [ ] Implement storage and palette derivation.
- [ ] Re-run tests and verify GREEN.
- [ ] Commit.

### Task 2: Theme picker and global application

**Files:**
- Modify: `app/src/main/java/com/example/aiassistant/ui/SettingsViewFactory.kt`
- Modify: `app/src/main/java/com/example/aiassistant/ui/MainActivity.kt`
- Modify: `app/src/main/res/values/ids.xml`
- Test: `app/src/test/java/com/example/aiassistant/ui/ThemePickerUiTest.kt`

**Interfaces:**
- Settings callback adds `onThemeSelected: (Int) -> Unit`.
- MainActivity persists the color and calls `recreate()`.

- [ ] Write failing tests for swatches, hex input, apply callback, and navigation contrast.
- [ ] Verify RED.
- [ ] Implement recommended swatches, hex input, preview, and apply action.
- [ ] Build all factories using the current runtime palette.
- [ ] Verify GREEN and commit.

### Task 3: Task screen redesign and app identity

**Files:**
- Modify: `app/src/main/java/com/example/aiassistant/ui/TaskViewFactory.kt`
- Modify: `app/src/main/res/values/strings.xml`
- Modify: `app/src/main/AndroidManifest.xml` only if launcher references require it.
- Add: launcher icon PNG/XML resources.
- Test: `app/src/test/java/com/example/aiassistant/ui/TaskPremiumUiTest.kt`

**Interfaces:**
- Existing task toggle and add callbacks remain unchanged.

- [ ] Write failing task empty-state and identity tests.
- [ ] Verify RED.
- [ ] Add Material cards, visible filters, empty-state CTA, `스마트비서` name, and launcher icon.
- [ ] Verify GREEN and commit.

### Task 4: Full verification and APK handoff

**Files:**
- No production files unless a regression fix is required.

- [ ] Run `gradle testDebugUnitTest --stacktrace`.
- [ ] Run offline manifest verification.
- [ ] Run `gradle assembleDebug`.
- [ ] Confirm GitHub Actions uploads `ai-assistant-debug-apk`.
- [ ] Download artifact, extract APK, calculate SHA-256, and provide the APK file.