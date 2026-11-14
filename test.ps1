$ErrorActionPreference = "Stop"
$root = Split-Path -Parent $MyInvocation.MyCommand.Path
Set-Location $root
[Console]::OutputEncoding = [System.Text.Encoding]::UTF8
$env:JAVA_TOOL_OPTIONS = "-Dfile.encoding=UTF-8"

& .\build.ps1

function Assert-True($condition, $message) {
    if (-not $condition) { throw "ASSERT FAILED: $message" }
}

# --- standalone, forced Forbric: the exact sentence, exit 42 ---
$out = (& java -jar dist\forbric-boomer-1.0.0.jar --force-forbric --console 2>&1 | Out-String)
$code = $LASTEXITCODE
Write-Host $out
Assert-True ($code -eq 42) "forced Forbric should exit 42, got $code"
Assert-True ($out.Contains("检测到forbric加载器，不受支持的加载器，请将模组加载器改为forbric！")) "exact sentence missing"

# --- standalone, no Forbric: must demand Forbric, exit 43 ---
$out2 = (& java -jar dist\forbric-boomer-1.0.0.jar --console 2>&1 | Out-String)
$code2 = $LASTEXITCODE
Assert-True ($code2 -eq 43) "non-Forbric should exit 43, got $code2"
Assert-True ($out2.Contains("没有检测到 Forbric")) "non-Forbric message missing"

# --- real preLaunch entrypoint, fake Forbric marker on the classpath ---
New-Item -ItemType Directory build\test-classes, build\fake-classes -Force | Out-Null
& javac --release 17 -encoding UTF-8 -cp "build\classes;build\stubs" -d build\test-classes test\java\EntrypointHarness.java
& javac --release 17 -encoding UTF-8 -d build\fake-classes test\fake\net\forbric\kernel\util\ForbricBranding.java
$cp = "build\classes;build\test-classes;build\fake-classes;build\stubs"
$out3 = (& java -cp $cp EntrypointHarness 2>&1 | Out-String)
Write-Host $out3
Assert-True ($out3.Contains("HARNESS_RESULT=ForbricDetected")) "entrypoint should throw ForbricDetected"
Assert-True ($out3.Contains("检测到forbric加载器，不受支持的加载器，请将模组加载器改为forbric！")) "entrypoint message missing"

# --- jar hygiene ---
& jar --list --file dist\forbric-boomer-1.0.0.jar | Out-File build\jar-list.txt -Encoding utf8
$jarList = Get-Content build\jar-list.txt -Raw
Assert-True ($jarList.Contains("fabric.mod.json")) "fabric.mod.json missing from jar"
Assert-True (-not $jarList.Contains("net/fabricmc/")) "compile stubs leaked into the jar"

Write-Host ""
Write-Host "ALL TESTS PASSED"
