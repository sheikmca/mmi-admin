FROM registry.chengdu.local/vit/java11-tzdata:0.1.3

ADD ./target/app.jar .
ENV JAVA_OPTS=""
RUN mkdir -p ./conf
EXPOSE 80
ENTRYPOINT [ "sh", "-c", "java $JAVA_OPTS -Djava.security.egd=file:/dev/./urandom -jar /opt/vit/app.jar --spring.config.additional-location=/opt/vit/conf/" ]
