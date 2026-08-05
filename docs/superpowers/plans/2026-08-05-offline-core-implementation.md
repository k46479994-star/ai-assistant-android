# AI 비서 오프라인 코어 Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** OpenAI·인터넷·API 키 없이 한국어 입력을 일정·할 일·메모로 분류하고, 저장 전 확인 후 캘린더 또는 로컬 데이터베이스에 저장하는 오프라인 코어를 만든다.

**Architecture:** 순수 Kotlin 분류·날짜 파싱 계층을 Android UI와 분리하고, Room 저장소와 Calendar Insert 게이트웨이를 인터페이스 뒤에 둔다. `MainActivity`는 화면 전환만 담당하며 화면 생성, 미리보기 검증, 저장 로직은 작은 클래스로 분리한다.

**Tech Stack:** Kotlin 1.9.24, Android Views, AppCompat, Material Components, Room 2.6.1, Kotlin coroutines, SharedPreferences, Robolectric, JUnit 4, Gradle 8.7, GitHub Actions

## Global Constraints

- 최소 Android 8.0(API 26)
- Jetpack Compose로 전환하지 않음
- OpenAI 실제 호출을 포함하지 않음
- `INTERNET`, `READ_CALENDAR`, `WRITE_CALENDAR` 권한을 선언하지 않음
- 모든 생성·수정·삭제는 사용자 확인 후 실행
- 입력은 1자 이상 500자 이하
- 일정은 외부 캘린더 확인 화면으로 전달하며 앱이 실제 저장 여부를 추정하지 않음
- 할 일·메모·학습 규칙은 앱 전용 Room 데이터베이스에 저장
- 분류·파싱 클래스는 Android UI에 의존하지 않는 순수 Kotlin 코드로 유지
- GitHub Actions에서 `testDebugUnitTest` 성공 후에만 `assembleDebug`와 APK 업로드를 실행

---

## File Structure

```text
build.gradle.kts
app/build.gradle.kts
.github/workflows/build-apk.yml
app/src/main/AndroidManifest.xml
app/src/main/java/com/example/aiassistant/AiAssistantApplication.kt
app/src/main/java/com/example/aiassistant/AppContainer.kt
app/src/main/java/com/example/aiassistant/classification/InputType.kt
app/src/main/java/com/example/aiassistant/classification/ClassificationResult.kt
app/src/main/java/com/example/aiassistant/classification/ParsedTemporal.kt
app/src/main/java/com/example/aiassistant/classification/LearnedRule.kt
app/src/main/java/com/example/aiassistant/classification/KoreanDateTimeParser.kt
app/src/main/java/com/example/aiassistant/classification/TitleExtractor.kt
app/src/main/java/com/example/aiassistant/classification/KeywordCandidateExtractor.kt
app/src/main/java/com/example/aiassistant/classification/LocalInputClassifier.kt
app/src/main/java/com/example/aiassistant/classification/RuleBasedClassifier.kt
app/src/main/java/com/example/aiassistant/classification/OfflineInputProcessor.kt
app/src/main/java/com/example/aiassistant/classification/ItemDraft.kt
app/src/main/java/com/example/aiassistant/data/AppDatabase.kt
app/src/main/java/com/example/aiassistant/data/TaskEntity.kt
app/src/main/java/com/example/aiassistant/data/TaskDao.kt
app/src/main/java/com/example/aiassistant/data/TaskRepository.kt
app/src/main/java/com/example/aiassistant/data/NoteEntity.kt
app/src/main/java/com/example/aiassistant/data/NoteDao.kt
app/src/main/java/com/example/aiassistant/data/NoteRepository.kt
app/src/main/java/com/example/aiassistant/data/LearnedRuleEntity.kt
app/src/main/java/com/example/aiassistant/data/LearnedRuleDao.kt
app/src/main/java/com/example/aiassistant/data/LearnedRuleStore.kt
app/src/main/java/com/example/aiassistant/data/SettingsStore.kt
app/src/main/java/com/example/aiassistant/calendar/CalendarGateway.kt
app/src/main/java/com/example/aiassistant/ui/AppScreen.kt
app/src/main/java/com/example/aiassistant/ui/MainActivity.kt
app/src/main/java/com/example/aiassistant/ui/HomeViewFactory.kt
app/src/main/java/com/example/aiassistant/ui/QuickInputViewFactory.kt
app/src/main/java/com/example/aiassistant/ui/PreviewFormState.kt
app/src/main/java/com/example/aiassistant/ui/PreviewViewFactory.kt
app/src/main/java/com/example/aiassistant/ui/TaskViewFactory.kt
app/src/main/java/com/example/aiassistant/ui/NoteViewFactory.kt
app/src/main/java/com/example/aiassistant/ui/SettingsViewFactory.kt
app/src/main/res/values/ids.xml
app/src/main/res/values/strings.xml
app/src/test/java/com/example/aiassistant/...
```

## Branch Sequence

1. `feature/v0-2-ui`에서 탭 높이 회귀 테스트와 수정 사항을 추가한다.
2. PR #1 CI가 성공하면 squash merge한다.
3. 승인된 설계·계획 PR #2를 최신 `main`에 반영하고 merge한다.
4. 최신 `main`에서 `feature/offline-core` 브랜치를 만든다.
5. Task 2부터는 `feature/offline-core`에서 수행한다.

---

### Task 1: Fix the v0.2 Full-Height Tab Regression

**Files:**
- Modify: `app/build.gradle.kts`
- Modify: `app/src/main/java/com/example/aiassistant/MainActivity.kt`
- Create: `app/src/test/java/com/example/aiassistant/MainActivityLayoutTest.kt`
- Modify: `.github/workflows/build-apk.yml`

**Interfaces:**
- Consumes: existing `MainActivity`, `showTab(Tab)` behavior on `feature/v0-2-ui`
- Produces: every tab root is attached with `MATCH_PARENT` width and height; PR #1 is safe to merge

- [ ] **Step 1: Add the unit-test runtime needed for the regression test**

Add these entries to `dependencies` and enable Android resources for unit tests:

```kotlin
android {
    testOptions {
        unitTests.isIncludeAndroidResources = true
    }
}

dependencies {
    testImplementation("junit:junit:4.13.2")
    testImplementation("androidx.test:core:1.6.1")
    testImplementation("org.robolectric:robolectric:4.13")
}
```

- [ ] **Step 2: Write the failing layout regression test**

```kotlin
package com.example.aiassistant

import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class MainActivityLayoutTest {
    @Test
    fun aiTabIsAttachedWithFullHeight() {
        val activity = Robolectric.buildActivity(MainActivity::class.java).setup().get()
        val decor = activity.findViewById<ViewGroup>(android.R.id.content)
        val root = decor.getChildAt(0) as LinearLayout
        val contentHost = root.getChildAt(0) as LinearLayout
        val bottomNavigation = root.getChildAt(1) as LinearLayout

        bottomNavigation.getChildAt(1).performClick()

        assertEquals(
            ViewGroup.LayoutParams.MATCH_PARENT,
            contentHost.getChildAt(0).layoutParams.height
        )
    }
}
```

- [ ] **Step 3: Run the test and verify the expected failure**

Run:

```bash
gradle testDebugUnitTest --tests com.example.aiassistant.MainActivityLayoutTest -i
```

Expected: FAIL because the selected tab child currently has `WRAP_CONTENT` height.

- [ ] **Step 4: Attach tab views with explicit full-size layout params**

Replace the final line of `showTab` with:

```kotlin
contentContainer.addView(
    view,
    LinearLayout.LayoutParams(
        LinearLayout.LayoutParams.MATCH_PARENT,
        LinearLayout.LayoutParams.MATCH_PARENT
    )
)
```

Remove the unused `ApplicationProvider` import from the test.

- [ ] **Step 5: Make CI run tests before building**

