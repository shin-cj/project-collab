# Project Collab

여러 사용자가 프로젝트와 작업을 함께 관리하는 협업 서비스입니다.

프로젝트마다 사용자의 역할이 달라질 수 있으며, `OWNER`, `ADMIN`, `MEMBER` 역할에 따라 프로젝트와 작업에 대한 권한을 구분했습니다.

## 실행 방법

### 실행 환경

- Java 17
- Spring Boot 3.3.13
- Gradle 8.10.2

별도의 데이터베이스 설치 없이 H2 인메모리 DB를 사용합니다. 실행하면 테이블과 기능 확인용 초기 데이터가 생성됩니다.

빌드:

```bash
./gradlew clean build
```

실행:

```bash
./gradlew bootRun
```

- Swagger UI: http://localhost:8080/swagger-ui/index.html
- H2 Console: http://localhost:8080/h2-console

H2 Console 접속 정보:

```text
JDBC URL: jdbc:h2:mem:projectcollab
User Name: sa
Password: 입력하지 않음
```

## 초기 데이터와 확인 순서

| 사용자 ID | 이름 | 이메일 | 프로젝트 역할 |
|---:|---|---|---|
| 1 | 신재형1 | shin1@example.com | OWNER |
| 2 | 신재형2 | shin2@example.com | ADMIN |
| 3 | 신재형3 | shin3@example.com | MEMBER |

- 프로젝트 ID `1`: Project Collab 개발
- 작업 2개: 사용자 API 구현, 프로젝트 권한 테스트

실행 직후 Swagger UI에서 다음 순서로 기능을 확인할 수 있습니다.

1. `GET /api/users`로 사용자 3명 조회
2. `GET /api/projects?requesterId=1`로 OWNER가 참여한 프로젝트 조회
3. `GET /api/projects/1/members?requesterId=1`로 역할별 멤버 조회
4. `GET /api/projects/1/tasks?requesterId=1`로 작업 2개 조회
5. `GET /api/projects/1/tasks?requesterId=1&keyword=사용자&status=TODO&page=0&size=10`으로 검색, 상태 필터, 페이징 확인

H2 인메모리 DB를 사용하므로 애플리케이션을 종료하면 데이터는 사라지고, 다시 실행하면 위 데이터가 새로 생성됩니다.

## API

인증은 구현 범위가 아니므로 요청자 ID를 `requesterId` 쿼리 파라미터로 전달합니다.

### 사용자

| Method | Endpoint | 설명 |
|---|---|---|
| POST | `/api/users` | 사용자 등록 |
| GET | `/api/users` | 사용자 목록 조회 |
| GET | `/api/users/{userId}` | 사용자 단건 조회 |

사용자 등록 요청:

```json
{
  "name": "신재형",
  "email": "user@example.com"
}
```

### 프로젝트

| Method | Endpoint | 권한 | 설명 |
|---|---|---|---|
| POST | `/api/projects?requesterId={id}` | 사용자 | 프로젝트 생성 |
| GET | `/api/projects?requesterId={id}` | 사용자 | 내가 참여한 프로젝트 목록 |
| GET | `/api/projects/{projectId}?requesterId={id}` | 프로젝트 멤버 | 프로젝트 상세 조회 |
| PUT | `/api/projects/{projectId}?requesterId={id}` | OWNER, ADMIN | 프로젝트 수정 |
| DELETE | `/api/projects/{projectId}?requesterId={id}` | OWNER | 프로젝트 삭제 |

프로젝트 생성 요청:

```json
{
  "name": "신규 프로젝트",
  "description": "프로젝트 설명"
}
```

프로젝트를 생성한 사용자는 자동으로 `OWNER` 멤버가 됩니다.

### 프로젝트 멤버

| Method | Endpoint | 권한 | 설명 |
|---|---|---|---|
| GET | `/api/projects/{projectId}/members?requesterId={id}` | 프로젝트 멤버 | 멤버 목록 조회 |
| POST | `/api/projects/{projectId}/members?requesterId={id}` | OWNER, ADMIN | 멤버 추가 |
| PATCH | `/api/projects/{projectId}/members/{memberId}/role?requesterId={id}` | OWNER, ADMIN | 역할 변경 |
| DELETE | `/api/projects/{projectId}/members/{memberId}?requesterId={id}` | OWNER, ADMIN | 멤버 제거 |

멤버 추가 요청:

```json
{
  "userId": 3,
  "role": "MEMBER"
}
```

역할 변경 요청:

```json
{
  "role": "ADMIN"
}
```

마지막 `OWNER`는 역할을 변경하거나 프로젝트에서 제거할 수 없습니다.

### 작업

| Method | Endpoint | 권한 | 설명 |
|---|---|---|---|
| POST | `/api/projects/{projectId}/tasks?requesterId={id}` | 프로젝트 멤버 | 작업 생성 |
| GET | `/api/projects/{projectId}/tasks/{taskId}?requesterId={id}` | 프로젝트 멤버 | 작업 단건 조회 |
| GET | `/api/projects/{projectId}/tasks?requesterId={id}` | 프로젝트 멤버 | 작업 목록 조회 |
| PUT | `/api/projects/{projectId}/tasks/{taskId}?requesterId={id}` | 담당자, OWNER, ADMIN | 작업 수정 |
| DELETE | `/api/projects/{projectId}/tasks/{taskId}?requesterId={id}` | 담당자, OWNER, ADMIN | 작업 삭제 |

