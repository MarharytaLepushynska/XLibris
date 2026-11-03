FROM eclipse-temurin:25-jre

WORKDIR /xlibris

RUN groupadd --system xlibrisgroup \
    && useradd --system --gid xlibrisgroup xlibrisuser

COPY --chown=xlibrisuser:xlibrisgroup build/libs/xlibris.jar server.jar

USER xlibrisuser

ENTRYPOINT ["java", "-jar", "server.jar"]