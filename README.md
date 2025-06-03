# SU-Talk Backend

이 프로젝트는 SU-Talk(수톡) 중고거래 플랫폼의 백엔드 서버입니다.  
Spring Boot 기반이며, 데이터베이스는 AWS의 MariaDB RDS를 사용합니다.

---

## 📚 기술 스택

- **Backend:** Spring Boot
- **Database:** MariaDB (Amazon RDS)
- **Deployment:** Docker, AWS EC2
- **CI/CD:** GitHub Actions + Docker 기반 자동 배포

---

## 🚩 주의 사항

데이터베이스 접속 정보는 로컬이나 AWS Secrets Manager 등 별도의 보안 환경에서 관리하시기 바랍니다.  
EC2 SSH 키 및 RDS 비밀번호 등 민감 정보는 GitHub Secrets로 관리합니다.

---

## 🔧 프로젝트 DB 연결 예시 (`application.properties`)

```properties
spring.datasource.url=jdbc:mariadb://[엔드포인트]:3306/[데이터베이스이름]
spring.datasource.username=[사용자명]
spring.datasource.password=[비밀번호]
spring.jpa.hibernate.ddl-auto=update
spring.jpa.database-platform=org.hibernate.dialect.MariaDBDialect
spring.jpa.show-sql=true
```

---

# 📁 프로젝트 구조
현재 프로젝트의 기본 폴더 구조는 다음과 같습니다.

```
 src
├── main
│   ├── java
│   │   └── com.example.kdt
│   │       ├── controller  - API 요청 처리
│   │       ├── service     - 비즈니스 로직 구현
│   │       ├── repository  - 데이터 접근 계층
│   │       └── entity      - 데이터베이스 엔티티 클래스
│   └── resources
│       └── application.properties - 설정 파일
└── test
    └── java - 테스트 코드 작성
```
---

# 🚀 배포 구성
GitHub Actions를 통해 main 브랜치 푸시 시 EC2 서버에 자동 배포

Docker로 패키징된 Spring Boot 애플리케이션 실행

탄력적 IP를 통한 EC2 고정 접근 경로 유지

EC2 SSH Key, 접속 정보 등은 GitHub Actions Secrets로 관리

---

# 🚧 향후 작업 계획
비즈니스 로직 추가 개발

API 테스트 진행 (Postman, RestDocs 등)

ERD 기반 Entity 보완 및 연관관계 최적화

프론트와 연동하여 전체 서비스 흐름 통합

---

# 📌 개발 현황

```
✅ AWS RDS 데이터베이스 설정 완료
✅ Entity 및 Repository 구성 완료
✅ Dockerfile 작성 및 Docker 빌드 테스트 완료
✅ GitHub Actions를 통한 EC2 자동 배포 구성 완료
✅ EC2 인스턴스 연결 및 Docker 기반 배포 성공
🔜 비즈니스 로직 구현 및 API 테스트

```
