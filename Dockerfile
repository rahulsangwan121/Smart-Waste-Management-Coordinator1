# 1. Java environment setup
FROM eclipse-temurin:17-jdk

# 2. Project directory in container
WORKDIR /app

# 3. Saari files copy karein
COPY . .

# 4. Bin folder banakar compile karein
RUN mkdir -p bin
# 'Main.java' root par hai aur 'src' folder bhi root par hai
RUN javac -d bin Main.java src/com/waste/service/*.java src/com/waste/models/*.java

# 5. Port define karein
EXPOSE 8080

# 6. App run karein (No package in Main)
CMD ["java", "-cp", "bin", "Main"]
