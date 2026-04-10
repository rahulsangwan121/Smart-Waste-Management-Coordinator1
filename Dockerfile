# 1. Java environment
FROM eclipse-temurin:17-jdk

# 2. Work directory
WORKDIR /app

# 3. Saari files copy karo
COPY . .

# 4. Bin folder banao aur compile karo
RUN mkdir -p bin
# DHAYAN DEIN: Yahan 'src/Main.java' ki jagah sirf 'Main.java' likha hai
RUN javac -d bin Main.java src/com/waste/service/*.java src/com/waste/models/*.java

# 5. Port expose
EXPOSE 8080

# 6. Run the application
# Agar Main.java mein koi package nahi hai, toh ye sahi hai
CMD ["java", "-cp", "bin", "Main"]
