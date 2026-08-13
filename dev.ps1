Write-Host "Starting Tech Support..." -ForegroundColor Cyan

$frontendPath = Join-Path $PSScriptRoot "frontend"

Write-Host "Starting Vite..." -ForegroundColor Green

Start-Process powershell `
    -ArgumentList @(
        "-NoExit",
        "-Command",
        "Set-Location '$frontendPath'; npm run dev"
    )

Write-Host "Starting Spring Boot..." -ForegroundColor Green

Set-Location $PSScriptRoot

.\gradlew.bat bootRun