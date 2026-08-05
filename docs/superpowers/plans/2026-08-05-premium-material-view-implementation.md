# Premium Material View Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Replace the current functional but plain View-based UI with a cohesive premium purple Material-style interface while preserving every offline classifier, Room, calendar handoff, privacy, and validation behavior.

**Architecture:** Keep the current programmatic Android View architecture. Introduce a small UI design-system helper layer, then migrate each screen factory and the activity chrome to shared tokens and builders. No Compose migration, no new network capability, and no changes to classification or persistence contracts.

**Tech Stack:** Kotlin, Android Views, AppCompat, Material Components, Room, Robolectric, JUnit 4, GitHub Actions.

## Global Constraints

- Keep `minSdk = 26`, `targetSdk = 35`, JDK 17, and the current application ID.
- Do not add `INTERNET`, `READ_CALENDAR`, or `WRITE_CALENDAR` permissions.
- Keep `android:allowBackup="false"`.
- Preserve current Room schema and all classifier/parser public behavior.
- Do not display microphone, weather, AI status, attachment, or cloud controls unless they are functional.
- Do not claim calendar counts because the app does not read calendar data.
- Every screen must remain usable with large font settings and narrow portrait screens through vertical scrolling.
- Existing test IDs used by `OfflineFlowTest`, `OfflineScreensTest`, and View factory tests must remain stable.

---

### Task 1: Add the shared premium design system

**Files:**
- Create: `app/src/main/java/com/example/aiassistant/ui/PremiumUi.kt`
- Create: `app/src/test/java/com/example/aiassistant/ui/PremiumUiTest.kt`

**Interfaces:**
- Produces: `object PremiumColors`, `object PremiumDimens`, `fun Context.dp(Int): Int`, `fun premiumCard(Context): MaterialCardView`, `fun premiumPrimaryButton(Context, String): MaterialButton`, `fun premiumSectionTitle(Context, String): TextView`, `fun premiumBodyText(Context, String): TextView`.

- [ ] Write `PremiumUiTest` asserting the primary color is `#7C5CFF`, background is `#F8F8FD`, cards have at least 24dp corner radius, and primary buttons are not all-caps.
- [ ] Run `gradle testDebugUnitTest --tests com.example.aiassistant.ui.PremiumUiTest` and verify RED because the helpers do not exist.
- [ ] Implement the exact tokens and builders in `PremiumUi.kt` using `MaterialCardView`, `MaterialButton`, `GradientDrawable`, and density-safe dp conversion.
- [ ] Re-run the focused test and `gradle assembleDebug`; both must pass.
- [ ] Commit with `feat: add premium view design system`.

### Task 2: Redesign the activity shell and bottom navigation

**Files:**
- Modify: `app/src/main/java/com/example/aiassistant/ui/MainActivity.kt`
- Create: `app/src/test/java/com/example/aiassistant/ui/PremiumNavigationTest.kt`

**Interfaces:**
- Consumes: `PremiumColors`, `PremiumDimens`, shared text/button helpers.
- Preserves: `navigate(AppScreen)`, existing navigation view IDs, `content_host`.

- [ ] Write a Robolectric test that creates `MainActivity`, checks the root background equals `PremiumColors.Background`, confirms all five navigation controls exist, and verifies selecting `nav_tasks` visibly marks only that destination selected.
- [ ] Run the focused test and verify RED against the current plain button navigation.
- [ ] Replace the top bar with a compact title and settings action using premium spacing; replace plain navigation buttons with rounded equal-width destinations using unicode-safe text labels and selected/unselected states.
- [ ] Keep screen switching and all current IDs unchanged.
- [ ] Run `PremiumNavigationTest`, `OfflineFlowTest`, and `assembleDebug`.
- [ ] Commit with `feat: redesign premium app shell`.

### Task 3: Redesign the home screen with truthful offline summaries

**Files:**
- Modify: `app/src/main/java/com/example/aiassistant/ui/HomeViewFactory.kt`
- Modify: `app/src/main/java/com/example/aiassistant/ui/MainActivity.kt`
- Modify: `app/src/test/java/com/example/aiassistant/ui/OfflineScreensTest.kt`
- Create: `app/src/test/java/com/example/aiassistant/ui/PremiumHomeViewTest.kt`

**Interfaces:**
- Preserve `HomeViewFactory.create(openTaskCount, latestNotes, onQuickInput, onSettings)`.
- Home may show open task count and latest note count only; it must not invent calendar data.

- [ ] Write tests for greeting copy, large `빠른 입력` action, truthful `오늘 마감 할 일 N개`, recent-note cards, empty states, and absence of `온라인`, `날씨`, and fake schedule counts.
- [ ] Run the focused tests and verify RED.
- [ ] Rebuild the home as a `ScrollView` containing a greeting header, hero quick-input card, three shortcut cards for 일정/할 일/메모, task summary, and recent notes.
- [ ] Wire shortcuts to existing destinations without changing repository calls.
- [ ] Run home tests, all `OfflineScreensTest`, and `assembleDebug`.
- [ ] Commit with `feat: add premium offline home screen`.

### Task 4: Redesign quick input and preview

**Files:**
- Modify: `app/src/main/java/com/example/aiassistant/ui/QuickInputViewFactory.kt`
- Modify: `app/src/main/java/com/example/aiassistant/ui/PreviewViewFactory.kt`
- Modify: `app/src/main/java/com/example/aiassistant/ui/PreviewView.kt`
- Modify: `app/src/test/java/com/example/aiassistant/ui/QuickInputViewFactoryTest.kt`
- Modify: `app/src/test/java/com/example/aiassistant/ui/PreviewViewFactoryTest.kt`

