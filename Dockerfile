# 1. Java environment setup
FROM eclipse-temurin:17-jdk

# 2. Project directory in container
WORKDIR /app

# 3. Saari files copy karein
COPY . .

# 4. Bin folder banakar compile karein
RUN mkdir -p bin
# DHAYAN DEIN: Yahan hum 'src/Main.java' use kar rahe hain
RUN javac -d bin src/Main.java src/com/waste/service/*.java src/com/waste/models/*.java

# 5. Port define karein
EXPOSE 8080

# 6. App run karein
# Kyunki Main.java default package mein hai aur 'bin' mein compile hui hai
CMD ["java", "-cp", "bin", "Main"]
