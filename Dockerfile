# 1. Java environment
FROM eclipse-temurin:17-jdk

# 2. Work directory
WORKDIR /app

# 3. Saari files copy karo
COPY . .

# 4. Bin folder banao aur compile karo
RUN mkdir -p bin
# Sab kuch 'src' ke andar hai isliye path change kiya
RUN javac -d bin src/Main.java src/com/waste/service/*.java src/com/waste/models/*.java

# 5. Port expose
EXPOSE 8080

# 6. Run the application
CMD ["java", "-cp", "bin", "Main"]
