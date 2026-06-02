# 구현 가이드

## 원칙

- 정책을 평가하고 `PolicyDecision`을 반환합니다.
- HTTP request, filter, controller, gateway route, 조직 header 해석은 저장소 밖에서 처리합니다.
- 예시 코드는 핵심 조립 방식만 보여줍니다. 
- HTTP adapter, DI container, import 문은 실행 계층에서 준비합니다.

## 의존성 선택

```gradle
dependencies {
    implementation("io.github.jho951:plugin-policy-engine-core:<version>")
    implementation("io.github.jho951:plugin-policy-engine-api:<version>")
    implementation("io.github.jho951:plugin-policy-engine-config:<version>")
}
```

### 선택 기준

| 필요한 기능                 | 추가할 모듈                        |
|------------------------|-------------------------------|
| 평가 모델과 엔진              | `plugin-policy-engine-core`   |
| 애플리케이션 클라이언트 계약       | `plugin-policy-engine-api`    |
| JSON file store와 기본 조립 | `plugin-policy-engine-config` |

### 2. 메모리 저장소

```java
InMemoryPolicyStore store = new InMemoryPolicyStore();
store.put(PolicyDefinition.builder("checkout.newFlow")
    .enabled(true)
    .rolloutPercent(50)
    .targeting(Targeting.builder()
        .allowGroup("beta")
        .requireAttrIn("region", new HashSet<String>(Arrays.asList("KR", "JP")))
        .build())
    .variant("A", 50)
    .variant("B", 50)
    .defaultVariant("A")
    .build());

PolicyEngineClient policies = PolicyEngineClientFactory.create(store);

PolicyContext ctx = PolicyContext.builder()
    .userId("user-1")
    .group("beta")
    .attr("region", "KR")
    .build();

PolicyDecision decision = policies.evaluate("checkout.newFlow", ctx);
boolean allowed = decision.allowed();
String variant = decision.variant();
```

1계층 밖에서 결정해야 하는 것:

- 정책 키를 어느 API, command, job에 연결할지
- request/header/session 값을 어떻게 `PolicyContext`로 바꿀지
- OFF일 때 어떤 응답이나 fallback을 사용할지
- 평가 결과를 어떻게 로깅하거나 metric으로 남길지

### 3. JSON File Store

```java
PolicyEngineClient policies = PolicyEngineClientFactory.create(
    PolicyEngineConfig.builder()
        .store(PolicyEngineConfig.Store.FILE)
        .filePath("/etc/app/policies.json")
        .cacheTtl(Duration.ofSeconds(3))
        .build()
);
```

Map 형태:

```json
{
  "checkout.newFlow": {
    "enabled": true,
    "rolloutPercent": 20,
    "defaultVariant": "A",
    "targeting": {
      "allowUserIds": ["admin-01"],
      "denyUserIds": ["blocked-01"],
      "allowGroups": ["beta-testers"],
      "denyGroups": ["suspended"],
      "requireAttrsIn": {
        "region": ["KR", "JP"],
        "plan": ["PRO"]
      }
    },
    "variants": [
      { "name": "A", "weight": 50 },
      { "name": "B", "weight": 50 }
    ]
  }
}
```

List 형태:

```json
[
  {
    "key": "checkout.newFlow",
    "enabled": true,
    "rolloutPercent": 20,
    "defaultVariant": "A"
  }
]
```

### 4. 평가 순서

`PolicyEngine.evaluate`는 아래 순서로 평가합니다.

1. `POLICY_NOT_FOUND`: 스토어에 정책 정의가 없으면 OFF
2. `POLICY_DISABLED`: `enabled=false`면 OFF
3. `TARGET_DENY`: `denyUserIds` 또는 `denyGroups` 매칭 시 OFF
4. `TARGET_ALLOW`: `allowUserIds` 또는 `allowGroups` 매칭 시 ON
5. `TARGET_MISS`: 타겟팅 조건(`requireAttrsIn`) 미충족 시 OFF
6. `ROLLOUT_OUT`: 롤아웃 버킷 탈락 시 OFF
7. `ROLLOUT_IN`: 롤아웃 통과 시 ON

### 5. Rollout과 Variant

- rollout 기준값은 `userId` 우선, 없으면 `attrs["anonId"]`를 사용합니다.
- 기준값이 없으면 안전하게 OFF 처리합니다.
- 해시는 SHA-256 기반으로 계산되어 동일 입력에 대해 결정론적입니다.
- `variants`가 비어 있으면 `defaultVariant`를 사용합니다.
- `variants`가 있으면 각 `weight` 합계를 기준으로 버킷을 나눕니다.
- `weight` 총합이 0 이하이면 `defaultVariant`를 사용합니다.
