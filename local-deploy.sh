#!/usr/bin/env bash
#本地部署
mkdir logs
mvn -Dmaven.test.skip=true clean package
cd web
mvn -Dmaven.test.skip=true io.fabric8:docker-maven-plugin:0.23.0:build
cd ..
docker stack deploy --compose-file local-docker-compose.yml sb

#使用以下指令 删除集群
#docker stack rm sb

#若有必要删除mysql 数据就
#docker volume rm sb_database