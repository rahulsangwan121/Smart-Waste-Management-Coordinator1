FROM eclipse-temurin:17-jdk

WORKDIR /app

# ✅ Important explicit copies
COPY web ./web
COPY src ./src
COPY *.txt ./
COPY Main.java ./

RUN mkdir -p bin
RUN javac -d bin src/Main.java src/com/waste/service/*.java src/com/waste/models/*.java

EXPOSE 8080

CMD ["java", "-cp", "bin", "Main"]
