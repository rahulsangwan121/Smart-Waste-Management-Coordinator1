# 1. Java 17 environment setup
FROM eclipse-temurin:17-jdk

# 2. Project directory set karna
WORKDIR /app

# 3. Saari files copy karna (src, web, bins.txt etc.)
COPY . .

# 4. 'bin' folder banana aur Java files ko compile karna
# Note: Aapka folder structure src/com/waste/service hai isliye ye command perfect hai
RUN mkdir -p bin
RUN javac -d bin src/Main.java src/com/waste/service/*.java

# 5. Render ke dynamic port ke liye expose (optional par achhi practice hai)
EXPOSE 8080

# 6. Main class ko run karne ki command
# CP (Classpath) bin folder hai aur Main class root par hai
CMD ["java", "-cp", "bin", "Main"]
