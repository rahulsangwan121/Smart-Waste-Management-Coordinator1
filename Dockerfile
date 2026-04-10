FROM eclipse-temurin:17-jdk

WORKDIR /app

# ✅ Correct paths (IMPORTANT)
COPY src ./src
COPY src/web ./web   # 🔥 YE LINE FIX HAI
COPY *.txt ./

RUN mkdir -p bin

RUN javac -d bin src/Main.java src/com/waste/service/*.java src/com/waste/models/*.java

EXPOSE 8080

CMD ["java", "-cp", "bin", "Main"]
