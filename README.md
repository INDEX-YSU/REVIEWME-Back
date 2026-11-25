# REVIEWME 백엔드

이메일 인증, 게시글, 댓글, 추천, 프로필 및 채팅 API를 제공합니다.

## Windows에서 로컬 실행

```powershell
.\gradlew.bat test bootJar
java -jar build/libs/reviewme-0.0.1-SNAPSHOT.jar --spring.profiles.active=local
```

`local` 프로필은 127.0.0.1:8080과 파일 기반 H2 데이터베이스 `data/reviewme.mv.db`를 사용합니다. `local,demo`를 지정하면 별도 DB에 시연용 계정과 예제 데이터를 준비합니다. Java 17 이상이 필요하며 Gradle 빌드는 JDK 17 도구 체인을 사용합니다.

- [Swagger UI](http://127.0.0.1:8080/swagger-ui.html)
- [OpenAPI JSON](http://127.0.0.1:8080/v3/api-docs)
- [정적 API 명세](http://127.0.0.1:8080/api_specification.html)

기존 Docker Compose 구성은 MySQL을 사용합니다. 로컬 데모는 위 H2 실행 명령으로 동작합니다.
