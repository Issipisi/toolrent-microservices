@echo off
echo ============================================
echo  INICIANDO TOOLRENT MICROSERVICES
echo ============================================
echo.

REM CONFIGURAR JAVA PARA ESTA SESION
set JAVA_HOME=D:\Program Files\Eclipse Adoptium\jdk-17.0.17.10-hotspot
set PATH=%JAVA_HOME%\bin;%PATH%

echo Java configurado: %JAVA_HOME%
echo.

REM VERIFICAR
java -version
echo.

REM MENU
echo Selecciona que servicio iniciar:
echo 1. Config Server (puerto 8081)
echo 2. Eureka Server (puerto 8761)
echo 3. API Gateway (puerto 8080)
echo 4. Tools Service
echo 5. Loans Service
echo 6. Iniciar TODOS (recomendado)
echo.
set /p choice="Opcion: "

if "%choice%"=="1" goto config
if "%choice%"=="2" goto eureka
if "%choice%"=="3" goto gateway
if "%choice%"=="4" goto tools
if "%choice%"=="5" goto loans
if "%choice%"=="6" goto all

:config
cd config-service
mvnw.cmd spring-boot:run
goto end

:eureka
cd eureka-service
mvnw.cmd spring-boot:run
goto end

:gateway
cd api-gateway
mvnw.cmd spring-boot:run
goto end

:tools
cd tools-service
mvnw.cmd spring-boot:run
goto end

:loans
cd loans-service
mvnw.cmd spring-boot:run
goto end

:all
echo Iniciando todos los servicios en terminales separadas...
start "Config Server" cmd /k "cd /d %CD%\config-service && set JAVA_HOME=D:\Program Files\Eclipse Adoptium\jdk-17.0.17.10-hotspot && set PATH=%%JAVA_HOME%%\bin;%%PATH%% && mvnw.cmd spring-boot:run"
timeout /t 10 /nobreak >nul
start "Eureka Server" cmd /k "cd /d %CD%\eureka-service && set JAVA_HOME=D:\Program Files\Eclipse Adoptium\jdk-17.0.17.10-hotspot && set PATH=%%JAVA_HOME%%\bin;%%PATH%% && mvnw.cmd spring-boot:run"
timeout /t 15 /nobreak >nul
start "API Gateway" cmd /k "cd /d %CD%\api-gateway && set JAVA_HOME=D:\Program Files\Eclipse Adoptium\jdk-17.0.17.10-hotspot && set PATH=%%JAVA_HOME%%\bin;%%PATH%% && mvnw.cmd spring-boot:run"
timeout /t 10 /nobreak >nul
start "Tools Service" cmd /k "cd /d %CD%\tools-service && set JAVA_HOME=D:\Program Files\Eclipse Adoptium\jdk-17.0.17.10-hotspot && set PATH=%%JAVA_HOME%%\bin;%%PATH%% && mvnw.cmd spring-boot:run"
start "Loans Service" cmd /k "cd /d %CD%\loans-service && set JAVA_HOME=D:\Program Files\Eclipse Adoptium\jdk-17.0.17.10-hotspot && set PATH=%%JAVA_HOME%%\bin;%%PATH%% && mvnw.cmd spring-boot:run"
echo.
echo Todos los servicios iniciados!
echo.
echo URLs para verificar:
echo 1. Config: http://localhost:8081/tools-service/default
echo 2. Eureka: http://localhost:8761
echo 3. Gateway: http://localhost:8080/actuator/health
echo 4. Tools: http://localhost:8080/api/tools/actuator/health
pause
goto end

:end