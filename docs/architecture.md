# 아키텍처

## 책임

- 사용자가 앱에 접속했을 때, 해당 정책 대상을 "on"으로 할지 "off"로 할지, 혹은 어떤 "버전(A/B)"을 보여줄지 판단 (정책 평가 모델)
- Allow(화이트리스트) / Deny(블랙리스트) 타겟팅
- 사용자의 속성(Attributes)을 보고 자격(Eligibility)을 부여
- 점진적 배포(Rollout) 시, 동일한 사용자는 항상 동일한 결과 제공 (결정론적 rollout)
- 하나의 정책 대상에 여러 버전(Variant A, B, C)이 있을 때, 각 버전이 노출될 확률을 설정 (variant 가중치 선택)
- `PolicyStore` SPI(Service Provider Interface)와 범용 구현
- XML이나 복잡한 설정 파일(YAML 등) 없이, 순수한 자바 코드(Fluent API/Builder 패턴)

## 원칙

- 정책 엔진은 정책 허용 여부와 variant 선택에 집중합니다.
- URL, HTTP request, filter, controller, gateway route 해석은 이 저장소 밖에서 처리합니다.
- 기본 구현은 제공하되, 서비스의 비즈니스 fallback을 결정하지 않습니다.
- 특정 URL, 특정 조직 헤더, 특정 service boundary 규칙을 하드코딩하지 않습니다.
- Spring, Servlet, WebFlux 같은 framework integration을 포함하지 않습니다.
- 1계층 확장은 정책 평가 동작 자체를 늘리는 방향이어야 합니다.

## 평가

1. 애플리케이션이 `PolicyContext`를 구성합니다.
2. `PolicyEngineClient`가 정책 키와 컨텍스트를 받습니다.
3. `PolicyEngine`이 `PolicyStore`에서 `PolicyDefinition`을 조회합니다.
4. disabled, deny, allow, eligibility, rollout, variant 순서로 평가합니다.
5. 애플리케이션이 `PolicyDecision`을 보고 비즈니스 fallback을 결정합니다.
