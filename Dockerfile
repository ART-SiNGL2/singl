FROM docker.io/bitnami/spark:3.1.2
ENV SPARK_MASTER local[*]
ENV SINGL_HOME /singl-2.0-0.3.3-SNAPSHOT
ENV PATH $SINGL_HOME/scripts:$PATH
ENV LANG C.UTF-8
WORKDIR /
USER root
WORKDIR /singl-2.0-0.3.3-SNAPSHOT
RUN curl --location https://github.com/zinggAI/zingg/releases/download/v0.3.3/zingg-0.3.3-SNAPSHOT-spark-3.1.2.tar.gz | \
tar --extract --gzip --strip=1 
RUN chmod -R a+rwx /zingg-2.0-0.3.3-SNAPSHOT/models
RUN chown -R 1001 /zingg-2.0-0.3.3-SNAPSHOT/models
USER 1001

