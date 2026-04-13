# 모듈 가이드

## 종류

- `plugin-policy-engine-core`: 플래그 평가 모델과 엔진
- `plugin-policy-engine-api`: 애플리케이션 연동 facade
- `plugin-policy-engine-config`: 순수 Java 구성 API와 JSON file store

## 읽는 법

- 평가 규칙과 모델을 먼저 이해하려면 `plugin-policy-engine-core`부터 보면 됩니다.
- 애플리케이션 코드가 의존할 최소 API가 필요하면 `plugin-policy-engine-api`를 보면 됩니다.
- 기본 조립 방식이나 JSON 파일 저장소가 필요하면 `plugin-policy-engine-config`를 보면 됩니다.
- 실제 조립 예시는 [구현 가이드](./implementation-guide.md)를 보면 됩니다.

## 책임 경계

- `plugin-policy-engine-core`
  - `FlagContext`, `FlagDefinition`, `FlagDecision`, `Targeting`, `FlagStore`, `FeatureFlagService`를 제공합니다.
  - rollout과 variant 계산은 동일 입력에 대해 결정론적으로 동작해야 합니다.
  - HTTP, DI, 저장소 구현 세부사항을 알면 안 됩니다.
- `plugin-policy-engine-api`
  - `FeatureFlagClient` facade를 제공합니다.
  - 실행 계층은 가능하면 이 모듈의 계약에 의존합니다.
- `plugin-policy-engine-config`
  - `FeatureFlagConfig`, `FeatureFlagClientFactory`, `JsonFileFlagStore`를 제공합니다.
  - 기본 조립은 제공하지만 framework 자동구성은 포함하지 않습니다.

## 1계층으로 유지하는 기준

- 포함 가능: 평가 모델, rollout/variant 알고리즘, `FlagStore` SPI, memory/file store, 순수 Java factory
- 포함 불가: 서비스 URL 정책, 조직 헤더 규약, 도메인 권한 모델, 프로젝트별 기본 플래그 정책
- framework integration은 generic이어도 1계층 본체가 아니라 adapter 계층으로 분리합니다.

## 배포 대상

- `plugin-policy-engine-core`
- `plugin-policy-engine-api`
- `plugin-policy-engine-config`
