# ============================================================
# gateway 微服务 Dockerfile
# 多阶段构建：Maven 编译 + JRE 运行
# ============================================================

FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /build

# 1. 先复制依赖描述文件（利用 Docker 缓存层）
COPY pom.xml .
COPY common/pom.xml common/
COPY gateway/pom.xml gateway/

# 1.5 为父 pom 中声明的缺失模块创建占位目录（避免 Maven Reactor 报错）
RUN for d in mailadmin user-service cart-service order-service coupon-service refund-service review-service chat-service; do \
      mkdir -p "$d" && \
      printf '<project xmlns="http://maven.apache.org/POM/4.0.0"><modelVersion>4.0.0</modelVersion><groupId>com.example</groupId><artifactId>%s</artifactId><version>1.0-SNAPSHOT</version></project>\n' "$d" > "$d/pom.xml"; \
    done

# 2. 下载依赖（此层可缓存，源码未变时不会重复下载）
RUN mvn dependency:go-offline -pl common,gateway -DskipTests || true

# 3. 复制源码
COPY common/src common/src/
COPY gateway/src gateway/src/

# 4. 编译打包
RUN mvn install -N -DskipTests && \
    mvn install -pl common -DskipTests && \
    mvn package -pl gateway -DskipTests -Dspring-boot.skip=false

# ============================================================
# 运行时镜像：只包含 JRE，体积小
# ============================================================
FROM eclipse-temurin:17-jre

# 设置时区
ENV TZ=Asia/Shanghai
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone

WORKDIR /app

COPY --from=build /build/gateway/target/gateway-1.0-SNAPSHOT.jar /app/app.jar

# JVM 参数（生产环境可调整堆内存）
ENV JAVA_OPTS="-Xms128m -Xmx256m"

EXPOSE 8080

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/app.jar"]
