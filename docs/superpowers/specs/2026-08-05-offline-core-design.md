# AI 비서 오프라인 코어 자동 분류 설계

## 1. 목적

앱은 OpenAI, 다른 생성형 AI, 인터넷 연결, API 키가 없어도 일정·할 일·메모를 생성하고 관리할 수 있어야 한다. AI는 나중에 낮은 신뢰도의 문장을 보조하는 선택 기능으로만 추가하며, 기본 동작을 대체하지 않는다.

핵심 흐름은 다음과 같다.

1. 사용자가 하나의 빠른 입력창에 자연어를 입력한다.
2. 앱이 기기 안에서 일정·할 일·메모 중 하나로 분류하고 날짜·시간·알림을 추출한다.
3. 실제 저장 내용을 미리보기 카드로 보여준다.
4. 사용자가 확인하거나 분류·필드를 수정한다.
5. 일정은 캘린더 앱의 확인 화면으로 전달하고, 할 일과 메모는 로컬 데이터베이스에 저장한다.

## 2. 범위

### 포함

- 한국어 텍스트 기반 빠른 입력
- 완전 오프라인 규칙 기반 분류
- 일정·할 일·메모 분류
- 날짜·시간·마감일·알림 추출
- 저장 전 미리보기와 수동 수정
- Room 기반 할 일·메모 저장
- Android Calendar Insert 인텐트를 통한 일정 생성
- 사용자가 수정한 분류를 기기 안의 키워드 규칙으로 기억
- 비행기 모드에서도 빠른 입력, 분류, 할 일, 메모가 정상 작동

### 제외

- 실제 OpenAI API 호출
- 캘린더 일정 읽기·수정·삭제
- 백그라운드 음성 호출
- PDF·이미지 분석
- 클라우드 동기화와 사용자 계정
- 온디바이스 신경망 모델

## 3. 기존 프로젝트와의 관계

현재 v0.2 PR은 홈·AI 채팅·일정·할 일·설정 탭을 추가한다. 오프라인 코어 구현 전 해당 PR의 탭 콘텐츠 높이 문제를 수정하고 `main`에 병합한다. 이후 하단 메뉴는 `홈 | 빠른 입력 | 일정 | 할 일 | 메모`로 정리하고, 설정은 홈 화면 상단의 설정 버튼으로 연다.

이번 작업에서는 Jetpack Compose로 전환하지 않는다. 기존 Android Views와 AppCompat를 유지하되, 모든 기능을 `MainActivity`에 넣지 않고 분류·파싱·저장·화면 책임을 분리한다.

## 4. 사용자 경험

### 4.1 빠른 입력

예시 입력:

```text
내일 오후 3시 병원, 30분 전에 알려줘
```

결과 미리보기:

```text
[일정]
제목: 병원
날짜: 2026-08-06
시간: 15:00–16:00
알림: 30분 전

[분류 변경] [취소] [캘린더에서 확인]
```

### 4.2 저장 정책

- 모든 항목은 저장 전 확인한다.
- 조회 동작은 확인 없이 실행할 수 있지만, 생성·수정·삭제는 항상 사용자의 명시적인 버튼 입력이 필요하다.
- 신뢰도가 낮으면 유형을 임의로 확정하지 않고 `일정 / 할 일 / 메모` 선택 화면을 보여준다.
- 필수 필드가 없으면 저장 버튼을 비활성화하고 누락된 필드만 입력받는다.

### 4.3 예시 분류

| 입력 | 예상 유형 | 추출 결과 |
|---|---|---|
| `내일 오후 3시 병원` | 일정 | 내일, 15:00, 기본 1시간 |
| `금요일 오전 회의 10분 전 알림` | 일정 | 다음 금요일, 구체적인 시각 입력 요구, 10분 전 |
| `금요일까지 보고서 제출` | 할 일 | 제목 `보고서 제출`, 마감일 다음 금요일 |
| `우유 사기` | 할 일 | 제목 `우유 사기`, 마감일 없음 |
| `교재 3장 복습하기` | 할 일 | 제목 그대로, 마감일 없음 |
| `프로젝트 아이디어: 발표 순서를 바꾸기` | 메모 | 본문 전체 저장 |
| `병원 알아보기` | 모호함 | 일정·할 일·메모 선택 요구 |

## 5. 아키텍처

```text
QuickInputView
    ↓ 원문
OfflineInputProcessor
    ├─ KoreanDateTimeParser
    ├─ RuleBasedClassifier
    ├─ LearnedRuleStore
    └─ TitleExtractor
    ↓ ClassificationResult
PreviewView
    ├─ 유형·필드 수정
    └─ 사용자 확인
         ├─ TaskRepository → Room
         ├─ NoteRepository → Room
         └─ CalendarGateway → ACTION_INSERT
```

