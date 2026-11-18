# Init git and push to K0Teu4/HabitTracker (empty repo)
$ErrorActionPreference = "Stop"
$Root = Split-Path -Parent $PSScriptRoot
Set-Location $Root

$remote = "https://github.com/K0Teu4/HabitTracker.git"

if (-not (Test-Path ".git")) {
    git init
    git branch -M main
}

git add .
git status

$msg = "Habit Tracker 0.3.0 — Quiet Precision UI, MVP habits/streak/calendar"
git commit -m $msg

$existing = git remote 2>$null
if ($existing -notcontains "origin") {
    git remote add origin $remote
} else {
    git remote set-url origin $remote
}

Write-Host "Pushing to $remote ..." -ForegroundColor Cyan
git push -u origin main

Write-Host "Done. https://github.com/K0Teu4/HabitTracker" -ForegroundColor Green
