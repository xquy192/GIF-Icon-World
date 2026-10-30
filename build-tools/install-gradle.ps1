param(
    [Parameter(Mandatory = $true)][string]$Version,
    [Parameter(Mandatory = $true)][string]$InstallRoot
)
$ErrorActionPreference = 'Stop'
$ProgressPreference = 'SilentlyContinue'
[Net.ServicePointManager]::SecurityProtocol = [Net.SecurityProtocolType]::Tls12

$archive = Join-Path $InstallRoot ("gradle-{0}-bin.zip" -f $Version)
$target = Join-Path $InstallRoot ("gradle-{0}" -f $Version)
$url = "https://services.gradle.org/distributions/gradle-$Version-bin.zip"

try {
    New-Item -ItemType Directory -Path $InstallRoot -Force | Out-Null
    Write-Host '[GRADLE] Downloading the official Gradle distribution...'
    Invoke-WebRequest -Uri $url -OutFile $archive -Headers @{ 'User-Agent' = 'GIF-Icon-World-build-helper' }
    if (-not (Test-Path -LiteralPath $archive) -or (Get-Item -LiteralPath $archive).Length -lt 1000000) {
        throw 'The downloaded Gradle archive is missing or unexpectedly small.'
    }

    if (Test-Path -LiteralPath $target) { Remove-Item -LiteralPath $target -Recurse -Force }
    Write-Host '[GRADLE] Extracting distribution...'
    Expand-Archive -LiteralPath $archive -DestinationPath $InstallRoot -Force
    if (-not (Test-Path -LiteralPath (Join-Path $target 'bin\gradle.bat'))) {
        throw "Gradle extraction check failed: $target\bin\gradle.bat is missing."
    }
    Write-Host "[GRADLE] Gradle $Version is ready."
    exit 0
}
catch {
    Write-Error ('[GRADLE SETUP ERROR] ' + $_.Exception.Message)
    exit 1
}
finally {
    if (Test-Path -LiteralPath $archive) { Remove-Item -LiteralPath $archive -Force -ErrorAction SilentlyContinue }
}