Insert before `Build debug APK`:

```yaml
      - name: Run unit tests
        run: gradle testDebugUnitTest --stacktrace
```

- [ ] **Step 6: Verify, commit, and merge PR #1**

Run:

```bash
gradle testDebugUnitTest
gradle assembleDebug
git add app/build.gradle.kts app/src/main/java/com/example/aiassistant/MainActivity.kt app/src/test/java/com/example/aiassistant/MainActivityLayoutTest.kt .github/workflows/build-apk.yml
git commit -m "fix: keep tab content full height"
```

Expected: tests and APK build pass. Push `feature/v0-2-ui`, wait for GitHub Actions success, then squash merge PR #1.

---

### Task 2: Add Room, Coroutines, and Test Infrastructure

**Files:**
- Modify: `build.gradle.kts`
- Modify: `app/build.gradle.kts`
- Create: `app/src/test/java/com/example/aiassistant/TestCoroutineRule.kt`

**Interfaces:**
- Consumes: merged v0.2 project on `main`
- Produces: Room annotation processing, lifecycle coroutines, deterministic coroutine tests

- [ ] **Step 1: Create the implementation branch**

```bash
git checkout main
git pull --ff-only
git checkout -b feature/offline-core
```

- [ ] **Step 2: Add the kapt plugin to the root build**

```kotlin
plugins {
    id("com.android.application") version "8.5.2" apply false
    id("org.jetbrains.kotlin.android") version "1.9.24" apply false
    id("org.jetbrains.kotlin.kapt") version "1.9.24" apply false
}
```

- [ ] **Step 3: Add app dependencies and Room schema configuration**

```kotlin
plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.kapt")
}

android {
    testOptions {
        unitTests.isIncludeAndroidResources = true
    }
    defaultConfig {
        javaCompileOptions {
            annotationProcessorOptions {
                arguments["room.schemaLocation"] = "$projectDir/schemas"
            }
        }
    }
}

dependencies {
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.4")
    implementation("androidx.room:room-runtime:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")
    kapt("androidx.room:room-compiler:2.6.1")

    testImplementation("junit:junit:4.13.2")
    testImplementation("androidx.test:core:1.6.1")
    testImplementation("androidx.room:room-testing:2.6.1")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.8.1")
    testImplementation("org.robolectric:robolectric:4.13")
}
```

- [ ] **Step 4: Add a deterministic main-dispatcher test rule**

```kotlin
package com.example.aiassistant

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.rules.TestWatcher
import org.junit.runner.Description

@OptIn(ExperimentalCoroutinesApi::class)
class TestCoroutineRule(
    val dispatcher: TestDispatcher = StandardTestDispatcher()
) : TestWatcher() {
    override fun starting(description: Description) = Dispatchers.setMain(dispatcher)
    override fun finished(description: Description) = Dispatchers.resetMain()
}
```

- [ ] **Step 5: Verify configuration and commit**

```bash
gradle testDebugUnitTest
gradle assembleDebug
git add build.gradle.kts app/build.gradle.kts app/src/test/java/com/example/aiassistant/TestCoroutineRule.kt
git commit -m "build: add offline storage and test dependencies"
```

Expected: existing tests pass and kapt configuration resolves without warnings that block the build.

---

### Task 3: Implement Korean Date, Time, Deadline, and Reminder Parsing

**Files:**
- Create: `app/src/main/java/com/example/aiassistant/classification/InputType.kt`
- Create: `app/src/main/java/com/example/aiassistant/classification/ClassificationResult.kt`
- Create: `app/src/main/java/com/example/aiassistant/classification/ParsedTemporal.kt`
- Create: `app/src/main/java/com/example/aiassistant/classification/KoreanDateTimeParser.kt`
- Create: `app/src/test/java/com/example/aiassistant/classification/KoreanDateTimeParserTest.kt`

**Interfaces:**
- Produces: `KoreanDateTimeParser.parse(text: String, now: ZonedDateTime): ParsedTemporal`
- Produces: exact domain enums and `ClassificationResult` fields from the approved design

- [ ] **Step 1: Write parser tests before implementation**

```kotlin
package com.example.aiassistant.classification

import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime
import org.junit.Assert.*
import org.junit.Test

class KoreanDateTimeParserTest {
    private val parser = KoreanDateTimeParser()
    private val now = ZonedDateTime.of(2026, 8, 5, 14, 0, 0, 0, ZoneId.of("Asia/Seoul"))

    @Test fun parsesTomorrowAfternoonAndReminder() {
        val result = parser.parse("내일 오후 3시 병원 30분 전에 알려줘", now)
        assertEquals(LocalDate.of(2026, 8, 6), result.date)
        assertEquals(LocalTime.of(15, 0), result.time)
        assertEquals(30, result.reminderMinutes)
        assertFalse(result.isPast)
    }

    @Test fun parsesDeadlineAsDueDate() {
        val result = parser.parse("금요일까지 보고서 제출", now)
        assertEquals(LocalDate.of(2026, 8, 7), result.dueDate)
        assertNull(result.time)
    }

    @Test fun sameWeekdayWithoutTimeMeansToday() {
        val wednesday = now
        val result = parser.parse("수요일 회의", wednesday)
        assertEquals(LocalDate.of(2026, 8, 5), result.date)
    }

    @Test fun sameWeekdayWithPastTimeMovesToNextWeek() {
        val result = parser.parse("수요일 오전 9시 회의", now)
        assertEquals(LocalDate.of(2026, 8, 12), result.date)
    }

    @Test fun explicitPastDateIsBlocked() {
        val result = parser.parse("2026-08-01 회의", now)
        assertTrue(result.isPast)
    }

    @Test fun parsesNoonMidnightAndClockFormats() {
        assertEquals(LocalTime.NOON, parser.parse("내일 정오 회의", now).time)
        assertEquals(LocalTime.MIDNIGHT, parser.parse("내일 자정 출발", now).time)
        assertEquals(LocalTime.of(15, 30), parser.parse("내일 15:30 회의", now).time)
    }
}
```

- [ ] **Step 2: Run tests and verify missing-type failures**

```bash
gradle testDebugUnitTest --tests com.example.aiassistant.classification.KoreanDateTimeParserTest
```

Expected: compilation fails because domain types and parser do not exist.

- [ ] **Step 3: Add the domain types**

```kotlin
package com.example.aiassistant.classification

enum class InputType { EVENT, TASK, NOTE, AMBIGUOUS }
enum class Confidence { HIGH, MEDIUM, LOW }
enum class RequiredField { TYPE, TITLE, EVENT_DATE, EVENT_TIME }
```

```kotlin
package com.example.aiassistant.classification

import java.time.LocalDate
import java.time.LocalTime

data class ClassificationResult(
    val originalText: String,
    val suggestedType: InputType,
    val confidence: Confidence,
    val title: String,
    val eventDate: LocalDate?,
    val eventStartTime: LocalTime?,
    val eventEndTime: LocalTime?,
    val taskDueDate: LocalDate?,
    val reminderMinutes: Int?,
    val missingFields: Set<RequiredField>,
    val matchedRules: List<String>,
    val isPastDate: Boolean = false
)
```

```kotlin
package com.example.aiassistant.classification

import java.time.LocalDate
import java.time.LocalTime

data class ParsedTemporal(
    val date: LocalDate?,
    val time: LocalTime?,
    val dueDate: LocalDate?,
    val reminderMinutes: Int?,
    val hasDateToken: Boolean,
    val hasTimeToken: Boolean,
    val hasDeadlineToken: Boolean,
    val consumedRanges: List<IntRange>,
    val isPast: Boolean
)
```

- [ ] **Step 4: Implement the parser with explicit regex precedence**

Implement these regexes in this order so a full date is not partially consumed as a month/day or time:

