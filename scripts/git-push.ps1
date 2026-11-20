# Push current tree to origin/main
$ErrorActionPreference = "Stop"
$Root = Split-Path -Parent $PSScriptRoot
Set-Location $Root

git remote set-url origin https://github.com/K0Teu4/HabitTracker.git
git add -A
git status
git commit -m "Habit Tracker 0.5.0 — onboarding visual, dark theme, day chips, UI fixes" 2>$null
if ($LASTEXITCODE -ne 0) { Write-Host "Nothing to commit or commit failed (ok if no changes)" }
git push -u origin main
Write-Host "Done: https://github.com/K0Teu4/HabitTracker" -ForegroundColor Green
