## DevSquad10: MSA 기반 B2B 물류 및 배송 플랫폼
MSA(Microservices Architecture)와 이벤트 기반 통신을 적용한 대규모 B2B 물류 관리 시스템
- **개발 기간:** 2025년 3월 11일 ~ 2025년 3월 26일
- **핵심 목표:** 서비스 간 독립성을 유지하면서 이벤트 기반 아키텍처 및 분산 락을 통해 대규모 트래픽 환경에서의 데이터 정합성 보장
- **담당 역할:** 백엔드 개발 (업체/주문/상품 도메인) 및 분산 시스템 성능 최적화
- **원본 레포지토리:** [DevSquad10 GitHub Repository](https://github.com/DevSquad10/b2b-service-platform)

<br><br>

## 시스템 아키텍처 및 ERD

<img width="100%" alt="DevSquad10 아키텍처" src="https://github.com/user-attachments/assets/0ddecc6a-7a5c-46d1-ad6e-16d3617cb1ce" />

<br>

<img width="100%" alt="DevSquad10 ERD" src="https://github.com/user-attachments/assets/a3a97c94-3753-4384-a9e5-54b5b13ab4eb" />

<br><br>

## 핵심 기술적 성과 및 트러블슈팅

### 1. 파티션 큐와 분산 락을 활용한 핫 키(Hot Key) 트래픽 격리 및 UX 방어
- **문제:** 특정 이벤트 상품에 트래픽이 집중될 경우, 다수의 스레드가 단일 큐에서 비관적 락을 획득하기 위해 대기하며 DB 커넥션 풀(HikariCP)을 모두 점유. 이로 인해 무관한 일반 상품의 결제 요청까지 지연되는 심각한 앞단 막힘(Head-of-Line Blocking) 현상 발생.
- **해결:** - RabbitMQ `x-consistent-hash` 익스체인지를 도입하여 `productId` 기준으로 라우팅을 3개의 큐로 분리, 특정 상품의 부하가 다른 상품에 영향을 주지 않도록 시스템을 물리적으로 격리.
  - DB 커넥션 고갈을 막기 위해 비관적 락을 제거하고, Redis 분산 락(Redisson)과 Facade 패턴을 적용하여 애플리케이션 레벨에서 동시성 제어.
- **결과:** 집중 부하 발생 시 일반 상품의 처리 지연 시간을 234ms에서 7ms로 약 **33배 향상(단축)** 시켰으며, 대규모 트래픽 상황에서도 100% 데이터 정합성을 유지하며 사용자 경험(UX)을 완벽하게 방어.
- **Wiki:** [RabbitMQ 파티셔닝 및 분산 락 적용기](https://github.com/DevSquad10/b2b-service-platform/wiki/%5BTrouble-Shooting%5D-%5B%EC%8A%B9%ED%98%84%5D-RabbitMQ-concurrency-%EC%84%A4%EC%A0%95%EA%B3%BC-%EB%B9%84%EA%B4%80%EC%A0%81-%EB%9D%BD%EC%9D%84-%ED%99%9C%EC%9A%A9%ED%95%9C-%EC%9E%AC%EA%B3%A0-%EA%B0%90%EC%86%8C-%EB%8F%99%EC%8B%9C%EC%84%B1-%EC%A0%9C%EC%96%B4)

<br>

### 2. Saga Pattern 기반 분산 트랜잭션 무한 재시도 방지 로직 구현
- **문제:** MSA 환경에서 주문-상품-배송 간 트랜잭션 실패 시, RabbitMQ의 기본 재시도 메커니즘으로 인해 실패한 이벤트가 무한 루프에 빠지며 메시지 브로커 자원 고갈 위험 발생.
- **해결:** DLQ(Dead Letter Queue)와 Redis 기반의 멱등성 검증 키를 활용하여 특정 횟수 이상 실패한 메시지를 별도로 격리하고, 보상 트랜잭션(Rollback) 이벤트를 발행하도록 Saga Pattern 고도화.
- **결과:** 트랜잭션 실패 시 데이터의 최종적 일관성(Eventual Consistency)을 안전하게 보장하고 시스템 마비 원천 차단.
- **Wiki:** [Saga Pattern 무한 재시도 방지](https://github.com/DevSquad10/b2b-service-platform/wiki/%5BTrouble-Shooting%5D-%5B%EC%8A%B9%ED%98%84%5D-saga-pattern-%EB%AC%B4%ED%95%9C-%EC%9E%AC%EC%8B%9C%EB%8F%84-%EB%B0%A9%EC%A7%80)

<br><br>

## 사용 기술 스택

- **Backend:** Java 17, Spring Boot 3.x, Spring Data JPA, QueryDSL
- **Database & Cache:** PostgreSQL, Redis
- **Messaging:** RabbitMQ
- **MSA Architecture:** Spring Cloud Gateway, Eureka, OpenFeign
- **DevOps & Infra:** Docker, Docker-Compose, Apache Tomcat 9.0
- **Testing & Tools:** JMeter, Swagger (Springdoc OpenAPI)
- **External API:** Gemini API (AI 자동화), Slack API

<br> <br>

## 이벤트 시퀀스
<img width="2585" height="1691" alt="배송이십조 전체 시퀀스" src="https://github.com/user-attachments/assets/2a5fdd31-40d1-43aa-aa03-e35867b095ea" />

<br><br>

## 프로젝트 구조

총 9개의 마이크로서비스로 구성되어 있으며, 아래는 제가 주로 담당하여 구현한 **상품, 주문, 업체 도메인**을 중심으로 요약한 디렉토리 구조입니다.

```text
b2b-project (Root)
├── com.devsquad10.product                 # 상품 및 재고 서비스 (담당)
│   ├── src/main/java/com/devsquad10/product
│   │   ├── presentation/                  # REST API Endpoints 및 외부 호출 응답
│   │   ├── application/                   # 비즈니스 로직, 분산 락 Facade, RabbitMQ 라우팅
│   │   ├── domain/                        # 핵심 도메인 모델 및 예외 처리
│   │   └── infrastructure/                # Redis/PostgreSQL 영속성 및 QueryDSL 구현
│   └── Dockerfile
│
├── com.devsquad10.order                   # 주문 서비스 (담당)
│   ├── src/main/java/com/devsquad10/order
│   │   └── (상품 서비스와 동일한 도메인 중심 계층형 아키텍처 적용)
│   └── Dockerfile
│
├── com.devsquad10.company                 # 업체 정보 관리 서비스 (담당)
│   ├── src/main/java/com/devsquad10/company
│   │   └── (상품 서비스와 동일한 도메인 중심 계층형 아키텍처 적용)
│   └── Dockerfile
│
├── com.devsquad10.eureka                  # 서비스 디스커버리 (공통)
├── com.devsquad10.gateway                 # API 게이트웨이 및 JWT 인증 (공통)
│
├── com.devsquad10.hub                     # 물류 허브 서비스 (타 팀원 담당)
├── com.devsquad10.shipping                # 배송 서비스 (타 팀원 담당)
├── com.devsquad10.message                 # Slack 알림 및 AI 연동 서비스 (타 팀원 담당)
└── com.devsquad10.user                    # 사용자 인증/인가 서비스 (타 팀원 담당)
