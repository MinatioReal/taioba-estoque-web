@echo off
chcp 65001 >nul
cd /d "%~dp0"
where java >nul 2>nul
if errorlevel 1 (
  echo Instale Java 17 ou superior para executar a Taioba.
  pause
  exit /b 1
)
if exist "taioba-web-1.0.0.jar" (
  set "TAIOBA_JAR=taioba-web-1.0.0.jar"
) else (
  set "TAIOBA_JAR=target\taioba-web-1.0.0.jar"
)
if not exist "%TAIOBA_JAR%" (
  echo Compile primeiro o projeto no NetBeans com Limpar e Construir.
  echo Ou use: mvn clean package
  pause
  exit /b 1
)
echo Taioba - abra http://localhost:8085 depois que o servidor iniciar.
echo Login: admin / admin123
echo Para encerrar, pressione Ctrl+C nesta janela.
java -jar "%TAIOBA_JAR%"
pause
