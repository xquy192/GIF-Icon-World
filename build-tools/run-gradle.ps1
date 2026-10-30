param(
    [Parameter(Mandatory = $true)][string]$GradleBat,
    [Parameter(Mandatory = $true)][string]$ProjectDir,
    [Parameter(Mandatory = $true)][string]$LogFile
)

$ErrorActionPreference = 'Stop'
$exitCode = 1
$locationPushed = $false

try {
    if (-not (Test-Path -LiteralPath $GradleBat -PathType Leaf)) {
        throw "Gradle was not found: $GradleBat"
    }
    if (-not (Test-Path -LiteralPath $ProjectDir -PathType Container)) {
        throw "Project directory was not found: $ProjectDir"
    }

    Push-Location -LiteralPath $ProjectDir
    $locationPushed = $true

    Write-Host "Project: $ProjectDir"
    Write-Host "Gradle:  $GradleBat"
    Write-Host "Log:     $LogFile"
    Write-Host '------------------------------------------------------------'

    & $GradleBat --no-daemon --stacktrace clean build 2>&1 | Tee-Object -FilePath $LogFile
    $gradleExitCode = $LASTEXITCODE
    if ($null -eq $gradleExitCode) { $gradleExitCode = 1 }

    Write-Host '------------------------------------------------------------'
    Write-Host "Gradle exit code: $gradleExitCode"
    $exitCode = [int]$gradleExitCode
}
catch {
    [Console]::Error.WriteLine("[BUILD HELPER ERROR] " + $_.Exception.Message)
    $exitCode = 1
}
finally {
    if ($locationPushed) {
        Pop-Location
    }
}

exit $exitCode
