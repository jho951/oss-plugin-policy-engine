# 아키텍처

## 책임

- `plugin-policy-engine-core`: 정책 평가에서 공통으로 쓰는 모델과 평가 엔진
- `plugin-policy-engine-api`: 실행 계층이 의존하는 얇은 facade
- `plugin-policy-engine-config`: 순수 Java 구성 API와 기본 저장소 조립

## 원칙

- 정책 엔진은 기능 공개 여부와 variant 선택에 집중합니다.
- URL, HTTP request, filter, controller, gateway route 해석은 이 저장소 밖에서 처리합니다.
- 기본 구현은 제공하되, 서비스의 비즈니스 fallback을 결정하지 않습니다.
- 특정 URL, 특정 조직 헤더, 특정 service boundary 규칙을 하드코딩하지 않습니다.
- Spring, Servlet, WebFlux 같은 framework integration을 포함하지 않습니다.
- 1계층 확장은 정책 평가 기능 자체를 늘리는 방향이어야 합니다.

## 평가 흐름

1. 애플리케이션이 `FlagContext`를 구성합니다.
2. `FeatureFlagClient`가 기능 키와 컨텍스트를 받습니다.
3. `FeatureFlagService`가 `FlagStore`에서 `FlagDefinition`을 조회합니다.
4. disabled, deny, allow, eligibility, rollout, variant 순서로 평가합니다.
5. 애플리케이션이 `FlagDecision`을 보고 비즈니스 fallback을 결정합니다.
