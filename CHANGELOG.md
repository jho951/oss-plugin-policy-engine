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
