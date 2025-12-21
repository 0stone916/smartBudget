# SmartBudget (개인 프로젝트)

## 🔐 인증 / 보안
- JWT 기반 인증/인가
- Access Token + Refresh Token 구조
- Redis 기반 단일 세션 관리
- 재로그인 시 기존 세션 즉시 만료
- Access Token 자동 재발급 처리

## 💰 예산 / 지출 관리
- 예산·지출 CRUD
- 사용자 + 년·월·카테고리 중복 등록 방지
- Optimistic Lock 기반 동시 수정 충돌 방지

## 🧪 테스트 & 안정성
- 멀티스레드 환경에서 동시 수정 테스트
- 중복 등록 시 DuplicateKeyException 검증

- ## 📘 상세 설계 문서
👉 https://www.notion.so/2cc2c24577cc80638969fa8cf6d240d5
