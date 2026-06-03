# 테스트/CI 가이드

## 로컬 테스트 실행

### 전체 빌드

```bash
./gradlew clean check
```

### 모듈 단위 테스트

```bash
./gradlew :plugin-policy-engine-core:test
./gradlew :plugin-policy-engine-api:test
./gradlew :plugin-policy-engine-config:test
```

## 현재 테스트 범위

- `plugin-policy-engine-core`
  - 정책 평가, 타게팅, 모델 불변성, 저장소 SPI 검증
- `plugin-policy-engine-api`
  - `PolicyEngineClient` 기본 메서드 검증
- `plugin-policy-engine-config`
  - 설정/팩토리, JSON 파일 저장소, 파일 캐시 동작 검증

## 커버리지

- JaCoCo XML/HTML 리포트 생성: `./gradlew jacocoTestReport`
- 품질 게이트 포함 실행: `./gradlew check`
- 최소 line coverage 기준: `coverage_minimum=0.80`

## GitHub Actions

### 현재 워크플로우 파일

- `.github/workflows/build.yml`
- `.github/workflows/publish.yml`

### `build.yml`

- 트리거: `main` 대상 PR, `main` push
- Java: Temurin 8
- 수행:
  1. `./gradlew clean check --no-daemon --stacktrace`
  2. JaCoCo XML/HTML 리포트 artifact 업로드

### `publish.yml`

- 트리거: `v*` 태그 push
- Java: Temurin 8
- 수행:
  1. `./gradlew check --no-daemon --stacktrace`
  2. `./gradlew -Prelease_version="$VERSION" publishToMavenLocal --no-daemon --stacktrace`
  3. Maven Central 배포 전 publication metadata를 검증

## 참고

- CI와 문서는 소스 트리 기준으로 설명합니다.
- generated build 산출물은 문서 기준이 아닙니다.
