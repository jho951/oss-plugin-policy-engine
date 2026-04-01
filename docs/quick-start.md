# 빠른 시작

이 문서는 플러그인 가능한 정책 엔진을 Spring Boot 또는 순수 Java로 붙이는 방법을 설명합니다.

## Spring Boot

### 1) 의존성 추가

```gradle
dependencies {
    implementation("io.github.jho951:plugin-policy-engine-config:1.0.6")
}
```

### 2) `application.yml` 설정

```yaml
pluginpolicyengine:
  store: MEMORY      # MEMORY | FILE
  cache-ttl: 3s      # FILE 스토어 캐시 TTL
  file-path: /etc/app/flags.json
```

### 3) 코드 사용

#### `FeatureFlagClient`를 직접 사용하는 경우

```java
@RestController
public class CheckoutController {
    private final FeatureFlagClient flags;

    public CheckoutController(FeatureFlagClient flags) {
        this.flags = flags;
    }

    @GetMapping("/checkout")
    public String checkout(@RequestHeader("X-User-Id") String userId) {
        var ctx = FlagContext.builder()
                .userId(userId)
                .group("beta")
                .attr("region", "KR")
                .build();

        boolean enabled = flags.isEnabled("checkout.newFlow", ctx);
        String variant = flags.variant("checkout.uiTest", ctx, "A");

        if (!enabled) return "OLD_CHECKOUT";
        return "B".equals(variant) ? "NEW_CHECKOUT_B" : "NEW_CHECKOUT_A";
    }
}
```

#### `@FeatureGate`로 HTTP 진입점을 게이팅하는 경우

```java
@RestController
public class PublicUserController {

    @FeatureGate("features.public-user-api")
    @GetMapping("/public/users")
    public String getUsers() {
        return "visible";
    }
}
```

기본 구현은 `X-User-Id`, `X-Region` 헤더를 읽어 `FlagContext`를 구성합니다. 서비스별 인증 체계에 맞춰 `FeatureFlagContextResolver` 빈을 직접 구현해 교체하는 방식을 권장합니다.

## Pure Java

### InMemory 예시

```java
var store = new InMemoryFlagStore();
store.put(FlagDefinition.builder("checkout.newFlow")
        .enabled(true)
        .rolloutPercent(50)
        .build());

var svc = new FeatureFlagService(store);
var ctx = FlagContext.builder().userId("u-1").build();

boolean enabled = svc.isEnabled("checkout.newFlow", ctx);
```

### File Store 예시

```java
var store = new JsonFileFlagStore("/etc/app/flags.json", Duration.ofSeconds(3));
var svc = new FeatureFlagService(store);
```
