# 📌 SmartBudget – 개인 예산 관리 시스템
> **데이터 정합성 보장 및 배치 운영 안정성을 강화한 백엔드 설계**

## 🛠 Tech Stack
- **Back-end**: Java 17, Spring Boot, Spring Batch 5, Redis, MySQL, MyBatis
- **Front-end**: React

## 🧪 테스트 및 안정성 검증
- **동시성 실증 테스트**: 멀티스레드 환경에서 N개 이상의 수정 요청 시 **1건만 성공하고 나머지는 예외 발생**함을 `assertThat`으로 검증
- **중복 등록 차단 검증**: 동일 조건의 예산 등록 시 `DuplicateKeyException` 발생 및 에러 핸들링 여부 테스트
- **배치 멱등성 테스트**: 배치가 중단된 후 다시 실행되어도 데이터가 중복 생성되지 않음을 직접 확인

## 💰 데이터 정합성 (Concurrency Control)
- **Optimistic Lock**: `@Version` 컬럼을 활용한 낙관적 락 적용으로 다중 탭/기기의 동시 수정 충돌 방지
- **복합 유니크 제약**: `사용자ID + 년 + 월 + 카테고리` 조합의 Unique Key 설정을 통해 물리적 데이터 무결성 확보
- **공통 응답 표준화**: `ApiResponse` 규격화 및 `GlobalExceptionHandler`를 통한 중앙 집중식 예외 처리 (409 Conflict 등)

## ⚙️ 운영 자동화 (Spring Batch)
- **전월 기반 예산 자동 생성**: **Spring Batch 5**를 도입하여 매월 1일 사용자별 지출 패턴을 분석한 예산 수립 자동화
- **장애 허용 설계(Fault Tolerance)**: `faultTolerant()` 및 `skip` 정책을 적용하여 특정 데이터 오류 시에도 전체 배치 중단 방지
- **실패 이력 관리**: `BatchSkipListener`를 통해 실패 데이터의 사유를 DB에 기록하여 사후 모니터링 및 복구 기반 마련
- **멱등성 확보**: 재실행 시 데이터 중복을 방지하기 위해 애플리케이션(`exists`)과 DB(`Unique Key`) 수준의 이중 방어 설계

## 🔐 인증 및 보안 (Security)
- **Double Token 구조**: Access Token(단기) 및 Refresh Token(장기)을 활용한 보안 강화
- **Redis 기반 단일 세션 관리**: Redis에 `userId`를 Key로 토큰을 저장하여, **재로그인 시 기존 세션을 즉시 Overwrite(무효화)** 하는 서버 주도 세션 제어 구현
- **자동 재발급 로직**: Axios 인터셉터를 통해 토큰 만료 시 사용자 개입 없이 자동 재발급 (다중 요청 시 Refresh 요청은 1회만 수행되도록 설계)
- **로그아웃 실효성**: 로그아웃 시 Redis 내 토큰 데이터를 즉시 삭제하여 Stateless 환경에서도 실시간 세션 차단 기능 확보
  
## ⚙️ 코드 품질 및 설계 표준화
- **중앙 집중식 예외 처리 (@RestControllerAdvice)**
    - **관심사 분리**: 비즈니스 로직과 예외 처리 로직을 완전히 분리하여 코드 가독성 및 유지보수 효율성 증대 [cite: 104, 105, 172]
    - **커스텀 예외(BusinessException) 설계**: 토큰 만료, 유효성 실패 등 세분화된 예외 상황을 체계적으로 관리하기 위해 자바 표준 상속 구조를 활용한 사용자 정의 예외 계층 구축 
    - **전역 응답 표준화**: `ErrorCode Enum`을 기반으로 프레임워크 예외와 커스텀 예외의 응답 규격을 `ApiResponse`로 단일화하여 프론트엔드 협업 생산성 극대화


## 📘 상세 설계 문서 (Portfolio)
👉 [SmartBudget 노션 상세 페이지](https://www.notion.so/2cc2c24577cc80638969fa8cf6d240d5)
