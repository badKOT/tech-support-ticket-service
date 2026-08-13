Write-Host "Stopping Tech Support..." -ForegroundColor Cyan

$ports = @(5173, 8080)

foreach ($port in $ports) {
    $connections = Get-NetTCPConnection `
        -LocalPort $port `
        -State Listen `
        -ErrorAction SilentlyContinue

    if (-not $connections) {
        Write-Host "Port $port is free" -ForegroundColor DarkGray
        continue
    }

    $processIds = $connections |
        Select-Object -ExpandProperty OwningProcess -Unique

    foreach ($processId in $processIds) {
        try {
            $process = Get-Process `
                -Id $processId `
                -ErrorAction Stop

            Write-Host `
                "Stopping $($process.ProcessName) (PID $processId) on port $port..." `
                -ForegroundColor Yellow

            Stop-Process `
                -Id $processId `
                -Force `
                -ErrorAction Stop

            Write-Host `
                "Port $port stopped" `
                -ForegroundColor Green
        }
        catch {
            Write-Host `
                "Failed to stop PID $processId on port ${port}: $($_.Exception.Message)" `
                -ForegroundColor Red
        }
    }
}

Write-Host "Done." -ForegroundColor Cyan