### 5.1 패키지 경계

```text
com.example.aiassistant
├─ classification
│  ├─ InputType.kt
│  ├─ ClassificationResult.kt
│  ├─ RuleBasedClassifier.kt
│  ├─ KoreanDateTimeParser.kt
│  ├─ TitleExtractor.kt
│  ├─ KeywordCandidateExtractor.kt
│  └─ OfflineInputProcessor.kt
├─ data
│  ├─ AppDatabase.kt
│  ├─ TaskEntity.kt
│  ├─ TaskDao.kt
│  ├─ TaskRepository.kt
│  ├─ NoteEntity.kt
│  ├─ NoteDao.kt
│  ├─ NoteRepository.kt
│  ├─ LearnedRuleEntity.kt
│  ├─ LearnedRuleDao.kt
│  ├─ LearnedRuleStore.kt
│  └─ SettingsStore.kt
├─ calendar
│  └─ CalendarGateway.kt
└─ ui
   ├─ MainActivity.kt
   ├─ HomeViewFactory.kt
   ├─ QuickInputViewFactory.kt
   ├─ PreviewViewFactory.kt
   ├─ TaskViewFactory.kt
   ├─ NoteViewFactory.kt
   └─ SettingsViewFactory.kt
```

각 분류·파싱 클래스는 Android UI에 의존하지 않는 순수 Kotlin 코드로 작성해 단위 테스트가 가능하게 한다. Room과 캘린더 접근은 인터페이스 뒤에 두어 테스트와 향후 교체를 쉽게 한다.

Room DAO와 Repository의 저장·조회 함수는 `suspend`로 작성하고 `lifecycleScope`에서 호출한다. 데이터베이스 작업은 메인 스레드를 차단하지 않는다. 설정값은 앱 전용 `SharedPreferences`를 감싼 `SettingsStore`에서 관리한다.

## 6. 분류 모델

### 6.1 결과 타입

```kotlin
enum class InputType { EVENT, TASK, NOTE, AMBIGUOUS }

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
    val matchedRules: List<String>
)

enum class Confidence { HIGH, MEDIUM, LOW }
enum class RequiredField { TYPE, TITLE, EVENT_DATE, EVENT_TIME }
```

### 6.2 점수 규칙

분류기는 각 유형에 정수 점수를 누적한다.

#### 일정 점수

- 명시적인 날짜와 시간이 모두 있음: `+5`
- `예약`, `회의`, `약속`, `수업`, `병원`, `치과`, `면접`, `행사`: `+3`
- `알림`, `분 전`, `시간 전` 표현: `+2`
- `까지`가 있고 시작 시간이 없음: `-2`

#### 할 일 점수

- `까지`, `마감`, `제출`: `+4`
- `사기`, `하기`, `복습`, `준비`, `전화`, `보내기`, `확인`, `신청`: `+3`
- 문장이 `해`, `해줘`, `하기`, `할 것` 형태로 끝남: `+2`
- 명시적인 날짜와 시간이 모두 있음: `-2`

#### 메모 점수

- `메모`, `기록`, `아이디어`, `내용`, `참고`: `+3`
- 날짜·시간·행동 단서가 모두 없음: `+2`
- 콜론 뒤에 설명형 문장이 이어짐: `+2`

#### 학습 규칙

사용자가 제안된 유형을 변경하면 `KeywordCandidateExtractor`가 날짜·시간·명령어·불용어를 제거한 뒤 2자 이상 20자 이하의 후보 토큰을 만든다. 미리보기 화면은 후보를 칩으로 표시하며, 사용자가 `이 표현을 기억`을 켜고 후보 하나를 직접 선택해야 규칙을 저장한다.

선택된 키워드는 공백과 문장부호를 정규화한 뒤 저장한다. 이후 입력에서 동일한 정규화 토큰이 발견되면 선택한 유형 점수에 `+6`을 적용한다. 부분 문자열만 일치하는 경우에는 적용하지 않는다. 동일 키워드 규칙은 하나만 유지하며 사용자가 설정에서 조회·삭제할 수 있다.

### 6.3 신뢰도

- `HIGH`: 최고 점수 7 이상이며 2위보다 3점 이상 높음
- `MEDIUM`: 최고 점수 5 이상이며 2위보다 2점 이상 높음
- `LOW`: 그 외
- `LOW`는 `AMBIGUOUS`로 표시하며 사용자가 유형을 선택해야 한다.

점수가 같을 때는 일정이나 할 일을 임의로 선택하지 않고 항상 `AMBIGUOUS`를 반환한다.

## 7. 한국어 날짜·시간 파싱

`java.time`을 사용하며 기기의 현재 날짜와 시간대를 기준으로 한다.

### 지원 표현

