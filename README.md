# 📌 SmartBudget – 개인 예산 관리 웹 서비스 (React + Spring Boot)

SmartBudget은 React와 Spring Boot로 구축한 개인 예산 관리 서비스로, 프론트·백엔드 분리형 Fullstack 구조 + JWT 인증 기반 서비스입니다.


## 🔧 사용 기술 스택

Frontend

- React

- Axios

- Chart.js, react-chartjs-2

Backend

- Spring Boot

- MyBatis

Database

- MySQL

Auth

- JWT (Access Token)

- BCryptPasswordEncoder

## 🚀 주요 기능
1) 회원가입 / 로그인

- 로그인 성공 시 서버에서 JWT 발급
- 프론트 엔드(localStorage)에서 JWT 저장 및 자동 전송
- JWT 만료 시 자동 로그아웃 처리

2) 예산/지출 관리(CRUD)

  - 예산/지출 등록, 수정, 삭제, 조회
  
  - 예산의 경우 “년월 별 카테고리 중복 방지” 로직 포함

## 🔐 JWT 인증/인가 구조 (로그인 → API 인증)

### ✔ JWT 생성 

<img  width="800" height="268" alt="image" src="https://github.com/user-attachments/assets/983df21a-7e2b-4579-b78b-f29911e0c5b6" />

- JwtUtil.generateToken()에서 사용자 ID를 기반으로 토큰 발급

- 성공/실패 여부를 통일된 ApiResponse 구조로 반환

- 실패 시 HTTP 상태코드 401 Unauthorized 반환

### ✔ API 요청 시 JWT 검증 및 인가
<img  width="800" height="646" alt="image" src="https://github.com/user-attachments/assets/991e59ae-0846-40f4-bc77-6db80a7546c9" />

- JwtUtil.extractUserId(token) 내부에서
>1. parseClaimsJws() → JWT 파싱
>2. 서명 검증 → 변조 방지
> 3. 만료 검증 → 토큰 유효 시간 확인
- 예외 발생 시 401 Unauthorized 반환

- 성공 시 API 정상 처리

### ✔ Axios 인터셉터로 자동 인증 처리
-  요청 인터셉터에서 JWT 자동 첨부
<img  width="800" alt="image" src="https://github.com/user-attachments/assets/fd893e80-eb42-495a-a1fa-333d33c5c7ce" />


-  401 응답 시 자동 로그아웃

<img  width="800" alt="image" src="https://github.com/user-attachments/assets/e23faac8-0790-46d6-9aee-965e07f03578" />



## 📄 전체 기능 화면 예시
<img width="800" height="702" alt="image" src="https://github.com/user-attachments/assets/c5123f57-5699-464b-86f4-9f54b3c6d25b" />

<img width="800" height="825" alt="image" src="https://github.com/user-attachments/assets/216ebff1-e1af-4c02-9361-51800c6625eb" />

<img width="800" height="643" alt="image" src="https://github.com/user-attachments/assets/29ebcac2-6828-4e0a-9c13-a4708ead851d" />


