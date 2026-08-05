# AI 비서 v0.2 UI Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 기존 단일 채팅 화면을 홈·채팅·일정·할 일·설정 탭이 있는 실행 가능한 v0.2 MVP로 개선한다.

**Architecture:** `MainActivity`는 공통 루트 레이아웃과 하단 메뉴를 관리한다. 각 탭은 별도 View 생성 함수로 구성하며 일정 추가는 Android Calendar Insert 인텐트를 사용한다.

**Tech Stack:** Kotlin, Android Views, AppCompat, Material Components, GitHub Actions, Gradle 8.7

## Global Constraints

- 최소 Android 8.0(API 26)
- 외부 UI 라이브러리 추가 금지
- OpenAI 실제 호출은 이번 범위에서 제외
- 캘린더 저장은 사용자 확인이 가능한 외부 캘린더 화면으로 전달

---

### Task 1: 화면 상태와 탭 전환

**Files:**
- Modify: `app/src/main/java/com/example/aiassistant/MainActivity.kt`

**Interfaces:**
- Produces: `showTab(Tab)`, `buildBottomNavigation()`, `enum class Tab`

- [ ] `Tab` 열거형과 콘텐츠 컨테이너를 추가한다.
- [ ] 하단의 홈·채팅·일정·할 일·설정 버튼이 `showTab`을 호출하게 한다.
- [ ] 앱 시작 시 홈 탭이 표시되는지 확인한다.
- [ ] `gradle assembleDebug`를 실행해 컴파일 성공을 확인한다.

### Task 2: 탭별 MVP 화면

**Files:**
- Modify: `app/src/main/java/com/example/aiassistant/MainActivity.kt`
- Modify: `app/src/main/res/values/strings.xml`

**Interfaces:**
- Produces: `buildHomeView()`, `buildChatView()`, `buildCalendarView()`, `buildTodoView()`, `buildSettingsView()`

- [ ] 홈 화면에 인사, 오늘 일정, 할 일, AI 추천 카드를 표시한다.
- [ ] 채팅 화면에 말풍선, 입력창, 전송 동작을 유지한다.
- [ ] 일정 화면에 일정 요약과 일정 추가 버튼을 표시한다.
- [ ] 할 일 화면에 체크 가능한 데모 항목을 표시한다.
- [ ] 설정 화면에 버전과 개인정보 안내를 표시한다.
- [ ] `gradle assembleDebug`를 실행해 컴파일 성공을 확인한다.

### Task 3: Google 캘린더 진입과 APK 검증

**Files:**
- Modify: `app/src/main/java/com/example/aiassistant/MainActivity.kt`
- Verify: `.github/workflows/build-apk.yml`

**Interfaces:**
- Produces: `openCalendarInsert()`

- [ ] `CalendarContract.ACTION_INSERT` 인텐트에 제목과 시작 시간을 넣는다.
- [ ] 처리 가능한 캘린더 앱이 없으면 토스트를 표시한다.
- [ ] main 브랜치 푸시 후 GitHub Actions가 성공하는지 확인한다.
- [ ] `ai-assistant-debug-apk` 아티팩트가 생성되는지 확인한다.
