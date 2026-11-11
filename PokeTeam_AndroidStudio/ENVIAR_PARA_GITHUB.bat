@echo off
cd /d "%~dp0"
where git >nul 2>nul
if errorlevel 1 (
  echo Git nao encontrado. Instale o Git para Windows.
  pause
  exit /b 1
)
if not exist .git (
  git init -b main
  if errorlevel 1 goto erro
)
for /f "delims=" %%A in ('git remote get-url origin 2^>nul') do set "ORIGIN=%%A"
if defined ORIGIN (
  if /i not "%ORIGIN%"=="https://github.com/renanmulinarixz/Atividade-Final---Android.git" (
    echo A pasta ja tem um repositorio remoto diferente: %ORIGIN%
    echo Por seguranca, nenhuma alteracao sera enviada.
    pause
    exit /b 1
  )
) else (
  git remote add origin https://github.com/renanmulinarixz/Atividade-Final---Android.git
)
git add .
git commit -m "Atividade final Android - PokeTeam"
git branch -M main
git push -u origin main
if errorlevel 1 goto erro
echo Projeto enviado ao GitHub.
pause
exit /b 0
:erro
echo Falha na operacao. Confira seu login do GitHub e a configuracao do Git.
pause
exit /b 1
