#!/usr/bin/env sh
set -eu
cd "$(dirname "$0")"
command -v git >/dev/null || { echo "Instale o Git antes de continuar."; exit 1; }
if [ ! -d .git ]; then git init -b main; fi
EXPECTED="https://github.com/renanmulinarixz/Atividade-Final---Android.git"
ORIGIN="$(git remote get-url origin 2>/dev/null || true)"
if [ -z "$ORIGIN" ]; then
  git remote add origin "$EXPECTED"
elif [ "$ORIGIN" != "$EXPECTED" ]; then
  echo "Remoto existente e diferente; envio interrompido: $ORIGIN"
  exit 1
fi
git add .
git commit -m "Atividade final Android - PokeTeam" || true
git branch -M main
git push -u origin main
