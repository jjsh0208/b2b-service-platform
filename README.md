## DevSquad10: MSA 기반 B2B 물류 및 배송 플랫폼
MSA(Microservices Architecture)와 이벤트 기반 통신을 적용한 대규모 B2B 물류 관리 시스템
- **개발 기간:** 2025년 3월 11일 ~ 2025년 3월 26일
- **핵심 목표:** 서비스 간 독립성을 유지하면서 이벤트 기반 아키텍처 및 분산 락을 통해 대규모 트래픽 환경에서의 데이터 정합성 보장
- **담당 역할:** 백엔드 개발 (업체/주문/상품 도메인) 및 분산 시스템 성능 최적화
- **원본 레포지토리:** [DevSquad10 GitHub Repository](https://github.com/DevSquad10/b2b-service-platform)

<br><br>

## 시스템 아키텍처 및 ERD

<img width="4648" height="2988" alt="배송이 10조 아키텍처 개선" src="https://github.com/user-attachments/assets/f20076c2-b95c-4210-a4fc-e20765b37a5d" />


<br>

<img width="100%" alt="DevSquad10 ERD" src="https://github.com/user-attachments/assets/a3a97c94-3753-4384-a9e5-54b5b13ab4eb" />

<br><br>

## 핵심 기술적 성과 및 트러블슈팅

### 1. 메시지 큐 파티셔닝과 분산 락을 활용한 핫 키(Hot Key) 트래픽 격리 및 동시성 제어
- **문제 (Challenge):** 특정 이벤트 상품에 트래픽이 폭발적으로 몰리는 핫 키(Hot Key) 상황 발생 시, 수많은 워커 스레드가 단일 큐에서 비관적 락을 획득하기 위해 대기하며 DB 커넥션 풀을 독점했습니다. 이로 인해 경합과 전혀 무관한 일반 상품을 구매하려는 유저들의 결제 요청까지 연쇄적으로 수백 밀리초 이상 지연되었습니다. 즉, 가장 앞에 있는 요청이 처리되지 않아 뒤에 있는 요청들이 줄줄이 대기하는 현상과, 특정 대상이 공유 자원을 독점하여 같은 인프라를 사용하는 다른 일반 사용자들의 성능까지 저하시키는 상황이 발생했습니다.
- **해결 (Solution):** - RabbitMQ `x-consistent-hash` 익스체인지를 도입하여 `productId`를 해시 키로 삼아 이벤트를 3개의 독립된 파티션 큐로 분산 라우팅했습니다.
  - DB 디스크 부하를 유발하는 비관적 락을 완전히 제거하고, Redis 기반의 Redisson 분산 락과 Facade 패턴을 적용하여 트랜잭션 생명주기를 엄격히 분리함으로써 애플리케이션 레벨에서 동시성을 제어했습니다.
- **결과 (Result):** 집중 부하 발생 시 일반 상품의 처리 지연 시간을 234ms에서 7ms로 약 **33배 비약적으로 단축(UX 방어율 100%)** 시켰으며, 20개의 스레드가 무작위로 DB 풀을 고갈시키던 방식을 큐당 1개(총 2~3개)의 커넥션만 점유하게 개선하여 전체 시스템의 가용 자원을 70% 이상 확보했습니다.
- **Wiki:** [RabbitMQ 파티셔닝 및 분산 락 적용기]([https://github.com/DevSquad10/b2b-service-platform/wiki/%5BTrouble-Shooting%5D-%5B%EC%8A%B9%ED%98%84%5D-RabbitMQ-concurrency-%EC%84%A4%EC%A0%95%EA%B3%BC-%EB%B9%84%EA%B4%80%EC%A0%81-%EB%9D%BD%EC%9D%84-%ED%99%9C%EC%9A%A9%ED%95%9C-%EC%9E%AC%EA%B3%A0-%EA%B0%90%EC%86%8C-%EB%8F%99%EC%8B%9C%EC%84%B1-%EC%A0%9C%EC%96%B4](https://ddong-kka.tistory.com/83)](https://ddong-kka.tistory.com/83))

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
```

## 👥 Team
| <img src="https://github.com/jjsh0208.png" width="120"><br>[전승현 (Leader)](https://github.com/jjsh0208) | <img src="https://github.com/minji-git.png" width="120"><br>[김민지](https://github.com/minji-git) | <img src="https://github.com/josephuk77.png" width="120"><br>[이승욱](https://github.com/josephuk77) | <img src="https://github.com/aerhergag00.png" width="120"><br>[이지웅](https://github.com/aerhergag00) |
| :---: | :---: | :---: | :---: |
| Company<br>Product<br>Order | Shipping<br>Shipping Agent | User<br>Eureka<br>Gateway | Hub<br>Message<br>Gemini AI |
