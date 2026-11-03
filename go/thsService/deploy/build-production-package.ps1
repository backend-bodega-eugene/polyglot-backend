$ErrorActionPreference = "Stop"

$ProjectRoot = Split-Path -Parent $PSScriptRoot
$DistDirectory = Join-Path $PSScriptRoot "dist"
$StageDirectory = Join-Path $DistDirectory "package"
$ImageArchive = Join-Path $StageDirectory "ths-service-api.tar"
$PackageArchive = Join-Path $DistDirectory "ths-service-production.zip"

if (Test-Path -LiteralPath $StageDirectory) {
    $resolvedDist = [System.IO.Path]::GetFullPath($DistDirectory)
    $resolvedStage = [System.IO.Path]::GetFullPath($StageDirectory)
    if (-not $resolvedStage.StartsWith($resolvedDist + [System.IO.Path]::DirectorySeparatorChar)) {
        throw "Refusing to remove staging directory outside deploy/dist"
    }
    Remove-Item -LiteralPath $StageDirectory -Recurse -Force
}

New-Item -ItemType Directory -Path $StageDirectory | Out-Null
New-Item -ItemType Directory -Path (Join-Path $StageDirectory "migrations") | Out-Null
New-Item -ItemType Directory -Path (Join-Path $StageDirectory "deploy/nginx/conf.d") | Out-Null
New-Item -ItemType Directory -Path (Join-Path $StageDirectory "deploy/cloudflare") | Out-Null

Push-Location $ProjectRoot
try {
    docker build --platform linux/amd64 -t ths-service-api:latest .
    if ($LASTEXITCODE -ne 0) {
        throw "Docker image build failed"
    }

    docker save --output $ImageArchive ths-service-api:latest
    if ($LASTEXITCODE -ne 0) {
        throw "Docker image export failed"
    }

    Copy-Item -LiteralPath "compose.production.yaml" -Destination $StageDirectory
    Copy-Item -LiteralPath ".env.production.example" -Destination $StageDirectory
    Copy-Item -LiteralPath "deploy/DEPLOYMENT.md" -Destination $StageDirectory
    Copy-Item -LiteralPath "deploy/APK-DOWNLOAD.md" -Destination $StageDirectory
    Get-ChildItem -LiteralPath "migrations" -Filter "*.sql" |
        Copy-Item -Destination (Join-Path $StageDirectory "migrations")
    Copy-Item -LiteralPath "deploy/production-deploy.sh" -Destination (Join-Path $StageDirectory "deploy")
    Copy-Item -LiteralPath "deploy/nginx/conf.d/default.conf" -Destination (Join-Path $StageDirectory "deploy/nginx/conf.d")

    # Antivirus/indexing can briefly hold newly copied staging files open.
    for ($archiveAttempt = 1; $archiveAttempt -le 3; $archiveAttempt++) {
        try {
            Compress-Archive -Path (Join-Path $StageDirectory "*") -DestinationPath $PackageArchive -Force -ErrorAction Stop
            break
        }
        catch {
            if ($archiveAttempt -eq 3) { throw }
            Start-Sleep -Milliseconds 1000
        }
    }
}
finally {
    Pop-Location
}

Write-Host "Production package created: $PackageArchive"
