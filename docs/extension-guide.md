# SPI 확장 가이드

## 확장 포인트 선택 기준

| 바꾸고 싶은 것 | 구현/교체할 계약 | 계층 경계 |
| --- | --- | --- |
| 플래그 저장소 | `FlagStore` | 플래그 정의 저장/조회까지만 담당 |
| 애플리케이션 facade | `FeatureFlagClient` | 평가 호출 표면까지만 담당 |
| 컨텍스트 생성 | 애플리케이션 adapter | request/header/session 해석은 저장소 밖에서 담당 |
| JSON 파일 저장 | `JsonFileFlagStore` | 파일 읽기와 JSON 파싱까지만 담당 |
| client 조립 | `FeatureFlagClientFactory` | 순수 Java 객체 생성까지만 담당 |

## 주의점

- `FlagStore` 구현은 서비스 URL, HTTP method, 조직 header 이름을 알면 안 됩니다.
- `FeatureFlagClient` 구현은 비즈니스 fallback 응답을 결정하면 안 됩니다.
- rollout 알고리즘을 바꾸는 변경은 기존 사용자에게 같은 입력의 결과를 바꿀 수 있으므로 호환성 영향을 명확히 봐야 합니다.
- 파일 저장소는 파싱 실패, 파일 없음, 여러 인스턴스 배포 상황을 고려해야 합니다.

## 1. `FlagStore`

`FlagStore`는 플래그 정의를 조회하는 최소 SPI입니다.

```java
public final class DatabaseFlagStore implements FlagStore {
    @Override
    public Optional<FlagDefinition> find(String key) {
        return repository.findByKey(key).map(this::toDefinition);
    }

    @Override
    public Map<String, FlagDefinition> findAll() {
        return repository.findAll().stream()
            .collect(Collectors.toMap(FlagDefinition::key, Function.identity()));
    }
}
```

구현해야 하는 것:

- key 기반 조회
- 조회 실패 시 `Optional.empty()`
- 가능하면 전체 조회

1계층 밖에서 결정해야 하는 것:

- DB schema
- cache invalidation 정책
- 운영 배포별 플래그 관리 UI
- audit log 저장 위치

## 2. `FeatureFlagClient`

기본 구현은 `FeatureFlagClientFactory.create(store)`로 충분합니다.
평가 호출 전후에 metric이나 tracing을 붙이고 싶으면 wrapper로 감싸는 방식을 권장합니다.

```java
public final class ObservedFeatureFlagClient implements FeatureFlagClient {
    private final FeatureFlagClient delegate;

    public ObservedFeatureFlagClient(FeatureFlagClient delegate) {
        this.delegate = delegate;
    }

    @Override
    public FlagDecision evaluate(String key, FlagContext ctx) {
        FlagDecision decision = delegate.evaluate(key, ctx);
        metrics.record(key, decision.reason());
        return decision;
    }
}
```

## 3. Context Adapter

`FlagContext`는 순수 입력 모델입니다.
HTTP나 messaging framework에서 어떤 값을 넣을지는 실행 계층이 결정합니다.

```java
FlagContext ctx = FlagContext.builder()
    .userId(currentUser.id())
    .groups(currentUser.groups())
    .attr("region", currentUser.region())
    .attr("plan", currentUser.plan())
    .build();
```

## 권장 책임 분리

- `plugin-policy-engine` 저장소는 플래그 평가, rollout, variant, 저장소 SPI에 집중합니다.
- HTTP adapter, DI 설정, controller annotation, gateway 정책은 애플리케이션이나 상위 계층이 소유합니다.
