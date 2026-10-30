$ErrorActionPreference = 'Stop'
$root = Split-Path -Parent $PSScriptRoot
$build = Get-Content -LiteralPath (Join-Path $root 'build.gradle') -Raw
$props = Get-Content -LiteralPath (Join-Path $root 'gradle.properties') -Raw
$errors = New-Object 'System.Collections.Generic.List[string]'
if ($build -notmatch "id\s+'net\.fabricmc\.fabric-loom'\s+version") {
    $errors.Add("Minecraft 26.3 requires the unobfuscated Loom plugin ID net.fabricmc.fabric-loom.")
}
if ($build -match '(?m)^\s*mappings\s+|officialMojangMappings\s*\(') {
    $errors.Add("Do not add Mojang/Yarn mappings for Minecraft 26.3; no mapping dependency is needed.")
}
if ($build -match '(?m)^\s*mod(Implementation|CompileOnly|RuntimeOnly)\s+') {
    $errors.Add("Use implementation/compileOnly/runtimeOnly with the unobfuscated 26.3 artifacts.")
}
if ($props -notmatch '(?m)^minecraft_version=26\.3\s*$') {
    $errors.Add("gradle.properties must target minecraft_version=26.3.")
}
if ($props -notmatch '(?m)^loom_version=1\.19-SNAPSHOT\s*$') {
    $errors.Add("This project is configured for Loom 1.19-SNAPSHOT.")
}
if ($errors.Count -gt 0) {
    Write-Host '[CONFIG CHECK FAILED]' -ForegroundColor Red
    foreach ($message in $errors) { Write-Host (' - ' + $message) -ForegroundColor Red }
    exit 1
}
Write-Host '[OK] Fabric 26.3 build configuration is valid.' -ForegroundColor Green
exit 0