- 상대 날짜: `오늘`, `내일`, `모레`
- 요일: `월요일`부터 `일요일`까지
- 월·일: `8월 10일`
- 전체 날짜: `2026-08-10`, `2026년 8월 10일`
- 시간: `오전 9시`, `오후 3시`, `15시`, `15:30`, `정오`, `자정`
- 알림: `10분 전`, `30분 전에`, `1시간 전`, `2시간 전에`
- 마감: `금요일까지`, `8월 10일까지`

### 해석 규칙

- 요일은 가장 가까운 해당 날짜를 선택한다.
- 같은 요일에 시간이 없으면 오늘로 해석한다.
- 같은 요일에 시간이 있고 해당 시간이 이미 지났으면 다음 주로 이동한다.
- 종료 시간이 없으면 일정 길이는 기본 1시간이다.
- 알림이 없으면 설정의 기본값을 사용하며 초기값은 30분 전이다.
- 날짜는 있지만 일정 시간이 없으면 `EVENT_TIME`을 누락 필드로 반환한다.
- 과거 날짜가 추출되면 저장을 막고 날짜 수정 메시지를 표시한다.
- 파서가 이해하지 못한 텍스트는 삭제하지 않고 제목 또는 본문에 그대로 남긴다.

## 8. 제목 추출

- 날짜·시간·알림 표현을 원문에서 제거한다.
- `일정 추가`, `할 일 추가`, `알려줘`, `해줘` 같은 명령어를 제거한다.
- 남은 문자열의 앞뒤 공백과 연속 구두점을 정리한다.
- 결과가 비어 있으면 제목을 필수 누락으로 표시한다.
- 메모 제목은 본문의 첫 줄 또는 앞 30자를 사용하고 본문 전체를 별도로 저장한다.

## 9. 로컬 데이터

### 9.1 할 일

```kotlin
TaskEntity(
    id: Long,
    title: String,
    originalText: String,
    dueDateEpochDay: Long?,
    isCompleted: Boolean,
    createdAtEpochMillis: Long,
    updatedAtEpochMillis: Long
)
```

### 9.2 메모

```kotlin
NoteEntity(
    id: Long,
    title: String,
    body: String,
    createdAtEpochMillis: Long,
    updatedAtEpochMillis: Long
)
```

### 9.3 학습 규칙

```kotlin
LearnedRuleEntity(
    id: Long,
    normalizedKeyword: String,
    targetTypeName: String,
    createdAtEpochMillis: Long,
    lastUsedAtEpochMillis: Long
)
```

`targetTypeName`에는 `EVENT`, `TASK`, `NOTE`만 저장하며 Repository에서 `InputType`으로 변환한다. 알 수 없는 값은 규칙을 무시하고 설정 화면에서 삭제 가능한 잘못된 항목으로 표시한다.

Room 스키마 버전은 `1`로 시작하며, 데이터베이스 파일은 앱 전용 저장소에 둔다. 네트워크 전송은 하지 않는다.

## 10. 화면 구조

### 홈

- 오늘 마감 할 일 수
- 최근 메모 최대 3개
- 빠른 입력으로 이동하는 큰 버튼
- 설정 버튼
- AI가 꺼져 있어도 모든 카드가 정상 표시

### 빠른 입력

- 여러 줄 텍스트 입력
- `분류하기` 버튼
- 최근 사용 예시 칩
- 처리 중 네트워크 표시 없음

### 미리보기

- 유형 선택: 일정·할 일·메모
- 제목 편집
- 유형에 맞는 날짜·시간·알림 필드
- `이 표현을 기억` 옵션은 사용자가 유형을 변경했을 때만 표시
- 기억 옵션을 켜면 후보 키워드 중 하나를 선택해야 함
- 취소와 저장 버튼

### 일정

- 이번 범위에서는 로컬 일정 목록을 읽지 않는다.
- 빠른 입력 또는 `새 일정` 버튼으로 캘린더 Insert 화면을 연다.
- 캘린더 권한 없이 동작한다.

### 할 일

- 진행 중·완료 필터
- 완료 체크
- 마감일 표시
- 항목 추가는 빠른 입력으로 연결

### 메모

- 최신순 목록
- 제목과 본문 일부 표시
- 항목 추가는 빠른 입력으로 연결

### 설정

- 기본 일정 길이: 초기값 60분
- 기본 알림: 초기값 30분 전
- 저장 전 확인: 이번 버전에서는 항상 켜짐이며 비활성화할 수 없음
- 학습 규칙 목록과 삭제
- AI 사용: 기본 꺼짐, 이번 버전에서는 설명만 표시

## 11. 캘린더 연결

`CalendarGateway`는 `Intent.ACTION_INSERT`와 `CalendarContract.Events.CONTENT_URI`를 사용한다.

