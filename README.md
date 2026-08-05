# AI 비서 Android MVP

Kotlin 기반 Android MVP입니다. 채팅, 일정, 할 일, 설정 화면을 포함합니다.

## 현재 기능
- 홈·AI 채팅·일정·할 일·설정 하단 탭
- 탭을 전환해도 유지되는 데모 채팅
- Android 캘린더 일정 추가 화면 연결
- GitHub Actions 단위 테스트 및 디버그 APK 자동 빌드

## Google 캘린더
일정 추가는 Android `CalendarContract.ACTION_INSERT`를 사용합니다. 사용자가 확인한 후 기기에 설치된 Google 캘린더 등의 캘린더 앱에서 최종 저장합니다.

## 자동 APK 빌드
`main` 브랜치에 푸시하거나 Pull Request를 열면 GitHub Actions가 단위 테스트 후 디버그 APK를 빌드합니다. Actions 실행 화면의 Artifacts에서 `ai-assistant-debug-apk`를 내려받으세요.

## 로컬 빌드
Android Studio에서 JDK 17 이상과 Android SDK 35를 설치한 뒤 `Build > Build APK(s)`를 실행하세요.

현재 AI 답변은 UI 데모이며 실제 OpenAI API 호출은 포함하지 않습니다. 다음 단계에서는 AI 없이도 작동하는 오프라인 일정·할 일·메모 자동 분류 기능을 구현합니다.
