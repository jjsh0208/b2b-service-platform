# 1️⃣ 빌드 단계
FROM eclipse-temurin:17-jdk AS builder

WORKDIR /com.devsquad10.product

# Gradle 캐시 최적화를 위해 Gradle Wrapper 및 관련 파일만 먼저 복사
COPY gradlew .
COPY gradle gradle
COPY build.gradle .
COPY settings.gradle .

# Gradle 실행 권한 부여
RUN chmod +x gradlew

# 의존성 캐싱을 위한 빈 Gradle 빌드 수행
RUN ./gradlew dependencies --no-daemon

# 프로젝트 전체 복사
COPY . .

# Gradle 실행 권한 부여
RUN chmod +x gradlew

# JAR 빌드
RUN ./gradlew clean bootJar

# 2️⃣ 실행 단계
FROM eclipse-temurin:17-jdk

WORKDIR /com.devsquad10.product

# 빌드된 JAR 파일만 복사
COPY --from=builder /com.devsquad10.product/build/libs/*.jar app.jar

# 컨테이너에서 사용할 포트 설정
EXPOSE 8080

# 백그라운드 실행을 위해 ENTRYPOINT 설정
ENTRYPOINT ["java", "-Djava.security.egd=file:/dev/./urandom", "-jar", "app.jar"]