FROM openjdk:11

RUN addgroup --system --gid 1002 app && adduser --system --uid 1002 --gid 1002 appuser
USER 1002

ENV REFILE_HOME /opt/refile

COPY /build/libs/*.jar ${REFILE_HOME}/lib/app.jar

WORKDIR ${REFILE_HOME}
ENTRYPOINT ["java","-jar","lib/app.jar"]
