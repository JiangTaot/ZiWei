# ============================================================
# ZiWei Server Dockerfile (多阶段构建)
# 阶段1: Maven 编译
# 阶段2: JRE 运行
# ============================================================

# ---- Stage 1: Build ----
FROM maven:3.9-eclipse-temurin-17 AS builder

WORKDIR /build
COPY pom.xml .
# 预先下载依赖（利用 Docker 缓存层）
RUN mvn dependency:go-offline -B -q

COPY src ./src
RUN mvn clean package -DskipTests -B -q

# ---- Stage 2: Runtime ----
FROM eclipse-temurin:17-jre-alpine

# 设置时区
RUN apk add --no-cache tzdata curl && \
    cp /usr/share/zoneinfo/Asia/Shanghai /etc/localtime && \
    echo "Asia/Shanghai" > /etc/timezone

WORKDIR /app

# 从构建阶段复制 JAR
COPY --from=builder /build/target/ziwei-server-*.jar app.jar

# 创建日志目录
RUN mkdir -p /app/logs

# 暴露服务端口
EXPOSE 48121

# JVM 参数（可根据实际内存调整）
ENV JAVA_OPTS="-Xms256m -Xmx512m -XX:+UseG1GC -XX:MaxGCPauseMillis=200"

# 默认 profile（可通过 -e 覆盖）
ENV SPRING_PROFILES_ACTIVE=docker

ENTRYPOINT ["sh", "-c", "java ${JAVA_OPTS} -jar app.jar"]
