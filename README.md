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

## 빌드 환경

- 라이브러리와 테스트 코드는 Java 8을 대상으로 컴파일합니다.
- 기본 Gradle 래퍼는 8.14.3이며 JDK 8에서 실행합니다.
- Gradle 5.5.1~8.14.x는 JDK 8로 실행할 수 있습니다. CI는 5.5.1, 6.9.4, 7.6.4, 8.14.3과 별도의 JDK 21에서 9.4.1을 확인합니다.
- Gradle 9를 직접 실행할 때는 JDK 17 이상이 필요합니다. JDK 26에서 실행하려면 Gradle 9.4.0 이상을 사용합니다. 컴파일과 테스트에는 JDK 8 도구체인을 사용합니다.

```bash
./gradlew check
```

명령은 저장소 루트에서 실행합니다. `docs` 디렉터리에서는 `../gradlew -p .. check`를 사용합니다.

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

- [문서 목차](./docs/readme.md)
