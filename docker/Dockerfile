#This docker file is meant to start a client or repository into it

FROM debian

RUN apt update; apt install openjdk-11-jdk git xvfb wine -y; dpkg --add-architecture i386 && apt-get update && apt-get install wine32 -y 

RUN git clone --depth 1 https://github.com/k1nd0ne/FTP-P2P.git; cd FTP-P2P; bash installWEB.sh

WORKDIR FTP-P2P

RUN bash installWEB.sh

ENTRYPOINT ["wine"]




