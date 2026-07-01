# 축제로 (FestaRoute) Backend

국내 지역축제 정보를 기반으로 맞춤형 축제 추천, AI 여행 플래너, 커뮤니티, 채팅 기능을 제공하는 Spring Boot 백엔드 프로젝트입니다.

---

## 프로젝트 소개

축제로는 한국관광공사 TourAPI를 활용하여 전국 축제 정보를 제공하고,
사용자의 관심사와 검색 이력을 기반으로 AI 추천 서비스를 제공하는 웹 서비스입니다.

Spring Boot 기반 REST API를 개발하고 Oracle DB, JWT 인증, AI 추천, 외부 API 연동 등을 구현했습니다.

---

## 주요 기능

### 회원

- 회원가입
- 로그인
- JWT 인증
- 소셜 로그인
- 회원정보 수정
- 회원 탈퇴

### 축제

- 축제 목록 조회
- 지역/기간/키워드 검색
- 축제 상세 조회
- 찜 기능
- 후기 및 평점

### AI

- AI 여행 플래너
- 사용자 맞춤 축제 추천
- Gemini Embedding 기반 유사도 검색

### 커뮤니티

- 게시글 CRUD
- 댓글 및 대댓글
- 파일 업로드
- 게시글 신고
- 댓글 신고

### 채팅

- 실시간 채팅
- 채팅방 관리

### 관리자

- 관리자 로그인
- 회원 관리
- 축제 관리
- 게시글 관리
- 댓글 관리
- 신고 관리
- 통계 대시보드

---

## 기술 스택

### Backend

- Java 21
- Spring Boot
- Spring Security
- JWT
- MyBatis

### Database

- Oracle Database
- MongoDB
- Qdrant(Vector DB)

### AI

- Gemini API
- Gemini Embedding
- RAG

### Infrastructure

- Docker
- Nginx
- GCP Compute Engine

### External API

- 한국관광공사 TourAPI
- Kakao Map API
- Kakao Local API
- 기상청 API

---

## 프로젝트 구조

```text
src
├── domains
│   ├── auth
│   ├── member
│   ├── festival
│   ├── planner
│   ├── board
│   ├── review
│   ├── chat
│   ├── admin
│   └── storage
├── common
├── config
└── ProjectApplication.java
