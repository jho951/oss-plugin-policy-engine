# 모듈

## 종류

| 모듈                            | 필요한 기능                           |
|-------------------------------|----------------------------------|
| `plugin-policy-engine-core`   | 정책 평가 모델과 엔진                    |
| `plugin-policy-engine-api`    | 애플리케이션 연동 클라이언트 계약              |
| `plugin-policy-engine-config` | 순수 Java 구성 API와 JSON file store  |

## 선택 기준

| 필요한 기능                      | 추가할 모듈                        |
|-----------------------------|-------------------------------|
| 평가 규칙과 모델 이해                | `plugin-policy-engine-core`   |
| 애플리케이션 코드가 의존할 최소 API가 필요   | `plugin-policy-engine-api`    |
| 기본 조립 방식이나 JSON 파일 저장소가 필요  | `plugin-policy-engine-config` |

## 책임

- `plugin-policy-engine-core`
  - `PolicyContext`, `PolicyDefinition`, `PolicyDecision`, `Targeting`, `PolicyStore`, `PolicyEngine`을 제공합니다.
  - rollout과 variant 계산은 동일 입력에 대해 결정론적으로 동작해야 합니다.
  - HTTP, DI, 저장소 구현 세부사항을 알면 안 됩니다.
  
- `plugin-policy-engine-api`
  - `PolicyEngineClient` 계약을 제공합니다.
  - 실행 계층은 가능하면 이 모듈의 계약에 의존합니다.
  
- `plugin-policy-engine-config`
  - `PolicyEngineConfig`, `PolicyEngineClientFactory`, `JsonFilePolicyStore`를 제공합니다.
  - 기본 조립은 제공하지만 framework 자동구성은 포함하지 않습니다.
