# 트러블슈팅

## 1. `FeatureFlagClientFactory.create(config)`에서 filePath 오류가 난다

### 원인
- `FeatureFlagConfig.Store.FILE`을 선택했지만 `filePath`가 비어 있습니다.

### 조치
- `filePath("/path/to/flags.json")`를 설정합니다.
- 파일 저장소가 필요 없으면 `FeatureFlagConfig.memory()`를 사용합니다.

## 2. 플래그가 항상 OFF로 평가된다

### 원인
- `FlagStore`에서 해당 key를 찾지 못했습니다.
- `enabled=false`로 정의되어 있습니다.
- rollout 기준값인 `userId`나 `attrs["anonId"]`가 없습니다.
- rollout bucket이 대상 비율 밖입니다.

### 조치
- `FlagDecision.reason()` 값을 먼저 확인합니다.
- 플래그 key와 JSON 정의를 확인합니다.
- rollout을 쓰는 경우 `FlagContext.userId()` 또는 `anonId`를 넣습니다.

## 3. 타겟팅 조건을 만족하지 못한다

### 원인
- `allowUserIds`, `allowGroups`, `requireAttrsIn` 값과 `FlagContext` 값이 일치하지 않습니다.
- `denyUserIds` 또는 `denyGroups`가 먼저 매칭되었습니다.

### 조치
- `FlagContext.groups()`와 `FlagContext.attrs()` 구성을 확인합니다.
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
- `JsonFileFlagStore`의 `cacheTtl` 동안 기존 캐시를 사용하고 있습니다.
- 파일 수정 시간이 바뀌지 않았을 수 있습니다.

### 조치
- 테스트에서는 `Duration.ZERO`를 사용합니다.
- 운영에서는 필요한 반영 주기에 맞춰 `cacheTtl`을 조정합니다.

## 6. JSON 파싱 실패를 바로 알 수 없다

### 원인
- 현재 파일 저장소는 파일 없음이나 파싱 실패 시 빈 맵으로 처리합니다.

### 조치
- 운영 환경에서는 별도 wrapper store를 만들어 로깅, 알림, 이전 캐시 유지 전략을 추가합니다.