```kotlin
private val fullKoreanDate = Regex("(\\d{4})년\\s*(\\d{1,2})월\\s*(\\d{1,2})일")
private val isoDate = Regex("(\\d{4})-(\\d{1,2})-(\\d{1,2})")
private val monthDay = Regex("(\\d{1,2})월\\s*(\\d{1,2})일")
private val relativeDate = Regex("오늘|내일|모레")
private val weekday = Regex("([월화수목금토일])요일")
private val amPmTime = Regex("(오전|오후)\\s*(\\d{1,2})시(?:\\s*(\\d{1,2})분)?")
private val colonTime = Regex("(?<!\\d)(\\d{1,2}):(\\d{2})(?!\\d)")
private val plainTime = Regex("(?<!월\\s)(?<!일\\s)(\\d{1,2})시(?:\\s*(\\d{1,2})분)?")
private val minuteReminder = Regex("(\\d+)\\s*분\\s*전(?:에)?")
private val hourReminder = Regex("(\\d+)\\s*시간\\s*전(?:에)?")
```

The implementation must:

```kotlin
fun parse(text: String, now: ZonedDateTime): ParsedTemporal {
    val consumed = mutableListOf<IntRange>()
    val parsedTime = parseTime(text, consumed)
    val parsedDate = parseDate(text, now, parsedTime, consumed)
    val reminder = parseReminder(text, consumed)
    val deadline = parsedDate != null && text.contains("까지")
    val dueDate = if (deadline) parsedDate else null
    val isPast = parsedDate?.isBefore(now.toLocalDate()) == true
    return ParsedTemporal(
        date = parsedDate,
        time = parsedTime,
        dueDate = dueDate,
        reminderMinutes = reminder,
        hasDateToken = parsedDate != null,
        hasTimeToken = parsedTime != null,
        hasDeadlineToken = deadline,
        consumedRanges = consumed.distinct(),
        isPast = isPast
    )
}
```

`parseDate` must choose the nearest weekday with this exact rule:

```kotlin
var daysAhead = (target.value - now.dayOfWeek.value + 7) % 7
if (daysAhead == 0 && parsedTime != null && !parsedTime.isAfter(now.toLocalTime())) {
    daysAhead = 7
}
return now.toLocalDate().plusDays(daysAhead.toLong())
```

Convert `오후 12시` to 12:00, `오전 12시` to 00:00, reject hours outside 0–23 and minutes outside 0–59 by returning no time match.

- [ ] **Step 5: Run parser tests and commit**

```bash
gradle testDebugUnitTest --tests com.example.aiassistant.classification.KoreanDateTimeParserTest
git add app/src/main/java/com/example/aiassistant/classification app/src/test/java/com/example/aiassistant/classification/KoreanDateTimeParserTest.kt
git commit -m "feat: parse Korean dates times and reminders offline"
```

Expected: all six parser tests pass.

---

### Task 4: Extract Titles and Learnable Keyword Candidates

**Files:**
- Create: `app/src/main/java/com/example/aiassistant/classification/TitleExtractor.kt`
- Create: `app/src/main/java/com/example/aiassistant/classification/KeywordCandidateExtractor.kt`
- Create: `app/src/test/java/com/example/aiassistant/classification/TitleExtractorTest.kt`
- Create: `app/src/test/java/com/example/aiassistant/classification/KeywordCandidateExtractorTest.kt`

**Interfaces:**
- Consumes: `ParsedTemporal.consumedRanges`
- Produces: `TitleExtractor.extract(text, consumedRanges, inputType): String`
- Produces: `KeywordCandidateExtractor.extract(text, consumedRanges): List<String>`

- [ ] **Step 1: Write failing extraction tests**

```kotlin
class TitleExtractorTest {
    @Test fun removesDateTimeReminderAndCommandWords() {
        val parser = KoreanDateTimeParser()
        val now = ZonedDateTime.parse("2026-08-05T14:00:00+09:00[Asia/Seoul]")
        val text = "내일 오후 3시 병원 30분 전에 알려줘"
        val parsed = parser.parse(text, now)
        assertEquals("병원", TitleExtractor().extract(text, parsed.consumedRanges, InputType.EVENT))
    }

    @Test fun noteTitleUsesFirstThirtyCharacters() {
        val text = "프로젝트 아이디어: 발표 순서를 바꾸고 마지막에 질의응답을 배치한다"
        assertEquals(text.take(30), TitleExtractor().extract(text, emptyList(), InputType.NOTE))
    }
}
```

```kotlin
class KeywordCandidateExtractorTest {
    @Test fun normalizesActionSuffixesAndRemovesNumbers() {
        val result = KeywordCandidateExtractor().extract("교재 3장 복습하기", emptyList())
        assertEquals(listOf("교재", "복습"), result)
    }

    @Test fun returnsDistinctTwoToTwentyCharacterTokens() {
        val result = KeywordCandidateExtractor().extract("병원 병원 예약", emptyList())
        assertEquals(listOf("병원", "예약"), result)
    }
}
```

- [ ] **Step 2: Verify tests fail because extractors are missing**

```bash
gradle testDebugUnitTest --tests "com.example.aiassistant.classification.*ExtractorTest"
```

- [ ] **Step 3: Implement range masking and command cleanup**

`TitleExtractor` must replace every consumed character with a space, then apply:

```kotlin
private val commandWords = Regex("일정\\s*추가|할\\s*일\\s*추가|메모해|기록해|알려줘|해줘|저장해|추가해")
private val punctuation = Regex("^[\\s,.:;·-]+|[\\s,.:;·-]+$")
private val repeatedWhitespace = Regex("\\s+")
```

For `NOTE`, return the original first line trimmed and limited to 30 characters. For other types, return the masked and cleaned text.

- [ ] **Step 4: Implement deterministic keyword normalization**

`KeywordCandidateExtractor` must:

```kotlin
private val stopWords = setOf("오늘", "내일", "모레", "오전", "오후", "전에", "알려줘", "해줘", "추가", "일정", "할일", "메모")
private val actionSuffixes = listOf("복습하기", "준비하기", "확인하기", "신청하기", "전화하기", "보내기", "사기", "하기")
```

Mask consumed ranges, split with `Regex("[^가-힣A-Za-z]+")`, normalize known suffixes (`복습하기` → `복습`, `준비하기` → `준비`), keep tokens of length 2–20, remove stop words and duplicates while preserving input order.

- [ ] **Step 5: Verify and commit**

```bash
gradle testDebugUnitTest --tests "com.example.aiassistant.classification.*ExtractorTest"
git add app/src/main/java/com/example/aiassistant/classification/TitleExtractor.kt app/src/main/java/com/example/aiassistant/classification/KeywordCandidateExtractor.kt app/src/test/java/com/example/aiassistant/classification
git commit -m "feat: extract offline titles and learned-rule candidates"
```

---

### Task 5: Implement Rule-Based Classification and Confidence

**Files:**
- Create: `app/src/main/java/com/example/aiassistant/classification/LearnedRule.kt`
- Create: `app/src/main/java/com/example/aiassistant/classification/LocalInputClassifier.kt`
- Create: `app/src/main/java/com/example/aiassistant/classification/RuleBasedClassifier.kt`
- Create: `app/src/test/java/com/example/aiassistant/classification/RuleBasedClassifierTest.kt`

**Interfaces:**
- Produces: approved `LocalInputClassifier.classify(text, now, learnedRules)` signature
- Consumes: parser, title extractor, normalized exact-token learned rules

- [ ] **Step 1: Write the required classification examples as failing tests**

