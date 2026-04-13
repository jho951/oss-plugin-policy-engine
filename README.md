# plugin-policy-engine

[![Build](https://github.com/jho951/plugin-policy-engine/actions/workflows/build.yml/badge.svg)](https://github.com/jho951/plugin-policy-engine/actions/workflows/build.yml)
[![Maven Central](https://img.shields.io/maven-central/v/io.github.jho951/plugin-policy-engine-core?label=maven%20central)](https://central.sonatype.com/search?q=jho951)
[![License](https://img.shields.io/badge/license-MIT-blue)](./LICENSE)
[![Tag](https://img.shields.io/github/v/tag/jho951/plugin-policy-engine)](https://github.com/jho951/plugin-policy-engine/tags)

## 공개 좌표

- `io.github.jho951:plugin-policy-engine-core`
- `io.github.jho951:plugin-policy-engine-api`
- `io.github.jho951:plugin-policy-engine-config`

## 무엇을 제공하나

- `plugin-policy-engine-core`: 플래그 정의, 평가 컨텍스트, 평가 결과, 타겟팅/롤아웃 엔진
- `plugin-policy-engine-api`: 애플리케이션이 의존하는 `FeatureFlagClient` facade
- `plugin-policy-engine-config`: 순수 Java 구성 API, JSON file store, 기본 client factory

## 책임 경계

이 저장소는 1계층 정책 평가 기능만 제공합니다.

### 포함

- 기능 플래그 평가 모델
- allow/deny 타겟팅
- 속성 기반 eligibility
- 결정론적 rollout
- variant 가중치 선택
- `FlagStore` SPI와 범용 구현
- 순수 Java 조립 API

### 포함 X

- 특정 서비스 URL 정책
- 특정 조직 헤더 규약
- gateway/internal boundary 규칙
- 도메인 권한 모델
- Spring, Servlet, WebFlux 같은 framework integration

## 빠른 시작

```gradle
repositories {
    mavenCentral()
}

dependencies {
    implementation("io.github.jho951:plugin-policy-engine-core:<version>")
    implementation("io.github.jho951:plugin-policy-engine-api:<version>")
    implementation("io.github.jho951:plugin-policy-engine-config:<version>")
}
```

## 문서

- [docs/README.md](docs/README.md)
- [구현 가이드](docs/implementation-guide.md)
- [기여 가이드](CONTRIBUTING.md)
