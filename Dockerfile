# 1-qadam: Asosiy image (JDK 21)
FROM openjdk:21-jdk-slim

# 2-qadam: JAR faylni container ichiga nusxalash
WORKDIR /app
COPY target/*.jar online_education.jar

# 3-qadam: Ilovani ishga tushirish
ENTRYPOINT ["java", "-jar", "online_education.jar"]
