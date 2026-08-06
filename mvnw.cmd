@echo off
setlocal
set MAVEN_VERSION=3.9.16
if not defined MAVEN_USER_HOME set MAVEN_USER_HOME=%USERPROFILE%\.m2
set MAVEN_HOME=%MAVEN_USER_HOME%\wrapper\dists\apache-maven-%MAVEN_VERSION%
if exist "%MAVEN_HOME%\bin\mvn.cmd" goto run
powershell -NoProfile -Command "$u='https://repo.maven.apache.org/maven2/org/apache/maven/apache-maven/%MAVEN_VERSION%/apache-maven-%MAVEN_VERSION%-bin.zip'; $z='$env:TEMP\apache-maven-%MAVEN_VERSION%-bin.zip'; Invoke-WebRequest $u -OutFile $z; New-Item -ItemType Directory -Force '%MAVEN_HOME%' | Out-Null; Expand-Archive -Force $z '$env:TEMP\maven-wrapper'; Copy-Item -Recurse -Force '$env:TEMP\maven-wrapper\apache-maven-%MAVEN_VERSION%\*' '%MAVEN_HOME%'
:run
call "%MAVEN_HOME%\bin\mvn.cmd" %*
