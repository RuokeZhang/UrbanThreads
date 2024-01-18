# 使用 Java 17 运行时基础镜像
FROM openjdk:17-jdk-slim

MAINTAINER test

# 从之前的构建阶段（build 阶段）复制构建好的应用程序（JAR 文件）到运行时镜像的 /usr/app 目录下
COPY inventory-service/target/inventory-service-0.0.1-SNAPSHOT.jar inventory-service.jar

# 设置容器启动时执行的命令，这里是使用 Java 运行 /usr/app/app.jar 文件
ENTRYPOINT ["java","-jar","inventory-service.jar"]

EXPOSE 8080