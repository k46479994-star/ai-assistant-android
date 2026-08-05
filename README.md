# AI 비서 Android — 오프라인 자동 정리 MVP

AI API, 인터넷 연결, 계정, API 키가 없어도 사용할 수 있는 Kotlin 기반 Android 개인 정리 앱입니다. 하나의 빠른 입력창에 한국어 문장을 입력하면 기기 안에서 **일정·할 일·메모**로 분류하고, 실제 저장 전 사용자가 결과를 확인하거나 수정할 수 있습니다.

## 현재 기능

- 홈 요약: 오늘 마감 할 일 수와 최근 메모
- 빠른 입력: 최대 500자의 한국어 자연어 입력
- 완전 오프라인 규칙 기반 분류
  - 일정: `내일 오후 3시 병원`
  - 할 일: `금요일까지 보고서 제출`
  - 메모: `프로젝트 아이디어: 발표 순서를 바꾸기`
- 날짜·시간·요일·마감·알림 표현 추출
- 분류 결과 미리보기와 유형·제목·날짜·시간·알림 수정
- 신뢰도가 낮은 문장은 자동 확정하지 않고 유형 선택 요구
- 사용자가 수정한 분류를 선택한 키워드 규칙으로 기기 안에 기억
- Room 데이터베이스에 할 일과 메모 저장
- 진행 중·완료 할 일 필터와 완료 처리
- 최근순 메모 목록
- 기본 일정 길이와 알림 시간 설정
- 기억한 분류 규칙 조회·삭제
- Android 캘린더 일정 추가 확인 화면 연결
- 단위·통합 테스트와 GitHub Actions APK 자동 빌드

## 오프라인 분류 흐름

```text
사용자 입력
  → KoreanDateTimeParser
  → RuleBasedClassifier
  → OfflineInputProcessor
  → 저장 전 미리보기
      ├─ 일정 → CalendarContract.ACTION_INSERT
      ├─ 할 일 → Room
      └─ 메모 → Room
```

분류기는 일정·할 일·메모 단서에 점수를 부여합니다. 점수 차이가 작거나 확신도가 낮으면 `AMBIGUOUS`로 처리하며, 사용자가 유형을 선택하기 전에는 저장할 수 없습니다.

## 지원하는 주요 표현

- 상대 날짜: `오늘`, `내일`, `모레`
- 요일: `월요일`부터 `일요일`까지
- 날짜: `8월 10일`, `2026년 8월 10일`, `2026-08-10`
- 시간: `오전 9시`, `오후 3시`, `15시`, `15:30`, `정오`, `자정`
- 알림: `10분 전`, `30분 전에`, `1시간 전`
- 마감: `금요일까지`, `8월 10일까지`

## Google 캘린더 등 캘린더 앱 연결

일정은 `CalendarContract.ACTION_INSERT`로 외부 캘린더 확인 화면에 전달합니다. 사용자가 제목과 시간을 최종 확인한 뒤 설치된 Google 캘린더 등의 캘린더 앱에서 저장합니다.

앱은 캘린더 내용을 직접 읽거나 수정하지 않으며 다음 권한을 요청하지 않습니다.

- `READ_CALENDAR`
- `WRITE_CALENDAR`

## 개인정보와 네트워크

오프라인 코어 빌드는 `INTERNET` 권한을 선언하지 않습니다.

- 할 일·메모·학습 규칙은 앱 전용 Room 데이터베이스에 저장
- 기본 일정 길이와 알림 설정은 앱 전용 SharedPreferences에 저장
- 입력 문장과 저장 데이터는 외부 서버로 전송하지 않음
- 실제 OpenAI 또는 다른 생성형 AI 호출 없음
- AI 기능은 향후 선택 가능한 보조 기능으로만 추가할 예정

GitHub Actions도 매 빌드마다 매니페스트에 `INTERNET`, `READ_CALENDAR`, `WRITE_CALENDAR` 권한이 없는지 확인합니다.

## 프로젝트 구조

```text
com.example.aiassistant
├─ classification   # 날짜·시간 파싱, 제목·키워드 추출, 규칙 기반 분류
├─ data             # Room, 저장소, 학습 규칙, 로컬 설정
├─ calendar         # 외부 캘린더 Insert 인텐트
└─ ui               # 홈, 빠른 입력, 미리보기, 일정, 할 일, 메모, 설정
```

주요 기술:

- Kotlin
- Android Views + AppCompat
- Room 2.6.1
- Kotlin Coroutines
- Robolectric
- JUnit 4
- Gradle 8.7
- 최소 Android 8.0(API 26)
- compile/target SDK 35

## 자동 APK 빌드

`main` 브랜치에 푸시하거나 Pull Request를 열면 GitHub Actions가 다음 순서로 실행됩니다.

```text
오프라인 권한 검증
→ gradle testDebugUnitTest
→ gradle assembleDebug
→ ai-assistant-debug-apk 업로드
```

성공한 실행의 **Actions → Artifacts → `ai-assistant-debug-apk`**에서 디버그 APK를 받을 수 있습니다.

## 로컬 빌드

Android Studio에서 JDK 17과 Android SDK 35를 준비한 뒤 프로젝트를 열어 Gradle 동기화를 실행합니다.

```bash
./gradlew testDebugUnitTest
./gradlew assembleDebug
```

생성 위치:

```text
app/build/outputs/apk/debug/app-debug.apk
```

## 현재 범위 밖

- 실제 생성형 AI 대화
- 캘린더 일정 읽기·수정·삭제
- 클라우드 동기화와 로그인
- 백그라운드 음성 호출
- PDF·이미지 분석
- Play Store 서명/AAB 배포

이 기능들은 오프라인 기본 기능을 대체하지 않고 후속 버전에서 선택적으로 추가할 수 있습니다.
