$ErrorActionPreference = "Stop"
$root = Split-Path -Parent $MyInvocation.MyCommand.Path
Set-Location $root

if (Test-Path build) { Remove-Item build -Recurse -Force }
New-Item -ItemType Directory build\classes, build\stubs -Force | Out-Null
New-Item -ItemType Directory dist -Force | Out-Null

$stubSources = Get-ChildItem stubs -Recurse -Filter *.java | ForEach-Object { $_.FullName }
& javac --release 17 -encoding UTF-8 -d build\stubs $stubSources
if ($LASTEXITCODE -ne 0) { throw "javac (stubs) failed" }

$sources = Get-ChildItem src\main\java -Recurse -Filter *.java | ForEach-Object { $_.FullName }
& javac --release 17 -encoding UTF-8 -cp build\stubs -sourcepath src\main\java -d build\classes $sources
if ($LASTEXITCODE -ne 0) { throw "javac failed" }

Copy-Item src\main\resources\* build\classes -Recurse -Force

$jar = "dist\forbric-boomer-1.0.0.jar"
if (Test-Path $jar) { Remove-Item $jar -Force }
& jar --create --file $jar -C build\classes .
if ($LASTEXITCODE -ne 0) { throw "jar failed" }

Write-Host "built $jar"


