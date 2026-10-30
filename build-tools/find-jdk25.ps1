param([Parameter(Mandatory = $false)][string]$ManualPath)
$ErrorActionPreference = 'SilentlyContinue'
$candidates = New-Object 'System.Collections.Generic.List[string]'

function Add-JdkHome([string]$Value) {
    if ([string]::IsNullOrWhiteSpace($Value)) { return }
    $path = $Value.Trim().Trim('"').Trim()
    if (-not $path) { return }
    $leaf = [IO.Path]::GetFileName($path)
    if (Test-Path -LiteralPath $path -PathType Leaf) {
        if ($leaf -imatch '^(java|javaw|javac)\.exe$') {
            $path = Split-Path -Parent (Split-Path -Parent $path)
        } else { return }
    }
    $path = $path.TrimEnd([char[]]@('\','/'))
    if ([IO.Path]::GetFileName($path) -ieq 'bin') { $path = Split-Path -Parent $path }
    if ((Test-Path -LiteralPath (Join-Path $path 'bin\java.exe')) -and
        (Test-Path -LiteralPath (Join-Path $path 'bin\javac.exe'))) {
        $candidates.Add($path)
    }
}

if ($ManualPath) { Add-JdkHome $ManualPath }
if ($env:GIF_ICON_WORLD_MANUAL_JDK) { Add-JdkHome $env:GIF_ICON_WORLD_MANUAL_JDK }
if ($env:JAVA_HOME) { Add-JdkHome $env:JAVA_HOME }

Get-Command java.exe, java -All -ErrorAction SilentlyContinue | ForEach-Object {
    if ($_.Source) { Add-JdkHome $_.Source }
    elseif ($_.Path) { Add-JdkHome $_.Path }
}

$pf86 = [Environment]::GetEnvironmentVariable('ProgramFiles(x86)')
$roots = @(
    (Join-Path $env:ProgramFiles 'Java'),
    (Join-Path $env:ProgramFiles 'Eclipse Adoptium'),
    (Join-Path $env:ProgramFiles 'Microsoft'),
    (Join-Path $env:ProgramFiles 'Zulu'),
    (Join-Path $env:ProgramFiles 'Amazon Corretto'),
    (Join-Path $env:ProgramFiles 'BellSoft'),
    (Join-Path $env:ProgramFiles 'Semeru'),
    (Join-Path $pf86 'Java'),
    (Join-Path $env:LOCALAPPDATA 'Programs\Eclipse Adoptium'),
    (Join-Path $env:LOCALAPPDATA 'Programs\Microsoft'),
    (Join-Path $env:LOCALAPPDATA 'Programs\Java'),
    (Join-Path $env:USERPROFILE '.jdks'),
    (Join-Path $env:LOCALAPPDATA 'GifIconWorldBuild')
) | Where-Object { $_ } | Select-Object -Unique

foreach ($root in $roots) {
    if (-not (Test-Path -LiteralPath $root -PathType Container)) { continue }
    Add-JdkHome $root
    foreach ($level1 in (Get-ChildItem -LiteralPath $root -Directory -ErrorAction SilentlyContinue)) {
        Add-JdkHome $level1.FullName
        Get-ChildItem -LiteralPath $level1.FullName -Directory -ErrorAction SilentlyContinue |
            ForEach-Object { Add-JdkHome $_.FullName }
    }
}

$registryRoots = @(
    'HKLM:\SOFTWARE\JavaSoft\JDK',
    'HKLM:\SOFTWARE\WOW6432Node\JavaSoft\JDK',
    'HKLM:\SOFTWARE\Eclipse Adoptium\JDK',
    'HKLM:\SOFTWARE\Microsoft\JDK',
    'HKCU:\SOFTWARE\JavaSoft\JDK'
)
foreach ($key in $registryRoots) {
    if (-not (Test-Path $key)) { continue }
    $base = Get-ItemProperty -LiteralPath $key
    if ($base.CurrentVersion) {
        $current = Get-ItemProperty -LiteralPath (Join-Path $key ([string]$base.CurrentVersion))
        if ($current.JavaHome) { Add-JdkHome $current.JavaHome }
    }
    Get-ChildItem -LiteralPath $key -ErrorAction SilentlyContinue | ForEach-Object {
        $entry = Get-ItemProperty -LiteralPath $_.PSPath
        if ($entry.JavaHome) { Add-JdkHome $entry.JavaHome }
    }
}

$valid = New-Object 'System.Collections.Generic.List[object]'
$seen = @{}
foreach ($home in $candidates) {
    $home = $home.TrimEnd('\','/')
    $key = $home.ToLowerInvariant()
    if ($seen.ContainsKey($key)) { continue }
    $seen[$key] = $true
    $java = Join-Path $home 'bin\java.exe'
    $javac = Join-Path $home 'bin\javac.exe'
    if (-not (Test-Path -LiteralPath $java) -or -not (Test-Path -LiteralPath $javac)) { continue }

    $major = 0
    $release = Join-Path $home 'release'
    if (Test-Path -LiteralPath $release) {
        $releaseText = Get-Content -LiteralPath $release -Raw -ErrorAction SilentlyContinue
        if ($releaseText -match '(?m)^JAVA_VERSION\s*=\s*"?([0-9]+)') { $major = [int]$Matches[1] }
    }
    if ($major -eq 0) {
        $versionOutput = (& $java -version 2>&1 | Out-String)
        if ($versionOutput -match '(?i)(?:version\s+)?"?([0-9]+)(?:\.|\s|"|$)') { $major = [int]$Matches[1] }
    }
    if ($major -ge 25) { $valid.Add([pscustomobject]@{ Home = $home; Major = $major }) }
}

$selected = $valid |
    Sort-Object @{ Expression = { if ($_.Major -eq 25) { 0 } else { 1 } } },
                @{ Expression = { $_.Major }; Descending = $true } |
    Select-Object -First 1
if ($selected) { Write-Output $selected.Home; exit 0 }
exit 1