- 제목, 시작 시각, 종료 시각을 인텐트에 넣는다.
- 알림 시간은 캘린더 앱이 지원하는 경우 사용자가 최종 확인한다.
- 앱은 외부 캘린더 화면에서 실제 저장 여부를 추정하지 않는다.
- 처리 가능한 캘린더 앱이 없으면 `사용 가능한 캘린더 앱이 없습니다`를 표시하고 미리보기 내용을 유지한다.
- 직접 읽기·쓰기 권한은 요청하지 않는다.

## 12. AI 확장 경계

로컬 분류기는 다음 인터페이스를 사용한다.

```kotlin
interface LocalInputClassifier {
    fun classify(
        text: String,
        now: ZonedDateTime,
        learnedRules: List<LearnedRule>
    ): ClassificationResult
}
```

향후 네트워크 AI는 별도 비동기 인터페이스로 추가한다.

```kotlin
interface OptionalFallbackClassifier {
    suspend fun classify(text: String, now: ZonedDateTime): ClassificationResult?
}
```

기본 구현은 항상 `RuleBasedClassifier`다. 향후 AI 기능이 켜져 있고 로컬 결과가 `LOW`일 때만 `OptionalFallbackClassifier`를 호출할 수 있다. AI 호출 실패, 네트워크 없음, API 한도 초과 시 로컬 결과와 수동 선택 화면으로 즉시 복귀한다.

앱의 핵심 데이터는 AI 응답 형식이나 특정 공급자에 의존하지 않는다. 오프라인 코어는 `INTERNET` 권한 없이도 빌드되고 동작한다.

## 13. 오류 처리

- 빈 입력: `내용을 입력해 주세요` 표시
- 500자를 초과한 입력: 500자 제한 안내
- 과거 날짜: 저장 비활성화와 날짜 수정 안내
- 일정 필수 필드 누락: 누락 필드 강조
- 캘린더 앱 없음: 미리보기 유지와 오류 토스트
- Room 저장 실패: 입력과 미리보기를 유지하고 재시도 버튼 표시
- 중복 저장 버튼 연타: 저장 중 버튼 비활성화
- 학습 키워드 후보가 없거나 선택하지 않음: 규칙은 저장하지 않고 항목만 정상 저장
- 저장된 학습 유형 값이 잘못됨: 해당 규칙을 분류에 적용하지 않음

## 14. 테스트 전략

### 단위 테스트

- 날짜·시간 파서의 상대 날짜, 요일, 오전·오후, 알림 표현
- 일정·할 일·메모 예제별 분류 점수와 신뢰도
- 동점과 낮은 점수의 `AMBIGUOUS` 처리
- 제목 추출
- 학습 후보 키워드 추출·정규화·완전 토큰 일치
- 학습 규칙 적용과 삭제
- 과거 날짜와 필수 필드 검증

### 데이터 테스트

- Room 인메모리 데이터베이스에서 할 일 저장·완료·조회
- 메모 저장·최신순 조회
- 학습 규칙의 키워드 고유성
- 잘못된 `targetTypeName`을 안전하게 무시하는지 확인

### UI 테스트

- 입력 → 미리보기 → 할 일 저장
- 입력 → 분류 수정 → 키워드 선택 → 학습 규칙 저장
- 일정 미리보기 → Calendar Insert 인텐트 생성
- 필수 필드 누락 시 저장 버튼 비활성화
- 저장 중 버튼을 반복해서 눌러도 중복 생성되지 않음

### CI

GitHub Actions에서 다음을 순서대로 실행한다.

```text
gradle testDebugUnitTest
gradle assembleDebug
```

두 명령이 성공한 경우에만 APK 아티팩트를 업로드한다.

## 15. 완료 기준

- 비행기 모드에서 할 일과 메모를 분류·저장·조회할 수 있다.
- `내일 오후 3시 병원`이 일정 미리보기로 표시된다.
- `금요일까지 보고서 제출`이 마감일이 있는 할 일로 표시된다.
- `프로젝트 아이디어: 발표 순서를 바꾸기`가 메모로 표시된다.
- 낮은 신뢰도의 입력은 자동 저장되지 않는다.
- 사용자가 변경한 키워드 규칙이 다음 입력에 반영되고 설정에서 삭제 가능하다.
- 캘린더 앱이 설치된 기기에서는 확인 화면이 열리고, 없는 기기에서는 앱이 종료되지 않는다.
- OpenAI 키와 인터넷 없이 모든 완료 기준을 충족한다.

## 16. 구현 선행 조건

- v0.2 PR의 채팅 탭 높이 문제를 수정한다.
- v0.2 PR의 GitHub Actions가 성공한 상태에서 `main`에 병합한다.
- 오프라인 코어 구현은 병합된 `main`에서 별도 기능 브랜치로 시작한다.
