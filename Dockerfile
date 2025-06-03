# Step 1: OpenJDK 17 이미지를 기반으로 사용
FROM openjdk:17-jdk-slim

# Step 2: 작업 디렉토리 설정
WORKDIR /app

# Step 3: 빌드된 JAR 파일을 컨테이너로 복사
COPY build/libs/*.jar app.jar

# Step 4: 애플리케이션 실행 명령
ENTRYPOINT ["java", "-jar", "app.jar"]

# Step 5: 포트 열기 (8080)
EXPOSE 8080
