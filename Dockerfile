#指定创建镜像的基础镜像
FROM amazoncorretto:17
#运行Linux系统的命令使用
RUN ln -sf /usr/share/zoneinfo/Asia/Shanghai /etc/localtime \
    && echo "Asia/Shanghai" > /etc/timezone
#指定镜像容器监听端口号；发布服务使用
EXPOSE 9527
RUN mkdir -p /apps/svr
COPY "./cloud-gateway9527-1.0-SNAPSHOT.jar" "/apps/svr/cloud-gateway.jar"
#切换到镜像容器中的指定目录中
WORKDIR /apps/svr
#指定运行容器启动过程执行命令，覆盖CMD参数
ENTRYPOINT ["java","-jar","-Dfile.encoding=utf-8","cloud-gateway.jar"]