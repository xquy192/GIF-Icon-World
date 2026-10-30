# Install an Eclipse Temurin JDK 25 for the current Windows user.
$ErrorActionPreference = 'Stop'
$ProgressPreference = 'SilentlyContinue'
[Net.ServicePointManager]::SecurityProtocol = [Net.SecurityProtocolType]::Tls12

$tools = Join-Path $env:LOCALAPPDATA 'GifIconWorldBuild'
$target = Join-Path $tools 'jdk-25'
$archive = Join-Path $tools 'jdk-25-windows-x64.zip'
$extract = Join-Path $tools 'jdk-25-extract'
New-Item -ItemType Directory -Path $tools -Force | Out-Null

try {
    [Console]::Error.WriteLine('Querying official Adoptium release metadata...')
    $metadataUrl = 'https://api.adoptium.net/v3/assets/latest/25/hotspot?architecture=x64&image_type=jdk&os=windows&vendor=eclipse'
    $assets = Invoke-RestMethod -Uri $metadataUrl -Headers @{ 'User-Agent' = 'GIF-Icon-World-build-script' }
    $asset = $assets | Select-Object -First 1
    if (-not $asset -or -not $asset.binary.package.link -or -not $asset.binary.package.checksum) {
        throw 'Adoptium did not provide a download URL and SHA-256 checksum.'
    }

    [Console]::Error.WriteLine('Downloading Temurin JDK 25...')
    Invoke-WebRequest -Uri $asset.binary.package.link -OutFile $archive -Headers @{ 'User-Agent' = 'GIF-Icon-World-build-script' }

    [Console]::Error.WriteLine('Verifying the downloaded archive (SHA-256)...')
    $actualHash = (Get-FileHash -LiteralPath $archive -Algorithm SHA256).Hash.ToLowerInvariant()
    $expectedHash = ([string]$asset.binary.package.checksum).ToLowerInvariant()
    if ($actualHash -ne $expectedHash) {
        throw 'The downloaded JDK checksum does not match release metadata. The archive was rejected.'
    }

    if (Test-Path -LiteralPath $extract) { Remove-Item -LiteralPath $extract -Recurse -Force }
    New-Item -ItemType Directory -Path $extract -Force | Out-Null
    Expand-Archive -LiteralPath $archive -DestinationPath $extract -Force

    $jdk = Get-ChildItem -LiteralPath $extract -Directory | Where-Object {
        (Test-Path -LiteralPath (Join-Path $_.FullName 'bin\java.exe')) -and
        (Test-Path -LiteralPath (Join-Path $_.FullName 'bin\javac.exe'))
    } | Select-Object -First 1
    if (-not $jdk) { throw 'The archive does not contain a complete JDK 25 installation.' }

    if (Test-Path -LiteralPath $target) { Remove-Item -LiteralPath $target -Recurse -Force }
    Move-Item -LiteralPath $jdk.FullName -Destination $target
    if (-not (Test-Path -LiteralPath (Join-Path $target 'bin\java.exe')) -or
        -not (Test-Path -LiteralPath (Join-Path $target 'bin\javac.exe'))) {
        throw 'The extracted JDK did not pass the installation check.'
    }

    Write-Output $target
}
catch {
    [Console]::Error.WriteLine('[JDK SETUP ERROR] ' + $_.Exception.Message)
    exit 1
}
finally {
    if (Test-Path -LiteralPath $archive) { Remove-Item -LiteralPath $archive -Force -ErrorAction SilentlyContinue }
    if (Test-Path -LiteralPath $extract) { Remove-Item -LiteralPath $extract -Recurse -Force -ErrorAction SilentlyContinue }
}
