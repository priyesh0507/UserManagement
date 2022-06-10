FROM openjdk:8
ADD ./target/user-mgmnt-1.0.jar user-mgmnt-1.0.jar
ENTRYPOINT ["java","-jar","/user-mgmnt-1.0.jar"]
