#编译系统
FROM maven:3.5.2-jdk-8 as builder

ADD . /build/
WORKDIR /build/
RUN ["mvn","-DskipTests=true","package"]
#到了这里的时候肯定是线上版本了，所以直接打包

FROM registry.cn-shanghai.aliyuncs.com/mingshz/single-app-tomcat
COPY context.xml /deploy/
COPY --from=builder /build/web/target/web-*.war /deploy/
RUN mv /deploy/web-*.war /deploy/ROOT.war
RUN rm -rf ${CATALINA_HOME}/webapps/*
VOLUME ["/data/resources"]

HEALTHCHECK CMD curl -f http://localhost:8080/MP_verify_works.txt || exit 1
