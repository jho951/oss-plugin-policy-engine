# Release Notes

## Tag Rules

이 프로젝트는 `vMAJOR.MINOR.PATCH` 형식을 기본 릴리스 태그로 사용합니다.

- 예시: `v1.0.6`, `v1.1.0`, `v2.0.0`
- 허용: `v1.0.6-beta.1`, `v1.0.6-rc.1`, `v1.0.6+build.42`

## Release Note Template

태그를 찍기 전에 아래 항목을 정리합니다. GitHub Release 본문은 [.github/RELEASE_TEMPLATE.md](../.github/RELEASE_TEMPLATE.md)를 기준으로 작성합니다.

### Summary

- 무엇이 바뀌었는지 한 줄로 적습니다.

### Breaking Changes

- 호환성에 영향을 주는 변경이 있으면 적습니다.

### New

- 새 기능, 새 스토어, 새 정책, 새 스타터를 적습니다.

### Fix

- 버그 수정, 성능 개선, 안정성 개선을 적습니다.

### Notes

- 배포 전 확인해야 할 설정, 마이그레이션, 운영 주의사항을 적습니다.

## GitHub Release Example

```md
# v1.0.6

## Summary
- Initial public release of Plugin Policy Engine.

## Breaking Changes
- None.

## New
- Plugin-friendly policy evaluation core.
- Spring Boot auto-configuration starter.
- JSON file-backed `FlagStore` implementation.

## Fix
- None.

## Notes
- Maven Central coordinates use the `plugin-policy-engine-*` artifact prefix.
```

## Release Flow

1. 변경 내용을 `main`에 병합합니다.
2. 릴리스 노트를 작성합니다.
3. `vMAJOR.MINOR.PATCH` 태그를 생성합니다.
4. 태그를 push합니다.
5. GitHub Actions `Publish Plugin Policy Engine To Maven Central` 워크플로 결과를 확인합니다.
