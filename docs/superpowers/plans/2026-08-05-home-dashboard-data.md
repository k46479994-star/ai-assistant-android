# Home Dashboard Data Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Show real open tasks and recent notes on the home dashboard with direct navigation to their full screens.

**Architecture:** Extend `HomeViewFactory` to receive lists of `TaskEntity` and `NoteEntity`, render compact cards, and expose callbacks for opening Tasks, Notes, and Calendar. `MainActivity.loadHome` loads the data from existing repositories and passes navigation callbacks. Calendar remains an external app flow, so the home card opens the app's calendar screen instead of claiming locally stored events.

**Tech Stack:** Kotlin, Android Views, Material Components, Room repositories, Robolectric tests, GitHub Actions.

## Global Constraints

- Keep the offline-first build with no INTERNET permission.
- Preserve existing quick-input behavior and theme customization.
- Show at most 3 open tasks and 2 recent notes.
- Keep all existing unit tests passing.

---

### Task 1: Home dashboard rendering

**Files:**
- Modify: `app/src/main/java/com/example/aiassistant/ui/HomeViewFactory.kt`
- Test: `app/src/test/java/com/example/aiassistant/ui/HomeDashboardDataTest.kt`

**Interfaces:**
- Consumes: `List<TaskEntity>`, `List<NoteEntity>`, navigation callbacks.
- Produces: compact task, note, and calendar cards with click behavior.

- [ ] Write Robolectric tests for task titles, note titles, item limits, and callbacks.
- [ ] Run `gradle testDebugUnitTest` and confirm the new tests fail.
- [ ] Implement the minimal card rendering and callback wiring.
- [ ] Run `gradle testDebugUnitTest` and confirm all tests pass.

### Task 2: Load real home data

**Files:**
- Modify: `app/src/main/java/com/example/aiassistant/ui/MainActivity.kt`

**Interfaces:**
- Consumes: `TaskRepository.listAll()` and `NoteRepository.listLatest(3)`.
- Produces: `HomeViewFactory.create(...)` arguments and screen navigation callbacks.

- [ ] Load open tasks, sorted by due date then creation time.
- [ ] Pass callbacks for Calendar, Tasks, and Notes screens.
- [ ] Run the full unit test suite.
- [ ] Build the debug APK through GitHub Actions and upload the artifact.