```kotlin
class RuleBasedClassifierTest {
    private val classifier = RuleBasedClassifier()
    private val now = ZonedDateTime.parse("2026-08-05T14:00:00+09:00[Asia/Seoul]")

    @Test fun classifiesDatedTimedHospitalAsEvent() {
        val result = classifier.classify("내일 오후 3시 병원", now, emptyList())
        assertEquals(InputType.EVENT, result.suggestedType)
        assertEquals(Confidence.HIGH, result.confidence)
        assertEquals("병원", result.title)
    }

    @Test fun classifiesDeadlineSubmissionAsTask() {
        val result = classifier.classify("금요일까지 보고서 제출", now, emptyList())
        assertEquals(InputType.TASK, result.suggestedType)
        assertEquals(LocalDate.of(2026, 8, 7), result.taskDueDate)
    }

    @Test fun classifiesShoppingAndReviewAsTasks() {
        assertEquals(InputType.TASK, classifier.classify("우유 사기", now, emptyList()).suggestedType)
        assertEquals(InputType.TASK, classifier.classify("교재 3장 복습하기", now, emptyList()).suggestedType)
    }

    @Test fun classifiesIdeaAsNote() {
        assertEquals(
            InputType.NOTE,
            classifier.classify("프로젝트 아이디어: 발표 순서를 바꾸기", now, emptyList()).suggestedType
        )
    }

    @Test fun lowConfidenceHospitalResearchIsAmbiguous() {
        val result = classifier.classify("병원 알아보기", now, emptyList())
        assertEquals(InputType.AMBIGUOUS, result.suggestedType)
        assertEquals(Confidence.LOW, result.confidence)
        assertTrue(result.missingFields.contains(RequiredField.TYPE))
    }

    @Test fun learnedRuleRequiresExactNormalizedToken() {
        val rule = LearnedRule("복습", InputType.NOTE)
        assertEquals(InputType.NOTE, classifier.classify("교재 복습하기", now, listOf(rule)).suggestedType)
        assertNotEquals(InputType.NOTE, classifier.classify("복습장 사기", now, listOf(rule)).suggestedType)
    }
}
```

- [ ] **Step 2: Verify RED**

```bash
gradle testDebugUnitTest --tests com.example.aiassistant.classification.RuleBasedClassifierTest
```

Expected: compile failure because classifier interfaces do not exist.

- [ ] **Step 3: Add exact public interfaces**

```kotlin
package com.example.aiassistant.classification

data class LearnedRule(val normalizedKeyword: String, val targetType: InputType)
```

```kotlin
package com.example.aiassistant.classification

import java.time.ZonedDateTime

interface LocalInputClassifier {
    fun classify(
        text: String,
        now: ZonedDateTime,
        learnedRules: List<LearnedRule>
    ): ClassificationResult
}
```

- [ ] **Step 4: Implement deterministic scoring**

Use three integer scores and append a human-readable entry to `matchedRules` for every applied rule. The exact scoring constants are:

```kotlin
private val eventKeywords = listOf("예약", "회의", "약속", "수업", "병원", "치과", "면접", "행사")
private val taskKeywords = listOf("사기", "하기", "복습", "준비", "전화", "보내기", "확인", "신청", "제출")
private val noteKeywords = listOf("메모", "기록", "아이디어", "내용", "참고")
```

Rules:

```text
EVENT: explicit date+time +5; event keyword +3; reminder expression +2; deadline without time -2
TASK: deadline/마감/제출 +4; task keyword +3; sentence ends in a task action +2; date+time -2
NOTE: note keyword +3; no date/time/task-action clues +2; colon followed by text +2
LEARNED: exact normalized token match +6 for the stored target type
```

Determine the result exactly as follows:

```kotlin
val ordered = scores.entries.sortedByDescending { it.value }
val best = ordered[0]
val second = ordered[1]
val confidence = when {
    best.value >= 7 && best.value - second.value >= 3 -> Confidence.HIGH
    best.value >= 5 && best.value - second.value >= 2 -> Confidence.MEDIUM
    else -> Confidence.LOW
}
val type = if (confidence == Confidence.LOW || best.value == second.value) {
    InputType.AMBIGUOUS
} else {
    best.key
}
```

Set missing fields:

```kotlin
val missing = buildSet {
    if (type == InputType.AMBIGUOUS) add(RequiredField.TYPE)
    if (title.isBlank()) add(RequiredField.TITLE)
    if (type == InputType.EVENT && parsed.date == null) add(RequiredField.EVENT_DATE)
    if (type == InputType.EVENT && parsed.time == null) add(RequiredField.EVENT_TIME)
}
```

For a provisional event end time use `startTime?.plusMinutes(60)`; Task 6 replaces this with the saved setting.

- [ ] **Step 5: Verify examples and commit**

```bash
gradle testDebugUnitTest --tests com.example.aiassistant.classification.RuleBasedClassifierTest
git add app/src/main/java/com/example/aiassistant/classification app/src/test/java/com/example/aiassistant/classification/RuleBasedClassifierTest.kt
git commit -m "feat: classify events tasks and notes without AI"
```

---

### Task 6: Apply Settings Defaults in the Offline Input Processor

**Files:**
- Create: `app/src/main/java/com/example/aiassistant/data/SettingsStore.kt`
- Create: `app/src/main/java/com/example/aiassistant/classification/OfflineInputProcessor.kt`
- Create: `app/src/test/java/com/example/aiassistant/classification/OfflineInputProcessorTest.kt`

**Interfaces:**
- Consumes: `LocalInputClassifier`, `LearnedRuleStore`, `SettingsStore`
- Produces: `suspend fun process(text: String, now: ZonedDateTime): ClassificationResult`

- [ ] **Step 1: Write a failing default-application test**

```kotlin
class OfflineInputProcessorTest {
    @Test fun appliesSavedDurationAndReminderToEvents() = runTest {
        val classifier = RuleBasedClassifier()
        val rules = FakeLearnedRuleStore(emptyList())
        val settings = FakeSettingsStore(duration = 90, reminder = 15)
        val processor = OfflineInputProcessor(classifier, rules, settings)
        val now = ZonedDateTime.parse("2026-08-05T14:00:00+09:00[Asia/Seoul]")

        val result = processor.process("내일 오후 3시 병원", now)

        assertEquals(LocalTime.of(16, 30), result.eventEndTime)
        assertEquals(15, result.reminderMinutes)
    }
}
```

The test file must include small fake implementations of the two interfaces defined below, not Mockito mocks.

- [ ] **Step 2: Verify RED**

```bash
gradle testDebugUnitTest --tests com.example.aiassistant.classification.OfflineInputProcessorTest
```

- [ ] **Step 3: Define settings and learned-rule read interfaces**

```kotlin
interface UserSettings {
    fun defaultEventDurationMinutes(): Int
    fun defaultReminderMinutes(): Int
}

interface LearnedRuleReader {
    suspend fun getValidRules(): List<LearnedRule>
}
```

`SettingsStore` wraps a named `SharedPreferences` file `offline_core_settings` and uses keys `event_duration_minutes` and `reminder_minutes`. Clamp duration to 15–480 and reminder to 0–1440. Defaults are 60 and 30.

- [ ] **Step 4: Implement processing and default enrichment**

```kotlin
class OfflineInputProcessor(
    private val classifier: LocalInputClassifier,
    private val learnedRuleReader: LearnedRuleReader,
    private val settings: UserSettings
) {
    suspend fun process(text: String, now: ZonedDateTime): ClassificationResult {
        require(text.isNotBlank()) { "내용을 입력해 주세요" }
        require(text.length <= 500) { "입력은 500자까지 가능합니다" }
        val base = classifier.classify(text, now, learnedRuleReader.getValidRules())
        if (base.suggestedType != InputType.EVENT || base.eventStartTime == null) return base
        return base.copy(
            eventEndTime = base.eventStartTime.plusMinutes(settings.defaultEventDurationMinutes().toLong()),
            reminderMinutes = base.reminderMinutes ?: settings.defaultReminderMinutes()
        )
    }
}
```

- [ ] **Step 5: Verify and commit**

