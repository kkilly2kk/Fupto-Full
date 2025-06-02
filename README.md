# FUPTO 쇼핑몰 프로젝트

## 프로젝트 개요

FUPTO는 스프링부트 백엔드와 Nuxt.js 프론트엔드를 활용한 풀스택 최저가 쇼핑몰 프로젝트입니다.

## 기술 스택

### 백엔드 (back)

- Java 21
- Spring Boot 3.3.5
- Spring Data JPA
- Spring Security
- JWT 인증 (JJWT 0.11.5)
- OAuth2 (Google, Naver, Kakao 소셜 로그인)
- MariaDB
- H2 Database (테스트 환경)
- Lombok
- ModelMapper

### 프론트엔드 (front)

- Nuxt.js 3.14.0
- Vue.js (최신 버전)
- Vue Router
- JWT 인증 (jwt-decode)

## 프로젝트 구조

```
fupto/
│── back/        # Spring Boot 기반 백엔드 API
│── front/       # Nuxt.js 기반 프론트엔드
│── README.md    # 프로젝트 소개 및 실행 방법
```

## 주요 기능

- 사용자 인증 (JWT 기반)
- 소셜 로그인 (Google, Naver, Kakao)
- 상품 조회 및 관리
- 찜하기 기능
- 찜한 상품 알림 가격 설정 후 알림 받기
- 파일 업로드 기능

## 시스템 설정

### 백엔드

- 서버 포트: 8085
- 컨텍스트 경로: /api/v1
- 파일 업로드 경로: uploads/
- 최대 파일 크기: 100MB (개별), 300MB (총)

### 데이터베이스

- MariaDB (개발 환경)
- H2 (테스트 환경)

## 시작하기

### 백엔드 실행

```bash
# 프로젝트 루트 디렉토리에서
cd back
./mvnw spring-boot:run
```

### 프론트엔드 실행

```bash
# 프로젝트 루트 디렉토리에서
cd front
npm install
npm run dev
```

## 개발 환경 설정

### 백엔드 설정

1. `Back/src/main/resources/application.yml.example`을 복사하여 `application.yml` 생성
2. `Back/src/main/resources/application-dev.yml.example`을 복사하여 `application-dev.yml` 생성
3. `Back/src/main/resources/.env.example`을 복사하여 `.env` 생성

### 백엔드 요구사항

- JDK 21
- Maven
- MariaDB 서버

### 프론트엔드 요구사항

- Node.js (최신 LTS 버전 권장)
- npm 또는 yarn

## API 엔드포인트

백엔드 API는 `http://localhost:8085/api/v1/` 경로로 접근 가능합니다.

## 소셜 로그인 설정

프로젝트에서는 다음 소셜 로그인 서비스를 지원합니다:

- Google
- Naver
- Kakao

각 서비스의 클라이언트 ID와 시크릿은 `application-dev.yml` 파일에서 설정해야 합니다.

## 라이선스

이 프로젝트는 **MIT 라이선스**를 따릅니다.  
자세한 사항은 [`LICENSE`](LICENSE) 파일을 참고하세요.
