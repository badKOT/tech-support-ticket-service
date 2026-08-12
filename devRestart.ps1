Write-Host "Restarting Tech Support..." -ForegroundColor Cyan

$rootPath = $PSScriptRoot

Write-Host "Stopping current processes..." -ForegroundColor Yellow

& "$rootPath\devStop.ps1"

Start-Sleep -Seconds 2

Write-Host "Starting application..." -ForegroundColor Green

& "$rootPath\dev.ps1"