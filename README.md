# 📌 물류 관리 및 배송 시스템 (DevSquad10)

![Image](https://github.com/user-attachments/assets/727bd681-d84b-4310-8b9e-080859fe3b39)

## 📖 프로젝트 목적 개요

### 대규모 AI 시스템 프로젝트

**물류 관리 및 배송 시스템을 MSA(Microservices Architecture) 기반의 시스템을 설계하고 구현하면서 다양한 기술과 방법론을 적용해보는게 목표입니다.**

### 주요 목표

- <b>MSA 설계 및 구현</b>
    - 서비스 간 독립성을 유지하면서 유기적으로 연동되며는 마이크로서비스 아키텍처(MSA) 설계
    - API 변경시 발생할 수 있는 문제를 최소화하기 위한 버전 관리

<br>

- <b>협업 및 프로젝트 관리</b>
    - GitHub Issues와 Slack을 활용하여 팀원 간 원활한 소통 및 업무 분배
    - API 요구사항 및 공유해야 할 정보는 Notion을 통해 문서화하여 체계적으로 관리
    - 코드 리뷰 및 PR(Pull Request) 프로세스를 통해 코드 품질 유지 및 개선

<br>

- <b>이벤트 기반 아키텍처 적용</b>
    - RabbitMQ를 활용한 비동기 메시징 시스템 도입
    - 서비스 간 의존도를 줄이고 확장성을 높이기 위한 이벤트 기반 통신(Event-Driven Architecture) 적용

<br>

- <b>AI 기술 적용 (Gemini API 활용)</b>
    - AI를 활용해 특정 기능을 자동화하고, 실제 프로젝트에 적용 경헙 확보

<br>

## 🎯 팀원 역할분담

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
  <th>Shipping</th> <!-- 민지 -->
  <th>User <br> Eureka <br> Gateway </th> <!-- 승욱 -->
  <th>Hub <br> Message <br> Gemini AI </th> <!-- 지웅 -->
  </tr>
</table>

<br>

## 📅 프로젝트 진행 기간

- 2025년 3월 11일 ~ 2025년 3월 26

## 🏗 서비스 구성

### 💾 프로젝트 구조

```
b2b-project/                         # B2B 루트 프로젝트
│── com.devsquad10.company/          # 업체 관련 서비스
│   ├── src/main/java/com/devsquad10/company/
│   │   ├── application/             # 애플리케이션 서비스 계층
│   │   ├── domain/                  # 도메인 모델 및 엔티티
│   │   ├── infrastructure/          # 데이터베이스, 외부 API 연동
│   │   ├── presentation/            # REST API 및 컨트롤러
│   ├── src/test/java/com/devsquad10/company/
│
│── com.devsquad10.eureka/           # 서비스 디스커버리 (Eureka)
│   ├── src/main/java/com/devsquad10/eureka/
│
│── com.devsquad10.gateway/          # API Gateway (Spring Cloud Gateway)
│   ├── src/main/java/com/devsquad10/gateway/
│       ├── infrastructure/ 
│
│── com.devsquad10.hub/              # 물류 허브 서비스
│   ├── src/main/java/com/devsquad10/hub/
│   │   ├── application/
│   │   ├── domain/
│   │   ├── infrastructure/
│   │   ├── presentation/
│
│── com.devsquad10.message/          # 메시징 서비스 (slack)
│   ├── src/main/java/com/devsquad10/message/
│   │   ├── application/
│   │   ├── domain/
│   │   ├── infrastructure/
│   │   ├── presentation/
│
│── com.devsquad10.order/            # 주문 서비스
│   ├── src/main/java/com/devsquad10/order/
│   │   ├── application/
│   │   ├── domain/
│   │   ├── infrastructure/
│   │   ├── presentation/
│
│── com.devsquad10.product/          # 상품 서비스
│   ├── src/main/java/com/devsquad10/product/
│   │   ├── application/
│   │   ├── domain/
│   │   ├── infrastructure/
│   │   ├── presentation/
│
│── com.devsquad10.shipping/         # 배송 서비스
│   ├── src/main/java/com/devsquad10/shipping/
│   │   ├── application/
│   │   ├── domain/
│   │   ├── infrastructure/
│   │   ├── presentation/
│
│── com.devsquad10.user/             # 사용자 서비스
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

### 🚀 서비스 엔드포인트

| 서비스명         | 설명                | 기본 URL                   | 포트    |
|--------------|-------------------|--------------------------|-------|
| **Eureka**   | 서비스 디스커버리         | `http://localhost:19091` | 19091 |
| **Gateway**  | API Gateway       | `http://localhost:19092` | 19092 |
| **Company**  | 업체 정보 관리 서비스      | `http://localhost:19093` | 19093 |
| **Hub**      | 물류 허브 서비스         | `http://localhost:19094` | 19094 |
| **Message**  | 메시징 서비스 ( Slack ) | `http://localhost:19095` | 19095 |
| **Order**    | 주문 서비스            | `http://localhost:19096` | 19096 |
| **Product**  | 상품 서비스            | `http://localhost:19097` | 19097 |
| **Shipping** | 배송 서비스            | `http://localhost:19098` | 19098 |
| **User**     | 사용자 서비스           | `http://localhost:19099` | 19099 |

<br>




<br>

## ☁️ Architecture

![Image](https://github.com/user-attachments/assets/0ddecc6a-7a5c-46d1-ad6e-16d3617cb1ce)

<br>

## 📌 ERD

![Image](https://github.com/user-attachments/assets/b3193410-ee5b-413e-851e-d6cfcc538d79)


 <br>

## 🚨 Trouble Shooting

트러블 슈팅 기재

## ⚙️ 적용 기술

적용 기술 정리

<br>

## 🛠 기술 스택

## Backend

- **Framework**: Spring Boot 3.x
  <!-- Spring Boot 최신 버전 사용 -->
- **Database Access**: Spring Data JPA , QueryDSL
  <!-- ORM 프레임워크로 데이터베이스와의 연동을 쉽게 처리 -->
- **Security**: Spring Security 6.x
  <!-- 인증과 인가를 위한 보안 모듈 -->
- **API Communication**: Feign Client
- **Message Broker**: RabbitMQ
- **Caching**: Redis
- **Testing**: JMeter
- **API Documentation**: Swagger (Springdoc OpenAPI)
  <!-- API 문서를 자동 생성해주는 Swagger 도구 -->
- **REST API**: RESTful API 설계
  <!-- REST 아키텍처 스타일에 따른 API 설계 -->

## Database

- **Primary DB**: PostgreSQL
  <!-- 주 데이터베이스로 사용 -->

## Server

- **Application Server**: Apache Tomcat 9.0
  <!-- 서블릿 컨테이너로 사용하는 Tomcat 서버 -->

## Authentication

- **Token-Based Authentication**: JWT (JSON Web Token)
  <!-- 토큰 기반 인증 방식으로 JWT 사용 -->

## Devops

- **배포 및 운영**: Docker, Docker-Compose

## Etc

- **외부 API 연동**
  -
        - **Gemini API** : 상품 발송 시한 예측 및 상품 설명 문구 추천을 위한 외부 AI API 연동.



