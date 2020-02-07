@echo off 
chcp 65001
set classpath=libraries\*;src\main\java
javac src/main/java/IndexPackage/*.java
java IndexPackage.IndexBuilder
pause