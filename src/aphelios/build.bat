@echo off
setlocal

set BIN_DIR=bin

if not exist %BIN_DIR% (
    mkdir %BIN_DIR%
)

javac -d %BIN_DIR% *.java

if errorlevel 1 (
    echo Compilation Error!
    exit /b 1
)

jar cvf Aphel.jar -C %BIN_DIR% .

if errorlevel 1 (
    echo Error in creating the jar file!
    exit /b 1
)

echo Build successful!
endlocal
