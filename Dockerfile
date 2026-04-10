FROM eclipse-temurin:17-jdk

WORKDIR /app

# ✅ Correct paths
COPY web ./web
COPY src ./src
COPY *.txt ./

# ❗ Main.java src ke andar hai
# isliye alag copy ki zarurat nahi

RUN mkdir -p bin

# ✅ Correct compile command
RUN javac -d bin src/Main.java src/com/waste/service/*.java src/com/waste/models/*.java

EXPOSE 8080

CMD ["java", "-cp", "bin", "Main"]
