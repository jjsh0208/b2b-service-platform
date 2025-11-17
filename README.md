# 📌 물류 관리 및 배송 시스템 (DevSquad10) - 개인 기여 내역

![Image](https://github.com/user-attachments/assets/727bd681-d84b-4310-8b9e-080859fe3b39)

## 🗣️ 프로젝트 소개

- **프로젝트 명:** 물류 관리 및 배송 시스템 (DevSquad10)
- **한 줄 소개:** MSA(Microservices Architecture)를 기반으로 설계·구현한 B2B 물류 관리 및 배송 시스템
- **개발 기간:** 2025년 3월 11일 ~ 2025년 3월 26일
- **원본 레포지토리:** [DevSquad10 GitHub Repository](https://github.com/DevSquad10/b2b-service-platform)
- **나의 역할:** **팀장 (Team Leader)** / 백엔드 개발 (`주문`, `상품`, `업체` 도메인 담당)

<br>

## 🚀 나의 핵심 기여 (My Key Contributions)

팀장 및 백엔드 개발자로서 프로젝트의 아키텍처 설계를 주도하고, 핵심 도메인 개발을 담당했습니다.

* **팀장 및 아키텍처 설계:** MSA 구조 및 도메인 분리(`주문`, `상품`, `업체`)를 주도했으며, 이벤트 시퀀스 다이어그램을 문서화하여 서비스 간 메시지 흐름을 조율했습니다.
* **핵심 API 개발:** `주문`, `상품`, `업체` 도메인의 백엔드 아키텍처 설계 및 RESTful API 개발을 담당했습니다.
* **비동기 이벤트 시스템 구축:** RabbitMQ를 도입하여 `주문-재고-알림`으로 이어지는 비동기 이벤트 처리 구조를 설계하고 운영했습니다.
* **성능 최적화:** Redis를 활용한 페이징 캐시 및 단건 캐시(상품, 업체) 전략을 수립하여 DB 부하를 분산하고 조회 성능을 개선했습니다.
* **분산 트랜잭션 (Saga):** Saga 패턴을 적용하여 `주문-재고` 흐름의 데이터 일관성을 보장하고, 실패 시 보상 트랜잭션 로직을 구현했습니다.
* **서비스 간 통신:** Feign Client를 활용한 서비스 간 동기 통신(허브 ID 검증 등)을 구현했습니다.

<br>

## 🛠️ 상세 구현 내용

### 1. MSA 아키텍처 설계 및 이벤트 흐름 조율 (팀장)

* **도메인 분리:** 프로젝트의 핵심 도메인을 `주문`, `상품`, `업체`, `배송`, `허브` 등으로 명확히 분리하여 서비스 간 책임을 명확화했습니다.
* **이벤트 흐름 설계:** RabbitMQ를 기반으로 한 도메인 간 비동기 이벤트 흐름을 설계하고 문서화하여 팀원 간의 개발 의존성을 최소화했습니다.
* **점진적 분리 전략:** 초기 개발 단계에서는 모놀리식 통합 구조로 시작하여 개발 속도와 테스트 효율성을 확보했습니다. 이후 각 도메인을 마이크로서비스로 분리 배포할 수 있도록 확장 가능한 구조로 설계했습니다.

### 2. RabbitMQ 기반 비동기 이벤트 처리 시스템

* **이벤트 발행/구독:** RabbitMQ의 `Topic Exchange`를 활용하여 이벤트 발행/구독 구조를 설계했습니다.
* **주문/재고 비동기 처리:** `주문 등록` 시 `재고 차감` 이벤트를 비동기 발행하고, `주문 변경/취소` 시 `재고 복원` 이벤트를 발행하여 데이터 정합성을 맞췄습니다.
* **Slack 알림 연동:** `재고 소진` 등 특정 비즈니스 이벤트 발생 시, RabbitMQ 이벤트를 소비(Consume)하여 Slack Webhook을 호출, 업체 담당자에게 실시간 재고 부족 알림을 전송하는 기능을 구현했습니다.

### 3. Saga 패턴 기반 분산 트랜잭션 처리

* **데이터 일관성 보장:** `주문 생성 → 재고 차감 → 배송 준비`로 이어지는 분산 트랜잭션 흐름을 **Saga 패턴**으로 구성하여 MSA 환경에서의 데이터 일관성을 보장했습니다.
* **보상 트랜잭션:** 재고 차감 등 중간 단계에서 이벤트 처리가 실패할 경우, 이전에 성공한 트랜잭션(예: 주문 상태 변경)을 롤백하는 보상 트랜잭션 로직을 구현했습니다.
* **무한 재시도 방지:** 이벤트 처리 실패 시, 재시도 횟수를 **Redis**로 카운트하여 관리했습니다. 최대 시도 횟수 초과 시, 해당 메시지를 실패 처리(DLQ 이동)하고 관리자에게 알림을 보내도록 설계했습니다.

### 4. Redis 기반 캐시 최적화

* **페이징 캐시:** 검색 조건(키워드, 카테고리, 페이지 번호)을 조합한 Key로 `페이지 단위의 검색 결과`를 Redis에 캐싱하여 복잡한 동적 쿼리로 인한 DB 부하를 줄였습니다.
* **단건 캐시:** `상품 상세`, `업체 정보` 등 변경 빈도가 낮고 조회가 빈번한 데이터를 Redis Hash로 관리하여 응답 속도를 향상시켰습니다.
* **캐시 무효화 (Write-Through):** 상품, 업체 등 원본 데이터 변경(CUD) 시, 관련 캐시 키를 즉시 삭제(Delete)하여 데이터 일관성을 유지했습니다.

### 5. Feign Client 기반 서비스 간 통신

* **동기식 통신:** 비동기 처리가 부적절한 실시간 유효성 검증 로직(예: 업체 등록 시 `허브 서비스`의 허브 ID 유효성 검증)은 Feign Client를 사용한 동기식 API 호출로 구현했습니다.

<br>

## 💡 기술적 트러블슈팅 및 개선 경험 (담당)

프로젝트를 진행하며 제가 직접 마주치고 해결한 기술적 문제입니다.

| 문제 상황 (Challenge) | 해결 과정 (Solution) | 관련 문서 (Links) |
| :--- | :--- | :--- |
| **Saga 패턴의 무한 재시도 문제** | 이벤트 처리 실패 시(예: 재고 부족) 명확한 종료 조건이 없어 동일한 실패 메시지가 계속 재발행되는 문제가 발생했습니다. <br/> **Redis를 활용해 재시도 횟수를 카운트**하고, 최대 시도 횟수(3회) 초과 시 해당 메시지를 DLQ(Dead Letter Queue)로 이동시켜 무한 루프를 방지했습니다. | [Blog](https://ddong-kka.tistory.com/27) |
| **재고 감소 동시성 문제** | 1. **RabbitMQ Consumer Concurrency 설정:** `concurrency` 옵션을 `1`로 설정하여 재고 차감 이벤트가 단일 스레드로 순차 처리되도록 보장했습니다.<br/> 2. **비관적 락 (Pessimistic Lock):** DB 트랜잭션 레벨에서 `Pessimistic Lock`을 적용하여 재고 엔티티 접근을 제어, 동시성 문제를 근본적으로 해결했습니다. | [Blog](https://ddong-kka.tistory.com/28?category=1164019) |

<br>

## 💻 사용 기술 스택

(프로젝트 전반 및 담당 도메인에서 사용한 기술입니다.)

* **Backend:** Java 17, Spring Boot 3.x, Spring Data JPA, QueryDSL, Spring Security 6.x
* **Database:** PostgreSQL, Redis
* **Messaging:** RabbitMQ
* **MSA Comms:** Feign Client, Spring Cloud Gateway, Eureka
* **DevOps:** Docker, Docker-Compose
* **Auth:** JWT (JSON Web Token)
* **API:** RESTful API, Swagger (Springdoc OpenAPI)
* **ETC:** Slack Webhook, Gemini API, JMeter
  
## ⚙️ 적용 기술

### *🔍 QueryDSL* ###

> 검색, 정렬 등 동적 쿼리 작성을 위해 사용하며, 타입 안전한 SQL 쿼리를 생성하기 위해 활용했습니다.

### *🚀 Redis* ###

> 연속된 요청으로 인한 DB 병목을 해소하기 위해 캐싱 용도로 사용하여 빠른 데이터 접근을 지원합니다.

### *📩 RabbitMQ 비동기 처리* ###

> MSA 도메인 간 비동기 이벤트 처리를 통해 서비스 간 결합도를 감소시키고 안정성을 향상시켰습니다.

### *⏰ Scheduler 사용* ###

> 매일 오전 6시에 배송 담당자들에게 당일 배송 메시지 안내를 자동으로 보내기 위해 사용했습니다.

### *🔒 비관적 락 구현* ###

> 재고 감소 , 배송 담당자 배정 할당 등 동시성 문제가 발생할 수 있는 중요한 트랜잭션에서 충돌을 방지하기 위해 사용했습니다.

### *🔗 Feign Client* ###

> MSA 환경에서 다른 서비스의 API를 호출할 때 간편하게 HTTP 통신을 처리하기 위해 사용했습니다.

### *🤖 Gemini AI* ###

> 슬랙 메시지 양식을 자동화하고, 배송 순서를 최적화하여 효율적인 물류 관리를 지원합니다.


## 🗂️ 프로젝트 구조

```
b2b-project/                         # B2B 루트 프로젝트
│── com.devsquad10.eureka/           # 서비스 디스커버리 (Port : 19091)
│   ├── src/main/java/com/devsquad10/eureka/
│
│── com.devsquad10.gateway/          # API Gateway (Port : 19092)
│   ├── src/main/java/com/devsquad10/gateway/
│       ├── infrastructure/
│
│── com.devsquad10.company/          # 업체 관련 서비스 (Port : 19093) 👈 **업체 서비스 (담당)**
│   ├── src/main/java/com/devsquad10/company/
│   │   ├── application/             # 애플리케이션 서비스 계층
│   │   ├── domain/                  # 도메인 모델 및 엔티티
│   │   ├── infrastructure/          # 데이터베이스, 외부 API 연동
│   │   ├── presentation/            # REST API 및 컨트롤러
│   ├── src/test/java/com/devsquad10/company/
│
│── com.devsquad10.hub/              # 물류 허브 서비스 (Port : 19094) 
│   ├── src/main/java/com/devsquad10/hub/
│   │   ├── application/
│   │   ├── domain/
│   │   ├── infrastructure/
│   │   ├── presentation/
│
│── com.devsquad10.message/          # 메시징 서비스 (Port : 19095) 
│   ├── src/main/java/com/devsquad10/message/
│   │   ├── application/
│   │   ├── domain/
│   │   ├── infrastructure/
│   │   ├── presentation/
│
│── com.devsquad10.order/            # 주문 서비스 (Port : 19096) 👈 **주문 서비스 (담당)**
│   ├── src/main/java/com/devsquad10/order/
│   │   ├── application/
│   │   ├── domain/
│   │   ├── infrastructure/
│   │   ├── presentation/
│
│── com.devsquad10.product/          # 상품 서비스 (Port : 19097)  👈 **상품 서비스 (담당)**
│   ├── src/main/java/com/devsquad10/product/
│   │   ├── application/
│   │   ├── domain/
│   │   ├── infrastructure/
│   │   ├── presentation/
│
│── com.devsquad10.shipping/         # 배송 서비스 (Port : 19098) 
│   ├── src/main/java/com/devsquad10/shipping/
│   │   ├── application/
│   │   ├── domain/
│   │   ├── infrastructure/
│   │   ├── presentation/
│
│── com.devsquad10.user/             # 사용자 서비스 (Port : 19099) 
│   ├── src/main/java/com/devsquad10/user/
│   │   ├── application/
│   │   ├── domain/
│   │   ├── infrastructure/
│   │   ├── presentation/
│
│
│── docker-compose.yml                # Docker 설정 파일
│── README.md                         # 프로젝트 설명 문서
│── settings.gradle.kts               # Gradle 설정 파일

```

<br>

## 📖 서비스 아키텍처

![Image](https://github.com/user-attachments/assets/0ddecc6a-7a5c-46d1-ad6e-16d3617cb1ce)

<br>

## 🧮 ERD 설계

![Image](https://github.com/user-attachments/assets/a3a97c94-3753-4384-a9e5-54b5b13ab4eb)


## 📌 팀원 역할분담

<table>
  <tr>
    <th>
      <a href="https://github.com/jjsh0208" target="_blank">
        전승현&lt;팀장&gt;
      </a>
    </th>
    <th>
      <a href="https://github.com/minji-git" target="_blank">
        김민지
      </a>
    </th>
    <th>
      <a href="https://github.com/josephuk77" target="_blank">
        이승욱
      </a>
    </th>
    <th>
      <a href="https://github.com/aerhergag00" target="_blank">
        이지웅
      </a>
    </th>
  </tr>
  <tr>
    <td>
      <img src="https://github.com/jjsh0208.png" width="150" alt="전승현 팀장">
    </td>
    <td>
      <img src="https://github.com/minji-git.png" width="150" alt="이채연">
    </td>
    <td>
      <img src="https://github.com/josephuk77.png" width="150" alt="이서우">
    </td>
    <td>
      <img src="https://github.com/aerhergag00.png" width="150" alt="윤창근">
    </td>
  </tr>
  <tr>

  <th>Company <br> Product <br> Order  <br>  <!-- 승현 -->
  <th>Shipping <br> Shipping Agent</th> <!-- 민지 -->
  <th>User <br> Eureka <br> Gateway </th> <!-- 승욱 -->
  <th>Hub <br> Message <br> Gemini AI </th> <!-- 지웅 -->
  </tr>
</table>

<br>

