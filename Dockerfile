FROM openjdk:11-jdk-slim
EXPOSE 50052
ARG JAR_FILE=build/libs/*-all.jar
ADD ${JAR_FILE} grpc.jar
ENV APP_NAME olucas-pix-grpc
ENTRYPOINT ["java", "-jar", "/olucas-pix-grpc.jar"]