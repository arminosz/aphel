@echo off
setlocal

set JAR_PATH=src\aphelios\Aphel.jar

if "%~1"=="" (
    echo Error: No .aph file specified, usage: apheldebug file.aph
    exit /b 1
)

java -cp %JAR_PATH% aphelios.Aphel -d %1

endlocal