**Interfaces:**
- Preserve submit/save callbacks, form validation, `RememberSelection`, all existing view IDs, 500-character limit, and exact validation messages.

- [ ] Extend tests to require scrollable premium containers, visible example chips, rounded primary CTA, editable preview cards, and unchanged validation behavior.
- [ ] Run both factory test classes and verify RED on style assertions.
- [ ] Migrate quick input to a hero card with multiline input, examples, inline error, and one primary classify action.
- [ ] Migrate preview to grouped cards for type, title, date/time, reminder, learned-expression controls, error, cancel, and save.
- [ ] Preserve duplicate-save prevention and disabled-save validation logic.
- [ ] Run both factory tests, `OfflineFlowTest`, and `assembleDebug`.
- [ ] Commit with `feat: redesign quick input and confirmation preview`.

### Task 5: Redesign calendar, task, and note screens

**Files:**
- Modify: `app/src/main/java/com/example/aiassistant/ui/CalendarViewFactory.kt`
- Modify: `app/src/main/java/com/example/aiassistant/ui/TaskViewFactory.kt`
- Modify: `app/src/main/java/com/example/aiassistant/ui/NoteViewFactory.kt`
- Modify: `app/src/test/java/com/example/aiassistant/ui/OfflineScreensTest.kt`
- Create: `app/src/test/java/com/example/aiassistant/ui/PremiumListScreensTest.kt`

**Interfaces:**
- Preserve all callbacks and list sorting/filtering behavior.
- Calendar remains an external-app handoff explanation and `새 일정` quick-input prefill.

- [ ] Write tests for premium empty states, section cards, task completion callback, task filters, note excerpt truncation, and calendar handoff copy.
- [ ] Run focused tests and verify RED.
- [ ] Rebuild all three screens with `ScrollView`, shared section headers, white cards, subdued metadata, and prominent add actions.
- [ ] Ensure completed tasks remain distinguishable without relying on color alone.
- [ ] Run premium list tests, `OfflineScreensTest`, and `assembleDebug`.
- [ ] Commit with `feat: redesign offline organizer screens`.

### Task 6: Redesign settings without exposing unfinished integrations

**Files:**
- Modify: `app/src/main/java/com/example/aiassistant/ui/SettingsViewFactory.kt`
- Modify: `app/src/test/java/com/example/aiassistant/ui/OfflineScreensTest.kt`
- Create: `app/src/test/java/com/example/aiassistant/ui/PremiumSettingsViewTest.kt`

**Interfaces:**
- Preserve duration/reminder save callback and learned-rule deletion callback.
- Keep `저장 전 확인: 항상 켜짐` and offline-mode messaging.

- [ ] Write tests requiring privacy/offline cards, numeric defaults, learned-rule cards, delete controls, and absence of fake OpenAI/Google connection switches.
- [ ] Run tests and verify RED.
- [ ] Rebuild settings using premium grouped cards and clear supporting copy.
- [ ] Keep invalid learned-rule types deletable and visibly labeled.
- [ ] Run settings tests, `OfflineScreensTest`, and `assembleDebug`.
- [ ] Commit with `feat: redesign offline settings screen`.

### Task 7: Add accessibility and responsive regressions

**Files:**
- Create: `app/src/test/java/com/example/aiassistant/ui/PremiumAccessibilityTest.kt`
- Modify any affected UI factories from Tasks 2-6.

**Interfaces:**
- Verifies all primary actions have text labels, content descriptions where icon-only, minimum 48dp touch targets, and scrollability under large text.

- [ ] Write Robolectric tests that apply font scale 1.5, measure each screen at 360x640dp, and assert the primary action remains reachable through a `ScrollView`.
- [ ] Verify tests fail for any fixed-height or unlabeled control.
- [ ] Fix only the affected layouts with weight removal, vertical scrolling, minimum heights, and content descriptions.
- [ ] Run `PremiumAccessibilityTest`, all UI tests, and `assembleDebug`.
- [ ] Commit with `test: harden premium UI accessibility`.

### Task 8: Final verification, PR, and APK

**Files:**
- Modify: `README.md`
- No permission or schema changes.

**Interfaces:**
- Produces: CI-gated `ai-assistant-debug-apk` artifact.

- [ ] Update README screenshots/copy description to say the app uses a premium offline-first Material View UI and list only implemented features.
- [ ] Run `gradle clean testDebugUnitTest assembleDebug`.
- [ ] Verify `AndroidManifest.xml` still has no forbidden permissions and still has `android:allowBackup="false"`.
- [ ] Open a PR from `design/premium-material-view` to `main` titled `feat: redesign offline assistant with premium Material UI`.
- [ ] Verify GitHub Actions unit tests, APK build, and artifact upload all succeed.
- [ ] Download the APK artifact and provide the uncompressed `.apk` to the user.

## Final Verification Checklist

- [ ] Existing offline parser/classifier behavior is unchanged.
- [ ] Existing Room tasks, notes, learned rules, and settings still work.
- [ ] External calendar insert still works without calendar permission.
- [ ] No unfinished AI, weather, microphone, file, or cloud controls are shown.
- [ ] No fabricated calendar count appears.
- [ ] All screens share premium colors, spacing, cards, and typography.
- [ ] All screens are scrollable and usable at 1.5 font scale.
- [ ] All unit and integration tests pass.
- [ ] Debug APK builds and is uploaded by GitHub Actions.