작업 상태는 다음 네 가지입니다.

```text
TODO, IN_PROGRESS, REVIEW, DONE
```

작업 생성 요청:

```json
{
  "title": "작업 제목",
  "description": "작업 설명",
  "assigneeId": 3
}
```

작업 목록은 다음 파라미터를 지원합니다.

| 파라미터 | 필수 | 설명 |
|---|---|---|
| `requesterId` | O | 요청 사용자 ID |
| `keyword` | X | 제목 검색, 대소문자 구분 없음 |
| `status` | X | 작업 상태 필터 |
| `page` | X | 페이지 번호, 기본값 0 |
| `size` | X | 페이지 크기, 기본값 20 |
| `sort` | X | 정렬 조건 |

예시:

```http
GET /api/projects/1/tasks?requesterId=1&keyword=사용자&status=TODO&page=0&size=10
```

작업 수정 요청:

```json
{
  "title": "수정된 작업 제목",
  "description": "수정된 설명",
  "assigneeId": 3,
  "status": "IN_PROGRESS",
  "version": 0
}
```

작업 응답 예시:

```json
{
  "id": 1,
  "projectId": 1,
  "title": "사용자 API 구현",
  "description": "사용자 등록 및 조회 API 구현",
  "status": "TODO",
  "assigneeId": 3,
  "assigneeName": "신재형3",
  "version": 0,
  "createdAt": "2026-08-25T19:00:00",
  "updatedAt": "2026-08-25T19:00:00"
}
```

## 권한 처리

권한은 프로젝트와 사용자의 연결 정보인 `ProjectMember`에서 확인합니다. 같은 사용자도 프로젝트별로 다른 역할을 가질 수 있습니다.

| 기능 | 허용 역할 |
|---|---|
| 프로젝트 생성 | 모든 사용자 |
| 프로젝트 수정 | OWNER, ADMIN |
| 프로젝트 삭제 | OWNER |
| 멤버 관리 | OWNER, ADMIN |
| 프로젝트·멤버 조회 | 프로젝트 멤버 |
| 작업 생성·조회 | 프로젝트 멤버 |
| 작업 수정·삭제 | 담당자, OWNER, ADMIN |

프로젝트에 참여하지 않은 사용자는 프로젝트 상세, 멤버, 작업을 조회할 수 없습니다. 작업을 조회할 때도 항상 `projectId` 조건을 함께 사용해 다른 프로젝트의 작업이 섞이지 않게 했습니다.

## 동시 수정 처리

작업 엔티티에 JPA의 `@Version`을 적용했습니다. 클라이언트는 작업을 조회했을 때 받은 `version`을 수정 요청에 포함해야 합니다.

먼저 처리된 요청으로 버전이 변경된 뒤 이전 버전으로 다시 수정하면 `409 Conflict`를 반환합니다.
마지막 요청이 앞선 변경을 조용히 덮어쓰게 두는 대신, 사용자에게 충돌을 알리고 최신 데이터를 다시 조회하도록 하는 방식을 선택했습니다.
작업 내용이 사라지는 상황을 막는 것이 더 중요하다고 판단했습니다.

## 오류 응답

오류 응답은 한 가지 형식으로 통일했습니다.

```json
{
  "timestamp": "2026-08-25T19:00:00",
  "status": 400,
  "error": "Bad Request",
  "code": "VALIDATION_ERROR",
  "message": "요청 값이 올바르지 않습니다.",
  "path": "/api/users",
  "fieldErrors": [
    {
      "field": "email",
      "rejectedValue": "wrong-email",
      "message": "올바른 이메일 형식이 아닙니다."
    }
  ]
}
```

주요 응답 코드는 다음과 같습니다.

- `400 Bad Request`: 입력값 오류 또는 마지막 OWNER 변경 시도
- `403 Forbidden`: 프로젝트 접근 권한 부족
- `404 Not Found`: 사용자, 멤버 또는 작업을 찾을 수 없음
- `409 Conflict`: 중복 데이터 또는 작업 수정 충돌

## 설계 결정

### 도메인별 패키지 구성

사용자, 프로젝트, 작업을 기준으로 패키지를 나누고 각 도메인 안에서 API, 애플리케이션, 도메인, 인프라 계층을 구분했습니다.
기능을 찾기 쉽고 한 도메인의 변경 범위를 좁히기 위한 구성입니다.

### 단방향 연관관계

`ProjectMember`와 `Task`에서 필요한 방향으로만 `ManyToOne` 관계를 두었습니다.
양방향 관계를 많이 만들면 JSON 직렬화와 연관관계 관리가 복잡해질 수 있어 현재 범위에서는 사용하지 않았습니다.

### DTO 사용

