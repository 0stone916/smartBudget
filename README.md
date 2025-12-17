# 📌 SmartBudget – 개인 예산 관리 웹 서비스 (React + Spring Boot)

SmartBudget은 React와 Spring Boot로 구축한 개인 예산 관리 서비스로, 프론트·백엔드 분리형 Fullstack 구조 + JWT 인증 기반 서비스입니다.


## 🔧 사용 기술 스택

`Front-End` 
- React

- Axios

- Chart.js, react-chartjs-2
  
`Back-end`

- Spring Boot

- MyBatis

`Database`

- MySQL

`Auth`

- JWT

- BCryptPasswordEncoder









## 🚀 주요 기능

### 1. 회원가입 / 로그인

- JWT 기반 인증/인가 구현
- Access Token + Refresh Token 구조 도입
- Redis를 활용한 **서버 기준 세션 관리**
- **단일 세션 강제** (재로그인 시 이전 세션 즉시 만료)
- Access Token 만료 시 **자동 재발급**
- Refresh 실패 시에만 로그아웃 처리

---

### 2. 예산 / 지출 관리 (CRUD)

- 예산·지출 등록, 수정, 삭제, 조회
- 예산 등록 시 **년·월·카테고리 중복 방지 로직** 구현
- **Optimistic Lock(version 기반)** 을 적용하여 동시 수정 충돌 방지

---

### 3. 공통 응답 규격 설계

- **공통 ApiResponse 구조** 적용
- 성공/실패 응답을 통합 포맷으로 관리하여
    
    프론트엔드와의 일관된 통신 구조 확립
    

---

## 🔐 JWT 인증 구조 개선 및 설계 의사결정

### 1. 기존 JWT 인증 구조와 한계

프로젝트 초기에는 다음과 같은 **단순 JWT 인증 구조**를 사용했다.

- 로그인 성공 시 JWT 발급
- 클라이언트(sessionStorage)에 JWT 저장
- API 요청 시 Authorization 헤더로 JWT 전달
- 서버에서는 JWT의 **서명·만료 여부만 검증**

이 구조는 구현이 단순하고 빠르지만, 실제 서비스 관점에서는 명확한 한계가 있었다.

### ❌ 문제점 정리

1. **서버가 세션 상태를 전혀 알 수 없음**
    - JWT는 stateless 구조
    - 로그아웃해도 서버는 토큰을 계속 유효하다고 인식
2. **단일 세션 제어 불가**
    - 동일 계정으로 여러 기기 동시 로그인 가능
    - 이전 로그인 세션을 강제로 종료할 방법이 없음
3. **토큰 탈취 대응 한계**
    - 토큰이 유출되면 만료 전까지 무조건 유효

→ 단순 JWT 구조만으로는 **보안·제어·운영 측면에서 부족**하다고 판단했다.

---

### 2. 인증/세션 관점의 개선 방향 설정

위 문제를 해결하기 위해, 인증 구조를 다음과 같은 방향으로 개선하고자 했다.

1. **서버 기준 세션 관리**
    - 서버가 “현재 유효한 토큰”을 직접 판단하도록 설계
2. **단일 세션 강제**
    - 재로그인 시 기존 세션 즉시 무효화
3. **사용자 경험 개선**
    - Access Token 만료 시 자동 재발급
4. **보안 강화**
    - 서버 기준 Refresh Token 단일 유효성 관리

이에 따라 JWT + Redis 기반의 **단일 세션 인증 구조**를 도입했다.

---

### 3. 단일 세션 도입 후 발견한 **동시성 문제의 한계**

프로젝트 초기에는 동시 수정 충돌의 주요 원인 또한

**동일 계정의 중복 로그인**에서 발생한다고 판단했다.

이에 따라 단일 세션 강제 정책을 적용하면서

재로그인 시 기존 세션을 즉시 무효화하도록 구현했다.

하지만 단일 세션 제어는

**“누가 접근할 수 있는가”** 는 해결하지만,

**“데이터가 동시에 수정되는 상황”** 까지 완전히 제어할 수는 없었다.

