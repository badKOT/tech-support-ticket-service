$ErrorActionPreference = "Stop"

$ImageName = "tech-support-backend"
$Server = "ticketuser@88.218.67.241"

$ImageTag = (git rev-parse --short HEAD).Trim()

if (-not $ImageTag) {
    throw "Could not determine Git commit SHA"
}

$Image = "${ImageName}:${ImageTag}"
$TarFile = "${ImageName}-${ImageTag}.tar"

Write-Host "Building $Image"

docker build -t $Image .

if ($LASTEXITCODE -ne 0) {
    throw "Docker build failed"
}

Write-Host "Saving $Image"

docker save $Image -o $TarFile

if ($LASTEXITCODE -ne 0) {
    throw "Docker save failed"
}

Write-Host "Uploading $TarFile"

scp `
    -o ServerAliveInterval=15 `
    -o ServerAliveCountMax=20 `
    $TarFile `
    "${Server}:/tmp/$TarFile"

if ($LASTEXITCODE -ne 0) {
    throw "SCP upload failed"
}

Remove-Item $TarFile

Write-Host ""
Write-Host "Backend image uploaded successfully."
Write-Host "Image: $Image"
Write-Host ""
Write-Host "Run on Kubernetes VM:"
Write-Host "./scripts/deploy-backend.sh $ImageTag"