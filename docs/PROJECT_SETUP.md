# 프로젝트 초기 세팅 가이드

## 1. 프로젝트 기본 정보

| 항목 | 설정값 |
|------|--------|
| 프로젝트명 | mini-3-back |
| 패키지명 | com.mini3.backend |
| Java | 17 |
| Spring Boot | 3.5.0 |
| Build Tool | Gradle (Groovy) |
| DB | MariaDB |
| ORM | Spring Data JPA |
| 인증 | Spring Security + JWT (JJWT) |
| 포트 | 8080 |

---

## 2. 의존성 목록 (build.gradle)

| 의존성 | 용도 |
|--------|------|
| spring-boot-starter-web | REST API |
| spring-boot-starter-data-jpa | JPA/Hibernate |
| spring-boot-starter-security | 인증/인가 |
| spring-boot-starter-validation | 요청 검증 (@Valid) |
| mariadb-java-client | MariaDB 드라이버 |
| lombok | 보일러플레이트 제거 |
| jjwt-api / jjwt-impl / jjwt-jackson (0.12.6) | JWT 토큰 |
| spring-boot-starter-test | 테스트 |
| spring-security-test | Security 테스트 |

---

## 3. 로컬 개발 환경 세팅

### 3-1. JDK 17 설치 확인

```bash
java -version
# openjdk version "17.x.x" 이면 OK
```

### 3-2. MariaDB 준비

**방법 A: 로컬 MariaDB**
```sql
CREATE DATABASE mini3;
```

**방법 B: Docker**
```bash
docker run -d --name mini3-mariadb \
  -p 3306:3306 \
  -e MARIADB_ROOT_PASSWORD=root \
  -e MARIADB_DATABASE=mini3 \
  mariadb:11
```

### 3-3. 환경변수 설정

application.yml에 비밀번호를 직접 넣지 않았음. 환경변수로 주입:

**방법 A: IntelliJ Run Configuration**
1. 우측 상단 실행 버튼 옆 `Mini3BackApplication` 클릭
2. Edit Configurations
3. Environment variables에 추가:
```
DB_USERNAME=root;DB_PASSWORD=본인비밀번호
```

**방법 B: .env 파일 (gitignore에 포함됨)**

프로젝트 루트에 `.env` 파일 생성:
```
DB_USERNAME=root
DB_PASSWORD=본인비밀번호
```

> ⚠️ `.env` 파일은 `.gitignore`에 포함되어 있어 GitHub에 올라가지 않음

### 3-4. 프로젝트 실행

```bash
./gradlew bootRun
```

또는 IntelliJ에서 `Mini3BackApplication.java` → 초록 ▶ 버튼 클릭

---

## 4. application.yml 설정 설명

```yaml
spring:
  datasource:
    url: jdbc:mariadb://localhost:3306/mini3  # DB 주소
    username: ${DB_USERNAME:root}             # 환경변수, 없으면 root
    password: ${DB_PASSWORD:}                 # 환경변수, 없으면 빈값

  jpa:
    hibernate:
      ddl-auto: update          # 엔티티 변경 시 테이블 자동 반영 (개발용)
    properties:
      hibernate:
        format_sql: true        # SQL 로그 보기 좋게 출력
        default_batch_fetch_size: 100  # N+1 방지용 배치 사이즈
    open-in-view: false         # OSIV 끔 (성능 이슈 방지)
```

---

## 5. 패키지 구조

```
src/main/java/com/mini3/backend
├── Mini3BackApplication.java        ← 진입점 (건드리지 말 것)
├── domain                           ← 각자 담당 도메인
│   ├── auth/                        ← 최혜인
│   ├── approval/                    ← 최혜인
│   ├── employee/                    ← 윤형진
│   ├── department/                  ← 윤형진
│   ├── attendance/                  ← 김민희
│   ├── leave/                       ← 김민희
│   ├── ai/                          ← 심소담
│   └── ats/                         ← 심소담
└── global                           ← 공통 (팀 협의)
    ├── config/
    ├── security/
    ├── exception/
    ├── response/
    └── util/
```

각 도메인 안에는 아래 하위 패키지가 있음:
```
controller/    ← API 엔드포인트
service/       ← 비즈니스 로직
repository/    ← DB 접근
entity/        ← JPA 엔티티
dto/           ← 요청/응답 객체
```
> ai 도메인은 외부 API 호출 위주라 repository, entity 없음

---

## 6. Git 브랜치 전략

```
main              ← 배포용 (직접 push 금지)
  └── develop     ← 개발 통합 브랜치
        ├── feature/auth-login        ← 최혜인
        ├── feature/employee-crud     ← 윤형진
        ├── feature/ai-analysis       ← 심소담
        └── feature/attendance-check  ← 김민희
```

### 작업 흐름
```bash
# 1. develop에서 feature 브랜치 생성
git checkout develop
git pull origin develop
git checkout -b feature/내작업명

# 2. 작업 후 커밋
git add .
git commit -m "feat: 기능 설명"

# 3. push 후 PR 생성
git push origin feature/내작업명
# GitHub에서 develop으로 PR 생성
```

---

## 7. 주의사항

- `Mini3BackApplication.java` 수정하지 말 것
- `global/` 패키지는 개인이 임의로 수정하지 말고 팀 협의 후 작업
- 남의 도메인 패키지에 파일 만들지 말 것
- application.yml에 비밀번호 하드코딩 절대 금지
- `.gitkeep` 파일은 해당 폴더에 실제 파일이 생기면 삭제해도 됨