```bash
gradle testDebugUnitTest --tests com.example.aiassistant.classification.OfflineInputProcessorTest
git add app/src/main/java/com/example/aiassistant/data/SettingsStore.kt app/src/main/java/com/example/aiassistant/classification/OfflineInputProcessor.kt app/src/test/java/com/example/aiassistant/classification/OfflineInputProcessorTest.kt
git commit -m "feat: apply offline scheduling defaults"
```

---

### Task 7: Persist Tasks, Notes, and Learned Rules with Room

**Files:**
- Create: `app/src/main/java/com/example/aiassistant/data/TaskEntity.kt`
- Create: `app/src/main/java/com/example/aiassistant/data/TaskDao.kt`
- Create: `app/src/main/java/com/example/aiassistant/data/TaskRepository.kt`
- Create: `app/src/main/java/com/example/aiassistant/data/NoteEntity.kt`
- Create: `app/src/main/java/com/example/aiassistant/data/NoteDao.kt`
- Create: `app/src/main/java/com/example/aiassistant/data/NoteRepository.kt`
- Create: `app/src/main/java/com/example/aiassistant/data/LearnedRuleEntity.kt`
- Create: `app/src/main/java/com/example/aiassistant/data/LearnedRuleDao.kt`
- Create: `app/src/main/java/com/example/aiassistant/data/LearnedRuleStore.kt`
- Create: `app/src/main/java/com/example/aiassistant/data/AppDatabase.kt`
- Create: `app/src/test/java/com/example/aiassistant/data/AppDatabaseTest.kt`

**Interfaces:**
- Produces: suspend insert/list/update APIs; `LearnedRuleStore` implements `LearnedRuleReader`

- [ ] **Step 1: Write Room behavior tests**

Use Robolectric and an in-memory database:

```kotlin
@RunWith(RobolectricTestRunner::class)
class AppDatabaseTest {
    private lateinit var db: AppDatabase

    @Before fun setUp() {
        db = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AppDatabase::class.java
        ).allowMainThreadQueries().build()
    }

    @After fun tearDown() = db.close()

    @Test fun tasksSaveCompleteAndSort() = runTest {
        val repository = TaskRepository(db.taskDao())
        val id = repository.insert("보고서 제출", "금요일까지 보고서 제출", LocalDate.of(2026, 8, 7), 1000)
        repository.setCompleted(id, true, 2000)
        assertTrue(repository.listAll().single().isCompleted)
    }

    @Test fun notesReturnNewestFirst() = runTest {
        val repository = NoteRepository(db.noteDao())
        repository.insert("첫 메모", "첫 메모", 1000)
        repository.insert("둘째 메모", "둘째 메모", 2000)
        assertEquals("둘째 메모", repository.listLatest(3).first().title)
    }

    @Test fun learnedKeywordIsUniqueAndInvalidTypeIsIgnored() = runTest {
        val store = LearnedRuleStore(db.learnedRuleDao())
        store.upsert("복습", InputType.NOTE, 1000)
        store.upsert("복습", InputType.TASK, 2000)
        db.learnedRuleDao().insertRaw(
            LearnedRuleEntity(0, "잘못된규칙", "BROKEN", 3000, 3000)
        )
        val rules = store.getValidRules()
        assertEquals(listOf(LearnedRule("복습", InputType.TASK)), rules)
    }
}
```

- [ ] **Step 2: Verify RED**

```bash
gradle testDebugUnitTest --tests com.example.aiassistant.data.AppDatabaseTest
```

- [ ] **Step 3: Implement exact entities and indexes**

```kotlin
@Entity(tableName = "tasks")
data class TaskEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val originalText: String,
    val dueDateEpochDay: Long?,
    val isCompleted: Boolean,
    val createdAtEpochMillis: Long,
    val updatedAtEpochMillis: Long
)
```

```kotlin
@Entity(tableName = "notes")
data class NoteEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val body: String,
    val createdAtEpochMillis: Long,
    val updatedAtEpochMillis: Long
)
```

```kotlin
@Entity(
    tableName = "learned_rules",
    indices = [Index(value = ["normalizedKeyword"], unique = true)]
)
data class LearnedRuleEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val normalizedKeyword: String,
    val targetTypeName: String,
    val createdAtEpochMillis: Long,
    val lastUsedAtEpochMillis: Long
)
```

- [ ] **Step 4: Implement DAOs with deterministic ordering**

Task DAO queries:

```kotlin
@Insert suspend fun insert(entity: TaskEntity): Long
@Query("SELECT * FROM tasks ORDER BY isCompleted ASC, CASE WHEN dueDateEpochDay IS NULL THEN 1 ELSE 0 END, dueDateEpochDay ASC, createdAtEpochMillis DESC")
suspend fun listAll(): List<TaskEntity>
@Query("UPDATE tasks SET isCompleted = :completed, updatedAtEpochMillis = :updatedAt WHERE id = :id")
suspend fun setCompleted(id: Long, completed: Boolean, updatedAt: Long)
@Query("SELECT COUNT(*) FROM tasks WHERE isCompleted = 0 AND dueDateEpochDay = :epochDay")
suspend fun countOpenDueOn(epochDay: Long): Int
```

Note DAO queries:

```kotlin
@Insert suspend fun insert(entity: NoteEntity): Long
@Query("SELECT * FROM notes ORDER BY createdAtEpochMillis DESC LIMIT :limit")
suspend fun listLatest(limit: Int): List<NoteEntity>
```

Learned rule DAO methods:

```kotlin
@Insert(onConflict = OnConflictStrategy.REPLACE)
suspend fun upsert(entity: LearnedRuleEntity): Long
@Insert suspend fun insertRaw(entity: LearnedRuleEntity): Long
@Query("SELECT * FROM learned_rules ORDER BY lastUsedAtEpochMillis DESC")
suspend fun listAll(): List<LearnedRuleEntity>
@Query("DELETE FROM learned_rules WHERE id = :id")
suspend fun delete(id: Long)
```

- [ ] **Step 5: Implement repositories and safe rule mapping**

`LearnedRuleStore.getValidRules()` must use `InputType.entries.firstOrNull { it.name == targetTypeName }`, accept only EVENT/TASK/NOTE, and skip all other values. `upsert` must normalize the selected keyword before saving and preserve the original `createdAtEpochMillis` when the keyword already exists by querying it first.

- [ ] **Step 6: Register the Room database**

```kotlin
@Database(
    entities = [TaskEntity::class, NoteEntity::class, LearnedRuleEntity::class],
    version = 1,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun taskDao(): TaskDao
    abstract fun noteDao(): NoteDao
    abstract fun learnedRuleDao(): LearnedRuleDao
}
```

- [ ] **Step 7: Verify and commit**

```bash
gradle testDebugUnitTest --tests com.example.aiassistant.data.AppDatabaseTest
git add app/src/main/java/com/example/aiassistant/data app/src/test/java/com/example/aiassistant/data app/schemas
git commit -m "feat: persist offline tasks notes and learned rules"
```

---

### Task 8: Create Draft Validation and Calendar Intent Generation

**Files:**
- Create: `app/src/main/java/com/example/aiassistant/classification/ItemDraft.kt`
- Create: `app/src/main/java/com/example/aiassistant/ui/PreviewFormState.kt`
- Create: `app/src/main/java/com/example/aiassistant/calendar/CalendarGateway.kt`
- Create: `app/src/test/java/com/example/aiassistant/ui/PreviewFormStateTest.kt`
- Create: `app/src/test/java/com/example/aiassistant/calendar/CalendarGatewayTest.kt`

**Interfaces:**
- Produces: `PreviewFormState.toDraft(): DraftValidationResult`
- Produces: `CalendarGateway.buildInsertIntent(event: EventDraft): Intent`

- [ ] **Step 1: Write failing validation tests**

