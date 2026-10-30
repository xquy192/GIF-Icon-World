param([Parameter(Mandatory = $true)][string]$JavaExe)
$ErrorActionPreference = 'Stop'
try {
    if (-not (Test-Path -LiteralPath $JavaExe -PathType Leaf)) { exit 2 }
    $jdkHome = Split-Path -Parent (Split-Path -Parent $JavaExe)
    $releaseFile = Join-Path $jdkHome 'release'
    if (Test-Path -LiteralPath $releaseFile -PathType Leaf) {
        $releaseText = Get-Content -LiteralPath $releaseFile -Raw
        if ($releaseText -match '(?m)^JAVA_VERSION\s*=\s*"?([0-9]+)') {
            $major = [int]$Matches[1]
            if ($major -gt 0) { Write-Output $major; exit 0 }
        }
    }
    $versionOutput = (& $JavaExe -version 2>&1 | Out-String)
    foreach ($pattern in @('(?i)\bversion\s+"?([0-9]+)', '(?im)^\s*openjdk\s+([0-9]+)', '(?im)^\s*java\s+([0-9]+)')) {
        if ($versionOutput -match $pattern) {
            $major = [int]$Matches[1]
            if ($major -gt 0) { Write-Output $major; exit 0 }
        }
    }
    [Console]::Error.WriteLine('Could not parse Java version: ' + $versionOutput.Trim())
    exit 3
}
catch {
    [Console]::Error.WriteLine('Java version check failed: ' + $_.Exception.Message)
    exit 4
}