엔티티를 API 응답으로 직접 반환하지 않고 요청·응답 DTO를 따로 두었습니다.
지연 로딩 객체가 그대로 노출되는 문제를 막고 API 형식이 엔티티 구조에 직접 의존하지 않도록 했습니다.

### 권한 확인 위치

별도 인증 시스템이 없기 때문에 컨트롤러에서 `requesterId`를 받고, 서비스 계층에서 프로젝트 멤버 여부와 역할을 확인합니다.
컨트롤러마다 권한 코드를 반복하지 않고 트랜잭션 안에서 대상 데이터와 함께 검사할 수 있도록 했습니다.

## 사용 기술과 선택 이유

| 기술 | 사용 이유 |
|---|---|
| Java 17 | 과제 요구 버전이며 record 등 기본 언어 기능을 사용할 수 있음 |
| Spring Boot 3.3 | 웹, 검증, JPA 설정을 일관되게 구성하기 위해 사용 |
| Spring Data JPA | 기본 CRUD와 페이징, 조건 조회를 간결하게 구현하기 위해 사용 |
| H2 | 별도 DB 설치 없이 실행 직후 기능을 확인하기 위해 사용 |
| Springdoc OpenAPI | 구현된 API를 Swagger UI에서 바로 확인하고 호출하기 위해 사용 |
| JUnit 5, MockMvc | 서버를 별도로 띄우지 않고 API와 권한 규칙을 함께 검증하기 위해 사용 |
| React | 과제의 선택 프론트엔드 요구사항에 맞춰 최소한의 작업 관리 화면을 구현하기 위해 사용 |
| Vite | 복잡한 설정 없이 React 소스를 빌드하고 결과물을 Spring 정적 리소스에 포함하기 위해 사용 |

현재 규모에서는 QueryDSL, Redis, 메시지 큐, Docker 등을 추가하지 않았습니다.
검색 조건이 단순하고 데이터가 인메모리에 있으므로 기술을 더 추가하는 것보다 기본 JPA 쿼리와 트랜잭션을 명확하게 유지하는 편이 낫다고 판단했습니다.

## 테스트

```bash
./gradlew test
```

다음 내용을 포함한 통합 테스트 9개가 통과합니다.

- 프로젝트 생성자의 OWNER 등록
- 비멤버 프로젝트 접근 차단
- MEMBER의 프로젝트 수정 차단
- 마지막 OWNER 역할 변경 차단
- 입력값 검증 오류 형식
- 작업 검색, 상태 필터, 페이징
- 담당자가 아닌 MEMBER의 작업 수정 차단
- 오래된 작업 버전 수정 충돌
- Spring 애플리케이션 컨텍스트 실행

## 프론트엔드

선택 항목으로 작업 목록을 확인하는 간단한 React 화면을 추가했습니다. React 빌드 결과를 Spring Boot 정적 리소스에 포함했기 때문에 프론트엔드를 따로 실행할 필요가 없습니다.

`./gradlew bootRun`으로 백엔드를 실행한 뒤 아래 주소로 접속합니다.

접속 주소: http://localhost:8080

이름과 이메일로 사용자를 등록할 수 있으며, 프로젝트 ID와 요청자 ID를 입력해 작업 목록 조회, 검색, 상태 필터, 생성, 상태 변경, 삭제를 확인할 수 있습니다. 프로젝트 멤버 관리는 Swagger UI에서 확인합니다.

프론트엔드 소스를 수정한 경우에만 아래 명령으로 Spring 정적 리소스를 다시 생성합니다.

```bash
cd frontend
npm install
npm run build
```

## 여러 회사의 데이터 분리

여러 회사가 함께 사용하는 서비스로 확장한다면 `Company` 또는 `Tenant` 엔티티를 추가하고, 사용자와 프로젝트를 포함한 분리 대상 테이블에 `tenant_id`를 저장하겠습니다.

요청을 처리할 때 인증된 사용자의 tenant 정보를 확인하고 모든 목록 및 단건 조회 조건에 `tenant_id`를 포함해야 합니다.
이메일 같은 유일성 제약도 전체 서비스 기준이 아니라 `(tenant_id, email)` 형태의 복합 유일성 제약으로 변경합니다.

조회 조건 누락으로 다른 회사 데이터가 노출되지 않도록 tenant 컨텍스트와 공통 조회 조건을 적용하고, 회사 간 접근 차단 통합 테스트를 추가하겠습니다.
회사별 백업이나 물리적 분리가 필요한 경우에는 회사별 스키마 또는 데이터베이스를 사용하는 방식으로 확장할 수 있습니다.

## 미구현 및 개선 사항

- 인증은 과제 범위에 따라 구현하지 않았습니다. 실제 서비스에서는 로그인 사용자 정보에서 요청자를 식별해야 합니다.
- 현재 H2 인메모리 DB와 `create-drop` 설정을 사용합니다. 운영 환경에서는 PostgreSQL 등의 DB와 Flyway 마이그레이션을 사용하겠습니다.
- 시간이 더 있다면 권한 조합별 테스트, API 문서 설명, 감사 로그를 추가하겠습니다.