```kotlin
class PreviewFormStateTest {
    @Test fun eventWithoutTimeCannotBeSaved() {
        val state = PreviewFormState(
            type = InputType.EVENT,
            title = "회의",
            originalText = "금요일 오전 회의",
            dateText = "2026-08-07",
            timeText = "",
            reminderText = "30"
        )
        val result = state.toDraft()
        assertTrue(result is DraftValidationResult.Invalid)
        assertTrue((result as DraftValidationResult.Invalid).fields.contains(RequiredField.EVENT_TIME))
    }

    @Test fun taskAndNoteProduceTypedDrafts() {
        assertTrue(taskState().toDraft() is DraftValidationResult.Valid)
        assertTrue(noteState().toDraft() is DraftValidationResult.Valid)
    }
}
```

- [ ] **Step 2: Write the failing intent test**

```kotlin
@RunWith(RobolectricTestRunner::class)
class CalendarGatewayTest {
    @Test fun buildsInsertIntentWithoutCalendarPermissions() {
        val event = EventDraft("병원", LocalDate.of(2026, 8, 6), LocalTime.of(15, 0), LocalTime.of(16, 0), 30)
        val intent = CalendarGateway().buildInsertIntent(event, ZoneId.of("Asia/Seoul"))
        assertEquals(Intent.ACTION_INSERT, intent.action)
        assertEquals(CalendarContract.Events.CONTENT_URI, intent.data)
        assertEquals("병원", intent.getStringExtra(CalendarContract.Events.TITLE))
        assertTrue(intent.hasExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME))
        assertTrue(intent.hasExtra(CalendarContract.EXTRA_EVENT_END_TIME))
    }
}
```

- [ ] **Step 3: Verify RED**

```bash
gradle testDebugUnitTest --tests "com.example.aiassistant.ui.PreviewFormStateTest" --tests "com.example.aiassistant.calendar.CalendarGatewayTest"
```

- [ ] **Step 4: Add typed drafts and validation**

```kotlin
sealed interface ItemDraft

data class EventDraft(
    val title: String,
    val date: LocalDate,
    val startTime: LocalTime,
    val endTime: LocalTime,
    val reminderMinutes: Int
) : ItemDraft

data class TaskDraft(val title: String, val originalText: String, val dueDate: LocalDate?) : ItemDraft
data class NoteDraft(val title: String, val body: String) : ItemDraft

sealed interface DraftValidationResult {
    data class Valid(val draft: ItemDraft) : DraftValidationResult
    data class Invalid(val fields: Set<RequiredField>, val message: String) : DraftValidationResult
}
```

`PreviewFormState.toDraft()` must parse ISO date `yyyy-MM-dd` and time `HH:mm`, reject blank titles, reject past event dates, create a one-hour end time only when no end time is supplied, and clamp reminder values to 0–1440.

- [ ] **Step 5: Implement CalendarGateway**

```kotlin
class CalendarGateway {
    fun buildInsertIntent(event: EventDraft, zoneId: ZoneId = ZoneId.systemDefault()): Intent {
        val start = ZonedDateTime.of(event.date, event.startTime, zoneId).toInstant().toEpochMilli()
        val end = ZonedDateTime.of(event.date, event.endTime, zoneId).toInstant().toEpochMilli()
        return Intent(Intent.ACTION_INSERT).apply {
            data = CalendarContract.Events.CONTENT_URI
            putExtra(CalendarContract.Events.TITLE, event.title)
            putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, start)
            putExtra(CalendarContract.EXTRA_EVENT_END_TIME, end)
        }
    }

    fun launch(activity: Activity, event: EventDraft): Boolean = try {
        activity.startActivity(buildInsertIntent(event))
        true
    } catch (_: ActivityNotFoundException) {
        false
    }
}
```

Do not add calendar permissions to the manifest.

- [ ] **Step 6: Verify and commit**

```bash
gradle testDebugUnitTest --tests "com.example.aiassistant.ui.PreviewFormStateTest" --tests "com.example.aiassistant.calendar.CalendarGatewayTest"
git add app/src/main/java/com/example/aiassistant/classification/ItemDraft.kt app/src/main/java/com/example/aiassistant/ui/PreviewFormState.kt app/src/main/java/com/example/aiassistant/calendar/CalendarGateway.kt app/src/test/java/com/example/aiassistant/ui app/src/test/java/com/example/aiassistant/calendar
git commit -m "feat: validate offline drafts and open calendar inserts"
```

---

### Task 9: Add the Application Container and Manifest Wiring

**Files:**
- Create: `app/src/main/java/com/example/aiassistant/AiAssistantApplication.kt`
- Create: `app/src/main/java/com/example/aiassistant/AppContainer.kt`
- Modify: `app/src/main/AndroidManifest.xml`
- Create: `app/src/test/java/com/example/aiassistant/AppContainerTest.kt`

**Interfaces:**
- Produces: one process-wide database, repositories, settings, processor, calendar gateway

- [ ] **Step 1: Write a failing application-container test**

```kotlin
@RunWith(RobolectricTestRunner::class)
class AppContainerTest {
    @Test fun applicationProvidesOneContainerInstance() {
        val application = ApplicationProvider.getApplicationContext<AiAssistantApplication>()
        assertSame(application.container, application.container)
        assertNotNull(application.container.offlineInputProcessor)
        assertNotNull(application.container.taskRepository)
    }
}
```

- [ ] **Step 2: Verify RED**

```bash
gradle testDebugUnitTest --tests com.example.aiassistant.AppContainerTest
```

- [ ] **Step 3: Implement the service locator**

```kotlin
class AppContainer(context: Context) {
    val database: AppDatabase = Room.databaseBuilder(
        context.applicationContext,
        AppDatabase::class.java,
        "ai_assistant.db"
    ).build()
    val taskRepository = TaskRepository(database.taskDao())
    val noteRepository = NoteRepository(database.noteDao())
    val learnedRuleStore = LearnedRuleStore(database.learnedRuleDao())
    val settingsStore = SettingsStore(context.applicationContext)
    val classifier: LocalInputClassifier = RuleBasedClassifier()
    val offlineInputProcessor = OfflineInputProcessor(classifier, learnedRuleStore, settingsStore)
    val calendarGateway = CalendarGateway()
}
```

```kotlin
class AiAssistantApplication : Application() {
    val container: AppContainer by lazy { AppContainer(this) }
}
```

- [ ] **Step 4: Register the application and preserve the no-network manifest**

```xml
<application
    android:name=".AiAssistantApplication"
    android:allowBackup="true"
    android:label="@string/app_name"
    android:supportsRtl="true"
    android:theme="@style/Theme.AiAssistant">
```

The manifest must contain no `<uses-permission>` entries.

- [ ] **Step 5: Verify and commit**

```bash
gradle testDebugUnitTest --tests com.example.aiassistant.AppContainerTest
git add app/src/main/java/com/example/aiassistant/AiAssistantApplication.kt app/src/main/java/com/example/aiassistant/AppContainer.kt app/src/main/AndroidManifest.xml app/src/test/java/com/example/aiassistant/AppContainerTest.kt
git commit -m "feat: wire offline services into the application"
```

---

### Task 10: Replace the Monolithic Activity with a Full-Height Navigation Shell

**Files:**
- Create: `app/src/main/java/com/example/aiassistant/ui/AppScreen.kt`
- Move/Modify: `app/src/main/java/com/example/aiassistant/MainActivity.kt` → `app/src/main/java/com/example/aiassistant/ui/MainActivity.kt`
- Modify: `app/src/main/AndroidManifest.xml`
- Create: `app/src/main/res/values/ids.xml`
- Create: `app/src/test/java/com/example/aiassistant/ui/MainActivityNavigationTest.kt`

