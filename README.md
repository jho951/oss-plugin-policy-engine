# Plugin Policy Engine

A plugin-friendly Java policy engine for rollout, targeting, and variant evaluation.

![CI](https://github.com/jho951/plugin-policy-engine/actions/workflows/build.yml/badge.svg)
![Release](https://github.com/jho951/plugin-policy-engine/actions/workflows/publish-central.yml/badge.svg)
![License](https://img.shields.io/badge/License-MIT-green)

## At a Glance

- Description: `Plugin Policy Engine is a JVM policy engine for rollout, targeting, and variant evaluation.`
- About: `JVM policy engine with Spring Boot support.`
- Topics: `java`, `spring-boot`, `feature-flags`, `policy-engine`, `maven-central`, `open-source`
- Website: `https://github.com/jho951/plugin-policy-engine`
- Release: signed tags publish to Maven Central via GitHub Actions

## Maven Coordinates

```gradle
dependencies {
    implementation("io.github.jho951:plugin-policy-engine-config:1.0.1")
}
```

Published artifacts:

- `io.github.jho951:plugin-policy-engine-core`
- `io.github.jho951:plugin-policy-engine-api`
- `io.github.jho951:plugin-policy-engine-store-file`
- `io.github.jho951:plugin-policy-engine-config`

## Modules

- `core`: evaluation engine, domain model, `FlagStore`
- `api`: minimal consumer-facing API (`FeatureFlagClient`)
- `store-file`: JSON-backed `FlagStore`
- `config`: Spring Boot auto-configuration starter

## Documentation

- [Docs index](./docs/README.md)
- [Overview](./docs/overview.md)
- [Architecture](./docs/architecture.md)
- [Quick start](./docs/quick-start.md)
- [Spring Boot config](./docs/spring-config.md)
- [Evaluation rules](./docs/evaluation-rules.md)
- [Release notes and tag rules](./docs/release.md)
- [Changelog](./CHANGELOG.md)
- [JSON store format](./docs/json-format.md)
- [Publishing guide](./docs/publishing.md)

## Development

```bash
./gradlew clean build
```

To publish locally:

```bash
./gradlew publishToMavenLocal
```

For Maven Central release setup, see [docs/publishing.md](./docs/publishing.md).

## Release

1. Rename the GitHub repository to `plugin-policy-engine`.
2. Register `OSSRH_USERNAME`, `OSSRH_PASSWORD`, `SIGNING_KEY`, and `SIGNING_PASSWORD` in GitHub Secrets.
3. Run `./gradlew clean build` and `./gradlew publishToMavenLocal`.
4. Tag the release with `vMAJOR.MINOR.PATCH` and push it.
5. Confirm the `Publish Plugin Policy Engine To Maven Central` workflow succeeded.
6. Draft the GitHub Release from [the release template](./.github/RELEASE_TEMPLATE.md) and [changelog](./CHANGELOG.md).
7. Apply the repository metadata guidance in [docs/repository-metadata.md](./docs/repository-metadata.md).

## Contributing

See [CONTRIBUTING.md](./CONTRIBUTING.md).

## Security

See [SECURITY.md](./SECURITY.md).

## License

Released under the [MIT License](./LICENSE).
