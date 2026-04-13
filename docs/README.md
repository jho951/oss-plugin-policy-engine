# Docs

## Guide

### 시작할 때

1. [아키텍처](./architecture.md)
2. [모듈 가이드](./modules.md)
3. [구현 가이드](./implementation-guide.md)
4. [SPI/extension 가이드](./extension-guide.md)

### 문제를 만났을 때

1. [트러블슈팅](./troubleshooting.md)

### 모듈과 테스트

1. [테스트/CI 가이드](./testing-and-ci.md)

## 읽는 순서

- 공개 설정 계약은 `oss-contract` 저장소를 봅니다.
- 처음 사용하는 사람은 `아키텍처`, `모듈 가이드`, `구현 가이드`, `SPI/extension 가이드` 순서로 보면 됩니다.
- 테스트를 돌리거나 publish 흐름을 확인할 때는 `테스트/CI 가이드`를 봅니다.
- SPI를 직접 구현하는 경우 `SPI/extension 가이드`를 먼저 보세요.
- Spring, Servlet, WebFlux 같은 adapter는 이 저장소의 순수 1계층 본체에 포함하지 않습니다.