**Interfaces:**
- Produces: `navigate(screen: AppScreen)` and full-height `FrameLayout` content host
- Bottom navigation: HOME, QUICK_INPUT, CALENDAR, TASKS, NOTES
- Settings is opened from HOME and is not a bottom-navigation item

- [ ] **Step 1: Write the failing navigation test**

```kotlin
@RunWith(RobolectricTestRunner::class)
class MainActivityNavigationTest {
    @Test fun startsOnHomeAndQuickInputOccupiesFullHost() {
        val activity = Robolectric.buildActivity(MainActivity::class.java).setup().get()
        assertNotNull(activity.findViewById<View>(R.id.screen_home))

        activity.findViewById<View>(R.id.nav_quick_input).performClick()

        val quick = activity.findViewById<View>(R.id.screen_quick_input)
        assertNotNull(quick)
        assertEquals(ViewGroup.LayoutParams.MATCH_PARENT, quick.layoutParams.height)
    }
}
```

- [ ] **Step 2: Add stable IDs**

```xml
<resources>
    <item name="content_host" type="id" />
    <item name="screen_home" type="id" />
    <item name="screen_quick_input" type="id" />
    <item name="screen_preview" type="id" />
    <item name="screen_calendar" type="id" />
    <item name="screen_tasks" type="id" />
    <item name="screen_notes" type="id" />
    <item name="screen_settings" type="id" />
    <item name="nav_home" type="id" />
    <item name="nav_quick_input" type="id" />
    <item name="nav_calendar" type="id" />
    <item name="nav_tasks" type="id" />
    <item name="nav_notes" type="id" />
</resources>
```

- [ ] **Step 3: Implement the navigation shell**

```kotlin
enum class AppScreen { HOME, QUICK_INPUT, PREVIEW, CALENDAR, TASKS, NOTES, SETTINGS }
```

`MainActivity` must create a vertical root, a `FrameLayout` with weight 1, and a five-button bottom bar. Every screen is attached with:

```kotlin
contentHost.removeAllViews()
contentHost.addView(
    screenView,
    FrameLayout.LayoutParams(
        FrameLayout.LayoutParams.MATCH_PARENT,
        FrameLayout.LayoutParams.MATCH_PARENT
    )
)
```

Update the manifest activity name to `.ui.MainActivity`.

- [ ] **Step 4: Keep temporary placeholder factories only inside this task**

Until Tasks 11–13 replace them, each `createScreen` branch returns a `TextView` with the correct screen ID and label. Do not leave placeholder branches after Task 13.

- [ ] **Step 5: Verify and commit**

```bash
gradle testDebugUnitTest --tests com.example.aiassistant.ui.MainActivityNavigationTest
gradle assembleDebug
git add app/src/main/java/com/example/aiassistant/ui app/src/main/AndroidManifest.xml app/src/main/res/values/ids.xml app/src/test/java/com/example/aiassistant/ui/MainActivityNavigationTest.kt
git commit -m "refactor: add full-height offline navigation shell"
```

---

### Task 11: Build Quick Input and Editable Preview Screens

**Files:**
- Create: `app/src/main/java/com/example/aiassistant/ui/QuickInputViewFactory.kt`
- Create: `app/src/main/java/com/example/aiassistant/ui/PreviewViewFactory.kt`
- Modify: `app/src/main/java/com/example/aiassistant/ui/MainActivity.kt`
- Modify: `app/src/main/res/values/strings.xml`
- Create: `app/src/test/java/com/example/aiassistant/ui/QuickInputViewFactoryTest.kt`
- Create: `app/src/test/java/com/example/aiassistant/ui/PreviewViewFactoryTest.kt`

**Interfaces:**
- Quick input callback: `(String) -> Unit`
- Preview callbacks: `onCancel`, `onSave(ItemDraft, RememberSelection?)`, `onTypeChanged(InputType)`
- `RememberSelection` contains the selected normalized keyword and target type

- [ ] **Step 1: Write failing factory tests**

The quick-input test must enter blank text, click `분류하기`, and verify the callback is not invoked and `내용을 입력해 주세요` appears. It must then enter 501 characters and verify `입력은 500자까지 가능합니다`.

The preview test must create an EVENT result with missing `EVENT_TIME`, render the view, and verify the save button is disabled. Changing the type from EVENT to TASK must reveal the remember checkbox and keyword chips; checking remember without selecting a chip must not create a learned rule selection.

- [ ] **Step 2: Verify RED**

```bash
gradle testDebugUnitTest --tests "com.example.aiassistant.ui.*ViewFactoryTest"
```

- [ ] **Step 3: Implement QuickInputViewFactory**

The view contains:

```text
Title: 빠른 입력
Multi-line EditText, maxLength 500
Example chips: 내일 오후 3시 병원 / 금요일까지 보고서 제출 / 프로젝트 아이디어 기록
Primary button: 분류하기
Inline error TextView
```

On submit, trim text, show exact validation messages, and invoke the callback once. No progress text may mention a network or AI server.

- [ ] **Step 4: Implement PreviewViewFactory with editable fields**

Use a `Spinner` for EVENT/TASK/NOTE, `EditText` for title, ISO date, `HH:mm` time, and reminder minutes. Show only fields relevant to the selected type. Keep the original text visible but not editable. The save button calls `PreviewFormState.toDraft()` and remains disabled while validation returns `Invalid`.

When the selected type differs from the original suggested type, show:

```text
CheckBox: 이 표현을 기억
Keyword candidate chips from KeywordCandidateExtractor
```

Exactly one chip may be selected. Saving without a selected chip still saves the item but does not persist a rule.

- [ ] **Step 5: Integrate processing and save coordination in MainActivity**

Use `lifecycleScope.launch` to call `offlineInputProcessor.process`. Store the active `ClassificationResult` in the activity while the preview is open. Disable the save button immediately on the first click. Save behavior:

```kotlin
when (draft) {
    is EventDraft -> if (!container.calendarGateway.launch(this, draft)) showCalendarError()
    is TaskDraft -> container.taskRepository.insert(...)
    is NoteDraft -> container.noteRepository.insert(...)
}
```

If a valid remember selection exists, call `learnedRuleStore.upsert` after the item operation succeeds. On Room failure, keep the preview and re-enable save with `저장하지 못했습니다. 다시 시도해 주세요`.

- [ ] **Step 6: Verify and commit**

```bash
gradle testDebugUnitTest --tests "com.example.aiassistant.ui.*ViewFactoryTest"
gradle assembleDebug
git add app/src/main/java/com/example/aiassistant/ui app/src/main/res/values/strings.xml app/src/test/java/com/example/aiassistant/ui
git commit -m "feat: add offline quick input and confirmation preview"
```

---

### Task 12: Add Home, Task, Note, Calendar, and Settings Views

**Files:**
- Create: `app/src/main/java/com/example/aiassistant/ui/HomeViewFactory.kt`
- Create: `app/src/main/java/com/example/aiassistant/ui/TaskViewFactory.kt`
- Create: `app/src/main/java/com/example/aiassistant/ui/NoteViewFactory.kt`
- Create: `app/src/main/java/com/example/aiassistant/ui/SettingsViewFactory.kt`
- Modify: `app/src/main/java/com/example/aiassistant/ui/MainActivity.kt`
- Create: `app/src/test/java/com/example/aiassistant/ui/OfflineScreensTest.kt`

**Interfaces:**
- Home input: today open-task count, latest three notes
- Task callbacks: toggle completion, open quick input
- Note callback: open quick input
- Settings callbacks: save defaults, delete learned rule

- [ ] **Step 1: Write failing screen tests**

