# Publishing

## Goals

이 프로젝트는 Maven Central 배포를 전제로 다음 요건을 갖추도록 구성합니다.

- 명확한 GAV 좌표
- 소스 JAR / Javadoc JAR 생성
- POM 메타데이터(license, developer, scm) 포함
- GPG 서명 지원
- GitHub Actions 기반 릴리스 자동화
- 릴리스 태그 검증과 릴리스 노트 규칙 문서화

## Artifact Coordinates

- `io.github.jho951:plugin-policy-engine-core`
- `io.github.jho951:plugin-policy-engine-api`
- `io.github.jho951:plugin-policy-engine-store-file`
- `io.github.jho951:plugin-policy-engine-config`

브랜딩과 배포 좌표를 `Plugin Policy Engine` 기준으로 통일했습니다.

## Required Secrets

Maven Central 배포용으로 아래 Gradle properties 또는 환경 변수를 준비합니다. 이 값들은 Central Portal User Token의 username/password여야 합니다.

- `ossrhUsername`
- `ossrhPassword`
- `signingKey`
- `signingPassword`

GitHub Packages도 병행할 경우:

- `githubUsername`
- `githubToken`

## Release Checklist

1. GitHub repository name을 `plugin-policy-engine`으로 변경합니다.
2. `build.gradle`의 `url`, `scm`, GitHub Packages URL이 새 저장소명과 일치하는지 확인합니다.
3. Maven Central 배포 시크릿을 GitHub Actions에 등록합니다.
4. `./gradlew clean build`로 전체 빌드를 확인합니다.
5. `./gradlew publishToMavenLocal`로 로컬 배포를 확인합니다.
6. `v1.0.6` 같은 태그를 생성한 뒤 push합니다.
7. `Publish Plugin Policy Engine To Maven Central` 워크플로가 성공하는지 확인합니다.
8. 릴리스 노트는 [Release notes and tag rules](./release.md)를 기준으로 작성합니다.
9. Central Portal에 배포가 보이지 않으면 manual upload step이 실행됐는지 확인합니다.

## Local Verification

```bash
./gradlew clean build
./gradlew publishToMavenLocal
```

서명까지 포함해서 검증하려면:

```bash
./gradlew publish
```

단, 이 경우 서명 키와 원격 저장소 credential이 필요합니다.

## GitHub Actions Release Flow

`v*` 태그를 push하면 `publish-central.yml` 워크플로가 실행됩니다.

예시:

```bash
git tag v1.0.6
git push origin v1.0.6
```

## Notes for Maven Central

- `MAVEN_CENTRAL_USERNAME`, `MAVEN_CENTRAL_PASSWORD`, `MAVEN_CENTRAL_GPG_PRIVATE_KEY`, `MAVEN_CENTRAL_GPG_PASSPHRASE`를 GitHub Secrets에 등록해야 합니다.
- `MAVEN_CENTRAL_USERNAME` / `MAVEN_CENTRAL_PASSWORD`는 Central Portal User Token의 username/password여야 합니다.
- `MAVEN_CENTRAL_GPG_PRIVATE_KEY` / `MAVEN_CENTRAL_GPG_PASSPHRASE`는 서명용 GPG 키입니다.
- GitHub Packages를 병행할 경우 `githubUsername`, `githubToken`도 별도로 준비합니다.
- `publish-central.yml`은 배포 후 Central Portal manual upload endpoint를 호출합니다.

## Notes

- 현재 설정은 `-SNAPSHOT` 버전이면 snapshots 저장소, 그 외는 releases 저장소로 배포합니다.
- Sonatype Central Portal 전환 정책에 따라 저장소 URL이나 토큰 방식은 추후 조정이 필요할 수 있습니다.
