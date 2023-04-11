FROM adoptopenjdk/openjdk8:ubi-slim AS base
ENV VIRTUAL_ENV=/opt/synapse/pythonvenv \
    PATH="$VIRTUAL_ENV/bin:$PATH"
RUN dnf install python38 -y && \
    dnf module install nodejs:16 -y && \
    dnf clean all
RUN python3 -m ensurepip --upgrade && \
    pip3 install --upgrade pip --no-cache-dir && \
    python3 -m venv $VIRTUAL_ENV

FROM base AS build
RUN dnf install maven -y
COPY . /tmp/synapse
WORKDIR /tmp/synapse
RUN mvn clean package -q -DskipTests

FROM base
COPY --from=build /tmp/synapse/synapse-web/target/synapse*SNAPSHOT.jar /opt/synapse/synapse.jar
RUN useradd synapseuser && \
    chown -R synapseuser /opt/synapse/
USER synapseuser
WORKDIR /opt/synapse/
RUN pip3 install checkov==2.0.257 --no-cache-dir
RUN npm --prefix ./eslint_work install eslint@7.24.0 
ENTRYPOINT ["java", "-jar", "/opt/synapse/synapse.jar"]