```kotlin
@RunWith(RobolectricTestRunner::class)
class OfflineScreensTest {
    @Test fun homeWorksWithoutAiOrNetworkState() {
        val view = HomeViewFactory(context).create(2, listOf(NoteEntity(1, "아이디어", "본문", 1, 1)), {}, {})
        assertNotNull(view.findViewById<View>(R.id.screen_home))
        assertTrue(viewText(view).contains("오늘 마감 할 일 2개"))
        assertFalse(viewText(view).contains("온라인"))
    }

    @Test fun taskCompletionCallbackReceivesIdAndState() {
        var received: Pair<Long, Boolean>? = null
        val view = TaskViewFactory(context).create(listOf(taskEntity()), { id, done -> received = id to done }, {})
        firstCheckBox(view).performClick()
        assertEquals(taskEntity().id to true, received)
    }
}
```

- [ ] **Step 2: Implement HomeViewFactory**

Display today’s open-task count, latest notes up to three, a large `빠른 입력` button, and a settings icon/button. Do not display online status or an AI availability requirement.

- [ ] **Step 3: Implement TaskViewFactory and NoteViewFactory**

Task screen includes `진행 중` and `완료` filter buttons, completion checkboxes, due dates, and an `추가` button that navigates to quick input. Note screen displays newest first with title and up to 80 body characters and also links to quick input.

- [ ] **Step 4: Implement the calendar landing view**

The calendar screen states that events are confirmed in the installed calendar app. `새 일정` navigates to quick input prefilled with `일정 추가: ` instead of opening an empty fixed event.

- [ ] **Step 5: Implement SettingsViewFactory**

Provide integer fields for default event duration and reminder, persist through `SettingsStore`, display the non-disableable text `저장 전 확인: 항상 켜짐`, display `AI 사용: 꺼짐 (오프라인 기본 모드)`, and render learned rules with a delete button per rule. Invalid stored rule types are labeled `잘못된 규칙` and remain deletable.

- [ ] **Step 6: Load data with lifecycleScope and refresh after mutations**

`MainActivity.navigate` launches repository reads, then replaces the screen when data arrives. Task completion and learned-rule deletion refresh their current screen. Database calls never run with `allowMainThreadQueries` in production.

- [ ] **Step 7: Verify and commit**

```bash
gradle testDebugUnitTest --tests com.example.aiassistant.ui.OfflineScreensTest
gradle assembleDebug
git add app/src/main/java/com/example/aiassistant/ui app/src/test/java/com/example/aiassistant/ui/OfflineScreensTest.kt
git commit -m "feat: add offline home task note calendar and settings screens"
```

---

### Task 13: Add End-to-End Offline Flow Tests and Remove Temporary UI

**Files:**
- Modify: `app/src/main/java/com/example/aiassistant/ui/MainActivity.kt`
- Create: `app/src/test/java/com/example/aiassistant/ui/OfflineFlowTest.kt`
- Delete: any temporary placeholder screen methods introduced in Task 10

**Interfaces:**
- Verifies: input → preview → task save; input → type correction → learned rule; event → calendar intent

- [ ] **Step 1: Write end-to-end Robolectric tests**

Use an in-memory `AppContainer` test constructor so the activity can receive test repositories. Cover:

```text
금요일까지 보고서 제출 → TASK preview → save → task list contains 보고서 제출
프로젝트 아이디어: 발표 순서를 바꾸기 → NOTE preview → save → notes list contains the body
병원 알아보기 → AMBIGUOUS preview → select TASK → select 병원 candidate → remember → save → next 병원 input receives TASK learned score
내일 오후 3시 병원 → EVENT preview → save → Calendar ACTION_INSERT intent is started
```

Also click save twice before the test dispatcher advances and assert only one database row exists.

- [ ] **Step 2: Verify tests fail before final wiring**

```bash
gradle testDebugUnitTest --tests com.example.aiassistant.ui.OfflineFlowTest
```

- [ ] **Step 3: Add test-injectable AppContainer wiring**

`AiAssistantApplication` keeps production defaults. `MainActivity` reads the application container through an overridable internal provider function or an application field that tests can replace before activity creation. Do not introduce a DI framework.

- [ ] **Step 4: Remove all placeholder screens and complete refresh paths**

Every `AppScreen` branch must call a real factory. After successful task/note save, navigate to the corresponding list. After launching Calendar, keep the preview available when the activity resumes because actual external save status is unknown.

- [ ] **Step 5: Verify all unit tests and commit**

```bash
gradle testDebugUnitTest
gradle assembleDebug
git add app/src/main/java/com/example/aiassistant app/src/test/java/com/example/aiassistant
git commit -m "test: verify complete offline classification flows"
```

Expected: all parser, classifier, Room, UI, and flow tests pass.

---

### Task 14: Harden CI, Verify No Network Permission, and Publish APK

**Files:**
- Modify: `.github/workflows/build-apk.yml`
- Modify: `README.md`

**Interfaces:**
- Produces: CI-gated `ai-assistant-debug-apk` artifact

- [ ] **Step 1: Replace the workflow build sequence**

```yaml
      - name: Verify offline manifest
        run: |
          if grep -q "android.permission.INTERNET\|android.permission.READ_CALENDAR\|android.permission.WRITE_CALENDAR" app/src/main/AndroidManifest.xml; then
            echo "Forbidden permission found"
            exit 1
          fi

      - name: Run unit tests
        run: gradle testDebugUnitTest --stacktrace

      - name: Build debug APK
        run: gradle assembleDebug --stacktrace

      - name: Upload APK
        uses: actions/upload-artifact@v4
        with:
          name: ai-assistant-debug-apk
          path: app/build/outputs/apk/debug/app-debug.apk
          if-no-files-found: error
```

- [ ] **Step 2: Document offline behavior and exact supported phrases**

README must list:

```text
내일 오후 3시 병원
금요일까지 보고서 제출
우유 사기
프로젝트 아이디어: 발표 순서를 바꾸기
```

State that AI, internet, API keys, and calendar permissions are not required. State that event creation opens the user’s installed calendar app for final confirmation.

- [ ] **Step 3: Run the full local verification**

```bash
gradle clean testDebugUnitTest assembleDebug
```

Expected: `BUILD SUCCESSFUL` and `app/build/outputs/apk/debug/app-debug.apk` exists.

- [ ] **Step 4: Commit and push**

```bash
git add .github/workflows/build-apk.yml README.md
git commit -m "ci: verify and package the offline assistant"
git push -u origin feature/offline-core
```

- [ ] **Step 5: Open the feature PR and verify GitHub Actions**

PR title:

```text
feat: add AI-free offline classification and local organizer
```

The PR body must enumerate parser tests, classifier examples, Room tests, no-network manifest verification, calendar Insert behavior, and the generated APK artifact. Merge only after all checks succeed.

---

## Final Verification Checklist

- [ ] `내일 오후 3시 병원` produces an EVENT preview for tomorrow at 15:00 with configured duration and reminder.
- [ ] `금요일까지 보고서 제출` produces a TASK preview with the nearest Friday due date.
- [ ] `우유 사기` and `교재 3장 복습하기` classify as TASK without AI.
- [ ] `프로젝트 아이디어: 발표 순서를 바꾸기` classifies as NOTE.
- [ ] `병원 알아보기` remains AMBIGUOUS until the user chooses a type.
- [ ] A changed classification only creates a learned rule when the user enables remembering and selects one candidate.
- [ ] Learned rules use exact normalized tokens, are unique per keyword, and can be deleted.
- [ ] Invalid stored rule type values are ignored safely and remain deletable.
- [ ] Past events and events with missing date/time cannot be saved.
- [ ] Double-clicking save creates only one task or note.
- [ ] Room operations do not run on the main thread in production.
- [ ] Calendar creation uses ACTION_INSERT and requests no calendar permission.
- [ ] The manifest contains no INTERNET permission.
- [ ] `gradle testDebugUnitTest` and `gradle assembleDebug` both pass.
- [ ] GitHub Actions uploads a non-expired `ai-assistant-debug-apk` artifact.
