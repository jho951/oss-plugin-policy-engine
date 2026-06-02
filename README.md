# plugin-policy-engine

[![Build](https://github.com/jho951/plugin-policy-engine/actions/workflows/build.yml/badge.svg)](https://github.com/jho951/plugin-policy-engine/actions/workflows/build.yml)
[![Maven Central](https://img.shields.io/maven-central/v/io.github.jho951/plugin-policy-engine-core?label=maven%20central)](https://central.sonatype.com/search?q=jho951)
[![License](https://img.shields.io/badge/license-Apache%202.0-blue)](./LICENSE)
[![Tag](https://img.shields.io/github/v/tag/jho951/plugin-policy-engine)](https://github.com/jho951/plugin-policy-engine/tags)

## 공개 좌표

- `io.github.jho951:plugin-policy-engine-core`
- `io.github.jho951:plugin-policy-engine-api`
- `io.github.jho951:plugin-policy-engine-config`

## 모듈

- `plugin-policy-engine-core`: 정책 정의, 평가 컨텍스트, 평가 결과, 타겟팅/롤아웃 엔진
- `plugin-policy-engine-api`: 애플리케이션이 의존하는 `PolicyEngineClient` 계약
- `plugin-policy-engine-config`: 순수 Java 구성 API, JSON file store, 기본 클라이언트 팩토리

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
[README](docs/README.md)
