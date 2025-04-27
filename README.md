# 📚 TalkStock - GitHub Wiki

## 목차
- [프로젝트 개요](#프로젝트-개요)
- [요구사항 문서화](#요구사항-문서화)
- [파트별 진행 상황](#파트별-진행-상황)
- [팀 소개](#팀-소개)

---

## 프로젝트 개요

### 비즈니스 요구사항 파약
- 이동 중 한포토와 사용이 \어너지에서 선수화한 증권정보 조회 가능
- 증권 리포트 요약 + TTS 기능을 통해 핸즈프리 앱 개발
- 칠시자도 이해 가능한 해석 개정 및 편의성 강화

### 기대효과 및 활용부문
- 증권 리포트 진입장론 낮음
- 리포트 소비 시간 단축 및 멀티테스킹 지원
- 투자 정보 접근성 향전

### 사용자 요구사항 정의
- 리포트 목록 표시 (RecyclerView)
- 상세 리포트 보기 및 원본 PDF 제공
- Google TTS를 통한 음성 출력
- 플레이리스트 기능
- 검색 및 추천 기능 제공

---

## 요구사항 문서화

### 요구사항 정의서
- 다양한 사용자 니즈 반영 기능 및 인터페이스 정의  
![요구사항 정의서](https://github.com/user-attachments/assets/d40f57c3-e5ad-48ca-bf76-6a735bc2a48f)

### 메뉴 구조도
- 메인화면, 상세화면, 검색화면, 추천화면 구성  
![메뉴 구조도](https://github.com/user-attachments/assets/ae334544-bde7-4b51-b090-2eb35b6ab4e0)

### 시켄스 다이어그램
- 로그인 및 추천  
![로그인](https://github.com/user-attachments/assets/6a8afaa9-ec07-4dd8-b424-1e23f0f6f99f)
- 리포트 크롤링  
![리포트 크롤링](https://github.com/user-attachments/assets/8ac60a66-cc82-44c8-add3-9a8be9008cf4)
- 검색  
![검색](https://github.com/user-attachments/assets/35e2cc84-1ad5-47a6-b29f-07f96a8a68a8)
- 키워드 추출  
![키워드 추출](https://github.com/user-attachments/assets/f6285aaf-53e6-49b9-8ab2-20b08c802a99)
- 플레이리스트 기능  
![플레이리스트 기능](https://github.com/user-attachments/assets/1096cea0-f116-4a6b-a12f-350e6109303a)  
![플레이리스트 화면2](https://github.com/user-attachments/assets/9cee8b8b-e5d1-4c31-a3ba-a64da9c3dd35)
- 오디오 재생 (TTS)  
![오디오 재생](https://github.com/user-attachments/assets/1e46fe4b-2b07-4a78-8591-755993181da9)

### 시스템 구성도
- Android App → Django 서버 (Azure VM, Docker Compose 배포) → MongoDB
- GitHub Actions + GitHub Runner를 통해 CI/CD 구성  
![시스템 구성도](https://github.com/user-attachments/assets/067d1881-415a-4f9e-b523-bf0e186619da)

### 프로토타입
- 버전별 주요 기능 및 UI 개정 이력  
![프로토타입](https://github.com/user-attachments/assets/b0adafd1-980f-4c73-89d6-51ee00127688)

### 형상관리
- GitHub, Docker, CI/CD 구창
- 첫팩 ELK Stack 등 드라이 포인트 해석 기능 규가 계획

---

## 파트별 진행 상황

### Front-end (김석영, 이재용)
- 초기 UI 프로토타입 → UI 개정 및 기능 가운
- TTS 기능을 이용한 mp3 저장 및 재생 기능
- 오디오 열결들기 기능 구현
- 검색 기능 활성화

### Back-end (조선현, 유안식)
- 네이버 리포트 크롤링
- 리포트 요약 (GPT-4o-mini 사용)
- 키워드 추출 (TF-IDF, KRWordRank 등 비교)
- 추천 알고린 구현

### Infra & PM (이재용, 조선현)
- Azure VM 환경 구축
- GitHub Actions + GitHub Runner를 통한 자동 배포
- 프로젝트 전반 관리, 주간 회의, 문서화 진행

---

## 팀 소개

| 이름 | 전공 | 학번 | 역할 |
| :--: | :--: | :--: | :--: |
| 조선현 | 소프트웨어전공 | 202035389 | Leader, PM, Back-end |
| 유안시귀 | 소프트웨어전공 | 202035355 | Back-end, AI |
| 김석영 | 소프트웨어전공 | 202035316 | Front-end |
| 이재용 | 소프트웨어전공 | 202236947 | Front-end, Infra |


---


