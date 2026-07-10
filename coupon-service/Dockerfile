# ============================================================
# coupon-service 优惠券微服务 Dockerfile
# ============================================================

FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /build

# 1. 复制依赖描述文件
COPY pom.xml .
COPY common/pom.xml common/
COPY coupon-service/pom.xml coupon-service/

# 1.5 为父 pom 中声明的缺失模块创建占位目录（避免 Maven Reactor 报错）
RUN for d in gateway mailadmin user-service cart-service order-service refund-service review-service chat-service; do \
      mkdir -p "$d" && \
      printf '<project xmlns="http://maven.apache.org/POM/4.0.0"><modelVersion>4.0.0</modelVersion><groupId>com.example</groupId><artifactId>%s</artifactId><version>1.0-SNAPSHOT</version></project>\n' "$d" > "$d/pom.xml"; \
    done

# 2. 下载依赖
RUN mvn dependency:go-offline -pl common,coupon-service -DskipTests || true

# 3. 复制源码
COPY common/src common/src/
COPY coupon-service/src coupon-service/src/

# 4. 编译打包
RUN mvn install -N -DskipTests && \
    mvn install -pl common -DskipTests && \
    mvn package -pl coupon-service -DskipTests -Dspring-boot.skip=false

# ============================================================
# 运行时镜像
# ============================================================
FROM eclipse-temurin:17-jre

ENV TZ=Asia/Shanghai
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone

WORKDIR /app

COPY --from=build /build/coupon-service/target/coupon-service-0.0.1-SNAPSHOT.jar /app/app.jar

ENV JAVA_OPTS="-Xms128m -Xmx256m"

EXPOSE 8086

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/app.jar"]
