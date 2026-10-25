# 트러블슈팅

## 1. `PolicyEngineClientFactory.create(config)`에서 filePath 오류가 난다

### 원인
- `PolicyEngineConfig.Store.FILE`을 선택했지만 `filePath`가 비어 있습니다.

### 조치
- `filePath("/path/to/policies.json")`를 설정합니다.
- 파일 저장소가 필요 없으면 `PolicyEngineConfig.memory()`를 사용합니다.

## 2. 정책이 항상 OFF로 평가된다

### 원인
- `PolicyStore`에서 해당 key를 찾지 못했습니다.
- `enabled=false`로 정의되어 있습니다.
- rollout 비율이 1~99%인데 기준값인 `userId`나 `attrs["anonId"]`가 없습니다.
- rollout bucket이 대상 비율 밖입니다.

### 조치
- `PolicyDecision.reason()` 값을 먼저 확인합니다.
- 정책 key와 JSON 정의를 확인합니다.
- rollout을 쓰는 경우 `PolicyContext.userId()` 또는 `anonId`를 넣습니다.

## 3. 타겟팅 조건을 만족하지 못한다

### 원인
- `allowUserIds`, `allowGroups`, `requireAttrsIn` 값과 `PolicyContext` 값이 일치하지 않습니다.
- `denyUserIds` 또는 `denyGroups`가 먼저 매칭되었습니다.

### 조치
- `PolicyContext.groups()`와 `PolicyContext.attrs()` 구성을 확인합니다.
- deny 규칙은 allow 규칙보다 우선한다는 점을 확인합니다.

## 4. variant가 기대와 다르다

### 원인
- variant 선택은 key와 `userId` 또는 `anonId` 기준으로 결정론적으로 계산됩니다.
- `variants`가 비어 있거나 weight 합계가 0 이하이면 `defaultVariant`를 반환합니다.

### 조치
- 같은 사용자에게 같은 variant가 나오는 것은 정상입니다.
- weight와 `defaultVariant` 값을 확인합니다.

## 5. JSON 파일을 바꿨는데 반영되지 않는다

### 원인
- `JsonFilePolicyStore`의 `cacheTtl` 동안 기존 캐시를 사용하고 있습니다.
- 파일 수정 시간이 바뀌지 않았을 수 있습니다.

### 조치
- 테스트에서는 `Duration.ZERO`를 사용합니다.
- 운영에서는 필요한 반영 주기에 맞춰 `cacheTtl`을 조정합니다.
- 애플리케이션에서 정책 파일을 갱신했다면 원자적 교체가 끝난 뒤 `JsonFilePolicyStore.invalidate()`를 호출합니다. 다음 조회에서 파일을 다시 읽습니다.
- `SEOUL_YEYAK` 연동은 1초 캐시를 사용하고 관리자 갱신 직후 캐시를 비웁니다. 다른 서버나 외부 프로세스의 변경은 캐시 만료 후 반영됩니다.

## 6. JSON 파싱 실패를 바로 알 수 없다

### 원인
- 기본 `JsonFilePolicyStore(String, Duration)`는 이전 버전과 동일하게 파일 없음이나 파싱 실패를 빈 맵으로 처리합니다.

### 조치
- 파일 오류를 요청 차단 및 장애 로그로 연결해야 한다면 `new JsonFilePolicyStore(filePath, ttl, true)`로 엄격 모드를 사용합니다. 파일 없음·읽기 실패는 `IllegalStateException`, 잘못된 JSON 형식이나 객체가 아닌 항목 및 불리언이 아닌 `enabled` 값은 `IllegalArgumentException`으로 전달됩니다.
- 엄격 모드를 사용할 때는 `PolicyEngineClientFactory.create(store)`로 생성한 저장소를 클라이언트에 전달합니다. `PolicyEngineConfig.file(...)`은 기본 모드로 저장소를 생성합니다.

## 7. `NoSuchMethodError` 또는 새 저장소 기능을 찾을 수 없다

### 원인
- 애플리케이션에 이전 `plugin-policy-engine-config` JAR가 남아 있거나 버전이 다른 JAR가 함께 들어 있습니다.

### 조치
- `plugin-policy-engine-core`, `plugin-policy-engine-api`, `plugin-policy-engine-config`를 모두 `3.0.1`로 맞추고 기존 `3.0.0` JAR를 제거합니다.
- `SEOUL_YEYAK`에서는 `WEB-INF/lib`의 세 JAR 버전을 확인한 뒤 애플리케이션을 다시 빌드합니다.

## 8. 테스트 시작 전에 Gradle의 Groovy 초기화가 실패한다

### 원인
- JDK 26으로 Gradle 5.5.1을 실행하는 등 Gradle 실행 버전과 JDK가 호환되지 않습니다. Java 8 대상 컴파일 설정은 Gradle 자체의 실행 JDK를 바꾸지 않습니다.

### 조치
- 기본 `./gradlew`는 Gradle 8.14.3이며 JDK 8로 실행합니다.
- Gradle 9를 직접 사용할 때는 JDK 17 이상을 선택합니다. JDK 26으로 Gradle을 실행해야 한다면 9.4.0 이상을 사용하고, 컴파일과 테스트에는 JDK 8 도구체인을 사용합니다.

## 9. `docs`가 빌드에 포함되지 않았다는 오류가 난다

### 원인
- Gradle을 `docs` 디렉터리에서 실행해 해당 디렉터리가 프로젝트 디렉터리로 선택되었습니다. 빌드 루트는 상위 디렉터리의 `settings.gradle`입니다.

### 조치
- 저장소 루트에서 `./gradlew check`를 실행합니다.
- `docs` 디렉터리에 머물러야 한다면 `../gradlew -p .. check`로 빌드 루트를 지정합니다.
- IntelliJ에서는 공유 실행 설정 **Policy Engine Check**를 선택합니다. 이 설정의 Gradle 프로젝트 경로는 저장소 루트입니다. `docs` 경로로 자동 생성된 임시 실행 설정은 사용하지 않습니다.

## 10. IntelliJ에서 Java 8과 Gradle 9.4.1이 호환되지 않아 동기화가 실패한다

### 원인
- IntelliJ가 프로젝트 JDK 8을 Gradle 실행 JVM으로 선택한 상태에서 Gradle 9.4.1 래퍼를 사용했습니다. Gradle 9.4.1 실행에는 JDK 17 이상이 필요합니다.
- `docs`를 가리키는 임시 Gradle 실행 설정이 있으면 빌드 루트도 잘못 선택됩니다.

### 조치
- 기본 래퍼 8.14.3을 사용하고 IntelliJ의 **Settings → Build, Execution, Deployment → Build Tools → Gradle**에서 **Gradle JVM**을 JDK 8로 설정합니다. 프로젝트 SDK와 Java 언어 수준도 8로 유지합니다.
- Gradle 프로젝트와 실행 설정의 경로를 `docs`가 아닌 저장소 루트로 지정하고 Gradle 프로젝트를 다시 불러옵니다.
- Gradle 9.4.1을 별도로 사용한다면 Gradle JVM을 JDK 17 이상으로 바꾸고 Java 8 도구체인을 설치합니다.
