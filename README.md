![Java](https://img.shields.io/badge/Java-17-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.3.3-6DB33F?style=for-the-badge&logo=springboot&logoColor=white)
![Spring Data JPA](https://img.shields.io/badge/Spring_Data_JPA-3.3.x-59666C?style=for-the-badge&logo=hibernate&logoColor=white)
![Spring Security](https://img.shields.io/badge/Spring_Security-6DB33F?style=for-the-badge&logo=springsecurity&logoColor=white)
![QueryDSL](https://img.shields.io/badge/QueryDSL-Query-4479A1?style=for-the-badge)
![MySQL](https://img.shields.io/badge/MySQL-8.x-4479A1?style=for-the-badge&logo=mysql&logoColor=white)
![AWS](https://img.shields.io/badge/AWS-EC2%20%7C%20RDS%20%7C%20S3-FF9900?style=for-the-badge&logo=amazonaws&logoColor=white)

# Spring Plus

> `f-api/spring-plus` 과제를 기반으로  
> Spring Boot Todo API에 JPA, QueryDSL, Spring Security, AWS 배포, 대용량 데이터 처리 실습을 반영한 백엔드 프로젝트입니다.

## 프로젝트 개요

이 프로젝트는 단순 CRUD 구현보다, 아래 항목들을 실제 코드와 실행 결과로 확인하는 과제형 프로젝트입니다.

- `@Transactional` 동작 이해와 서비스 계층 수정
- JWT 기반 인증과 닉네임 클레임 반영
- JPQL / QueryDSL 기반 조회 개선
- 테스트 코드 수정
- AOP 수정
- JPA Cascade / N+1 해결
- Spring Security 전환
- EC2 / RDS / S3를 활용한 배포
- JDBC 기반 500만 건 적재 및 조회 성능 비교

## 기술 스택

### Backend
- Java 17
- Spring Boot 3.3.3
- Spring Web
- Spring Data JPA
- Spring Security
- QueryDSL
- Validation

### Database
- MySQL 8.x

### Infra
- AWS EC2
- AWS RDS (MySQL)
- AWS S3
- AWS Systems Manager Parameter Store

### Test
- JUnit 5
- Spring Boot Test
- JDBC Test

## 구현 범위

| 번호 | 내용 | 상태 |
| --- | --- | --- |
| 1 | `@Transactional` 저장 오류 수정 | 완료 |
| 2 | JWT에 nickname 반영 | 완료 |
| 3 | JPQL 기반 Todo 검색 조건 확장 | 완료 |
| 4 | 실패하던 컨트롤러 테스트 수정 | 완료 |
| 5 | AOP 동작 수정 | 완료 |
| 6 | Cascade로 Todo 생성 시 작성자 담당자 등록 | 완료 |
| 7 | Comment 조회 N+1 해결 | 완료 |
| 8 | QueryDSL로 Todo 단건 조회 전환 | 완료 |
| 9 | Spring Security 도입 | 완료 |
| 10 | QueryDSL 기반 검색 API | 완료 |
| 11 | 매니저 등록과 로그 저장 트랜잭션 분리 | 완료 |
| 12 | AWS EC2 / RDS / S3 적용 | 완료 |
| 13 | 대용량 데이터 처리 및 조회 성능 비교 | 완료 |

## 실행 방법

### 1. 요구 사항

- Java 17
- MySQL 8.x
- Gradle 또는 IntelliJ

### 2. 로컬 DB

```sql
CREATE DATABASE spring_plus;
```

### 3. 로컬 설정 예시

`src/main/resources/application.yml`

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/spring_plus?rewriteBatchedStatements=true&cachePrepStmts=true&useServerPrepStmts=false
    username: root
    password: YOUR_PASSWORD
    driver-class-name: com.mysql.cj.jdbc.Driver

  jpa:
    hibernate:
      ddl-auto: update
    show-sql: true

jwt:
  secret:
    key: YOUR_BASE64_SECRET_KEY
```

### 4. 실행

```bash
./gradlew bootRun
```

## 인증 방식

`/auth/signin`, `/auth/signup`, `/health`를 제외한 API는 JWT 인증이 필요합니다.

```http
Authorization: Bearer {token}
```

## 주요 API

| Method | Endpoint | 설명 | 인증 |
| --- | --- | --- | --- |
| POST | `/auth/signup` | 회원가입 | ❌ |
| POST | `/auth/signin` | 로그인 | ❌ |
| GET | `/health` | 헬스 체크 | ❌ |
| GET | `/users/search?nickname={nickname}` | 닉네임 정확 일치 검색 | ✅ |
| POST | `/users/profile-image` | 프로필 이미지 업로드 | ✅ |
| GET | `/users/profile-image` | 프로필 이미지 다운로드 URL 조회 | ✅ |

## 과제별 핵심 구현

### 3. JPQL 기반 Todo 검색 조건 확장

`weather`, `modifiedAt` 시작일, `modifiedAt` 종료일을 모두 optional 하게 받을 수 있도록 JPQL 메서드로 구현했습니다.

관련 코드:

- [TodoRepository.java](src/main/java/org/example/expert/domain/todo/repository/TodoRepository.java)
- [TodoService.java](src/main/java/org/example/expert/domain/todo/service/TodoService.java)

### 5. AOP 수정

관리자 권한 변경 API 호출 전에 로깅이 동작하도록 AOP 포인트컷을 수정했습니다.

적용 대상:

- `UserAdminController.changeUserRole(..)`

관련 코드:

- [AdminAccessLoggingAspect.java](src/main/java/org/example/expert/aop/AdminAccessLoggingAspect.java)

### 8. QueryDSL로 Todo 단건 조회 전환

`TodoService.getTodo()`는 이제 QueryDSL 기반 저장소 메서드로 Todo와 작성자(User)를 함께 조회합니다.
`fetchJoin()`을 사용해 연관 User를 즉시 가져오도록 구성했습니다.

관련 코드:

- [TodoService.java](src/main/java/org/example/expert/domain/todo/service/TodoService.java)
- [TodoRepositoryCustom.java](src/main/java/org/example/expert/domain/todo/repository/TodoRepositoryCustom.java)
- [TodoRepositoryImpl.java](src/main/java/org/example/expert/domain/todo/repository/TodoRepositoryImpl.java)

## 12. AWS 활용

### 12-1. EC2

- EC2 인스턴스에서 Spring Boot 애플리케이션 실행
- Elastic IP 연결 완료
- Health Check API 추가

Health Check URL:
<img width="538" height="223" alt="Image" src="https://github.com/user-attachments/assets/50ee2911-65c9-4082-a6a2-62332fa4e92d" />
<img width="981" height="709" alt="Image" src="https://github.com/user-attachments/assets/405b7018-404e-40a2-bc60-a17fbc58e4b2" />
```text
http://3.39.128.40:8080/health
```

응답:

```json
{
  "status": "up"
}
```

### 12-2. RDS

- MySQL RDS 생성
- EC2와 같은 VPC에 연결
- RDS 보안 그룹 인바운드 `3306` 소스를 EC2 보안 그룹으로 제한
- EC2에서 실행된 애플리케이션이 RDS에 회원 데이터를 저장하는 것 확인

운영 설정은 Parameter Store의 `SPRING_APPLICATION_JSON` 하나로 주입했습니다.
<img width="1008" height="744" alt="Image" src="https://github.com/user-attachments/assets/c945bfc0-15c0-45b5-a734-e53d46637865" />
<img width="970" height="743" alt="Image" src="https://github.com/user-attachments/assets/94aecf76-a462-4a72-8d7e-b304e7eef3bd" />
<img width="923" height="604" alt="Image" src="https://github.com/user-attachments/assets/9c318125-16c0-4d44-abf4-95c663af7b69" />
예시:

```json
{
  "spring": {
    "datasource": {
      "url": "jdbc:mysql://my-database.c5uoymugqebj.ap-northeast-2.rds.amazonaws.com:3306/test?rewriteBatchedStatements=true&cachePrepStmts=true&useServerPrepStmts=false",
      "username": "admin",
      "password": "********",
      "driver-class-name": "com.mysql.cj.jdbc.Driver"
    }
  },
  "jwt": {
    "secret": {
      "key": "********"
    }
  },
  "cloud": {
    "aws": {
      "region": {
        "static": "ap-northeast-2"
      },
      "s3": {
        "bucket": "export-s3-dorayaki-bucket"
      }
    }
  }
}
```

### 12-3. S3

- S3 버킷 생성
- EC2 IAM Role에 S3 접근 정책 연결
- 유저 프로필 이미지 업로드 및 presigned URL 조회 API 구현

S3 관련 구현 파일:

- [UserController.java](src/main/java/org/example/expert/domain/user/controller/UserController.java)
- [UserService.java](src/main/java/org/example/expert/domain/user/service/UserService.java)
- [S3Service.java](src/main/java/org/example/expert/domain/user/service/S3Service.java)
- [User.java](src/main/java/org/example/expert/domain/user/entity/User.java)

구현 API:

#### 업로드
<img width="979" height="762" alt="Image" src="https://github.com/user-attachments/assets/4570723a-091a-40e7-aedc-deb97f712704" />
<img width="966" height="980" alt="image" src="https://github.com/user-attachments/assets/ed936178-e0da-41a6-9e5a-cf31517d1c8a" />
<img width="978" height="1108" alt="image" src="https://github.com/user-attachments/assets/01773d13-ab31-483b-9d1e-9f2235179696" />

```http
POST /users/profile-image
Content-Type: multipart/form-data
Authorization: Bearer {token}
```

응답 예시:

```json
{
  "key": "profiles/uuid_filename.png"
}
```

#### 다운로드 URL 조회

```http
GET /users/profile-image
Authorization: Bearer {token}
```

응답 예시:

```json
{
  "url": "https://..."
}
```

#### 연결 상태 사진
<img width="1280" height="1008" alt="image" src="https://github.com/user-attachments/assets/2b8eca74-a1fe-4766-b3ed-1685c5985c7f" />
<img width="1280" height="535" alt="image" src="https://github.com/user-attachments/assets/7d62d8a1-ca36-4935-bf1d-9954a702af16" />
<img width="1280" height="963" alt="image" src="https://github.com/user-attachments/assets/75a4f065-26a1-4155-af5c-19d2ea5f7931" />
<img width="1280" height="532" alt="image" src="https://github.com/user-attachments/assets/014abff5-67fd-40d2-903e-cc44c4b020fd" />
<img width="1280" height="538" alt="image" src="https://github.com/user-attachments/assets/cbfeb75a-9864-48d1-a88a-a1cffd3f5859" />
<img width="1280" height="418" alt="image" src="https://github.com/user-attachments/assets/fba0ba75-e2db-4fc3-9ece-f69cb891a58d" />
<img width="1280" height="451" alt="image" src="https://github.com/user-attachments/assets/9c33dbee-0342-4979-86e0-ab2107f21cbb" />
<img width="1280" height="578" alt="image" src="https://github.com/user-attachments/assets/aaec28a1-338c-4122-ba74-a9a9a53354b2" />
<img width="1280" height="544" alt="image" src="https://github.com/user-attachments/assets/e06717e1-8d0c-4cbc-9aeb-7355c7c9bc21" />
<img width="1280" height="777" alt="image" src="https://github.com/user-attachments/assets/391b3f5a-ca4e-4943-aa46-6ebeb49bd548" />




## 13. 대용량 데이터 처리

### 구현 전략

13번은 일반 통합 테스트보다 `수동 실행형 성능 실험`에 가깝다고 판단했습니다.

- 500만 건 적재: JDBC 테스트 코드 사용
- 조회 속도 비교: JDBC 직접 조회 + API 조회 병행
- 인덱스 적용 여부에 따른 `EXPLAIN`과 응답 시간 비교

관련 파일:

- [UserBulkInsertTest.java](src/test/java/org/example/expert/domain/user/service/UserBulkInsertTest.java)
- [UserController.java](src/main/java/org/example/expert/domain/user/controller/UserController.java)
- [UserService.java](src/main/java/org/example/expert/domain/user/service/UserService.java)
- [UserRepository.java](src/main/java/org/example/expert/domain/user/repository/UserRepository.java)
- [User.java](src/main/java/org/example/expert/domain/user/entity/User.java)

### 13-1. 500만 건 적재

- JDBC `PreparedStatement` batch insert 사용
- `BATCH_SIZE = 20_000`
- 이메일은 실행 시점 prefix를 붙여 중복 없이 생성
- 닉네임은 해시 혼합 기반 문자열로 생성하여 단순 순번보다 중복 가능성을 낮춤

예시 실행 로그:

```text
Inserted rows: 200000, prefix=bulk20260407052100
...
Bulk insert completed. prefix=bulk20260407052100
```

### 13-2. 닉네임 정확 일치 검색 API

```http
GET /users/search?nickname={nickname}
```

현재 구현은 `nickname` 정확 일치 조건으로 사용자 1건을 조회합니다.

### 13-3. 성능 비교

#### 인덱스 미적용

JDBC `EXPLAIN` 결과:

```text
type=ALL, key=null, rows=5037697, extra=Using where
```

JDBC 조회 시간:

```text
1775ms
```

#### 인덱스 적용 (`idx_users_nickname`)

JDBC `EXPLAIN` 결과:

```text
type=ref, key=idx_users_nickname, rows=1, extra=null
```

JDBC 조회 시간:

```text
16ms
```

### 13-4. 비교 표

| 방법 | EXPLAIN type | 사용 인덱스 | 조회 대상 rows | JDBC 조회 시간 |
| --- | --- | --- | ---: | ---: |
| 인덱스 없음 | `ALL` | `null` | 5,037,697 | 1775ms |
| nickname 인덱스 적용 | `ref` | `idx_users_nickname` | 1 | 16ms |

### 13-5. 개선 포인트

- `nickname` 단일 인덱스 적용
- `rewriteBatchedStatements=true`로 MySQL batch insert 최적화
- `PreparedStatement` batch 처리
- JDBC 직접 측정과 API 측정을 분리해 해석 가능하게 구성
- `EXPLAIN` 결과를 함께 비교해 풀스캔과 인덱스 탐색 차이를 확인

## 트러블슈팅

### 1. 대용량 테스트가 전체 애플리케이션 컨텍스트에 종속되는 문제

초기에는 `@SpringBootTest` 기반 테스트로 작성하여 JWT 설정, Security, AOP까지 같이 로딩됐습니다.
특히 `AdminAccessLoggingAspect`가 `HttpServletRequest`를 생성자 주입받고 있어 대용량 JDBC 테스트와 무관한 이유로 실패했습니다.

해결:

- `UserBulkInsertTest`를 `@JdbcTest` 기반으로 변경
- 롤백 없이 수동 실행 가능한 JDBC 전용 테스트로 분리

### 2. 재실행 시 email unique 충돌

초기 bulk insert 코드는 `user1@test.com` 같은 고정 이메일을 사용해 재실행 시 중복 에러가 발생했습니다.

해결:

- 실행 시점 prefix를 붙여 재실행 가능하도록 수정

### 3. S3 프로필 업로드 API 403

초기에는 `UserController`가 다른 컨트롤러와 달리 구형 `@Auth` 방식 파라미터 주입을 사용하고 있어 Security principal 주입이 맞지 않았습니다.

해결:

- `@AuthenticationPrincipal AuthUser` 방식으로 통일

### 4. 과제 요구사항과 구현 기술의 대응 정리

초기에는 3번 검색 조건 구현을 QueryDSL로 작성했지만, 과제 3번은 JPQL 사용이 요구사항이었습니다.
반대로 과제 8번은 `getTodo()` 단건 조회를 QueryDSL로 전환하는 항목이었습니다.

해결:

- 3번은 JPQL 기반 검색으로 정리
- 8번은 `getTodo()`를 QueryDSL + `fetchJoin()`으로 전환

## AWS 캡처 첨부 위치

아래 항목에 콘솔 캡처를 첨부하면 제출 요구사항을 충족할 수 있습니다.

### EC2
- EC2 인스턴스 요약
- Elastic IP 연결 화면
- EC2 보안 그룹 인바운드 규칙
- Health Check API 호출 화면

### RDS
- RDS 인스턴스 요약
- RDS 연결 및 보안 탭
- RDS 보안 그룹 인바운드 `3306 -> EC2 SG`
- EC2에서 MySQL 접속 후 `SELECT ... FROM users` 결과

### S3
- S3 버킷 생성 화면
- 업로드된 `profiles/...` 객체 목록
- IAM Role에 S3 정책 연결 화면
- Postman 업로드 / presigned URL 응답 화면

## Postman 컬렉션

AWS 검증용 컬렉션:

- [spring-plus-aws-check.postman_collection.json](postman/spring-plus-aws-check.postman_collection.json)

포함 요청:

- `/health`
- `/auth/signup`
- `/auth/signin`
- `/users/profile-image`
- `/users/profile-image`

## 프로젝트 의의

이 프로젝트는 기능 추가 자체보다, 아래를 한 저장소 안에서 직접 확인한 과제였습니다.

- JPA / QueryDSL 기반 도메인 개선
- Spring Security 구조 전환
- AWS EC2 / RDS / S3 연동
- Parameter Store 기반 운영 설정 주입
- JDBC 기반 대량 적재와 조회 성능 비교

즉 단순 CRUD보다 `실행`, `배포`, `성능`, `트러블슈팅`까지 연결해서 다뤘다는 점이 이번 과제의 핵심입니다.
