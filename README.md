# 🍽️ hi-eating (하이이팅)

> **"AI가 고르고, AI가 쓰고, AI가 검증하는 스마트 핫딜 마케팅 & 실시간 상담 커머스 플랫폼"**  
> **hi-eating**은 사용자의 행동 데이터를 다차원 벡터로 분석하여 초개인화된 상품을 추천하고, Spring AI와 Ollama 원격 LLM을 연동한 지능형 핫딜 타겟팅 파이프라인 및 RabbitMQ 기반의 고신뢰성 비동기 메시징 아키텍처를 구현한 혁신적인 이커머스 웹 애플리케이션입니다.

---

## 📑 목차
- [1. 프로젝트 개요](#1-프로젝트-개요)
- [2. 핵심 아키텍처 및 시스템 흐름도](#2-핵심-아키텍처-및-시스템-흐름도)
- [3. 핵심 기능 설명](#3-핵심-기능-설명)
- [4. 기술 스택](#4-기술-스택)
- [5. 트러블슈팅 및 최적화 경험](#5-트러블슈팅-및-최적화-경험)
- [6. 프로젝트 시작 가이드](#6-프로젝트-시작-가이드)

---

## 1. 프로젝트 개요

* **개발 기간**: 2026.07
* **기획 배경**: 현대 이커머스에서 불특정 다수를 타겟으로 하는 마케팅 이메일은 낮은 전환율과 높은 피로도를 유발합니다. 또한 실시간 문의 대응의 부재는 고객 이탈의 주원인입니다.
* **데이터 출처**: **현대백화점 그룹 현대그린푸드의 케어푸드 전문 브랜드 '그리팅(Greating)'**의 실제 상품 데이터를 **크롤링(Crawling)**하여 풍부하고 사실적인 건강식/케어푸드 제품 데이터셋을 구축했습니다.
* **브랜드 의미**: **hi-eating (하이이팅)**은 현대그린푸드 '그리팅(Greating)'에 영감을 얻어 친근한 인사(**Hi**)와 건강한 식사(**Eating**)를 결합한 브랜드명으로 기획되었습니다.
* **해결 방안**: 
  1. 사용자의 실제 쇼핑 이력(방문, 찜, 구매, 평점)을 **벡터 공간에 사영하여 맞춤형 상품을 추천**합니다.
  2. 새로운 핫딜 등록 시, 카테고리 적합도가 높은 고객을 **AI가 스스로 선별(Targeting)하고, 맞춤형 홍보 메일을 직접 작성(Generation)한 후, 품질을 자가 검증(Validation)**하는 자동 마케팅 파이프라인을 구동합니다.
  3. 대량의 메일 발송은 **RabbitMQ 메시지 브로커**를 통해 비동기로 격리하여 시스템의 안정성을 극대화합니다.
  4. 웹소켓 기반의 **실시간 1:1 사용자-어드민 상담 채널**을 구축하여 실시간 고객 관리를 완성했습니다.

---

## 2. 핵심 아키텍처 및 시스템 흐름도

`hi-eating`은 AI 연산 및 메일 발송과 같은 무거운 작업을 메인 비즈니스 스레드와 격리하기 위해 **이중 비동기 파이프라인(Scheduler + MQ)**을 채택했습니다.

### ⚙️ 시스템 흐름도 (Architecture Diagram)

```mermaid
graph TD
    Client[📱 Client Browser / UI] <-->|HTTP / WebSockets| SpringBoot[☕ Spring Boot Core Server]
    SpringBoot <-->|MyBatis Mapper| DB[(💾 Oracle DB)]
    
    subgraph AI Pipeline (Asynchronous Scheduler)
        SpringBoot -.->|1. Polling Job| JobProcessor[🤖 Target Selection Job Processor]
        JobProcessor -->|2. Target Scoring Temp 0.2| OllamaVal[🧠 Ollama Validation Server :11435]
        JobProcessor -->|3. Content Auto-Gen Temp 0.7| OllamaGen[✍️ Ollama Generation Server :11434]
        JobProcessor -->|4. Quality Self-Check| OllamaVal
        JobProcessor -->|5. Update Status & Logs| DB
    end

    subgraph Vector Recommendation Engine
        SpringBoot -->|Build User Profile Vector| UserProfileService[👤 User Profile Service]
        SpringBoot -->|Caching Embeddings| ProductEmbedService[📦 Product Embedding Service]
        ProductEmbedService <-->|Embedding Model| OllamaRecommend[📐 Ollama Embedding Server :11436]
        UserProfileService & ProductEmbedService -->|Cosine Similarity| RecommendService[🎯 Recommendation Service]
    end

    subgraph Reliable Messaging Pipeline
        DB -.->|1. Scan Approved Drafts| PublishScheduler[⏰ Email Publish Scheduler]
        PublishScheduler -->|2. High-Reliability Send| EmailPublisher[✉️ Email Publisher]
        EmailPublisher -->|3. Convert & Send| RabbitMQ[🐇 RabbitMQ Message Broker]
        RabbitMQ -->|4. Publisher Confirm Ack / Return| EmailPublisher
    end
```

---

## 3. 핵심 기능 설명

### 🤖 1. AI 기반 핫딜 마케팅 자동화 파이프라인
* **비동기 작업 스케줄러 (`TargetSelectionJobProcessor`)**: 핫딜이 생성되면 비동기 백그라운드 작업이 큐잉됩니다.
* **정밀 타겟 스코어링 (`TargetUserScoringAiClient`)**: 
  - 후보 고객의 카테고리별 활동 로그를 수집하여 AI 검증용 모델(Ollama 포트 11435, `temperature 0.2`로 일관성 확보)에 전달합니다.
  - JSON 스키마를 엄격히 지정하여 적합도 점수(0~100)를 도출합니다.
  - **Divide & Conquer Fallback**: AI 응답 파싱 실패 시 대상을 절반씩 분할하여 재시도하며, 최종 오류 시 활동 점수 룰에 따른 기본값(50점)을 부여하는 강력한 대체 메커니즘을 가집니다.
* **이메일 자동 생성 (`EmailGenerationAiService`)**: 타겟이 확정되면 생성용 모델(Ollama 포트 11434, `temperature 0.7`로 창의성 발휘)이 핫딜 상품 스펙에 맞는 맞춤형 광고 메일 제목과 본문을 동적으로 생성합니다.
* **이메일 품질 자가 검증 (`HotDealEmailQualityValidationService`)**:
  - 작성된 본문에 대해 맞춤법, 할인율 오류, 허위 정보, 비속어, 본문 길이 등을 자가 평가(`PASS`/`FAIL`)하고 사유(`issues`)를 JSON으로 매핑하여 관리자의 검수 편의성을 증대합니다.

### 🎯 2. 개인화 텍스트 임베딩 추천 엔진
* **사용자 행동 분석 프로필 (`UserProfileService`)**: 
  - 사용자의 행동 이력에 따라 가중치(구매 3.0, 찜 2.0, 방문 1.0, 4점 이상 평점 1.5)를 다르게 수집합니다.
  - 각 활동의 텍스트 요약을 임베딩하여 가중 평균을 구해 사용자의 취향을 대표하는 단일 **프로필 벡터**를 완성합니다.
* **코사인 유사도 매칭 (`RecommendationService`)**:
  - `nomic-embed-text` 모델을 사용하여 전체 활성 상품명을 벡터화하고 Caching합니다.
  - 실시간으로 코사인 유사도를 계산하여 유저 취향과 가장 밀착된 상위 7개(`TOP_N`) 상품 리스트를 렌더링합니다.

### 🐇 3. 고신뢰성 비동기 메시지 발행 (RabbitMQ)
* **Publisher Confirms & Returns**: 
  - 메시지가 유실되는 것을 방지하기 위해 `CorrelationData` 객체 및 `confirmCallback`을 적용했습니다.
  - RabbitMQ 브로커가 메시지를 안전하게 수신했는지 (`Ack`) 비동기 확인을 거치며, 라우팅 오류(`ReturnedMessage`) 감지 시 발송 로그 테이블에 실패 사유와 상태 코드를 기입합니다.

### 💬 4. 실시간 1:1 고객 관리 채팅 (WebSockets)
* 일반 사용자용 플로팅 채팅 위젯과 관리자 전용 어드민 대화 패널 간의 양방향 WebSocket 통신 채널을 운영합니다.
* 다중 동시 쓰기 환경에서 웹소켓 충돌을 예방하는 스레드 세이프 구조를 완성했습니다.

---

## 4. 기술 스택

### 💻 Backend
* **Language**: Java 17
* **Framework**: Spring Boot 3.x
* **Security**: Spring Security 6 (Cookie CSRF 기반 세션 인증)
* **Data Access**: MyBatis (Camel-case mapping)
* **Build Tool**: Gradle

### 🗄️ Database & Broker
* **Database**: Oracle DB (Thin driver)
* **Message Broker**: RabbitMQ (AMQP)

### 🧠 Artificial Intelligence
* **Framework**: Spring AI
* **Local LLM Server**: Ollama (nomic-embed-text, kosa-ollama3)

### 🎨 Frontend
* **Template Engine**: Thymeleaf
* **Style & Interaction**: Vanilla CSS, Vanilla JavaScript (ES6+), Lottie Web Animation

---

## 5. 트러블슈팅 및 최적화 경험

### 🛡️ Spring Security 6 CSRF 403 Forbidden 해결
* **문제**: Spring Security 6로 마이그레이션된 환경에서 프론트엔드 비동기(AJAX/fetch) 통신 시 CSRF 토큰 검증 단계에서 403 에러가 빈번하게 발생.
* **원인**: 스프링 시큐리티 6의 기본 설정인 지연 로드(`DeferredCsrfToken`) 핸들러 작동 방식에 의해 JavaScript가 HTTP 헤더에 담아야 할 토큰이 최초 렌더링 단계에서 적절히 생성 및 노출되지 않음.
* **해결**: `SecurityConfig`에 `CsrfTokenRequestAttributeHandler` 커스텀 설정 및 `csrfRequestAttributeName(null)`을 정의하여 매 요청마다 즉각적으로 토큰을 로드하게 강제하고, `CookieCsrfTokenRepository.withHttpOnlyFalse()` 옵션을 결합하여 클라이언트 스크립트가 안정적으로 토큰을 획득하도록 해결했습니다.

### 💬 WebSocket `TEXT_FULL_WRITING` 동시성 예외 해결
* **문제**: 대화 상대방이 동시에 채팅 메시지를 보내거나 시스템 메시지 브로드캐스트가 겹칠 때 웹소켓 세션에서 `IllegalStateException: The remote endpoint was in state [TEXT_FULL_WRITING]`가 터지며 서버가 비정상 다운되거나 메시지가 유실됨.
* **원인**: 표준 웹소켓 세션(`WebSocketSession`)은 기본적으로 논블로킹 통신을 전제로 하여 여러 스레드에서 단일 세션으로 동시에 `sendMessage()`를 실행할 때 충돌이 발생함.
* **해결**: `ChatWebSocketHandler` 내부의 송신 로직에 `synchronized(session)` 임계 영역을 지정하여 메시지 전송 스레드가 직렬로 쓰기 연산을 수행하도록 보장하여 동시성 레이스 컨디션을 예방했습니다.

### ⚡ 스케줄러 동시 점유 병목 개선
* **문제**: AI 프로세싱 주기적 폴링 및 RabbitMQ 발송 자동화 스케줄러가 정해진 딜레이대로 실행되지 않고 서로 지연을 유발함.
* **원인**: Spring Task Scheduler의 기본 스레드 풀 크기가 `1`로 정의되어 있어 단일 스레드가 AI 모델 통신 대기(Timeout 120초) 상태에 빠졌을 때 다른 모든 스케줄링 태스크가 무한 대기 상태에 걸림.
* **해결**: `application.properties` 파일에 `spring.task.scheduling.pool.size=4` 속성을 정의하여 스케줄러 풀을 대폭 확보하여 독립적인 스레드 바인딩을 실현시켰습니다.

### ✍️ IME 한글 입력 중복 제출 방지 (Double Enter)
* **문제**: 한국어 자음/모음이 결합되는 한글 조합 중에 엔터 키를 입력할 경우 채팅 내용이 빈 버퍼로 이중 제출되거나 두 번 전송되는 기현상 발생.
* **원인**: IME 입력 상태에서는 브라우저의 키다운 이벤트가 문자 조립 완료 신호와 입력 신호 두 개를 모두 발생시키기 때문임.
* **해결**: `chat-widget.js` 이벤트 리스너 내부에 `event.isComposing` 프로퍼티 검사를 도입하여 문자 입력 조합 중에는 폼 전송 이벤트 호출을 차단하도록 수정했습니다.

---

## 6. 프로젝트 시작 가이드

### 📋 요구 사항 (Prerequisites)
* Java 17 SDK 이상
* RabbitMQ 브로커 실행 상태 (Port 5672)
* Oracle DB 실행 상태 (Port 1521)
* Ollama 실행 상태 (Port 11434, 11435, 11436에 별도 서버 설정 혹은 포트포워딩)

### 🛠️ Ollama 로컬 모델 준비
```bash
# 생성용 및 검증용 모델 다운로드
ollama pull kosa-ollama3:latest

# 임베딩용 추천 모델 다운로드
ollama pull nomic-embed-text:latest
```

*본 프로젝트는 현대퓨처넷 교육과정의 팀 프로젝트 결과물로, 초개인화된 AI 추천 및 고성능 비동기 아키텍처 구현을 목표로 제작되었습니다.*
