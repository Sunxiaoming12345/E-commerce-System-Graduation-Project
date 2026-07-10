FROM eclipse-temurin:17-jre
ENV TZ=Asia/Shanghai
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone
WORKDIR /app
COPY app.jar /app/app.jar
ENV JAVA_OPTS="-Xms128m -Xmx256m"
EXPOSE 8089
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/app.jar"]
