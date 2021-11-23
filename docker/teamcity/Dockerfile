FROM jetbrains/teamcity-agent
RUN apt-get update -yq \
        && apt-get install bsdmainutils -yq \
        && apt-get install tree -yq \
        && apt-get install openjdk-8-jdk -yq \
        && apt-get install openjdk-17-jdk -yp \
        && apt-get install binutils -yq \
        && apt-get install lsof -yq \
        && apt-get install curl -yq       
