# Changelog

All notable changes to this project will be documented in this file.

The newest release goes first. The release format follows semantic versioning and the tag rules in [docs/release.md](./docs/release.md).

## Unreleased

### Summary

- Pending release notes.

### Breaking Changes

- None.

### New

- None.

### Fix

- None.

### Notes

- None.

## 1.0.0 - 2026-04-01

### Summary

- Initial public release of Plugin Policy Engine.

### Breaking Changes

- None.

### New

- Plugin-friendly policy evaluation core.
- Spring Boot auto-configuration starter.
- JSON file-backed `FlagStore` implementation.
- Web gating support through `@FeatureGate`.
- Deterministic rollout and variant evaluation.

### Fix

- None.

### Notes

- Maven Central coordinates use the `plugin-policy-engine-*` artifact prefix.

## 1.0.6 - 2026-04-01

### Summary

- Align publish workflow with the exact Maven Central secret names.

### Breaking Changes

- None.

### New

- Workflow now consumes `MAVEN_CENTRAL_*` secrets.

### Fix

- Central Portal publish auth and signing wiring updated to the provided secret names.

### Notes

- Use `v1.0.6` for the release tag.

## 1.0.5 - 2026-04-01

### Summary

- Retry publish with dual secret-name support for Central Portal credentials.

### Breaking Changes

- None.

### New

- Workflow accepts `MAVENCENTRAL_*` or `OSSRH_*` secret names.

### Fix

- Publish workflow now tolerates both secret naming conventions.

### Notes

- Use `v1.0.5` for the release tag.

## 1.0.4 - 2026-04-01

### Summary

- Retry publish after refreshing Central Portal credentials.

### Breaking Changes

- None.

### New

- No code changes. Release metadata updated for the new publish attempt.

### Fix

- Publish retry with `v1.0.4`.

### Notes

- Use `v1.0.4` for the release tag.

## 1.0.3 - 2026-04-01

### Summary

- Retry publish after rotating to the new release tag and refreshed Central Portal secrets.

### Breaking Changes

- None.

### New

- No code changes. Release metadata updated for the new publish attempt.

### Fix

- Publish retry with `v1.0.3`.

### Notes

- Use `v1.0.3` for the release tag.

## 1.0.2 - 2026-04-01

### Summary

- Rebuild release after migrating publish flow to the Central Portal compatibility API.

### Breaking Changes

- None.

### New

- Central Portal upload step in GitHub Actions.
- Release Drafter config path fix.

### Fix

- Maven Central publish flow updated from legacy OSSRH endpoint to Central Portal compatibility endpoint.

### Notes

- Use `v1.0.2` for the release tag.

## 1.0.1 - 2026-04-01

### Summary

- Retry release after Maven Central package conflict on `1.0.0`.

### Breaking Changes

- None.

### New

- No code changes. Release metadata and versioning were updated for a new publish attempt.

### Fix

- Maven Central publish version bumped to avoid immutable coordinate conflict.

### Notes

- Use `v1.0.1` for the release tag.
