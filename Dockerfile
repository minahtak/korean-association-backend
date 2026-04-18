# 1. 자바 17 버전을 가져옵니다 (스프링 부트 3.x 기본 설정)
FROM eclipse-temurin:17-jdk

# 2. 작업할 폴더를 만듭니다
WORKDIR /app

# 3. 내 컴퓨터에 있는 프로젝트 파일들을 모두 복사해 넣습니다
COPY . .

# 4. 윈도우에서 만든 gradlew 파일이 리눅스에서도 잘 실행되도록 오류를 방지합니다 (매우 중요!)
RUN sed -i 's/\r$//' gradlew
RUN chmod +x ./gradlew

# 5. 프로젝트를 빌드합니다 (테스트는 건너뜀)
RUN ./gradlew build -x test

# 6. 빌드된 jar 파일을 실행합니다
CMD ["sh", "-c", "java -jar build/libs/*.jar"]