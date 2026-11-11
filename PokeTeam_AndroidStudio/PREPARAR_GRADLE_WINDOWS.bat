@echo off
cd /d "%~dp0"
powershell -NoProfile -Command "$ErrorActionPreference='Stop'; $root='https://raw.githubusercontent.com/gradle/gradle/v8.11.1/'; Invoke-WebRequest ($root+'gradle/wrapper/gradle-wrapper.jar') -OutFile 'gradle/wrapper/gradle-wrapper.jar'; Invoke-WebRequest ($root+'gradlew') -OutFile 'gradlew'; Invoke-WebRequest ($root+'gradlew.bat') -OutFile 'gradlew.bat'; Write-Host 'Gradle Wrapper preparado.'"
if errorlevel 1 echo Nao foi possivel baixar. Verifique a internet.
pause