예를 들어 동일 브라우저의 다중 탭에서 거의 동시에 발생한 수정 요청은 단일 세션 정책만으로는 충돌을 방지할 수 없었다.

---

### 4. 데이터 계층까지 확장한 최종 설계 (Optimistic Lock)

이에 따라 인증/세션 계층과 데이터 계층의 책임을 분리하여 설계를 확장했다.

- **인증 계층**
    - JWT + Redis 기반 단일 세션 강제
    - 사용자 접근 및 세션 유효성 관리
- **데이터 계층**
    - Optimistic Lock 적용
    - 동시 수정 충돌을 데이터 수준에서 감지 및 차단

이를 통해 인증 단계에서는 접근을 통제하고,

데이터 수정 단계에서는 **정합성을 보장하는 이중 방어 구조**를 완성했다.

---

## 5. 개선된 JWT 인증 구조 개요

### 🔹 토큰 구성

| 토큰 | 역할 |
| --- | --- |
| Access Token | API 인증용 (짧은 만료 시간) |
| Refresh Token | Access Token 재발급용 (긴 만료 시간) |

### 🔹 저장 구조

- **클라이언트**
    - sessionStorage: Access Token / Refresh Token
- **서버 (Redis)**
    - userId → Access Token
    - userId → Refresh Token

➡ 서버가 **모든 유효 토큰을 직접 관리**하는 구조

---

## 6. 로그인 흐름 (단일 세션 핵심)

1. 사용자가 ID / 비밀번호로 로그인 요청
2. 서버에서 사용자 인증 성공
3. Access Token + Refresh Token 발급
4. Redis에 토큰 저장
    - 기존 토큰이 있으면 **덮어쓰기**
5. 클라이언트에 토큰 반환

### ✔ 결과

- 동일 계정으로 다시 로그인하면
    
    → 이전 Access / Refresh Token 즉시 무효화
    
- **단일 세션 보장**

---

## 7. API 요청 시 인증/인가 처리 흐름

### 인증 필터 동작 방식

1. Authorization 헤더에서 Access Token 추출
2. JWT 서명 및 만료 검증
3. 토큰에서 userId 추출
4. Redis에 저장된 Access Token과 비교
5. 불일치 시 401 Unauthorized 반환

### ✔ 효과

- 로그아웃된 토큰 즉시 차단
- 다른 기기 로그인 시 이전 세션 자동 만료
- 서버 기준으로 **실시간 세션 검증 가능**

---

## 8. Access Token 만료 처리 (자동 재발급)

### 프론트엔드 처리 방식

- Axios 인터셉터에서 401 응답 감지
- Refresh Token으로 `/auth/refresh` 요청
- Refresh 성공 시:
    - 새 Access Token 발급
    - 실패한 요청 자동 재시도
- Refresh 실패 시:
    - 세션 만료로 판단 → 로그아웃 처리

### ✔ 사용자 경험 개선

- 토큰 만료로 인한 강제 로그아웃 최소화
- 다중 API 요청 중에도 refresh 요청은 **1회만 수행**

---

## 9. 로그아웃 처리

1. 클라이언트에서 `/auth/logout` 호출
2. 서버에서 Redis의 Access / Refresh Token 삭제
3. 이후 동일 토큰으로 요청 시 모두 401 반환

➡ **서버 기준 강제 로그아웃 구현**

---

## 10. 보안 설정 개선

- JWT 서명 키(`JWT_SECRET`)를
    - `application.yml` 하드코딩 방식에서
    - **`.env` 환경변수 기반 관리 방식으로 개선**
- Git에 민감 정보 노출 방지

---

## 📄 전체 기능 화면 예시
<img width="800" height="702" alt="image" src="https://github.com/user-attachments/assets/c5123f57-5699-464b-86f4-9f54b3c6d25b" />

<img width="800" height="825" alt="image" src="https://github.com/user-attachments/assets/216ebff1-e1af-4c02-9361-51800c6625eb" />

<img width="800" height="643" alt="image" src="https://github.com/user-attachments/assets/29ebcac2-6828-4e0a-9c13-a4708ead851d" />


